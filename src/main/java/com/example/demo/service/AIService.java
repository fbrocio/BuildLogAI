package com.example.demo.service;

import com.example.demo.dto.AIResponse;
import com.example.demo.dto.RecordDTO;
import com.example.demo.model.Record;
import com.example.demo.model.RecordStatus;
import com.example.demo.model.RecordType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${google.gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ✅ MÉTODO PRINCIPAL (usa AIResponse correctamente)
    public List<Record> generateRecordsFromText(String text, Long projectId) {

        AIResponse aiResponse = processAIResponse(text);

        return aiResponse.getRecords().stream().map(dto -> {
            Record record = new Record();

            record.setTitle(dto.getTitle());
            record.setDescription(dto.getDescription());
            record.setType(parseType(dto.getType()));
            record.setStatus(parseStatus(dto.getStatus()));
            record.setProjectId(projectId);

            return record;
        }).toList();
    }

    // ✅ ORQUESTADOR (este es el importante)
    public AIResponse processAIResponse(String text) {

        try {
            String rawResponse = callAI(text);
            System.out.println("RAW IA:\n" + rawResponse);   // ← clave

            String clean = cleanJson(rawResponse);
            System.out.println("CLEAN JSON:\n" + clean);     // ← clave

            AIResponse response = objectMapper.readValue(clean, AIResponse.class);

            validate(response);
            return response;

        } catch (Exception e) {
            e.printStackTrace(); // ← para ver el error real en consola
            throw new RuntimeException("Error procesando respuesta de IA", e);
        }
    }

    // ✅ SOLO llamada HTTP → devuelve String
    private String callAI(String text) {

        String[] models = {
                "gemini-2.5-flash",   // principal
                "gemini-1.5-flash"    // fallback
        };

        String prompt = buildPrompt(text);

        var requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        int maxRetries = 3;

        for (String model : models) {

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            int delay = 1000;

            for (int i = 0; i < maxRetries; i++) {
                try {
                    ResponseEntity<Map> response =
                            restTemplate.postForEntity(url, requestBody, Map.class);

                    return extractText(response.getBody());

                } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {

                    if (i == maxRetries - 1) break;

                    sleep(delay);
                    delay *= 2; // backoff exponencial

                } catch (Exception e) {
                    throw new RuntimeException("Error llamando a IA", e);
                }
            }
        }

        throw new RuntimeException("IA no disponible (todos los modelos fallaron)");
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private String extractText(Map body) {
        List candidates = (List) body.get("candidates");
        Map firstCandidate = (Map) candidates.get(0);
        Map content = (Map) firstCandidate.get("content");
        List parts = (List) content.get("parts");

        return (String) ((Map) parts.get(0)).get("text");
    }

    private String cleanJson(String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new RuntimeException("Respuesta IA vacía");
        }

        // Quitar markdown
        raw = raw.replace("```json", "")
                .replace("```", "")
                .trim();

        // Buscar primer '{' y último '}'
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start == -1 || end == -1 || end <= start) {
            throw new RuntimeException("No se encontró JSON válido en la respuesta: " + raw);
        }

        return raw.substring(start, end + 1);
    }

    private void validate(AIResponse response) {

        if (response.getRecords() == null || response.getRecords().isEmpty()) {
            throw new RuntimeException("IA devolvió lista vacía");
        }

        for (RecordDTO r : response.getRecords()) {

            if (r.getTitle() == null || r.getDescription() == null) {
                throw new RuntimeException("Campos obligatorios vacíos");
            }

            parseType(r.getType());
            parseStatus(r.getStatus());
        }
    }

    private String buildPrompt(String text) {
        return  """
Eres un sistema que transforma texto en JSON estructurado.

OBLIGATORIO:
- Devuelve SOLO JSON válido
- NO escribas texto adicional
- NO expliques nada
- NO hagas sugerencias
- NO añadas frases como "Aquí tienes"
- Si no puedes generar JSON, devuelve EXACTAMENTE: {}

FORMATO:
{
  "records": [
    {
      "title": "string",
      "description": "string",
      "type": "PENDIENTE | INCIDENCIA | AVANCE",
      "status": "ABIERTA | CERRADA",
      "structuredData": {
        "concept": string | null,
        "quantity": number | null,
        "unit": string | null,
        "unit_price": number | null,
        "total_price": number | null,
        "percentage": number | null,
        "duration_days": number | null,
        "date": string | null,
        "contact": string | null
      }
    }
  ]
}

REGLAS:
- Si hay varias acciones, crea varios records
- No inventes datos
- Usa null si no hay información
- status = ABIERTA por defecto

EJEMPLO:
Entrada: "Han llegado rotas 5 luminarias. Llamar a ARKOSLight"

Salida:
{
  "records": [
    {
      "title": "Recepción de luminarias defectuosas",
      "description": "Han llegado 5 luminarias en mal estado",
      "type": "INCIDENCIA",
      "status": "ABIERTA",
      "structuredData": {
        "concept": "luminarias",
        "quantity": 5,
        "unit": "uds",
        "unit_price": null,
        "total_price": null,
        "percentage": null,
        "duration_days": null,
        "date": null,
        "contact": null
      }
    },
    {
      "title": "Notificar incidencia a ARKOSLight",
      "description": "Contactar con ARKOSLight para informar de luminarias defectuosas",
      "type": "PENDIENTE",
      "status": "ABIERTA",
      "structuredData": {
        "concept": null,
        "quantity": null,
        "unit": null,
        "unit_price": null,
        "total_price": null,
        "percentage": null,
        "duration_days": null,
        "date": null,
        "contact": "ARKOSLight"
      }
    }
  ]
}

TEXTO:
"""  + text;
    }

    private RecordType parseType(String value) {
        try {
            return RecordType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return RecordType.PENDIENTE;
        }
    }

    private RecordStatus parseStatus(String value) {
        try {
            return RecordStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return RecordStatus.ABIERTA;
        }
    }
}
package com.example.demo.controller;

import com.example.demo.dto.AIRequest;
import com.example.demo.dto.AIResponse;
import com.example.demo.dto.TextRequest;
import com.example.demo.dto.TranscriptionResponse;
import com.example.demo.service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService){
        this.aiService = aiService;
    }


    @PostMapping("/parse")
    public AIResponse parse(@RequestBody AIRequest request) {
        return aiService.processAIResponse(request.getText());
    }


}


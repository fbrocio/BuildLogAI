package com.example.demo.validation;

import com.example.demo.dto.AIResponse;
import com.example.demo.dto.RecordDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AIValidator {
    private final Validator validator;

    public AIValidator(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public void validate (AIResponse response){
        if (response.getRecords() == null || response.getRecords().isEmpty()) {
            throw new RuntimeException("La respuesta no contiene records");
        }
        for (RecordDTO record : response.getRecords()) {
            Set<ConstraintViolation<RecordDTO>> violations = validator.validate(record);

            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                for(ConstraintViolation<RecordDTO> v: violations){
                    sb.append(v.getPropertyPath())
                            .append(": ")
                            .append(v.getMessage())
                            .append(" | ");
                }
                throw new RuntimeException("Error de validación: " + sb);
            }
        }
    }
}

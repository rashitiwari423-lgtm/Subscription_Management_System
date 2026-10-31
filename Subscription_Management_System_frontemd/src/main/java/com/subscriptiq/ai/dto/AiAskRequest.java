package com.subscriptiq.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Natural language business inquiry request")
public class AiAskRequest {

    @NotBlank(message = "Question cannot be blank")
    @Schema(description = "Natural language query regarding subscriptions, payments, or revenue",
            example = "Mere business mein abhi kya problem hai?",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String question;
}


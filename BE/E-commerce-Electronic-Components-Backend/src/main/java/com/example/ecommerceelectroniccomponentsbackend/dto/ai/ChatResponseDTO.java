package com.example.ecommerceelectroniccomponentsbackend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    private UUID sessionId;
    private String message;

    @Builder.Default
    private List<String> toolsUsed = new ArrayList<>();

    @Builder.Default
    private List<Object> referencedData = new ArrayList<>();

    private LocalDateTime timestamp;
}

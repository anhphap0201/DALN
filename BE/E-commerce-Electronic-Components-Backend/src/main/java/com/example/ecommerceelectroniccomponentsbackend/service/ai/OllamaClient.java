package com.example.ecommerceelectroniccomponentsbackend.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Component
@Slf4j
public class OllamaClient {

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;
    private final boolean isConfigured;

    public OllamaClient(
            @Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ai.ollama.model:qwen2.5:7b}") String model,
            ObjectMapper objectMapper
    ) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.isConfigured = baseUrl != null && !baseUrl.isEmpty();
    }

    public record OllamaChatResult(
            boolean success,
            String content,
            List<Map<String, Object>> toolCalls,
            String errorMessage
    ) {}

    /**
     * Gửi yêu cầu chat tới Ollama hỗ trợ Tool Calling
     */
    public OllamaChatResult chat(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        if (!isConfigured) {
            return new OllamaChatResult(false, null, null, "Ollama base-url chưa được cấu hình.");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", this.model);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            if (tools != null && !tools.isEmpty()) {
                requestBody.put("tools", tools);
            }

            String responseString = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            if (responseString == null) {
                return new OllamaChatResult(false, null, null, "Không nhận được phản hồi từ Ollama.");
            }

            JsonNode rootNode = objectMapper.readTree(responseString);
            JsonNode messageNode = rootNode.path("message");

            String content = messageNode.path("content").asText(null);
            List<Map<String, Object>> toolCalls = new ArrayList<>();

            JsonNode toolCallsNode = messageNode.path("tool_calls");
            if (toolCallsNode.isArray()) {
                for (JsonNode tc : toolCallsNode) {
                    Map<String, Object> callMap = objectMapper.convertValue(tc, new TypeReference<>() {});
                    toolCalls.add(callMap);
                }
            }

            return new OllamaChatResult(true, content, toolCalls, null);

        } catch (Exception e) {
            log.warn("Không thể kết nối tới Ollama tại local: {}", e.getMessage());
            return new OllamaChatResult(false, null, null, "Không thể kết nối tới Ollama (" + e.getMessage() + ")");
        }
    }
}

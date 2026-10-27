package com.example.ecommerceelectroniccomponentsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordSearchRequest {

    @NotBlank(message = "Từ khóa tìm kiếm không được để trống")
    private String keyword;

    @Builder.Default
    private Integer resultsCount = 0;
}

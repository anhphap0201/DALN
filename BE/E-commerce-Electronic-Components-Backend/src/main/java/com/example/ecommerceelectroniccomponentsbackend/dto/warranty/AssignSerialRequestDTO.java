package com.example.ecommerceelectroniccomponentsbackend.dto.warranty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignSerialRequestDTO {

    @NotNull(message = "Mã orderItemId không được để trống")
    private Long orderItemId;

    // Danh sách các mã serial gán cho order item này (số lượng bằng orderItem.quantity)
    private List<String> serialNumbers;

    // Tùy chọn sinh tự động nếu serialNumbers rỗng
    private Boolean autoGenerate;
}

package com.example.PRM.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {

    /** Câu hỏi của user, vd: "áo này đi đám cưới phối quần gì?" */
    private String message;

    /**
     * Optional — chỉ định rõ user đang hỏi về món đồ nào trong tủ.
     * Nếu null, AI sẽ tự đoán hoặc lấy món đầu tiên trong tủ.
     */
    private UUID wardrobeItemId;
}

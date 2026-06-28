package com.example.PRM.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// ReceivedReq.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedReq {
    private MultipartFile receiptProofFile;
}

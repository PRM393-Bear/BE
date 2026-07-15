package com.example.PRM.dto.request.community;

import lombok.Data;
import java.util.UUID;

@Data
public class PostCommentReq {
    private String content;
    private UUID parentCommentId;
}

package com.example.PRM.dto.response.community;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class PostCommentRes {
    private UUID id;
    private String content;
    private OffsetDateTime createdAt;
    private UUID parentCommentId;
    
    // Author info
    private UUID authorId;
    private String authorName;
    private String authorAvatar;
}

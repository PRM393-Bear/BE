package com.example.PRM.dto.response.community;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommunityPostRes {
    private UUID id;
    private String content;
    private List<String> images;
    private OffsetDateTime createdAt;
    
    // Author info
    private UUID authorId;
    private String authorName;
    private String authorAvatar;
    
    // Interaction metrics
    private long likeCount;
    private long commentCount;
    private boolean isLikedByMe;
    private boolean isHidden;
}

package com.example.PRM.dto.request.community;

import lombok.Data;
import java.util.List;

@Data
public class CommunityPostReq {
    private String content;
    private List<String> images;
}

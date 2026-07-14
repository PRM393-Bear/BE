package com.example.PRM.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryRes {
    private String id;
    private String name;
    private String description;
}

package com.example.PRM.dto.response.wardrobeItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WardrobeItemLogRes {
    private String name;
    private UUID id;
    private String username;
    private UUID userId;
}

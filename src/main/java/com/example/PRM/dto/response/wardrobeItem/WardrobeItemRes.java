package com.example.PRM.dto.response.wardrobeItem;

import com.example.PRM.status_enum.WardrobeStatus;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WardrobeItemRes {
    @Column(length = 150)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private WardrobeStatus status;
    private UUID itemId;
}

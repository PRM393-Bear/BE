package com.example.PRM.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ProductLogRes {
    private String name;
    private String username;
    private UUID userId;
    private UUID id;
}

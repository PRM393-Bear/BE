package com.example.PRM.entity;

import com.example.PRM.status_enum.ProductStatus;
import com.example.PRM.status_enum.ProductType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    private Short condition;

    @Column(length = 100)
    private String brand;

    private Long price;

    @Column(length = 20)
    private String size;

    @Column(length = 50)
    private String color;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> images;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> aiTags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    private Short lifecycleGeneration;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }

}

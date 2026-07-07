package com.example.PRM.entity;

import com.example.PRM.status_enum.AddedVia;
import com.example.PRM.status_enum.WardrobeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "wardrobe_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WardrobeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private WardrobeStatus status;

    @Enumerated(EnumType.STRING)
    private AddedVia addedVia;

    private LocalDate acquiredAt;

    @Column(length = 50)
    private String condition; // "Còn mới", "Đã qua sử dụng"...

    @Column(columnDefinition = "TEXT")
    private String conditionNote; //

    @ManyToOne
    @JoinColumn(name = "donation_request_id")
    private DonationRequest donationRequest;


}

package com.example.PRM.entity;

import com.example.PRM.status_enum.ItemDonationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "donation_request_items")
public class DonationRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "donation_request_id")
    private DonationRequest donationRequest;

    @ManyToOne
    @JoinColumn(name = "wardrobe_item_id")
    private WardrobeItem wardrobeItem;

    private ItemDonationStatus status;
    private String rejectReason;
    private String note;
}

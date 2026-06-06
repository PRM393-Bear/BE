package com.example.PRM.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(length = 50)
    private String trackingCode;

    @Column(columnDefinition = "TEXT")
    private String paymentProofUrl;

    private OffsetDateTime completedAt;

    public enum TransactionStatus {
        PENDING, PAYMENT_UPLOADED, PAYMENT_CONFIRMED, SHIPPING, RECEIVED, COMPLETED, CANCELLED
    }
}

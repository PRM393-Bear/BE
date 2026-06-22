package com.example.PRM.status_enum;

public enum ItemDonationStatus {
    /** Waiting for organization review */
    PENDING_REVIEW,

    /** Accepted by organization */
    ACCEPTED,

    /** Rejected by organization */
    REJECTED,

    /** Waiting for shipment creation */
    WAITING_SHIPMENT,

    /** Shipment created */
    SHIPPING_CREATED,

    /** Being transported */
    IN_TRANSIT,

    /** Successfully received by organization */
    RECEIVED,

    /** Shipment lost during transportation */
    LOST
}

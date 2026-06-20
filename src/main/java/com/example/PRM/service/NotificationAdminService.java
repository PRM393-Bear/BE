package com.example.PRM.service;

import com.example.PRM.entity.DonationRequest;

import java.util.List;

public interface NotificationAdminService {
    public void notifyAdminPendingOverdue(List<DonationRequest> list);
}

package com.example.PRM.service;

import java.util.UUID;

public interface TransactionService {
    void completeTransaction(UUID transactionId);
}

package com.example.PRM.serviceImpl;

import com.example.PRM.entity.Transaction;
import com.example.PRM.entity.WardrobeItem;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.TransactionRepository;
import com.example.PRM.service.TransactionService;
import com.example.PRM.status_enum.TransactionStatus;
import com.example.PRM.status_enum.WardrobeStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void completeTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(()
                -> new NotFoundException("Transaction not found with id: " + transactionId));
        if(transaction.getStatus().equals(TransactionStatus.COMPLETED)){
            WardrobeItem wi = new WardrobeItem();
            wi.setUser(transaction.getBuyer());
            wi.setProduct(transaction.getProduct());
            wi.setStatus(WardrobeStatus.OWNED);
        }
    }
}

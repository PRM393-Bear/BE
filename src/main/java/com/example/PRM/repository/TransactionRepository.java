package com.example.PRM.repository;

import com.example.PRM.entity.Transaction;
import com.example.PRM.status_enum.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findById(UUID transactionId);
}

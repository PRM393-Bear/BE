package com.example.PRM.transaction;

import com.example.PRM.entity.Product;
import com.example.PRM.entity.Transaction;
import com.example.PRM.entity.User;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.repository.TransactionRepository;
import com.example.PRM.serviceImpl.TransactionServiceImpl;
import com.example.PRM.status_enum.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TransactionServiceImpl}.
 *
 * NOTE: the second branch test assumes {@code TransactionStatus} has a value other than
 * COMPLETED named PENDING. Adjust the constant name below if your enum uses a different
 * name for the non-completed state.
 *
 * NOTE ALSO: completeTransaction currently builds a WardrobeItem locally but never
 * persists it (no WardrobeItemRepository is injected/called), so there is no observable
 * side effect to assert beyond "no exception is thrown" and that findById was called.
 * If this is unintentional in the production code, it may be worth revisiting.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionServiceImpl transactionService;

    private UUID transactionId;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository);
        transactionId = UUID.randomUUID();

        transaction = new Transaction();
        transaction.setBuyer(new User());
        transaction.setProduct(new Product());
    }

    @Test
    void completeTransaction_notFound_throwsNotFoundException() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> transactionService.completeTransaction(transactionId));

        assertTrue(ex.getMessage().contains("Transaction not found"));
    }

    @Test
    void completeTransaction_statusCompleted_doesNotThrow() {
        transaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.completeTransaction(transactionId));
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void completeTransaction_statusNotCompleted_doesNotThrow() {
        transaction.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.completeTransaction(transactionId));
        verify(transactionRepository).findById(transactionId);
    }
}

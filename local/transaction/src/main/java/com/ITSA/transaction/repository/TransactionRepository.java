package com.ITSA.transaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.ITSA.transaction.enums.TransactionStatus;
import com.ITSA.transaction.exceptions.DataProcessingException;
import com.ITSA.transaction.exceptions.TransactionNotFoundException;
import com.ITSA.transaction.models.Transaction;

public interface TransactionRepository {

    Transaction saveTransaction(Transaction transaction) throws DataProcessingException;

    Transaction updateTransactionStatus(Integer transactionId, TransactionStatus newStatus) throws TransactionNotFoundException, DataProcessingException;

    // Custom method to find transactions by status
    List<Transaction> findByStatus(TransactionStatus status) throws DataProcessingException;

    // Custom method to find transactions by user ID
    List<Transaction> findByUserId(Integer userId) throws DataProcessingException;

    // Custom method to find transactions within a date range
    List<Transaction> findBetweenDates(LocalDateTime startDate, LocalDateTime endDate) throws DataProcessingException;

    // Custom method to delete a transaction by ID
    boolean deleteTransactionById(Integer transactionId) throws DataProcessingException;

    // Custom method to approve a transaction by ID
    Transaction approveTransaction(Integer transactionId, Integer approverId) throws TransactionNotFoundException, DataProcessingException;

    // Custom method to get a transaction by ID
    Optional<Transaction> findById(Integer transactionId) throws DataProcessingException;

    // Custom method to list transactions with pagination without Spring
    List<Transaction> findAllTransactions(int page, int size) throws DataProcessingException;

    // Custom method to count the total number of transactions for pagination
    long countTransactions() throws DataProcessingException;

    // Custom method to update the action field of a transaction
    Transaction updateTransactionAction(Integer transactionId, String actionJson) throws TransactionNotFoundException, DataProcessingException;

    // Custom method to update the entire transaction (could be used for multiple
    // fields)
    Transaction updateTransaction(Transaction transaction) throws TransactionNotFoundException;

}
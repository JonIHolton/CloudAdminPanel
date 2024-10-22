package com.ITSA.transaction.repository;

import com.ITSA.transaction.enums.TransactionStatus;
import com.ITSA.transaction.exceptions.DataProcessingException;
import com.ITSA.transaction.exceptions.TransactionNotFoundException;
import com.ITSA.transaction.models.Transaction;
import com.ITSA.transaction.models.User;
import com.ITSA.transaction.utils.ObjectMapperUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRepositoryImpl implements TransactionRepository {

  private static final Logger log = LoggerFactory.getLogger(
    TransactionRepositoryImpl.class
  );

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  @Transactional
  public Transaction saveTransaction(Transaction transaction)
    throws DataProcessingException {
    log.info(
      "Attempting to save transaction for user: {}",
      transaction.getUser().getUserId()
    );
    try {
      if (transaction.getAction() != null) {
        // Ensure action is properly formatted JSON string
        transaction.setAction(
          ObjectMapperUtil
            .getMapper()
            .writeValueAsString(transaction.getAction())
        );
      }
      entityManager.persist(transaction);
      return transaction;
    } catch (Exception e) {
      log.error(
        "Failed to save transaction for user: {}",
        transaction.getUser().getUserId(),
        e
      );
      throw new DataProcessingException(
        "Failed to save transaction due to internal error."
      );
    }
  }

  @Override
  @Transactional
  public Transaction updateTransactionStatus(
    Integer transactionId,
    TransactionStatus newStatus
  ) throws TransactionNotFoundException, DataProcessingException {
    log.info("Updating status for transaction ID: {}", transactionId);
    return findById(transactionId)
      .map(transaction -> {
        transaction.setStatus(newStatus);
        transaction.setStatusUpdateTime(LocalDateTime.now());
        return entityManager.merge(transaction);
      })
      .orElseThrow(() ->
        new TransactionNotFoundException(
          "Transaction not found with ID: " + transactionId
        )
      );
  }

  // Example method for fetching transactions by status, utilizing Jakarta Persistence API
  @Override
  public List<Transaction> findByStatus(TransactionStatus status)
    throws DataProcessingException {
    log.info("Fetching transactions with status: {}", status);
    try {
      return entityManager
        .createQuery(
          "SELECT t FROM Transaction t WHERE t.status = :status",
          Transaction.class
        )
        .setParameter("status", status)
        .getResultList();
    } catch (Exception e) {
      log.error("Error fetching transactions by status: {}", status, e);
      throw new DataProcessingException("Error fetching transactions.");
    }
  }

  // Continuing within the TransactionRepositoryImpl class

  @Override
  public List<Transaction> findByUserId(Integer userId)
    throws DataProcessingException {
    log.info("Fetching transactions for user ID: {}", userId);
    try {
      return entityManager
        .createQuery(
          "SELECT t FROM Transaction t WHERE t.user.id = :userId",
          Transaction.class
        )
        .setParameter("userId", userId)
        .getResultList();
    } catch (Exception e) {
      log.error("Error fetching transactions for user ID: {}", userId, e);
      throw new DataProcessingException(
        "Error fetching transactions for user."
      );
    }
  }

  @Override
  public List<Transaction> findBetweenDates(
    LocalDateTime startDate,
    LocalDateTime endDate
  ) throws DataProcessingException {
    log.info(
      "Fetching transactions between dates: {} and {}",
      startDate,
      endDate
    );
    try {
      return entityManager
        .createQuery(
          "SELECT t FROM Transaction t WHERE t.dateTime BETWEEN :startDate AND :endDate",
          Transaction.class
        )
        .setParameter("startDate", startDate)
        .setParameter("endDate", endDate)
        .getResultList();
    } catch (Exception e) {
      log.error("Error fetching transactions between dates", e);
      throw new DataProcessingException(
        "Error fetching transactions in date range."
      );
    }
  }

  @Override
  @Transactional
  public boolean deleteTransactionById(Integer transactionId)
    throws DataProcessingException {
    log.info("Deleting transaction with ID: {}", transactionId);
    try {
      Transaction transaction = entityManager.find(
        Transaction.class,
        transactionId
      );
      if (transaction != null) {
        entityManager.remove(transaction);
        return true;
      } else {
        log.warn(
          "Transaction with ID {} not found, cannot be deleted",
          transactionId
        );
        return false;
      }
    } catch (Exception e) {
      log.error("Error deleting transaction with ID: {}", transactionId, e);
      throw new DataProcessingException("Error deleting transaction.");
    }
  }

  @Override
  @Transactional
  public Transaction approveTransaction(
    Integer transactionId,
    Integer approverId
  ) throws TransactionNotFoundException, DataProcessingException {
    log.info(
      "Approving transaction with ID: {}, by approver ID: {}",
      transactionId,
      approverId
    );
    try {
      Transaction transaction = findById(transactionId)
        .orElseThrow(() ->
          new TransactionNotFoundException(
            "Transaction not found with ID: " + transactionId
          )
        );

      User approver = Optional
        .ofNullable(entityManager.find(User.class, approverId))
        .orElseThrow(() ->
          new TransactionNotFoundException(
            "Approver not found with ID: " + approverId
          )
        );

      transaction.setApprover(approver);
      transaction.setStatus(TransactionStatus.APPROVED);
      transaction.setStatusUpdateTime(LocalDateTime.now());
      return entityManager.merge(transaction);
    } catch (TransactionNotFoundException e) {
      throw e; // Rethrow the same exception to avoid wrapping it into a generic one
    } catch (Exception e) {
      log.error("Error approving transaction with ID: {}", transactionId, e);
      throw new DataProcessingException("Error approving transaction.");
    }
  }

  @Override
  public List<Transaction> findAllTransactions(int page, int size)
    throws DataProcessingException {
    log.info(
      "Fetching all transactions with pagination - Page: {}, Size: {}",
      page,
      size
    );
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
      Root<Transaction> rootEntry = cq.from(Transaction.class);
      CriteriaQuery<Transaction> all = cq.select(rootEntry);

      TypedQuery<Transaction> allQuery = entityManager.createQuery(all);
      allQuery.setFirstResult(page * size);
      allQuery.setMaxResults(size);

      return allQuery.getResultList();
    } catch (Exception e) {
      log.error("Error fetching all transactions with pagination", e);
      throw new DataProcessingException(
        "Error fetching transactions with pagination."
      );
    }
  }

  @Override
  public long countTransactions() throws DataProcessingException {
    log.info("Counting total number of transactions");
    try {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<Long> cq = cb.createQuery(Long.class);
      Root<Transaction> root = cq.from(Transaction.class);
      cq.select(cb.count(root));
      return entityManager.createQuery(cq).getSingleResult();
    } catch (Exception e) {
      log.error("Error counting total number of transactions", e);
      throw new DataProcessingException("Error counting transactions.");
    }
  }

  @Override
  @Transactional
  public Transaction updateTransactionAction(
    Integer transactionId,
    String actionJson
  ) throws TransactionNotFoundException, DataProcessingException {
    log.info("Updating action for transaction ID: {}", transactionId);
    return findById(transactionId)
      .map(transaction -> {
        try {
          transaction.setAction(actionJson); // Assuming actionJson is already a properly formatted JSON string
          return entityManager.merge(transaction);
        } catch (Exception e) {
          log.error(
            "Error updating action for transaction ID: {}",
            transactionId,
            e
          );
          throw e;
        }
      })
      .orElseThrow(() ->
        new TransactionNotFoundException(
          "Transaction not found with ID: " + transactionId
        )
      );
  }

  @Override
  @Transactional
  public Transaction updateTransaction(Transaction transaction)
    throws TransactionNotFoundException {
    log.info("Updating transaction: {}", transaction.getTransactionId());
    if (transaction.getTransactionId() == null) {
      throw new IllegalArgumentException(
        "Transaction ID must not be null when updating"
      );
    }

    return Optional
      .ofNullable(
        entityManager.find(Transaction.class, transaction.getTransactionId())
      )
      .map(existingTransaction -> {
        //TODO:
        // Copy necessary fields from `transaction` to `existingTransaction`
        // Ensure proper handling of any fields that require transformation or special logic
        return entityManager.merge(existingTransaction);
      })
      .orElseThrow(() ->
        new TransactionNotFoundException(
          "Transaction not found with ID: " + transaction.getTransactionId()
        )
      );
  }

  @Override
  public Optional<Transaction> findById(Integer transactionId) throws DataProcessingException {
      log.info("Fetching transaction with ID: {}", transactionId);
      try {
          Transaction transaction = entityManager.find(Transaction.class, transactionId);
          return Optional.ofNullable(transaction);
      } catch (Exception e) {
          log.error("Error fetching transaction with ID: {}", transactionId, e);
          throw new DataProcessingException("Error fetching transaction by ID.");
      }
  }
  
}

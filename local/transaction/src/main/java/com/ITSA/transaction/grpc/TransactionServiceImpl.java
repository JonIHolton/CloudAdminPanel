package com.ITSA.transaction.grpc;

import com.ITSA.transaction.exceptions.ServiceException;
import com.ITSA.transaction.models.Transaction;
import com.ITSA.transaction.repository.TransactionRepository;
import com.ITSA.transaction.repository.TransactionRepositoryImpl;
import com.ITSA.transaction.utils.ResponseUtil;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import transactions.Transaction.ApproveTransactionRequest;
import transactions.Transaction.CreateTransactionRequest;
import transactions.Transaction.DeleteTransactionRequest;
import transactions.Transaction.GetTransactionRequest;
import transactions.Transaction.ListTransactionsRequest;
import transactions.Transaction.ListTransactionsResponse;
import transactions.Transaction.ServiceResponseWrapper;
import transactions.Transaction.TransactionResponse;
import transactions.Transaction.UpdateTransactionStatusRequest;
import transactions.TransactionServiceGrpc.TransactionServiceImplBase;

public class TransactionServiceImpl extends TransactionServiceImplBase {

  private final TransactionRepository transactionRepository = new TransactionRepositoryImpl();

  @Override
  public void createTransaction(
    CreateTransactionRequest request,
    StreamObserver<ServiceResponseWrapper> responseObserver
  ) {
    if (request.getRequestId().isEmpty() || request.getUserId().isEmpty()) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Missing required fields",
          400
        )
      );
      responseObserver.onCompleted();
      return;
    }
    String actionJson = null;
    try {
      actionJson =
        JsonFormat
          .printer()
          .includingDefaultValueFields()
          .print(request.getAction());
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          e.getMessage(),
          500
        )
      );
    }

    try {
      Transaction transaction = new Transaction();

      transaction.setAction(actionJson); 

      Transaction savedTransaction = transactionRepository.saveTransaction(
        transaction
      );
      TransactionResponse transactionResponse = buildTransactionResponse(
        savedTransaction
      );

      responseObserver.onNext(
        ResponseUtil.buildSuccessResponse(
          Any.pack(transactionResponse),
          request.getRequestId()
        )
      );
    } catch (Exception e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Internal server error",
          500
        )
      );
    }
    responseObserver.onCompleted();
  }

  @Override
  public void getTransaction(
    GetTransactionRequest request,
    StreamObserver<ServiceResponseWrapper> responseObserver
  ) {
    if (
      request.getRequestId().isEmpty() || request.getTransactionId().isEmpty()
    ) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Missing required fields",
          400
        )
      );
      responseObserver.onCompleted();
      return;
    }

    try {
      Transaction transaction = transactionRepository
        .findById(Integer.parseInt(request.getTransactionId()))
        .orElseThrow(() -> new ServiceException("Transaction not found", 404));
      TransactionResponse transactionResponse = buildTransactionResponse(
        transaction
      );

      responseObserver.onNext(
        ResponseUtil.buildSuccessResponse(
          Any.pack(transactionResponse),
          request.getRequestId()
        )
      );
    } catch (ServiceException e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          e.getMessage(),
          e.getErrorCode()
        )
      );
    } catch (Exception e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Internal server error",
          500
        )
      );
    }
    responseObserver.onCompleted();
  }

  // Helper method to build TransactionResponse
  private TransactionResponse buildTransactionResponse(Transaction transaction)
    throws InvalidProtocolBufferException {
    Struct.Builder actionBuilder = Struct.newBuilder();
    JsonFormat.parser().merge(transaction.getAction(), actionBuilder);

    return TransactionResponse
      .newBuilder()
      .setTransactionId(String.valueOf(transaction.getTransactionId()))
      .setUserId(transaction.getUser().getUserId().toString())
      .setDateTime(
        Timestamp
          .newBuilder()
          .setSeconds(Instant.now().getEpochSecond())
          .build()
      )
      .setStatus(convertToProtoEnum(transaction.getStatus()))
      .setAction(actionBuilder)
      .setApproverId(
        Optional
          .ofNullable(transaction.getApprover())
          .map(approver -> approver.getUserId().toString())
          .orElse("")
      )
      .build();
  }


  @Override
  public void updateTransactionStatus(
    UpdateTransactionStatusRequest request,
    StreamObserver<ServiceResponseWrapper> responseObserver
  ) {
    if (
      request.getRequestId().isEmpty() ||
      request.getTransactionId().isEmpty() ||
      request.getStatus() == null
    ) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Missing required fields",
          400
        )
      );
      responseObserver.onCompleted();
      return;
    }

    try {
      Transaction transaction = transactionRepository
        .findById(Integer.parseInt(request.getTransactionId()))
        .orElseThrow(() -> new ServiceException("Transaction not found", 404));

      transaction.setStatus(convertToJavaEnum(request.getStatus()));

      Transaction updatedTransaction = transactionRepository.updateTransaction(
        transaction
      );
      TransactionResponse transactionResponse = buildTransactionResponse(
        updatedTransaction
      );

      responseObserver.onNext(
        ResponseUtil.buildSuccessResponse(
          Any.pack(transactionResponse),
          request.getRequestId()
        )
      );
    } catch (ServiceException e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          e.getMessage(),
          e.getErrorCode()
        )
      );
    } catch (Exception e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Internal server error",
          500
        )
      );
    }
    responseObserver.onCompleted();
  }

  @Override
  public void listTransactions(
    ListTransactionsRequest request,
    StreamObserver<ServiceResponseWrapper> responseObserver
  ) {
    if (request.getRequestId().isEmpty()) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Missing required fields",
          400
        )
      );
      responseObserver.onCompleted();
      return;
    }

    try {
      // Assuming a method exists to list transactions with pagination
      List<Transaction> transactions = transactionRepository.findAllTransactions(
        request.getPageNumber(),
        request.getPageSize()
      );
      List<TransactionResponse> transactionResponses = transactions
        .stream()
        .map(transaction -> {
          try {
            return this.buildTransactionResponse(transaction);
          } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());

      Any packedResponses = Any.pack(
        ListTransactionsResponse
          .newBuilder()
          .addAllTransactions(transactionResponses)
          .build()
      );

      responseObserver.onNext(
        ResponseUtil.buildSuccessResponse(
          packedResponses,
          request.getRequestId()
        )
      );
    } catch (Exception e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Internal server error",
          500
        )
      );
    }
    responseObserver.onCompleted();
  }

  @Override
  public void deleteTransaction(
    DeleteTransactionRequest request,
    StreamObserver<ServiceResponseWrapper> responseObserver
  ) {
    if (
      request.getRequestId().isEmpty() || request.getTransactionId().isEmpty()
    ) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Missing required fields",
          400
        )
      );
      responseObserver.onCompleted();
      return;
    }

    try {
      boolean deleted = transactionRepository.deleteTransactionById(
        Integer.parseInt(request.getTransactionId())
      );
      if (!deleted) {
        throw new ServiceException(
          "Transaction not found or could not be deleted",
          404
        );
      }

      responseObserver.onNext(ResponseUtil.buildSuccessResponse(null, null));
    } catch (ServiceException e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          e.getMessage(),
          e.getErrorCode()
        )
      );
    } catch (Exception e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Internal server error",
          500
        )
      );
    }
    responseObserver.onCompleted();
  }

  @Override
  public void approveTransaction(
    ApproveTransactionRequest request,
    StreamObserver<ServiceResponseWrapper> responseObserver
  ) {
    if (
      request.getRequestId().isEmpty() ||
      request.getTransactionId().isEmpty() ||
      request.getApproverId().isEmpty()
    ) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Missing required fields",
          400
        )
      );
      responseObserver.onCompleted();
      return;
    }

    try {
      Transaction transaction = transactionRepository
        .findById(Integer.parseInt(request.getTransactionId()))
        .orElseThrow(() -> new ServiceException("Transaction not found", 404));

      // Assuming logic to set the approver and update the transaction status
      // TODO:
      transaction.setApprover(
        null
        /* logic to find and set approver based on request.getApproverId() */
      );
      transaction.setStatus(
        com.ITSA.transaction.enums.TransactionStatus.APPROVED
      );
      Transaction updatedTransaction = transactionRepository.updateTransaction(
        transaction
      );

      TransactionResponse transactionResponse = buildTransactionResponse(
        updatedTransaction
      );
      responseObserver.onNext(
        ResponseUtil.buildSuccessResponse(
          Any.pack(transactionResponse),
          request.getRequestId()
        )
      );
    } catch (ServiceException e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          e.getMessage(),
          e.getErrorCode()
        )
      );
    } catch (Exception e) {
      responseObserver.onNext(
        ResponseUtil.buildErrorResponse(
          request.getRequestId(),
          "Internal server error",
          500
        )
      );
    }
    responseObserver.onCompleted();
  }

  // Inside TransactionServiceImpl class

  private com.ITSA.transaction.enums.TransactionStatus convertToJavaEnum(
    transactions.Transaction.TransactionStatus protoStatus
  ) {
    switch (protoStatus) {
      case CREATED:
        return com.ITSA.transaction.enums.TransactionStatus.CREATED;
      case PENDING_APPROVAL:
        return com.ITSA.transaction.enums.TransactionStatus.PENDING_APPROVAL;
      case CHECKER_NOTIFIED:
        return com.ITSA.transaction.enums.TransactionStatus.CHECKER_NOTIFIED;
      case APPROVED:
        return com.ITSA.transaction.enums.TransactionStatus.APPROVED;
      case REJECTED:
        return com.ITSA.transaction.enums.TransactionStatus.REJECTED;
      case AUTOMATIC_APPROVAL:
        return com.ITSA.transaction.enums.TransactionStatus.AUTOMATIC_APPROVAL;
      case AUTOMATIC_REJECTION:
        return com.ITSA.transaction.enums.TransactionStatus.AUTOMATIC_REJECTION;
      case COMPLETED:
        return com.ITSA.transaction.enums.TransactionStatus.COMPLETED;
      default:
        throw new IllegalArgumentException(
          "Unknown TransactionStatus from Proto: " + protoStatus
        );
    }
  }

  private transactions.Transaction.TransactionStatus convertToProtoEnum(
    com.ITSA.transaction.enums.TransactionStatus javaStatus
  ) {
    switch (javaStatus) {
      case CREATED:
        return transactions.Transaction.TransactionStatus.CREATED;
      case PENDING_APPROVAL:
        return transactions.Transaction.TransactionStatus.PENDING_APPROVAL;
      case CHECKER_NOTIFIED:
        return transactions.Transaction.TransactionStatus.CHECKER_NOTIFIED;
      case APPROVED:
        return transactions.Transaction.TransactionStatus.APPROVED;
      case REJECTED:
        return transactions.Transaction.TransactionStatus.REJECTED;
      case AUTOMATIC_APPROVAL:
        return transactions.Transaction.TransactionStatus.AUTOMATIC_APPROVAL;
      case AUTOMATIC_REJECTION:
        return transactions.Transaction.TransactionStatus.AUTOMATIC_REJECTION;
      case COMPLETED:
        return transactions.Transaction.TransactionStatus.COMPLETED;
      default:
        throw new IllegalArgumentException(
          "Unknown TransactionStatus from Java: " + javaStatus
        );
    }
  }
}

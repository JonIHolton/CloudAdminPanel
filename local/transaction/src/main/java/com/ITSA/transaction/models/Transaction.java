package com.ITSA.transaction.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.ITSA.transaction.enums.TransactionStatus;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private Integer transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver", referencedColumnName = "user_id")
    private User approver;

    @Convert(converter = TransactionStatusConverter.class)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "status_update_time", nullable = false)
    private LocalDateTime statusUpdateTime;

    @Column(name = "action", nullable = false, columnDefinition = "JSON")
    private String action;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * @param user
     * @param approver
     * @param status
     * @param action
     */
    public Transaction(User user, User approver, TransactionStatus status, String action) {
        this.user = user;
        this.approver = approver;
        this.status = status;
        this.action = action;
    }
}

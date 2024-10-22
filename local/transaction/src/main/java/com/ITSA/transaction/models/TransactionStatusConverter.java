package com.ITSA.transaction.models;

import com.ITSA.transaction.enums.TransactionStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TransactionStatus status) {
        return status.getValue();
    }

    @Override
    public TransactionStatus convertToEntityAttribute(Integer value) {
        return TransactionStatus.valueOf(value);
    }
}

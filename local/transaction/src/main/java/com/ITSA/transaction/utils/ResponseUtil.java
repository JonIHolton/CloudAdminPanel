package com.ITSA.transaction.utils;

import com.google.protobuf.Any;

import transactions.Transaction.ErrorResponse;
import transactions.Transaction.ResponseMetadata;
import transactions.Transaction.ServiceResponseWrapper;

import java.time.LocalDateTime;

public class ResponseUtil {

    public static ServiceResponseWrapper buildSuccessResponse(Any payload, String requestId) {
        ServiceResponseWrapper.Builder responseWrapper = ServiceResponseWrapper.newBuilder();
        ResponseMetadata metadata = buildMetadata(requestId, true, "");
        responseWrapper.setMetadata(metadata);
        responseWrapper.setPayload(payload);
        return responseWrapper.build();
    }

    public static ServiceResponseWrapper buildErrorResponse(String requestId, String errorMessage, int errorCode) {
        ServiceResponseWrapper.Builder responseWrapper = ServiceResponseWrapper.newBuilder();
        ResponseMetadata metadata = buildMetadata(requestId, false, errorMessage);
        ErrorResponse errorResponse = ErrorResponse.newBuilder()
                .setCode(errorCode)
                .setMessage(errorMessage)
                .build();
        responseWrapper.setMetadata(metadata);
        responseWrapper.setError(errorResponse);
        return responseWrapper.build();
    }

    private static ResponseMetadata buildMetadata(String requestId, boolean success, String errorMessage) {
        return ResponseMetadata.newBuilder()
                .setRequestId(requestId)
                .setTimestamp(LocalDateTimeToTimeStampUtil.convertLocalDateTimeToTimestamp(LocalDateTime.now()))
                .setSuccess(success)
                .setErrorMessage(errorMessage)
                .build();
    }
}

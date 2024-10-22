package com.ITSA.transaction.utils;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.google.protobuf.Timestamp;

public class LocalDateTimeToTimeStampUtil {
    


    public static Timestamp convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.newBuilder().setSeconds(localDateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(localDateTime.getNano()).build();
    }
}

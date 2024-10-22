package com.ITSA.AdminProxy.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@ToString
public class TempLogRequestDTO {
    private int start = 0;
    private int size = 10;
    private String filters;
    private String sorting; // Consider using a more structured approach if needed

    @Getter
    @Setter
    @ToString
    public static class Filter {
        private String id;
        private Object value; // Use Object if value can be String or another structure (e.g., list for timestamps)
    }
}

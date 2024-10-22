
package com.ITSA.AdminProxy.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewLogRequestDTO {
    private String logId;
    private double startTimestamp;
    private double endTimestamp;
    private double searchTimestamp = 2112425714.0;
    private String description;
    private String initiatorUser;
    private String targetUser;
    private String sort;
    private int start = 0;
    private int size = 10;

    // Getters and Setters
}
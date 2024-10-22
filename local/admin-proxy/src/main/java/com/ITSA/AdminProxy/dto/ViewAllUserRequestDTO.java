package com.ITSA.AdminProxy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViewAllUserRequestDTO {
    private int start;
    private int size;
    private String filters;
    private String sorting;
    
}

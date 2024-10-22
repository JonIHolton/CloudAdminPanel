package com.ITSA.AdminProxy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    private int totalRowCount;

    public Meta(int totalRowCount) {
        this.totalRowCount = totalRowCount;
    }
}

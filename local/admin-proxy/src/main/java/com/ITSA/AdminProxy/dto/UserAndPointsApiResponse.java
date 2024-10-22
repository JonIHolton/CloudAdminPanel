package com.ITSA.AdminProxy.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAndPointsApiResponse {
    private UserAndPointResponse data;
    private Meta meta;

    public UserAndPointsApiResponse(UserAndPointResponse data, int totalRowCount) {
        this.data = data;
        this.meta = new Meta(totalRowCount);
    }
}

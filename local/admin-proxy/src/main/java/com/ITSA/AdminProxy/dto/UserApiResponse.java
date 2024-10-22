package com.ITSA.AdminProxy.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserApiResponse {
    private List<SingleUserResponseDTO> data;
    private Meta meta;

    public UserApiResponse(List<SingleUserResponseDTO> data, int totalRowCount) {
        this.data = data;
        this.meta = new Meta(totalRowCount);
    }
}

package com.ITSA.AdminProxy.service;

import com.ITSA.AdminProxy.dto.TempLogRequestDTO;
import com.ITSA.AdminProxy.dto.ViewLogRequestDTO;

import org.springframework.http.ResponseEntity;

public interface LogService {

    ResponseEntity<?> getLogs(ViewLogRequestDTO TempLogRequestDTO);
    ResponseEntity<?> getLogById(String logId);

}
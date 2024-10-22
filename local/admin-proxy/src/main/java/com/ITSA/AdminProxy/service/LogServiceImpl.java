package com.ITSA.AdminProxy.service;

import org.springframework.http.ResponseEntity;
import com.ITSA.AdminProxy.util.HttpClientUtility;
import com.ITSA.AdminProxy.dto.TempLogRequestDTO;
import com.ITSA.AdminProxy.dto.ViewLogRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {
    private static final Logger logger = LoggerFactory.getLogger(
        LogServiceImpl.class
    );

    @Autowired
    private HttpClientUtility httpClientUtility;

    private static final String LOGGING_URL = "http://127.0.0.1:8005/logs/query";

    @Override
    public ResponseEntity<?> getLogs(ViewLogRequestDTO viewLogRequestDTO)
    {
        try {
            logger.info(viewLogRequestDTO.getDescription());
            logger.info(viewLogRequestDTO.getLogId());
            logger.info(viewLogRequestDTO.toString());
            ResponseEntity<?> response = httpClientUtility.getForObject(LOGGING_URL, viewLogRequestDTO);
            logger.info(response.toString());
            return response;

        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error invoking logging Service: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getLogById(String logId) {
        try {
            logger.debug(logId);
            ResponseEntity<?> response = httpClientUtility.getForObject(LOGGING_URL + "/get_by_id", logId);
            return response;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving log by ID: " + e.getMessage());
        }
    }
    
}

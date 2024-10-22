package com.ITSA.AdminProxy.util;


import java.util.List;

// import org.hibernate.validator.internal.util.logging.Log_.logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpClientUtility {

    private final RestTemplate restTemplate;

    public HttpClientUtility() {
        this.restTemplate = new RestTemplate();
    }

    public <T> T getForObject(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }

    // for JSON request
    public ResponseEntity<?> getForObject(String url, Object request) {
        ResponseEntity<?> response = restTemplate.postForEntity(url, request, Object.class);
        // System.out.println("Response from logging service: " + response.toString());
        return response;
    }

        // New method to fetch a list of strings
    public List<String> getListOfString(String url) {
        ResponseEntity<List<String>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {});
        return response.getBody();
    }
    public String getPolicyDocument(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    public ResponseEntity<?> getForEntity(String url) {
        return restTemplate.getForEntity(url, Object.class);
    }
    
    
}
 

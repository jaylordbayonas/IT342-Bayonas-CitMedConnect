package edu.cit.bayonas.citmedconnect.features.oauth2.dto;

import jakarta.validation.constraints.NotBlank;

public class CodeExchangeRequest {
    
    @NotBlank(message = "Authorization code is required")
    private String code;
    
    private String redirectUri;

    // Constructors
    public CodeExchangeRequest() {}
    
    public CodeExchangeRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    @Override
    public String toString() {
        return "CodeExchangeRequest{" +
                "code='" + code + '\'' +
                ", redirectUri='" + redirectUri + '\'' +
                '}';
    }
}
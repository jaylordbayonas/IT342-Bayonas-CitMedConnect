package edu.cit.bayonas.citmedconnect.features.oauth2.dto;

import jakarta.validation.constraints.NotBlank;

public class GitHubOAuth2CallbackRequest {
    
    @NotBlank(message = "Access token is required")
    private String accessToken;

    // Constructors
    public GitHubOAuth2CallbackRequest() {}
    
    public GitHubOAuth2CallbackRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    @Override
    public String toString() {
        return "GitHubOAuth2CallbackRequest{" +
                "accessToken='" + (accessToken != null ? "[REDACTED]" : "null") + '\'' +
                '}';
    }
}
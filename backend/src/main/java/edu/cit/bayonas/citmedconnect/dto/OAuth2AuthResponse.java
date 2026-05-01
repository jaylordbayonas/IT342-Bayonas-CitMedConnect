package edu.cit.bayonas.citmedconnect.dto;

public class OAuth2AuthResponse {
    private boolean success;
    private String message;
    private UserDTO user;
    private String token;
    private Boolean isNewUser;

    public OAuth2AuthResponse() {
    }

    public OAuth2AuthResponse(boolean success, String message, UserDTO user, String token, Boolean isNewUser) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
        this.isNewUser = isNewUser;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getIsNewUser() {
        return isNewUser;
    }

    public void setIsNewUser(Boolean isNewUser) {
        this.isNewUser = isNewUser;
    }
}

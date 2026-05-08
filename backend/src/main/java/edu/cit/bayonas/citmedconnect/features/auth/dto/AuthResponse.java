package edu.cit.bayonas.citmedconnect.features.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private Boolean success;
    private String message;
    private String token;
    private UserData user;

    public AuthResponse() {}

    public AuthResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(Boolean success, String message, String token, UserData user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserData {
        private String id;
        private String schoolId;
        private String name;
        private String email;

        public UserData() {}

        public UserData(String schoolId, String name, String email) {
            this.id = schoolId; // Keep for backward compatibility
            this.schoolId = schoolId; // Add explicit schoolId field
            this.name = name;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
            this.schoolId = id; // Keep both fields in sync
        }

        public String getSchoolId() {
            return schoolId;
        }

        public void setSchoolId(String schoolId) {
            this.schoolId = schoolId;
            this.id = schoolId; // Keep both fields in sync
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

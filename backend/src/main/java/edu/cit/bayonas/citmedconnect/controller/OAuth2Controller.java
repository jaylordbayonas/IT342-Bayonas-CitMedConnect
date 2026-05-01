package edu.cit.bayonas.citmedconnect.controller;

import java.util.UUID;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.bayonas.citmedconnect.dto.GitHubOAuth2UserInfo;
import edu.cit.bayonas.citmedconnect.dto.OAuth2AuthResponse;
import edu.cit.bayonas.citmedconnect.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.service.OAuth2Service;

@RestController
@RequestMapping("/api/auth/oauth2")
@CrossOrigin(origins = "*")
public class OAuth2Controller {

    @Autowired
    private OAuth2Service oAuth2Service;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    /**
     * Handle GitHub OAuth2 callback
     * POST /api/auth/oauth2/github/callback
     * 
     * Expects: { "accessToken": "github_access_token" }
     */
    @PostMapping("/github/callback")
    public ResponseEntity<OAuth2AuthResponse> handleGitHubCallback(@RequestBody GitHubOAuth2CallbackRequest request) {
        try {
            // Validate access token
            if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new OAuth2AuthResponse(
                        false,
                        "Access token is required",
                        null,
                        null,
                        null
                    ));
            }

            // Get user info from GitHub
            GitHubOAuth2UserInfo gitHubUser = oAuth2Service.getGitHubUserInfo(request.getAccessToken());

            if (gitHubUser == null || gitHubUser.getId() == null) {
                return ResponseEntity.badRequest()
                    .body(new OAuth2AuthResponse(
                        false,
                        "Failed to retrieve GitHub user information",
                        null,
                        null,
                        null
                    ));
            }

            // Process OAuth2 user - create or update
            UserEntity user = oAuth2Service.processGitHubOAuth2(gitHubUser);
            UserDTO userDTO = oAuth2Service.convertToUserDTO(user);

            // Generate token
            String token = generateSimpleToken();

            // Determine if this is a new user (no phone/role setup yet)
            boolean isNewUser = user.getPhone() == null || user.getPhone().isEmpty();

            return ResponseEntity.ok()
                .body(new OAuth2AuthResponse(
                    true,
                    "GitHub login successful",
                    userDTO,
                    token,
                    isNewUser
                ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new OAuth2AuthResponse(
                    false,
                    "GitHub authentication failed: " + e.getMessage(),
                    null,
                    null,
                    null
                ));
        }
    }

    /**
     * Generate a simple token (replace with JWT in production)
     */
    private String generateSimpleToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Exchange GitHub authorization code for access token
     * POST /api/auth/oauth2/github/exchange-code
     *
     * Expects: { "code": "github_authorization_code" }
     * Returns: { "accessToken": "github_access_token" }
     */
    @PostMapping("/github/exchange-code")
    public ResponseEntity<Map<String, String>> exchangeCodeForToken(@RequestBody CodeExchangeRequest request) {
        try {
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Authorization code is required"));
            }

            RestClient restClient = RestClient.create();
            MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
            payload.add("client_id", githubClientId);
            payload.add("client_secret", githubClientSecret);
            payload.add("code", request.getCode());
            if (request.getRedirectUri() != null && !request.getRedirectUri().isBlank()) {
                payload.add("redirect_uri", request.getRedirectUri());
            }

            String response = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(payload)
                .retrieve()
                .body(String.class);

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (jsonResponse.has("error")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", jsonResponse.get("error").getAsString()));
            }

            String accessToken = jsonResponse.get("access_token").getAsString();
            return ResponseEntity.ok(Map.of("accessToken", accessToken));

        } catch (Exception e) {
            if (e instanceof RestClientResponseException restClientException) {
                String responseBody = restClientException.getResponseBodyAsString();
                try {
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonResponse.has("error_description")) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", jsonResponse.get("error_description").getAsString()));
                    }
                    if (jsonResponse.has("error")) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", jsonResponse.get("error").getAsString()));
                    }
                } catch (Exception ignored) {
                    // Fall through to generic response below.
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to exchange code: " + e.getMessage()));
        }
    }

    /**
     * Request DTO for code exchange
     */
    public static class CodeExchangeRequest {
        private String code;
        private String redirectUri;

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
    }
    /**
     * Request DTO for GitHub OAuth2 callback
     */
    public static class GitHubOAuth2CallbackRequest {
        private String accessToken;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}

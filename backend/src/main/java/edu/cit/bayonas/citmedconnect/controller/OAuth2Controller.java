package edu.cit.bayonas.citmedconnect.controller;

import java.util.UUID;

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

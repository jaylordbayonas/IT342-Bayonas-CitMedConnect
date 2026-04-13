package edu.cit.bayonas.citmedconnect.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import edu.cit.bayonas.citmedconnect.dto.GitHubOAuth2UserInfo;
import edu.cit.bayonas.citmedconnect.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;

@Service
public class OAuth2Service {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private static final String GITHUB_API_URL = "https://api.github.com/user";
    private static final String GITHUB_PROVIDER = "github";

    /**
     * Get user info from GitHub using access token
     */
    public GitHubOAuth2UserInfo getGitHubUserInfo(String accessToken) {
        try {
            RestClient restClient = RestClient.create();
            GitHubOAuth2UserInfo userInfo = restClient.get()
                .uri(GITHUB_API_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .body(GitHubOAuth2UserInfo.class);
            
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub user info: " + e.getMessage());
        }
    }

    /**
     * Get or create user from GitHub OAuth2 info
     */
    public UserEntity processGitHubOAuth2(GitHubOAuth2UserInfo gitHubUser) {
        // Check if user already exists by OAuth
        UserEntity existingUser = userRepository.findByOauthProviderAndOauthId(
            GITHUB_PROVIDER,
            String.valueOf(gitHubUser.getId())
        );

        if (existingUser != null) {
            return existingUser;
        }

        // Check if user exists by email
        UserEntity userByEmail = null;
        if (gitHubUser.getEmail() != null) {
            userByEmail = userRepository.findByEmail(gitHubUser.getEmail());
        }

        if (userByEmail != null) {
            // Link existing user to OAuth
            userByEmail.setOauthProvider(GITHUB_PROVIDER);
            userByEmail.setOauthId(String.valueOf(gitHubUser.getId()));
            return userRepository.save(userByEmail);
        }

        // Create new user
        UserEntity newUser = new UserEntity();
        newUser.setSchoolId(UUID.randomUUID().toString());
        newUser.setFirstName(gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin());
        newUser.setLastName("");
        newUser.setEmail(gitHubUser.getEmail() != null ? gitHubUser.getEmail() : gitHubUser.getLogin() + "@github.local");
        newUser.setPhone("");
        newUser.setPassword(""); // OAuth users don't need password
        newUser.setRole("STUDENT");
        newUser.setGender("OTHER");
        newUser.setAge(0);
        newUser.setOauthProvider(GITHUB_PROVIDER);
        newUser.setOauthId(String.valueOf(gitHubUser.getId()));

        return userRepository.save(newUser);
    }

    /**
     * Convert UserEntity to UserDTO
     */
    public UserDTO convertToUserDTO(UserEntity user) {
        UserDTO dto = new UserDTO();
        dto.setSchoolId(user.getSchoolId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setGender(user.getGender());
        dto.setAge(user.getAge());
        return dto;
    }
}

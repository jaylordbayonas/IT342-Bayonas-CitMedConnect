package edu.cit.bayonas.citmedconnect.features.oauth2.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import edu.cit.bayonas.citmedconnect.features.oauth2.dto.GitHubOAuth2UserInfo;
import edu.cit.bayonas.citmedconnect.features.auth.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.features.auth.mapper.UserMapper;

@Service
public class OAuth2Service {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserMapper userMapper;

    private static final String GITHUB_API_URL = "https://api.github.com/user";
    private static final String GITHUB_PROVIDER = "github";

    /**
     * Get user info from GitHub using access token
     */
    public GitHubOAuth2UserInfo getGitHubUserInfo(String accessToken) {
        try {
            System.out.println("Fetching GitHub user info with access token");
            
            RestClient restClient = RestClient.create();
            GitHubOAuth2UserInfo userInfo = restClient.get()
                .uri(GITHUB_API_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .body(GitHubOAuth2UserInfo.class);
            
            System.out.println("Successfully fetched GitHub user info: " + userInfo);
            return userInfo;
        } catch (Exception e) {
            System.err.println("Failed to fetch GitHub user info: " + e.getMessage());
            throw new RuntimeException("Failed to fetch GitHub user info: " + e.getMessage());
        }
    }

    /**
     * Get or create user from GitHub OAuth2 info
     */
    public UserEntity processGitHubOAuth2(GitHubOAuth2UserInfo gitHubUser) {
        System.out.println("Processing GitHub OAuth2 user: " + gitHubUser);
        
        // Check if user already exists by OAuth
        UserEntity existingUser = userRepository.findByOauthProviderAndOauthId(
            GITHUB_PROVIDER,
            String.valueOf(gitHubUser.getId())
        );

        if (existingUser != null) {
            System.out.println("Found existing OAuth user: " + existingUser.getSchoolId());
            return existingUser;
        }

        // Check if user exists by email
        UserEntity userByEmail = null;
        if (gitHubUser.getEmail() != null && !gitHubUser.getEmail().isEmpty()) {
            userByEmail = userRepository.findByEmail(gitHubUser.getEmail());
        }

        if (userByEmail != null) {
            System.out.println("Found existing user by email, linking OAuth: " + userByEmail.getSchoolId());
            // Link existing user to OAuth
            userByEmail.setOauthProvider(GITHUB_PROVIDER);
            userByEmail.setOauthId(String.valueOf(gitHubUser.getId()));
            return userRepository.save(userByEmail);
        }

        // Create new user
        System.out.println("Creating new OAuth user");
        UserEntity newUser = new UserEntity();
        newUser.setSchoolId(generateUniqueSchoolId());
        
        // Handle name parsing
        String fullName = gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin();
        String[] nameParts = fullName.split(" ", 2);
        newUser.setFirstName(nameParts[0]);
        newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        // Set email with fallback
        String email = gitHubUser.getEmail() != null && !gitHubUser.getEmail().isEmpty() 
            ? gitHubUser.getEmail() 
            : gitHubUser.getLogin() + "@github.local";
        newUser.setEmail(email);
        
        newUser.setPhone(""); // Will be filled during profile completion
        newUser.setPassword(""); // OAuth users don't need password
        newUser.setRole("STUDENT"); // Default role
        newUser.setGender("OTHER"); // Will be filled during profile completion
        newUser.setAge(0); // Will be filled during profile completion
        newUser.setOauthProvider(GITHUB_PROVIDER);
        newUser.setOauthId(String.valueOf(gitHubUser.getId()));

        UserEntity savedUser = userRepository.save(newUser);
        System.out.println("Created new OAuth user: " + savedUser.getSchoolId());
        return savedUser;
    }

    /**
     * Convert UserEntity to UserDTO using the mapper
     */
    public UserDTO convertToUserDTO(UserEntity user) {
        return userMapper.toDTO(user);
    }
    
    /**
     * Generate a unique school ID
     */
    private String generateUniqueSchoolId() {
        String schoolId;
        do {
            schoolId = "GH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsById(schoolId));
        return schoolId;
    }
    
    /**
     * Check if a user needs profile completion
     */
    public boolean isProfileIncomplete(UserEntity user) {
        return user.getPhone() == null || user.getPhone().isEmpty() ||
               user.getAge() == 0 ||
               "OTHER".equals(user.getGender());
    }
    
    /**
     * Generate a simple token (replace with JWT in production)
     */
    public String generateSimpleToken() {
        return UUID.randomUUID().toString();
    }
}
package com.example.task_collaboration.infrastructure.oauth2.dto;

public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(java.util.Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }

    @Override
    public Boolean getEmailVerified() {
        // GitHub doesn't provide email_verified field in user info
        // We assume email is verified if it exists
        return getEmail() != null && !getEmail().isEmpty();
    }
}

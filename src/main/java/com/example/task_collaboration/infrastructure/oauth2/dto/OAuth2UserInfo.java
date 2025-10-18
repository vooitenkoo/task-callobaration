package com.example.task_collaboration.infrastructure.oauth2.dto;

public abstract class OAuth2UserInfo {
    protected java.util.Map<String, Object> attributes;

    public OAuth2UserInfo(java.util.Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public java.util.Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

    public abstract String getImageUrl();

    public abstract Boolean getEmailVerified();
}

# OAuth2 Integration Guide

## 🚀 Overview

This project now includes comprehensive OAuth2 authentication support with Google and GitHub providers. Users can authenticate using their existing social media accounts, making the login process seamless and secure.

## 🔧 How OAuth2 Works

### 1. **OAuth2 Flow Overview**

OAuth2 is an authorization framework that allows applications to obtain limited access to user accounts on HTTP services. Here's how it works in our application:

```
1. User clicks "Login with Google/GitHub"
2. User is redirected to the OAuth2 provider (Google/GitHub)
3. User authorizes the application
4. Provider redirects back with authorization code
5. Application exchanges code for access token
6. Application uses access token to get user info
7. Application creates/updates user account
8. Application generates JWT tokens for the user
9. User is redirected back to frontend with tokens
```

### 2. **Architecture Components**

#### **OAuth2 Configuration**
- **Client Registration**: Configured in `application.properties`
- **Provider Configuration**: Endpoints for Google and GitHub
- **Security Configuration**: Integrated with Spring Security

#### **Custom OAuth2 User Service**
- **CustomOAuth2UserService**: Handles OAuth2 user processing
- **OAuth2UserInfo**: Abstract class for provider-specific user data
- **GoogleOAuth2UserInfo**: Google-specific user data extraction
- **GitHubOAuth2UserInfo**: GitHub-specific user data extraction

#### **Authentication Handlers**
- **OAuth2AuthenticationSuccessHandler**: Processes successful authentication
- **OAuth2AuthenticationFailureHandler**: Handles authentication failures

#### **Database Integration**
- **User Entity**: Extended with OAuth2 fields
- **Migration**: V14 adds OAuth2 columns to users table

## 🛠️ Setup Instructions

### 1. **Google OAuth2 Setup**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs"
5. Set application type to "Web application"
6. Add authorized redirect URIs:
   - `http://localhost:8084/login/oauth2/code/google`
   - `https://yourdomain.com/login/oauth2/code/google` (for production)
7. Copy Client ID and Client Secret

### 2. **GitHub OAuth2 Setup**

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click "New OAuth App"
3. Fill in application details:
   - **Application name**: Task Collaboration Hub
   - **Homepage URL**: `http://localhost:8084`
   - **Authorization callback URL**: `http://localhost:8084/login/oauth2/code/github`
4. Copy Client ID and Client Secret

### 3. **Environment Configuration**

Set the following environment variables or update `application.properties`:

```properties
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# GitHub OAuth2
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# OAuth2 Redirect URI (for frontend)
app.oauth2.redirect-uri=http://localhost:3000/oauth2/redirect
```

## 📱 Frontend Integration

### 1. **OAuth2 Login URLs**

The application provides the following OAuth2 login endpoints:

- **Google**: `/oauth2/authorization/google`
- **GitHub**: `/oauth2/authorization/github`

### 2. **Frontend Implementation Example**

```javascript
// React example
const handleGoogleLogin = () => {
  window.location.href = 'http://localhost:8084/oauth2/authorization/google';
};

const handleGitHubLogin = () => {
  window.location.href = 'http://localhost:8084/oauth2/authorization/github';
};

// Handle OAuth2 redirect
useEffect(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');
  const error = urlParams.get('error');
  
  if (token) {
    const userData = JSON.parse(decodeURIComponent(token));
    // Store tokens and user data
    localStorage.setItem('accessToken', userData.accessToken);
    localStorage.setItem('refreshToken', userData.refreshToken);
    // Redirect to dashboard
  } else if (error) {
    // Handle error
    console.error('OAuth2 error:', error);
  }
}, []);
```

### 3. **OAuth2 Response Format**

After successful authentication, the user is redirected with the following data:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "imageUrl": "https://lh3.googleusercontent.com/...",
  "provider": "GOOGLE",
  "role": "USER"
}
```

## 🔐 Security Features

### 1. **JWT Integration**
- OAuth2 users receive JWT tokens just like local users
- Same token format and validation
- Seamless integration with existing authentication

### 2. **User Account Linking**
- Existing local users can link OAuth2 accounts
- Same email address automatically links accounts
- Provider information is stored for future logins

### 3. **Data Protection**
- OAuth2 tokens are not stored in the database
- Only essential user information is persisted
- Email verification status is tracked

## 📊 Database Schema

### **Users Table Extensions**

```sql
-- OAuth2 fields added to users table
ALTER TABLE users 
ADD COLUMN provider VARCHAR(50),
ADD COLUMN provider_id VARCHAR(255),
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN image_url VARCHAR(1000);

-- Unique constraint for provider + provider_id
ALTER TABLE users 
ADD CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id);
```

### **User Entity Fields**

```java
public enum AuthProvider {
    LOCAL, GOOGLE, GITHUB
}

// OAuth2 fields
private AuthProvider provider;
private String providerId;
private Boolean emailVerified;
private String imageUrl;
```

## 🧪 Testing

### 1. **API Endpoints**

- **GET** `/api/oauth2/login-urls` - Get OAuth2 login URLs
- **GET** `/api/oauth2/providers` - Get provider information

### 2. **Swagger UI**

OAuth2 endpoints are documented in Swagger UI with:
- OAuth2 security scheme configuration
- Provider information
- Example requests and responses

### 3. **Test Scenarios**

1. **New User Registration**: First-time OAuth2 login creates new user
2. **Existing User Login**: OAuth2 login for existing user updates provider info
3. **Account Linking**: Local user with same email gets OAuth2 provider linked
4. **Error Handling**: Invalid credentials, network errors, etc.

## 🚀 Production Deployment

### 1. **Environment Variables**

Set production OAuth2 credentials:

```bash
export GOOGLE_CLIENT_ID=your-production-google-client-id
export GOOGLE_CLIENT_SECRET=your-production-google-client-secret
export GITHUB_CLIENT_ID=your-production-github-client-id
export GITHUB_CLIENT_SECRET=your-production-github-client-secret
export APP_OAUTH2_REDIRECT_URI=https://yourdomain.com/oauth2/redirect
```

### 2. **OAuth2 Provider Configuration**

Update redirect URIs in OAuth2 provider settings:
- **Google**: `https://yourdomain.com/login/oauth2/code/google`
- **GitHub**: `https://yourdomain.com/login/oauth2/code/github`

### 3. **CORS Configuration**

Update CORS settings for production domain:

```java
configuration.setAllowedOrigins(List.of(
    "https://yourdomain.com",
    "https://www.yourdomain.com"
));
```

## 🔍 Troubleshooting

### Common Issues

1. **"Invalid redirect URI"**
   - Check OAuth2 provider configuration
   - Ensure redirect URI matches exactly

2. **"Client ID not found"**
   - Verify environment variables are set
   - Check application.properties configuration

3. **"Email not found from OAuth2 provider"**
   - Some OAuth2 providers require additional scopes
   - Check scope configuration in application.properties

4. **CORS errors**
   - Update CORS configuration for your frontend domain
   - Ensure credentials are allowed

### Debug Mode

Enable debug logging:

```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG
```

## 📚 Additional Resources

- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [GitHub OAuth2 Documentation](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [OAuth2 RFC Specification](https://tools.ietf.org/html/rfc6749)

## 🎯 Benefits

1. **User Experience**: Seamless login without creating new accounts
2. **Security**: Leverages proven OAuth2 security standards
3. **Scalability**: Easy to add more OAuth2 providers
4. **Integration**: Works with existing JWT authentication
5. **Flexibility**: Supports both local and OAuth2 authentication

This OAuth2 integration provides a professional, secure, and user-friendly authentication system that's ready for production use! 🚀

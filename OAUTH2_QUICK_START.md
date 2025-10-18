# 🚀 OAuth2 Quick Start Guide

## ⚡ Быстрый запуск OAuth2

### 1. **Настройка Google OAuth2**

1. Перейдите в [Google Cloud Console](https://console.cloud.google.com/)
2. Создайте проект или выберите существующий
3. Включите Google+ API
4. Перейдите в "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs"
5. Добавьте redirect URI: `http://localhost:8084/login/oauth2/code/google`
6. Скопируйте Client ID и Client Secret

### 2. **Настройка GitHub OAuth2**

1. Перейдите в [GitHub Developer Settings](https://github.com/settings/developers)
2. Нажмите "New OAuth App"
3. Заполните:
   - **Application name**: Task Collaboration Hub
   - **Homepage URL**: `http://localhost:8084`
   - **Authorization callback URL**: `http://localhost:8084/login/oauth2/code/github`
4. Скопируйте Client ID и Client Secret

### 3. **Настройка переменных окружения**

Создайте файл `.env` или установите переменные окружения:

```bash
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export GITHUB_CLIENT_ID=your-github-client-id
export GITHUB_CLIENT_SECRET=your-github-client-secret
```

### 4. **Запуск приложения**

```bash
./gradlew bootRun
```

### 5. **Тестирование OAuth2**

#### **Прямые ссылки для входа:**
- **Google**: `http://localhost:8084/oauth2/authorization/google`
- **GitHub**: `http://localhost:8084/oauth2/authorization/github`

#### **API endpoints:**
- **GET** `/api/oauth2/login-urls` - Получить URL для входа
- **GET** `/api/oauth2/providers` - Информация о провайдерах

#### **Swagger UI:**
- Откройте: `http://localhost:8084/swagger-ui.html`
- OAuth2 endpoints будут в разделе "OAuth2"

## 🔧 Frontend Integration

### **React Example:**

```javascript
// OAuth2 login buttons
const GoogleLogin = () => {
  window.location.href = 'http://localhost:8084/oauth2/authorization/google';
};

const GitHubLogin = () => {
  window.location.href = 'http://localhost:8084/oauth2/authorization/github';
};

// Handle OAuth2 redirect
useEffect(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');
  
  if (token) {
    const userData = JSON.parse(decodeURIComponent(token));
    console.log('OAuth2 User:', userData);
    // Store tokens and redirect
  }
}, []);
```

## 📊 Что происходит при OAuth2 входе

1. **Пользователь нажимает "Login with Google"**
2. **Перенаправление на Google** → Пользователь авторизует приложение
3. **Google перенаправляет обратно** → С кодом авторизации
4. **Приложение обменивает код на токен** → Получает access token
5. **Получение данных пользователя** → Из Google API
6. **Создание/обновление пользователя** → В базе данных
7. **Генерация JWT токенов** → Access + Refresh токены
8. **Перенаправление на frontend** → С токенами в URL

## 🎯 Результат OAuth2 входа

После успешного входа пользователь получает:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "email": "john.doe@gmail.com",
  "imageUrl": "https://lh3.googleusercontent.com/...",
  "provider": "GOOGLE",
  "role": "USER"
}
```

## 🔐 Безопасность

- ✅ **JWT токены** - Стандартная аутентификация
- ✅ **OAuth2 стандарт** - Проверенная безопасность
- ✅ **HTTPS в продакшене** - Защищенная передача
- ✅ **Валидация токенов** - Проверка подлинности
- ✅ **CORS настройки** - Контроль доступа

## 🚀 Готово к продакшену!

OAuth2 интеграция полностью готова и включает:
- ✅ Google OAuth2
- ✅ GitHub OAuth2  
- ✅ JWT интеграция
- ✅ Swagger документация
- ✅ Обработка ошибок
- ✅ Безопасность
- ✅ Масштабируемость

**Наслаждайтесь профессиональной OAuth2 аутентификацией!** 🎉

# Selenium WebDriver — краткое руководство

## Что такое Selenium?

**Selenium** — это инструмент для **автоматизации браузера**. Он позволяет программе управлять браузером как реальный пользователь: открывать страницы, кликать, вводить текст, проверять отображение элементов.

### Для чего используется:
- **E2E (end-to-end) тестирование** — проверка всего приложения от UI до БД
- **Регрессионное тестирование** — убедиться, что после изменений ничего не сломалось
- **Автоматизация** — рутинные действия в браузере

---

## Запуск демо-тестов

```bash
./gradlew test --tests "com.example.task_collaboration.SeleniumDemoTest"
```

**Требования:** установленный Google Chrome. Selenium 4 автоматически скачивает ChromeDriver.

---

## Что делают демо-тесты

| Тест | Что проверяет |
|------|----------------|
| 1. Открытие страницы | `driver.get(url)` и `getTitle()` |
| 2. Поиск по ID | `findElement(By.id("main-title"))` |
| 3. Поиск по классу | `findElement(By.className("description"))` |
| 4. Форма | `sendKeys()`, `click()`, явное ожидание |
| 5. Список элементов | `findElements()` — несколько элементов |
| 6. Атрибуты | `getAttribute("placeholder")` |

---

## Полезные селекторы

```java
By.id("element-id")
By.className("my-class")
By.cssSelector("#id .class")      // CSS-селектор
By.xpath("//button[@type='submit']")
By.tagName("h1")
By.name("username")
```

---

## Запуск с видимым браузером

В `SeleniumDemoTest.java` закомментируй или удали строку:
```java
options.addArguments("--headless=new");
```
Тогда Chrome будет открываться и ты увидишь, как выполняются тесты.

---

## Тестирование Swagger UI вашего приложения

Когда приложение запущено на `http://localhost:8084`, можно добавить тест:

```java
driver.get("http://localhost:8084/swagger-ui.html");
WebElement title = driver.findElement(By.tagName("h1"));
assertThat(title.getText()).contains("Swagger");
```

package com.example.task_collaboration;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Демонстрация Selenium WebDriver — инструмент для автоматизации браузера.
 *
 * <p><b>Для чего нужен Selenium:</b>
 * <ul>
 *   <li>E2E (end-to-end) тестирование веб-приложений — проверка работы как реальный пользователь</li>
 *   <li>Автоматизация рутинных действий в браузере</li>
 *   <li>Проверка UI: клики, ввод текста, навигация, проверка отображения элементов</li>
 * </ul>
 *
 * <p><b>Требования:</b> Установленный Chrome. Selenium 4 автоматически скачивает ChromeDriver.
 */
@DisplayName("Selenium WebDriver — демо-тесты")
class SeleniumDemoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("1. Открытие страницы и проверка заголовка")
    void openPageAndCheckTitle() {
        String url = getDemoPageUrl();
        driver.get(url);

        String title = driver.getTitle();
        assertThat(title).isEqualTo("Демо для Selenium");
    }

    @Test
    @DisplayName("2. Поиск элемента по ID и проверка текста")
    void findElementByIdAndCheckText() {
        driver.get(getDemoPageUrl());

        WebElement heading = driver.findElement(By.id("main-title"));
        String text = heading.getText();

        assertThat(text).isEqualTo("Привет, Selenium!");
    }

    @Test
    @DisplayName("3. Поиск элемента по CSS-классу")
    void findElementByClassName() {
        driver.get(getDemoPageUrl());

        WebElement description = driver.findElement(By.className("description"));
        assertThat(description.getText()).contains("тестовая страница");
    }

    @Test
    @DisplayName("4. Ввод текста в поле и отправка формы")
    void fillFormAndSubmit() {
        driver.get(getDemoPageUrl());

        WebElement usernameInput = driver.findElement(By.id("username"));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement submitBtn = driver.findElement(By.id("submit-btn"));

        usernameInput.sendKeys("ТестовыйПользователь");
        emailInput.sendKeys("test@example.com");
        submitBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));
        WebElement result = driver.findElement(By.id("result-text"));
        assertThat(result.getText()).isEqualTo("ТестовыйПользователь / test@example.com");
    }

    @Test
    @DisplayName("5. Поиск нескольких элементов (список)")
    void findMultipleElements() {
        driver.get(getDemoPageUrl());

        List<WebElement> items = driver.findElements(By.cssSelector("#items .item"));
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getText()).isEqualTo("Элемент 1");
        assertThat(items.get(1).getText()).isEqualTo("Элемент 2");
        assertThat(items.get(2).getText()).isEqualTo("Элемент 3");
    }

    @Test
    @DisplayName("6. Проверка атрибутов элемента")
    void checkElementAttributes() {
        driver.get(getDemoPageUrl());

        WebElement usernameInput = driver.findElement(By.id("username"));
        assertThat(usernameInput.getAttribute("placeholder")).isEqualTo("Введите имя");
        assertThat(usernameInput.getAttribute("type")).isEqualTo("text");
    }

    private String getDemoPageUrl() {
        URL resource = getClass().getClassLoader().getResource("selenium-demo.html");
        assertThat(resource).isNotNull();
        return resource.toExternalForm();
    }
}

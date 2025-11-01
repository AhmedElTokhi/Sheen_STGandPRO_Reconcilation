package Reconciliation_TC;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import utilities.ZohoUtils;

import java.time.Duration;

//Consider with max export from zoho is 3000
//######So as a precondition empty zoho before running this test######

public class A4_zohoExport_TC {

    // ✅ WebDriver instance
    WebDriver driver;

    // ✅ Config values (loaded from ZohoUtils)
    private String exportUrl;
    private String username;
    private String password;

    // ✅ Locators
    private static final By EXPORT_BUTTON = By.className("blue-btn");
    private static final By emailField = By.cssSelector("#login_id");
    private static final By passwordField = By.cssSelector("#password");
    private static final By NextBtn = By.cssSelector("#nextbtn");
    private static final By TERMINATE_ALL_SESSIONS = By.id("continue_button");
    private static final By SelectModule = By.xpath("//*[@id='s2id_autogen1']/a");

    /**
     * ▶️ Test: Main export flow
     */
    @Test
    public void ExportFlow() throws Exception {
        // 🔹 Load configuration from ZohoUtils
        String configPath = "./src/main/resources/zohoConfig.json";
        ZohoUtils.ConfigModel config = ZohoUtils.getConfig(configPath);

        exportUrl = config.getExportUrl();
        username  = config.getUsername();
        password  = config.getPassword();

        // 🔹 Setup browser
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        manageBrowser();

        // 🔹 Navigate and login
        navigateTo(exportUrl);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        login();

        // 🔹 Handle possible popup
        clickIfExists(TERMINATE_ALL_SESSIONS);

        // 🔹 Go back to export page and select module
        navigateTo(exportUrl);
        clickDropDown();
    }

    /**
     * 🔹 Navigate to given URL
     */
    public void navigateTo(String url) {
        driver.navigate().to(url);
    }

    /**
     * 🔹 Perform login using loaded credentials
     */
    public void login() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // 🔹 Email field
        wait.until(ExpectedConditions.elementToBeClickable(emailField)).sendKeys(username);
        driver.findElement(NextBtn).click();

        // 🔹 Password field
        wait.until(ExpectedConditions.elementToBeClickable(passwordField)).sendKeys(password);
        driver.findElement(NextBtn).click();
    }


    /**
     * 🔹 Click dropdown, search for "Export Tickets" and click export
     */
    public void clickDropDown() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 🔹 Switch to iframe
        WebElement iframe = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='zd_setup_iframe']"))
        );
        driver.switchTo().frame(iframe);

        // 🔹 Open module dropdown
        wait.until(ExpectedConditions.elementToBeClickable(SelectModule));
        driver.findElement(SelectModule).click();

        // 🔹 Search and select "Export Tickets"
        WebElement searchBox = driver.findElement(By.xpath("//*[@id='s2id_autogen2_search']"));
        searchBox.sendKeys("Export Tickets");
        searchBox.sendKeys(Keys.ENTER);

        // 🔹 Click export button
        driver.findElement(EXPORT_BUTTON).click();
    }

    /**
     * 🔹 Maximize browser window
     */
    public void manageBrowser() {
        driver.manage().window().maximize();
    }

    /**
     * 🔹 Click an element if it exists
     */
    public void clickIfExists(By locator) {
        if (!driver.findElements(locator).isEmpty()) {
            driver.findElement(locator).click();
        }
    }

    /**
     * 🔹 Quit browser session after test
     */
    @AfterTest
    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
}

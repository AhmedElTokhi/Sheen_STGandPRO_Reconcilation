package Reconciliation_TC;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;



import java.time.Duration;

public class zohoExport {

    private static final String EXPORT_URL = "https://desk.zoho.com/agent/wondertravel/reconciliation-departmente/setup#setup/data-administration/export";
    private static final String USERNAME = "hsheimy@shuratech.com";
    private static final String PASSWORD = "7!w9kC.j!K6P4mnjXS";
    private static final By EXPORT_BUTTON = By.className("blue-btn");
    private static final By emailField = By.cssSelector("#login_id");
    private static final By passwordField = By.cssSelector("#password");
    private static final By NextBtn = By.cssSelector("#nextbtn");
    private static final By TERMINATE_ALL_SESSIONS = By.id("continue_button");
    private static final By SelectModule = By.id("#s2id_autogen1");



    WebDriver driver;

    @Test
    public void ExportFlow() throws InterruptedException {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        manageBrowser();
        navigateTo(EXPORT_URL);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        login();
        clickIfExists(TERMINATE_ALL_SESSIONS);
        navigateTo(EXPORT_URL);
        clickDropDown();
    }

    public void navigateTo(String EXPORT_URL) {
        driver.navigate().to(EXPORT_URL);
    }

    public void login() {
        driver.findElement(emailField).click();
        driver.findElement(emailField).sendKeys(USERNAME);
        driver.findElement(NextBtn).click();
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(NextBtn).click();



    }


    public void ExportTickets() {
        driver.findElement(SelectModule).click();
        WebElement dropDown = driver.findElement(By.className("select2-results"));
        Select selectObject = new Select(dropDown);
        selectObject.selectByVisibleText("Export Tickets");
//        driver.findElement(exportTickets).click();
        driver.findElement(EXPORT_BUTTON).click();

    }

    public void clickDropDown() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//        WebElement iframe = driver.findElement(By.xpath("//*[@id='zd_setup_iframe']"));
//        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframe));
//        driver.switchTo().frame(iframe);
//        System.out.println("Switched to iframe");
//        //JS Executor to click on hidden elements in DOM//
//         JavascriptExecutor js = (JavascriptExecutor) driver;
//        WebElement hiddenBtn1 = driver.findElement(SelectModule);
//        js.executeScript("arguments[6].click();", hiddenBtn1);
        Thread.sleep(2000);
        WebElement iframe = driver.findElement(By.xpath("//*[@id='zd_setup_iframe']"));
        driver.switchTo().frame(iframe);
        System.out.println("Switched to iframe");   //For testing
        WebElement list1=driver.findElement(By.xpath("//*[@id='s2id_autogen1']/a"));
        System.out.println("element is defined");//For testing
        list1.click();
        System.out.println("Clicked on the dropdown to select module");
        WebElement List1=driver.findElement(By.xpath("//*[@id='s2id_autogen2_search']"));
        List1.sendKeys("Export Tickets");//For testing
        List1.sendKeys(Keys.ENTER);

        //using JS executor to click on export button

        //##################################
//        WebElement exportBtn = driver.findElement(EXPORT_BUTTON);
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        js.executeScript(
//                "function fire(el, type){ " +
//                        "  el.dispatchEvent(new MouseEvent(type, {bubbles:true, cancelable:true, view:window})); " +
//                        "} " +
//                        "var el = arguments[0]; " +
//                        "fire(el, 'mouseover'); " +
//                        "fire(el, 'mousedown'); " +
//                        "fire(el, 'mouseup'); " +
//                        "fire(el, 'click');",
//                exportBtn
//        );



        //js.executeScript("arguments[0].click();", exportBtn);
//####################################################################


        //Using normal click method to click on export button

        driver.findElement(EXPORT_BUTTON).submit();

        //##########################################################
//        driver.findElement(EXPORT_BUTTON).click();
//
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        js.executeScript("arguments[0].click();", EXPORT_BUTTON);
        //##########################################################
    }

    public void manageBrowser() {
        driver.manage().window().maximize();
    }
    public void clickIfExists(By locator) {
        if (!driver.findElements(TERMINATE_ALL_SESSIONS).isEmpty()) {
            driver.findElement(TERMINATE_ALL_SESSIONS).click();
        }
    }

    public void quit() {
        driver.quit();
    }
}

//login✅
//handlePop-ups✅
//navigate to export page✅
//select module dropdown✅
//select view✅
//click export button✅
//Sign-out



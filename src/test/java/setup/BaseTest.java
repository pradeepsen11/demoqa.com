package setup;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;

public class BaseTest {

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	Map<String, Object> prefs = new HashMap<>();
	Map<String, Object> profile = new HashMap<>();
	Map<String, Object> contentSettings = new HashMap<>();
	String os = System.getProperty("os.name").toLowerCase();

	@Test(enabled = false)
	public void environmentSetupStatus() {
	}

	@BeforeTest
	@Parameters("url")
	public void beforeClass(ITestContext context, String url) {
//		System.setProperty("webdriver.chrome.driver", "./Drivers/chromedriver.exe");

		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--incognito");
//		options.addArguments("--headless");
		driver.set(new ChromeDriver(options));

		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		getDriver().manage().window().maximize();
		getDriver().get(url);
	}

	@AfterTest
	public void afterClass(ITestContext context) {
		if (driver != null) {
			getDriver().manage().deleteAllCookies();

			getDriver().quit();
			driver.remove();
		}
	}

	public static WebDriver getDriver() {
		return driver.get();
	}
}

package utility;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import setup.BaseTest;

public class GenericFunctions extends BaseTest {

	private static final int DEFAULT_WAIT_SECONDS = 10;

	public static void click(WebElement element) {
		for (int i = 0; i < 3; i++) {
			try {
				waitForClickability(element);
				scrollToElement(element);
				element.click();
				break;
			} catch (StaleElementReferenceException e) {
				e.printStackTrace();
				sleep(1000);
			} catch (ElementClickInterceptedException e) {
				e.printStackTrace();
				clickAtCoordinates(1, 1);
				sleep(500);
			}
		}

	}

	public static void sendKeys(WebElement element, String text) {
		waitForVisibility(element);
		element.sendKeys(text);
	}

	public static void clearAndSendKeys(WebElement element, String text) {
		waitForVisibility(element);
		element.clear();
		element.sendKeys(text);
	}

	public static String generateRandomPhoneNo() {
		int firstDigit = ThreadLocalRandom.current().nextInt(6, 10);
		long remainingDigits = ThreadLocalRandom.current().nextLong(1_000_000_000L);

		return firstDigit + String.format("%09d", remainingDigits);
	}

	public static String currentDateAndTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		return LocalDateTime.now().format(formatter);
	}

	public static String formatTime(long millis) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter);
	}

	public static String getCurrentTime() {
		LocalTime now = LocalTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

		return now.format(formatter);
	}

	public static String overallExecutionTime(long startTime, long endTime) {
		long duration = endTime - startTime;

		long seconds = (duration / 1000) % 60;
		long minutes = (duration / (1000 * 60)) % 60;
		long hours = (duration / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public static void browseAndUploadFile(WebElement inputFileElement, String filePath) {

		System.out.println("Uploading Document .....");

		if (filePath == null || filePath.trim().isEmpty()) {
			throw new IllegalArgumentException("Upload file path cannot be null or empty.");
		}

		File file = new File(filePath);

		if (!file.isAbsolute()) {
			file = new File(System.getProperty("user.dir"), filePath);
		}

		String absolutePath = file.getAbsolutePath();

		if (!file.exists()) {
			throw new RuntimeException("Upload file not found: " + absolutePath);
		}

		if (!file.isFile()) {
			throw new RuntimeException("Upload path is not a file: " + absolutePath);
		}

		WebDriver driver = getDriver();
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

		boolean elementReady = false;

		for (int i = 0; i < 3; i++) {
			try {
				scrollToElement(inputFileElement);

				jsExecutor.executeScript("arguments[0].removeAttribute('disabled');", inputFileElement);

				elementReady = true;
				break;

			} catch (StaleElementReferenceException e) {
				System.err.println("Retrying upload because of stale element. Attempt: " + (i + 1));

			} catch (Exception e) {
				System.err
						.println("Retrying upload because of exception. Attempt: " + (i + 1) + " | " + e.getMessage());
			}
		}

		if (!elementReady) {
			throw new RuntimeException("Unable to prepare upload element after 3 attempts.");
		}

		inputFileElement.sendKeys(absolutePath);
	}

	public static void waitForVisibility(WebElement element) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(DEFAULT_WAIT_SECONDS));

		wait.until(ExpectedConditions.visibilityOf(element));
	}

	public static void waitForClickability(WebElement element) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(DEFAULT_WAIT_SECONDS));

		wait.until(ExpectedConditions.elementToBeClickable(element));
	}

	public static void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread was interrupted during sleep.", e);
		}
	}

	public static void clickAtCoordinates(int x, int y) {
		Actions actions = new Actions(getDriver());
		actions.moveByOffset(x, y).click().perform();
		actions.moveByOffset(-x, -y).perform();
	}

	public static void clear(WebElement element) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(DEFAULT_WAIT_SECONDS));

		wait.until(ExpectedConditions.visibilityOf(element)).clear();
	}

	public static String getText(WebElement element) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(DEFAULT_WAIT_SECONDS));

		return wait.until(ExpectedConditions.visibilityOf(element)).getText();
	}

	public static boolean isDisplayed(WebElement element) {
		try {
			return element.isDisplayed();

		} catch (NoSuchElementException | StaleElementReferenceException e) {
			return false;
		}
	}

	public static boolean isEnabled(WebElement element) {
		try {
			return element.isEnabled();

		} catch (NoSuchElementException | StaleElementReferenceException e) {
			return false;
		}
	}

	public static void scrollToElement(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();

		js.executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", element);
	}
}

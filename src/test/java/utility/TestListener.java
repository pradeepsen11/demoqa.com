package utility;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import setup.BaseTest;

import java.io.File;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class TestListener extends BaseTest implements ITestListener {

	@Override
	public void onStart(ITestContext context) {
		String suiteName = context.getCurrentXmlTest().getName();
		String reportName = "ExtentReport_" + suiteName;
		ExtentReports reporter = ExtentManager.getReporter(reportName);
		context.setAttribute("reporter", reporter);
		System.out.println("Starting Test Suite: " + suiteName);
	}

	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		String suiteName = result.getTestContext().getCurrentXmlTest().getName();
		ExtentReports reporter = (ExtentReports) result.getTestContext().getAttribute("reporter");

		System.out.println("Starting Test: " + testName + " in suite: " + suiteName);
		ExtentTestManager.startTest(testName, "Execution of " + testName, reporter);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		ExtentTestManager.log(LogStatus.INFO, getDriver().getCurrentUrl());
		String testName = result.getMethod().getMethodName();
		ExtentTest test = ExtentTestManager.getTest();

		ExtentTestManager.log(LogStatus.INFO, "Test Passed");

		WebDriver driver = getDriver();
		String screenshot = "data:image/png;base64," + ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
		test.log(LogStatus.INFO, "Screenshot", test.addBase64ScreenShot(screenshot));

		System.out.println("Test Passed: " + testName);
		ExtentTestManager.endTest();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		ExtentTest test = ExtentTestManager.getTest();

		Throwable error = result.getThrowable();
		test.log(LogStatus.FAIL, error);

		WebDriver driver = getDriver();
		if (isDriverAlive(driver)) {
			String screenshot = "data:image/png;base64,"
					+ ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
			test.log(LogStatus.INFO, "Screenshot", test.addBase64ScreenShot(screenshot));
		} else {
			test.log(LogStatus.FAIL, "Browser session no longer exists.");
		}

		System.out.println("Test Failed: " + result.getMethod().getMethodName());
		ExtentTestManager.endTest();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ExtentTestManager.log(LogStatus.SKIP, "Test Skipped");
		System.out.println("Test Skipped: " + result.getMethod().getMethodName());
		ExtentTestManager.endTest();
	}

	@Override
	public void onFinish(ITestContext context) {
		String suiteName = context.getCurrentXmlTest().getName();
		String reportBaseName = "ExtentReport_" + suiteName;
		String reportFileName = reportBaseName + ".html";

		ExtentManager.endReporter(reportBaseName);
		System.out.println("Finished Test Suite: " + suiteName);

		int failedCount = context.getFailedTests().size() + context.getFailedConfigurations().size();

		String workingDir = System.getProperty("user.dir");
		File reportsDir = new File(workingDir, "ExtentReports");

		File latestReport = new File(reportsDir, reportFileName);

		// ✅ Rename only if no failures
		if (failedCount == 0 && latestReport.exists()) {
			File renamed = new File(reportsDir, reportBaseName + "_PASS.html");
			if (latestReport.renameTo(renamed)) {
				System.out.println("✅ Renamed final report (PASS): " + renamed.getAbsolutePath());
			} else {
				System.err.println("❌ Failed to rename report file");
			}
		} else if (failedCount > 0) {
			System.out
					.println("⚠️ Build had failures → keeping original report name: " + latestReport.getAbsolutePath());
		} else {
			System.err.println("❌ Report file not found: " + latestReport.getAbsolutePath());
		}
	}

	public String escapeHtml(String text) {
		if (text == null)
			return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	public static boolean isDriverAlive(WebDriver driver) {
		try {
			driver.getWindowHandles();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

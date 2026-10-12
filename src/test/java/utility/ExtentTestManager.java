package utility;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class ExtentTestManager {

	private static ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
	private static ThreadLocal<ExtentReports> reporterThread = new ThreadLocal<>();

	public static synchronized ExtentTest startTest(String testName, String desc, ExtentReports extent) {
		ExtentTest test = extent.startTest(testName, desc);
		testThread.set(test);
		reporterThread.set(extent);
		return test;
	}

	public static synchronized ExtentTest getTest() {
		return testThread.get();
	}

	public static synchronized void endTest() {
		ExtentTest test = testThread.get();
		if (test != null) {
			ExtentReports extent = reporterThread.get();
			if (extent != null) {
				extent.endTest(test);
			}
		}
		testThread.remove();
		reporterThread.remove();
	}

	public static void addHeadlineCommentInTestReport(String comment) {
		log(LogStatus.INFO, "<span style='color:red; font-weight:bold; font-size:18px;'>" + comment + "</span>");
		System.out.println("[" + LogStatus.INFO + "] [" + GenericFunctions.getCurrentTime() + "] " + comment);
	}

	public static void addSubHeadlineCommentInTestReport(String comment) {
		log(LogStatus.INFO, "<span style='color:green; font-weight:bold; font-size:16px;'>" + comment + "</span>");
		System.out.println("[" + LogStatus.INFO + "] [" + GenericFunctions.getCurrentTime() + "] " + comment);
	}

	public static void addWarningMessage(String comment) {
		log(LogStatus.WARNING, comment);
	}

	public static void addFailureMessage(String comment) {
		log(LogStatus.FAIL, comment);
	}

	public static void addInfoMessage(String comment) {
		log(LogStatus.INFO, comment);
	}

	public static void addSuccessMessage(String comment) {
		log(LogStatus.PASS, comment);
	}

	public static void log(LogStatus status, String message) {
		ExtentTest test = getTest();
		if (test != null) {
			test.log(status, message);
		}
		System.out.println("[" + status.name() + "] [" + GenericFunctions.getCurrentTime() + "] " + message);
	}
}

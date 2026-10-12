package utility;

import java.util.concurrent.ConcurrentHashMap;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.ExtentTestInterruptedException;

public class ExtentManager {
	private static ConcurrentHashMap<String, ExtentReports> extentMap = new ConcurrentHashMap<>();
	private static ThreadLocal<Long> startTime = new ThreadLocal<>();

	public synchronized static ExtentReports getReporter(String reportName) {
		if (!extentMap.containsKey(reportName)) {
			String workingDir = System.getProperty("user.dir");
			String reportPath;
			reportPath = workingDir + "/ExtentReports/" + reportName + ".html";

			ExtentReports extent = new ExtentReports(reportPath, true);
			extentMap.put(reportName, extent);
			startTime.set(System.currentTimeMillis()); // Start time for this suite
		}
		return extentMap.get(reportName);
	}

	public synchronized static void endReporter(String reportName) {
		ExtentReports extent = extentMap.get(reportName);
		if (extent != null) {
			long endTime = System.currentTimeMillis();

			extent.addSystemInfo("Start Time", GenericFunctions.formatTime(startTime.get()));
			extent.addSystemInfo("End Time", GenericFunctions.formatTime(endTime));
			extent.addSystemInfo("Execution Time", GenericFunctions.overallExecutionTime(startTime.get(), endTime));

			try {
				// ✅ Safely end the test if it's still active
				ExtentTest test = ExtentTestManager.getTest();
				if (test != null) {
					extent.endTest(test);
				}
			} catch (Exception e) {
				System.err.println("Warning: Failed to end test before closing extent.");
				e.printStackTrace();
			}

			extent.flush();

			try {
				extent.close(); // ✅ Now it's safe to close
			} catch (ExtentTestInterruptedException e) {
				System.err.println("Extent report closed before all tests were ended.");
				e.printStackTrace();
			}

			extentMap.remove(reportName);
			startTime.remove();
		}
	}
}

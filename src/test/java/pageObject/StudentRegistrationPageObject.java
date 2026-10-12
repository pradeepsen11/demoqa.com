package pageObject;

import groovy.util.logging.Slf4j;
import pages.StudentRegistrationPage;
import setup.BaseTest;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import utility.ExtentTestManager;
import utility.GenericFunctions;

@Slf4j
public class StudentRegistrationPageObject extends BaseTest {

	WebDriver driver = getDriver();
	StudentRegistrationPage studentRegistrationPage = new StudentRegistrationPage(driver);

	public void navigateToHomePage() {
		driver.get("https://demoqa.com/");
		ExtentTestManager.addInfoMessage("Navigate to DemoQA home page");
	}

	public boolean isFormsSectionDisplayed() {
		return GenericFunctions.isDisplayed(studentRegistrationPage.formsSection);
	}

	public boolean isPracticeFormDisplayed() {
		return GenericFunctions.isDisplayed(studentRegistrationPage.practiceForm);
	}

	public void clickFormsSection() {
		GenericFunctions.click(studentRegistrationPage.formsSection);
		ExtentTestManager.addInfoMessage("Click Forms section");
	}

	public void clickPracticeForm() {
		GenericFunctions.click(studentRegistrationPage.practiceForm);
		ExtentTestManager.addInfoMessage("Click Practice Form");
	}

	public void enterFirstName(String value) {
		GenericFunctions.clearAndSendKeys(studentRegistrationPage.firstName, value);
		ExtentTestManager.addInfoMessage("Enter first name - " + value);
	}

	public void enterLastName(String value) {
		GenericFunctions.clearAndSendKeys(studentRegistrationPage.lastName, value);
		ExtentTestManager.addInfoMessage("Enter last name - " + value);
	}

	public void enterEmail(String value) {
		GenericFunctions.clearAndSendKeys(studentRegistrationPage.email, value);
		ExtentTestManager.addInfoMessage("Enter email - " + value);
	}

	public void enterMobile(String value) {
		GenericFunctions.clearAndSendKeys(studentRegistrationPage.mobileNumber, value);
		ExtentTestManager.addInfoMessage("Enter mobile number - " + value);
	}

	public void enterAddress(String value) {
		GenericFunctions.clearAndSendKeys(studentRegistrationPage.currentAddress, value);
		ExtentTestManager.addInfoMessage("Enter current address - " + value);
	}

	public void selectGender(WebElement gender) {
		GenericFunctions.click(gender);
		ExtentTestManager.addInfoMessage("Select gender - " + getGenderName(gender));
	}

	public void selectStateAndCity(String state, String city) {
		selectState(state);
		selectCity(city);
	}

	public WebElement getFirstNameField() {
		return studentRegistrationPage.firstName;
	}

	public WebElement getLastNameField() {
		return studentRegistrationPage.lastName;
	}

	public WebElement getEmailField() {
		return studentRegistrationPage.email;
	}

	public WebElement getMobileField() {
		return studentRegistrationPage.mobileNumber;
	}

	public WebElement getAddressField() {
		return studentRegistrationPage.currentAddress;
	}

	public WebElement getDateOfBirthField() {
		return studentRegistrationPage.dateOfBirth;
	}

	public WebElement getSubmitButton() {
		return studentRegistrationPage.submitButton;
	}

	public void clearField(WebElement element) {
		GenericFunctions.clearAndSendKeys(element, "");
		ExtentTestManager.addInfoMessage("Clear field");
	}

	public String getFieldValue(WebElement element) {
		return element.getAttribute("value");
	}

	public String getValidationMessage(WebElement element) {
		return (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", element);
	}

	public boolean isFieldValid(WebElement element) {
		return (Boolean) ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", element);
	}

	public boolean isDatePickerDisplayed() {
		return GenericFunctions.isDisplayed(studentRegistrationPage.datePicker);
	}

	public void enterSubjectSearchText(String subject) {
		GenericFunctions.click(studentRegistrationPage.subjects);
		GenericFunctions.sendKeys(studentRegistrationPage.subjects, subject);
		ExtentTestManager.addInfoMessage("Search subject - " + subject);
	}

	private void enterSubjectSearchTextWithoutLogging(String subject) {
		GenericFunctions.click(studentRegistrationPage.subjects);
		GenericFunctions.sendKeys(studentRegistrationPage.subjects, subject);
	}

	public boolean isSubjectOptionDisplayed(String subject) {
		boolean displayed;
		try {
			displayed = new WebDriverWait(driver, Duration.ofSeconds(5))
					.until(ExpectedConditions.visibilityOfElementLocated(
							By.xpath("//*[contains(@id,'react-select-2-option-') and normalize-space()="
									+ xpathLiteral(subject) + "]"))) != null;
		} catch (Exception e) {
			displayed = false;
		}

		ExtentTestManager.addInfoMessage("Check subject option - " + subject + " - " + displayed);
		return displayed;
	}

	public void selectSubject(String subject) {
		enterSubjectSearchTextWithoutLogging(subject);
		WebElement option = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[contains(@id,'react-select-2-option-') and normalize-space()="
						+ xpathLiteral(subject) + "]")));
		GenericFunctions.click(option);
		ExtentTestManager.addInfoMessage("Select subject - " + subject);
	}

	public int getSelectedSubjectCount() {
		return driver.findElements(By.xpath("//div[contains(@aria-label,'Remove ')]")).size();
	}

	public int getSelectedHobbyCount() {
		int count = 0;
		if (studentRegistrationPage.sports.isSelected())
			count++;
		if (studentRegistrationPage.reading.isSelected())
			count++;
		if (studentRegistrationPage.music.isSelected())
			count++;
		return count;
	}

	public void uploadFile(String filePath) {
		GenericFunctions.browseAndUploadFile(studentRegistrationPage.uploadPicture, filePath);
		ExtentTestManager.addInfoMessage("Upload file - " + filePath);
	}

	public String getUploadedFileName() {
		String value = studentRegistrationPage.uploadPicture.getAttribute("value");
		return value == null ? "" : value.substring(value.lastIndexOf('\\') + 1);
	}

	public boolean isCityOptionDisplayed(String city) {
		boolean displayed;
		try {
			displayed = new WebDriverWait(driver, Duration.ofSeconds(5))
					.until(ExpectedConditions.visibilityOfElementLocated(
							By.xpath("//*[contains(@id,'react-select-4-option-') and normalize-space()="
									+ xpathLiteral(city) + "]"))) != null;
		} catch (Exception e) {
			displayed = false;
		}

		ExtentTestManager.addInfoMessage("Check city option - " + city + " - " + displayed);
		return displayed;
	}

	public void openCityDropdown() {
		GenericFunctions.click(studentRegistrationPage.city);
		ExtentTestManager.addInfoMessage("Open City dropdown");
	}

	public boolean isCityEnabled() {
		boolean enabled;
		try {
			WebElement cityInput = driver.findElement(By.id("react-select-4-input"));
			enabled = cityInput.isEnabled();
		} catch (Exception e) {
			String ariaDisabled = studentRegistrationPage.city.getAttribute("aria-disabled");
			enabled = !"true".equalsIgnoreCase(ariaDisabled);
		}

		ExtentTestManager.addInfoMessage("Check City dropdown is enabled - " + enabled);
		return enabled;
	}

	public void changeState(String state) {
		selectState(state);
	}

	public String getSubmittedValue(String fieldName) {
		String xpath = "//div[contains(@class,'modal-content')]//tbody/tr[td[1][normalize-space()="
				+ xpathLiteral(fieldName) + "]]/td[2]";
		return GenericFunctions.getText(new WebDriverWait(driver, Duration.ofSeconds(10))
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)))).trim();
	}

	public List<WebElement> getSubmissionRows() {
		return driver.findElements(By.cssSelector(".modal-content tbody tr"));
	}

	public void submitForm() {
		GenericFunctions.click(studentRegistrationPage.submitButton);
		ExtentTestManager.addInfoMessage("Click to submit the form");
	}

	public void registerStudent(String firstName, String lastName, String email, WebElement gender, long mobileNumber,
			String dob_in_ddmmyyyy, String[] subject, WebElement[] hobbies, String currentAddress,
			String profilePicPath, String state, String city) {

		GenericFunctions.clearAndSendKeys(studentRegistrationPage.firstName, firstName);
		ExtentTestManager.addInfoMessage("Enter first name - " + firstName);

		GenericFunctions.clearAndSendKeys(studentRegistrationPage.lastName, lastName);
		ExtentTestManager.addInfoMessage("Enter last name - " + lastName);

		GenericFunctions.clearAndSendKeys(studentRegistrationPage.email, email);
		ExtentTestManager.addInfoMessage("Enter email - " + email);

		GenericFunctions.click(gender);
		ExtentTestManager.addInfoMessage("Select gender - " + getGenderName(gender));

		GenericFunctions.clearAndSendKeys(studentRegistrationPage.mobileNumber, String.valueOf(mobileNumber));
		ExtentTestManager.addInfoMessage("Enter mobile number - " + mobileNumber);

		selectDateOfBirth(dob_in_ddmmyyyy);
		selectSubjects(subject);
		selectHobbies(hobbies);

		if (profilePicPath != null && !profilePicPath.trim().isEmpty()) {
			GenericFunctions.browseAndUploadFile(studentRegistrationPage.uploadPicture, profilePicPath);
			ExtentTestManager.addInfoMessage("Upload profile picture - " + profilePicPath);
		}

		GenericFunctions.clearAndSendKeys(studentRegistrationPage.currentAddress, currentAddress);
		ExtentTestManager.addInfoMessage("Enter current address - " + currentAddress);

		selectState(state);
		selectCity(city);

		submitForm();
	}

	public void selectDateOfBirth(String dob) {
		String[] date = dob.split("/");
		String day = date[0];
		String month = date[1];
		String year = date[2];

		GenericFunctions.click(studentRegistrationPage.dateOfBirth);

		Select monthSelect = new Select(studentRegistrationPage.datePickerMonth);
		Select yearSelect = new Select(studentRegistrationPage.datePickerYear);

		monthSelect.selectByValue(String.valueOf(Integer.parseInt(month) - 1));
		yearSelect.selectByValue(year);

		String dateXpath = "//div[contains(@class,'react-datepicker__day') "
				+ "and not(contains(@class,'outside-month')) and normalize-space()='" + Integer.parseInt(day) + "']";
		GenericFunctions.click(driver.findElement(By.xpath(dateXpath)));
		ExtentTestManager.addInfoMessage("Select dob - " + dob);
	}

	private void selectSubjects(String[] subjects) {
		if (subjects == null)
			return;
		for (String subject : subjects) {
			if (subject == null || subject.trim().isEmpty())
				continue;
			selectSubject(subject.trim());
		}
	}

	private void selectHobbies(WebElement[] hobbies) {
		if (hobbies == null)
			return;
		for (WebElement hobby : hobbies) {
			if (hobby != null && !hobby.isSelected()) {
				GenericFunctions.click(hobby);
				ExtentTestManager.addInfoMessage("Select hobby - " + getHobbyName(hobby));
			}
		}
	}

	private void selectState(String state) {
		if (state == null || state.trim().isEmpty())
			throw new IllegalArgumentException("State cannot be null or empty.");
		GenericFunctions.click(studentRegistrationPage.state);
		WebElement option = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[contains(@id,'react-select-3-option-') and normalize-space()="
						+ xpathLiteral(state.trim()) + "]")));
		GenericFunctions.click(option);
		ExtentTestManager.addInfoMessage("Select state - " + state);
	}

	public void selectCity(String city) {
		if (city == null || city.trim().isEmpty())
			throw new IllegalArgumentException("City cannot be null or empty.");
		GenericFunctions.click(studentRegistrationPage.city);
		WebElement option = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions
				.elementToBeClickable(By.xpath("//*[contains(@id,'react-select-4-option-') and normalize-space()="
						+ xpathLiteral(city.trim()) + "]")));
		GenericFunctions.click(option);
		ExtentTestManager.addInfoMessage("Select city - " + city);
	}

	private String getGenderName(WebElement gender) {
		String id = gender.getAttribute("id");
		if ("gender-radio-1".equals(id))
			return "Male";
		if ("gender-radio-2".equals(id))
			return "Female";
		if ("gender-radio-3".equals(id))
			return "Other";
		return "Unknown";
	}

	private String getHobbyName(WebElement hobby) {
		String id = hobby.getAttribute("id");
		if ("hobbies-checkbox-1".equals(id))
			return "Sports";
		if ("hobbies-checkbox-2".equals(id))
			return "Reading";
		if ("hobbies-checkbox-3".equals(id))
			return "Music";
		return "Unknown";
	}

	private String xpathLiteral(String value) {
		if (!value.contains("'"))
			return "'" + value + "'";
		if (!value.contains("\""))
			return "\"" + value + "\"";
		String[] parts = value.split("'");
		StringBuilder result = new StringBuilder("concat('");
		for (int i = 0; i < parts.length; i++) {
			if (i > 0)
				result.append("', \"'\", '");
			result.append(parts[i]);
		}
		result.append("')");
		return result.toString();
	}

	public WebElement getMaleGender() {
		return studentRegistrationPage.maleGender;
	}

	public WebElement getFemaleGender() {
		return studentRegistrationPage.femaleGender;
	}

	public WebElement getOtherGender() {
		return studentRegistrationPage.otherGender;
	}

	public WebElement getSportsHobby() {
		return studentRegistrationPage.sports;
	}

	public WebElement getReadingHobby() {
		return studentRegistrationPage.reading;
	}

	public WebElement getMusicHobby() {
		return studentRegistrationPage.music;
	}

	public boolean isSubmissionConfirmationDisplayed() {
		return GenericFunctions.isDisplayed(studentRegistrationPage.submissionConfirmation);
	}

	public String getSubmissionConfirmationText() {
		return GenericFunctions.getText(studentRegistrationPage.submissionConfirmation);
	}

	public void closeSubmissionConfirmation() {
		GenericFunctions.click(studentRegistrationPage.closeButton);
		ExtentTestManager.addInfoMessage("Close submission confirmation popup");
	}
}

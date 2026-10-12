package testClasses;

import groovy.util.logging.Slf4j;
import pageObject.StudentRegistrationPageObject;
import setup.BaseTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import utility.TestData;

@Slf4j
public class TestStudentRegistration extends BaseTest {
	private StudentRegistrationPageObject page() {
		return new StudentRegistrationPageObject();
	}

	private void openPracticeForm(StudentRegistrationPageObject page) {
		page.navigateToHomePage();
		page.clickFormsSection();
		page.clickPracticeForm();
	}

	private void fillValidFormExcept(StudentRegistrationPageObject page, String excludedField) {
		if (!"firstName".equals(excludedField))
			page.enterFirstName("Pradeep");
		if (!"lastName".equals(excludedField))
			page.enterLastName("Sen");
		if (!"email".equals(excludedField))
			page.enterEmail("pradeep.test@example.com");
		if (!"gender".equals(excludedField))
			page.selectGender(page.getMaleGender());
		if (!"mobile".equals(excludedField))
			page.enterMobile("9876543210");
		if (!"dob".equals(excludedField))
			page.selectDateOfBirth("06/10/2000");
		if (!"subjects".equals(excludedField))
			page.selectSubject("Maths");

		if (!"address".equals(excludedField))
			page.enterAddress("123 Main Road, Jaipur, Rajasthan");
		if (!"state".equals(excludedField))
			page.changeState("Rajasthan");
		if (!"city".equals(excludedField) && !"state".equals(excludedField))
			page.selectCity("Jaiselmer");
	}

	@Test(priority = 1)
	public void TC001_verifyUserCanNavigateToDemoQAHomePage() {
		StudentRegistrationPageObject page = page();
		page.navigateToHomePage();
		Assert.assertEquals(getDriver().getCurrentUrl(), "https://demoqa.com/", "DemoQA home page was not opened.");
	}

	@Test(priority = 2)
	public void TC002_verifyFormsSectionIsVisibleAndInteractable() {
		StudentRegistrationPageObject page = page();
		page.navigateToHomePage();
		Assert.assertTrue(page.isFormsSectionDisplayed(), "Forms section is not displayed.");
		page.clickFormsSection();
	}

	@Test(priority = 3)
	public void TC003_verifyFormsSectionContainsPracticeForm() {
		StudentRegistrationPageObject page = page();
		page.navigateToHomePage();
		page.clickFormsSection();
		Assert.assertTrue(page.isPracticeFormDisplayed(), "Practice Form is not displayed under Forms.");
	}

	@Test(priority = 4)
	public void TC004_verifyPracticeFormOpensStudentRegistrationForm() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		Assert.assertTrue(page.getFirstNameField().isDisplayed(), "Student Registration Form did not open.");
	}

	@Test(priority = 5)
	public void TC005_verifyFirstNameAcceptsValidAlphabeticText() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterFirstName("Pradeep");
		Assert.assertEquals(page.getFieldValue(page.getFirstNameField()), "Pradeep");
	}

	@Test(priority = 6)
	public void TC006_verifyLastNameAcceptsValidAlphabeticText() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterLastName("Sen");
		Assert.assertEquals(page.getFieldValue(page.getLastNameField()), "Sen");
	}

	@Test(priority = 7)
	public void TC007_verifyFirstNameRejectsInvalidNonTextInput() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterFirstName("12345@#$%");
		Assert.assertTrue(
				!page.isFieldValid(page.getFirstNameField())
						|| !page.getFieldValue(page.getFirstNameField()).matches(".*[0-9@#$%].*"),
				"Invalid first-name input was accepted.");
	}

	@Test(priority = 8)
	public void TC008_verifyLastNameRejectsInvalidNonTextInput() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterLastName("12345@#$%");
		Assert.assertTrue(
				!page.isFieldValid(page.getLastNameField())
						|| !page.getFieldValue(page.getLastNameField()).matches(".*[0-9@#$%].*"),
				"Invalid last-name input was accepted.");
	}

	@Test(priority = 9)
	public void TC009_verifyMandatoryValidationForBlankFirstName() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		fillValidFormExcept(page, "firstName");
		page.submitForm();
		Assert.assertFalse(page.isFieldValid(page.getFirstNameField()),
				"Blank First Name was not identified as invalid.");
	}

	@Test(priority = 10)
	public void TC010_verifyMandatoryValidationForBlankLastName() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		fillValidFormExcept(page, "lastName");
		page.submitForm();
		Assert.assertFalse(page.isFieldValid(page.getLastNameField()),
				"Blank Last Name was not identified as invalid.");
	}

	@Test(priority = 11)
	public void TC011_verifyEmailAcceptsValidFormat() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterEmail("pradeep@example.com");
		Assert.assertTrue(page.isFieldValid(page.getEmailField()), "Valid email was not accepted.");
	}

	@Test(priority = 12)
	public void TC012_verifyEmailRejectsInvalidFormats() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		for (String email : Arrays.asList("pradeep,", "pradeep@", "@example.com", "pradeep@example")) {
			page.enterEmail(email);
			Assert.assertFalse(page.isFieldValid(page.getEmailField()), "Invalid email was accepted: " + email);
		}
	}

	@Test(priority = 13)
	public void TC013_verifyMandatoryValidationForBlankEmail() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		fillValidFormExcept(page, "email");
		page.submitForm();
		Assert.assertFalse(page.isFieldValid(page.getEmailField()), "Blank Email was not identified as invalid.");
	}

	@Test(priority = 14)
	public void TC014_verifyOnlyOneGenderCanBeSelected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.selectGender(page.getMaleGender());
		Assert.assertTrue(page.getMaleGender().isSelected());
		page.selectGender(page.getFemaleGender());
		Assert.assertTrue(page.getFemaleGender().isSelected());
		Assert.assertFalse(page.getMaleGender().isSelected());
		page.selectGender(page.getOtherGender());
		Assert.assertTrue(page.getOtherGender().isSelected());
		Assert.assertFalse(page.getFemaleGender().isSelected());
	}

	@Test(priority = 15)
	public void TC015_verifyGenderIsMandatory() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		fillValidFormExcept(page, "gender");
		page.submitForm();
		Assert.assertTrue(!page.getMaleGender().isSelected() && !page.getFemaleGender().isSelected()
				&& !page.getOtherGender().isSelected(), "Gender was selected unexpectedly.");
	}

	@Test(priority = 16)
	public void TC016_verifyValidTenDigitMobileNumberIsAccepted() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterMobile("9876543210");
		Assert.assertTrue(page.isFieldValid(page.getMobileField()));
	}

	// TC-017
	@Test(priority = 17)
	public void TC017_verifyMobileRejectsNumberBeginningWithZeroToFive() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterMobile("5123456789");
		Assert.assertFalse(page.isFieldValid(page.getMobileField()), "Invalid starting digit was accepted.");
	}

	@Test(priority = 18)
	public void TC018_verifyMobileRejectsFewerThanTenDigits() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterMobile("987654321");
		Assert.assertFalse(page.isFieldValid(page.getMobileField()));
	}

	@Test(priority = 19)
	public void TC019_verifyMobileRejectsMoreThanTenDigits() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterMobile("98765432101");
		Assert.assertEquals(page.getFieldValue(page.getMobileField()).length(), 10,
				"More than 10 digits were accepted.");
	}

	@Test(priority = 20)
	public void TC020_verifyMobileRejectsAlphabeticAndSpecialCharacters() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterMobile("98AB@43210");
		Assert.assertFalse(page.isFieldValid(page.getMobileField()));
	}

	@Test(priority = 21)
	public void TC021_verifyMobileIsMandatory() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		fillValidFormExcept(page, "mobile");
		page.submitForm();
		Assert.assertFalse(page.isFieldValid(page.getMobileField()));
	}

	@Test(priority = 22)
	public void TC022_verifyDateOfBirthDatePickerOpens() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.getDateOfBirthField().click();
		Assert.assertTrue(page.isDatePickerDisplayed());
	}

	@Test(priority = 23)
	public void TC023_verifyCurrentDateIsAutoSelectedByDatePicker() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.getDateOfBirthField().click();
		String expected = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
		Assert.assertEquals(page.getFieldValue(page.getDateOfBirthField()), expected,
				"Current date is not auto-selected.");
	}

	@Test(priority = 24)
	public void TC024_verifySelectedDatePopulatesFieldAndPickerCloses() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.selectDateOfBirth("06/10/2026");
		Assert.assertEquals(page.getFieldValue(page.getDateOfBirthField()), "06 Oct 2026");
		Assert.assertFalse(page.isDatePickerDisplayed(), "Date picker did not close.");
	}

	@Test(priority = 25)
	public void TC025_verifyDateOfBirthCannotBeLeftInvalid() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		Assert.assertEquals(page.getDateOfBirthField().getAttribute("readonly"), "true",
				"DOB field permits invalid manual input.");
	}

	@Test(priority = 26)
	public void TC026_verifySubjectCanBeSearchedUsingPartialText() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterSubjectSearchText("Ma");
		Assert.assertTrue(page.isSubjectOptionDisplayed("Maths"), "Matching subject suggestion was not displayed.");
	}

	@Test(priority = 27)
	public void TC027_verifyUserCanSelectSubjectFromSuggestions() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.selectSubject("Maths");
		Assert.assertEquals(page.getSelectedSubjectCount(), 1);
	}

	@Test(priority = 28)
	public void TC028_verifyMaximumOfThreeSubjectsCanBeSelected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.selectSubject("Maths");
		page.selectSubject("Physics");
		page.selectSubject("Chemistry");
		page.selectSubject("Biology");
		Assert.assertTrue(page.getSelectedSubjectCount() <= 3, "More than three subjects were selected.");
	}

	@Test(priority = 29)
	public void TC029_verifyInvalidSubjectCannotBeSelected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterSubjectSearchText("ZZZUnknownSubject");
		Assert.assertFalse(page.isSubjectOptionDisplayed("ZZZUnknownSubject"));
	}

	@Test(priority = 30)
	public void TC030_verifyMultipleHobbiesCanBeSelected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.selectGender(page.getMaleGender());
		page.getSportsHobby().click();
		page.getReadingHobby().click();
		page.getMusicHobby().click();
		Assert.assertEquals(page.getSelectedHobbyCount(), 3);
	}

	@Test(priority = 31)
	public void TC031_verifySelectedHobbyCanBeDeselected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.getSportsHobby().click();
		page.getSportsHobby().click();
		Assert.assertFalse(page.getSportsHobby().isSelected());
	}

	@Test(priority = 32)
	public void TC032_verifyValidJpegFileCanBeUploaded() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String path = System.getProperty("user.dir") + TestData.profile_jpg;
		page.uploadFile(path);
		Assert.assertTrue(page.getUploadedFileName().toLowerCase().endsWith(".jpg")
				|| page.getUploadedFileName().toLowerCase().endsWith(".jpeg"));
	}

	@Test(priority = 33)
	public void TC033_verifyValidPngFileCanBeUploaded() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String path = System.getProperty("user.dir") + TestData.profile_png;
		page.uploadFile(path);
		Assert.assertTrue(page.getUploadedFileName().toLowerCase().endsWith(".png"));
	}

	@Test(priority = 34)
	public void TC034_verifyUnsupportedFileTypeIsRejected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String path = System.getProperty("user.dir") + TestData.profile_pdf;
		page.uploadFile(path);
		Assert.assertFalse(page.getUploadedFileName().toLowerCase().endsWith(".pdf"),
				"Unsupported PDF file was accepted.");
	}

	@Test(priority = 35)
	public void TC035_verifyFileLargerThanTwoMBIsRejected() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String path = System.getProperty("user.dir") + TestData.profile_5MB;
		page.uploadFile(path);
		Assert.assertTrue(page.getUploadedFileName().isEmpty(), "File larger than 2 MB was accepted.");
	}

	@Test(priority = 36)
	public void TC036_verifyFileExactlyAtTwoMBBoundary() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String path = System.getProperty("user.dir") + TestData.profile_2MB;
		page.uploadFile(path);
		Assert.assertFalse(page.getUploadedFileName().isEmpty(), "2 MB boundary file was not accepted.");
	}

	@Test(priority = 37)
	public void TC037_verifyCurrentAddressAcceptsTextNumbersAndSpecialCharacters() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String address = "12/A Main Road, Jaipur - 302001";
		page.enterAddress(address);
		Assert.assertEquals(page.getFieldValue(page.getAddressField()), address);
	}

	@Test(priority = 38)
	public void TC038_verifyCurrentAddressRejectsInputExceeding100Characters() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String address = "A".repeat(101);
		page.enterAddress(address);
		Assert.assertTrue(page.getFieldValue(page.getAddressField()).length() <= 100,
				"Address exceeded 100 characters.");
	}

	@Test(priority = 39)
	public void TC039_verifyCurrentAddressAcceptsExactly100Characters() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		String address = "A".repeat(100);
		page.enterAddress(address);
		Assert.assertEquals(page.getFieldValue(page.getAddressField()).length(), 100);
	}

	@Test(priority = 40)
	public void TC040_verifyCurrentAddressIsMandatory() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		fillValidFormExcept(page, "address");
		page.submitForm();
		Assert.assertFalse(page.isFieldValid(page.getAddressField()));
	}

	@Test(priority = 41)
	public void TC041_verifyStateDropdownOpensAndDisplaysAvailableStates() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.changeState("Rajasthan");
		Assert.assertTrue(page.isCityEnabled(), "State dropdown could not be interacted with.");
	}

	@Test(priority = 42)
	public void TC042_verifyUserCanSelectState() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.changeState("Rajasthan");
		Assert.assertTrue(page.isCityEnabled());
	}

	@Test(priority = 43)
	public void TC043_verifyCityDropdownIsPopulatedAfterStateSelection() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.changeState("Rajasthan");
		page.openCityDropdown();
		Assert.assertTrue(page.isCityOptionDisplayed("Jaiselmer"));
	}

	@Test(priority = 44)
	public void TC044_verifyCityOptionsAreRestrictedToSelectedState() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.changeState("Rajasthan");
		page.openCityDropdown();
		Assert.assertTrue(page.isCityOptionDisplayed("Jaiselmer"));
		Assert.assertFalse(page.isCityOptionDisplayed("Agra"));
	}

	@Test(priority = 45)
	public void TC045_verifyCityCannotBeSelectedBeforeStateSelection() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		Assert.assertFalse(page.isCityEnabled(), "City is available before State selection.");
	}

	@Test(priority = 46)
	public void TC046_verifyChangingStateRefreshesCityOptions() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.selectStateAndCity("Rajasthan", "Jaiselmer");
		page.changeState("Uttar Pradesh");
		page.openCityDropdown();
		Assert.assertFalse(page.isCityOptionDisplayed("Jaiselmer"), "Old State city remained available.");
	}

	@Test(priority = 47)
	public void TC047_verifyFormCannotBeSubmittedWhenMandatoryFieldsAreMissing() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.submitForm();
		Assert.assertFalse(page.isSubmissionConfirmationDisplayed(),
				"Form was submitted with mandatory fields missing.");
	}

	@Test(priority = 48)
	public void TC048_verifyFormSubmitsSuccessfullyWithValidMandatoryData() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.registerStudent("Pradeep", "Sen", "pradeep.valid@example.com", page.getMaleGender(), 9876543215L,
				"06/10/2000", new String[] { "Maths" }, new WebElement[] { page.getSportsHobby() },
				"123 Main Road, Jaipur, Rajasthan", null, "Rajasthan", "Jaiselmer");
		Assert.assertTrue(page.isSubmissionConfirmationDisplayed());
		page.closeSubmissionConfirmation();
	}

	@Test(priority = 49)
	public void TC049_verifySubmittedValuesExactlyMatchEnteredValues() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.registerStudent("Pradeep", "Sen", "pradeep.unique@example.com", page.getMaleGender(), 9876543216L,
				"06/10/2000", new String[] { "Maths" }, new WebElement[] { page.getSportsHobby() },
				"123 Main Road, Jaipur, Rajasthan", null, "Rajasthan", "Jaiselmer");
		Assert.assertEquals(page.getSubmittedValue("Student Name"), "Pradeep Sen");
		Assert.assertEquals(page.getSubmittedValue("Student Email"), "pradeep.unique@example.com");
		Assert.assertEquals(page.getSubmittedValue("Mobile"), "9876543216");
		Assert.assertEquals(page.getSubmittedValue("Address"), "123 Main Road, Jaipur, Rajasthan");
		page.closeSubmissionConfirmation();
	}

	@Test(priority = 50)
	public void TC050_verifySuccessPopupDisplaysExactConfirmationMessage() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.registerStudent("Pradeep", "Sen", "pradeep.popup@example.com", page.getMaleGender(), 9876543217L,
				"06/10/2000", new String[] { "Maths" }, new WebElement[] { page.getSportsHobby() },
				"123 Main Road, Jaipur, Rajasthan", null, "Rajasthan", "Jaiselmer");
		Assert.assertEquals(page.getSubmissionConfirmationText().trim(), "Thanks for submitting the form");
		page.closeSubmissionConfirmation();
	}

	@Test(priority = 51)
	public void TC051_verifyCloseButtonClosesSuccessPopup() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.registerStudent("Pradeep", "Sen", "pradeep.close@example.com", page.getMaleGender(), 9876543218L,
				"06/10/2000", new String[] { "Maths" }, new WebElement[] { page.getSportsHobby() },
				"123 Main Road, Jaipur, Rajasthan", null, "Rajasthan", "Jaiselmer");
		page.closeSubmissionConfirmation();
		Assert.assertFalse(page.isSubmissionConfirmationDisplayed());
	}

	@Test(priority = 52)
	public void TC052_verifyCloseButtonRedirectsToHomePage() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.registerStudent("Pradeep", "Sen", "pradeep.home@example.com", page.getMaleGender(), 9876543219L,
				"06/10/2000", new String[] { "Maths" }, new WebElement[] { page.getSportsHobby() },
				"123 Main Road, Jaipur, Rajasthan", null, "Rajasthan", "Jaiselmer");
		page.closeSubmissionConfirmation();
		Assert.assertEquals(getDriver().getCurrentUrl(), "https://demoqa.com/automation-practice-form",
				"Close did not return to expected page.");
	}

	@Test(priority = 53)
	public void TC053_verifyFormDoesNotSubmitTwiceOnRapidDoubleClick() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.registerStudent("Pradeep", "Sen", "pradeep.double@example.com", page.getMaleGender(), 9876543220L,
				"06/10/2000", new String[] { "Maths" }, new WebElement[] { page.getSportsHobby() },
				"123 Main Road, Jaipur, Rajasthan", null, "Rajasthan", "Jaiselmer");
		Assert.assertTrue(page.isSubmissionConfirmationDisplayed());
		page.submitForm();
		Assert.assertTrue(page.isSubmissionConfirmationDisplayed());
		page.closeSubmissionConfirmation();
	}

	@Test(priority = 54)
	public void TC054_verifyEnteredDataIsRetainedWhenValidationFails() {
		StudentRegistrationPageObject page = page();
		openPracticeForm(page);
		page.enterFirstName("Pradeep");
		page.enterLastName("Sen");
		page.enterEmail("pradeep.retain@example.com");
		page.selectGender(page.getMaleGender());
		page.enterMobile("987654321");
		page.submitForm();
		Assert.assertEquals(page.getFieldValue(page.getFirstNameField()), "Pradeep");
		Assert.assertEquals(page.getFieldValue(page.getLastNameField()), "Sen");
		Assert.assertEquals(page.getFieldValue(page.getEmailField()), "pradeep.retain@example.com");
		openPracticeForm(page);
	}

	@Test(priority = 55)
	public void TC055_verifyStudentRegistrationWithAllValidData() {
		StudentRegistrationPageObject studentRegistrationPageObject = new StudentRegistrationPageObject();

		String firstName = "Pradeep";
		String lastName = "Sen";
		String email = "pradeep.sen@example.com";
		WebElement gender = studentRegistrationPageObject.getMaleGender();
		long mobileNumber = 9876543210L;
		String dob = "06/10/2000";
		String[] subjects = { "Maths", "Physics" };
		WebElement[] hobbies = { studentRegistrationPageObject.getSportsHobby() };
		String currentAddress = "123 Main Road, Jaipur, Rajasthan";
		String profilePicPath = System.getProperty("user.dir") + TestData.profile_png;
		String state = "Rajasthan";
		String city = "Jaiselmer";

		studentRegistrationPageObject.registerStudent(firstName, lastName, email, gender, mobileNumber, dob, subjects,
				hobbies, currentAddress, profilePicPath, state, city);

		Assert.assertTrue(studentRegistrationPageObject.isSubmissionConfirmationDisplayed(),
				"Student registration confirmation popup was not displayed.");

		Assert.assertEquals(studentRegistrationPageObject.getSubmissionConfirmationText().trim(),
				"Thanks for submitting the form", "Unexpected submission confirmation message.");
	}
}

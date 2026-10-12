package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import setup.BaseTest;

public class StudentRegistrationPage extends BaseTest {
	WebDriver driver;

	public StudentRegistrationPage(WebDriver driver) {
		super();
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "firstName")
	public WebElement firstName;

	@FindBy(id = "lastName")
	public WebElement lastName;

	@FindBy(id = "userEmail")
	public WebElement email;

	@FindBy(id = "gender-radio-1")
	public WebElement maleGender;

	@FindBy(id = "gender-radio-2")
	public WebElement femaleGender;

	@FindBy(id = "gender-radio-3")
	public WebElement otherGender;

	@FindBy(id = "userNumber")
	public WebElement mobileNumber;

	@FindBy(id = "dateOfBirthInput")
	public WebElement dateOfBirth;

	@FindBy(css = ".react-datepicker")
	public WebElement datePicker;

	@FindBy(css = "select.react-datepicker__month-select")
	public WebElement datePickerMonth;

	@FindBy(css = "select.react-datepicker__year-select")
	public WebElement datePickerYear;

	@FindBy(id = "subjectsInput")
	public WebElement subjects;

	@FindBy(id = "hobbies-checkbox-1")
	public WebElement sports;

	@FindBy(id = "hobbies-checkbox-2")
	public WebElement reading;

	@FindBy(id = "hobbies-checkbox-3")
	public WebElement music;

	@FindBy(id = "uploadPicture")
	public WebElement uploadPicture;

	@FindBy(id = "currentAddress")
	public WebElement currentAddress;

	@FindBy(css = "#state .css-13cymwt-control")
	public WebElement state;

	@FindBy(css = "#city .css-13cymwt-control")
	public WebElement city;

	@FindBy(id = "submit")
	public WebElement submitButton;

	@FindBy(xpath = "//*[normalize-space()='Thanks for submitting the form']")
	public WebElement submissionConfirmation;

	@FindBy(xpath = "//button[normalize-space()='Close']")
	public WebElement closeButton;

	@FindBy(xpath = "//*[normalize-space()='Forms']")
	public WebElement formsSection;

	@FindBy(xpath = "//span[normalize-space()='Practice Form']")
	public WebElement practiceForm;

	@FindBy(css = ".modal-content")
	public WebElement submissionModal;
}

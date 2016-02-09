*** Settings ***
Documentation     INFUND-43 As an applicant and I am on the application form on an open application, I will receive feedback if I my input is invalid, so I know how I should enter the question
Suite Setup       Login as User    &{lead_applicant_credentials}
Suite Teardown    TestTeardown User closes the browser
Resource          ../../../resources/GLOBAL_LIBRARIES.robot
Resource          ../../../resources/variables/GLOBAL_VARIABLES.robot
Resource          ../../../resources/variables/User_credentials.robot
Resource          ../../../resources/keywords/Login_actions.robot
Resource          ../../../resources/keywords/Applicant_actions.robot

*** Test Cases ***
Verify the validation error when the Project title field is empty
    [Documentation]    -INFUND-43
    [Tags]    Applicant    Validations
    Given Applicant goes to the 'application details' question
    When the applicant clears the application title field
    Then The applicant should get a validation error message    Please enter the full title of the project.

Verify the validation error for an invalid date (Year)
    [Documentation]    -INFUND-43
    [Tags]    Applicant    Validations
    Given Applicant goes to the 'application details' question
    And the applicant inserts an invalid date "18-11-2015"
    Then the applicant should get a validation error message    Please enter a future date
    And the Year field is empty
    Then the applicant should get a validation error message    This field should be a number
    And the applicant inserts "2016" in the Year field(valid date)
    And the applicant should not see the validation error any more

Verify the validation error for an invalid date (day)
    [Documentation]    -INFUND-43
    [Tags]    Applicant    Validations
    Given Applicant goes to the 'application details' question
    And the applicant inserts "32" in the day field
    And the applicant should get a validation error message    Please enter a valid date
    When the applicant inserts "0" in the day field
    And the applicant should get a validation error message    Please enter a valid date
    And the applicant inserts "-1" in the day field
    Then the applicant should get a validation error message    Please enter a valid date
    And the day field is empty
    Then the applicant should get a validation error message    This field should be a number
    And the applicant inserts 01 in the day
    And the applicant should not see the validation error any more

Verify the validation error for an invalid date (month)
    [Documentation]    -INFUND-43
    [Tags]    Applicant    Validations
    Given Applicant goes to the 'application details' question
    And the applicant inserts "0" in the month field
    And the applicant should get a validation error message    Please enter a valid date
    When the applicant inserts "13" in the month field
    And the applicant should get a validation error message    Please enter a valid date
    And the applicant inserts "-1" in the month field
    Then the applicant should get a validation error message    Please enter a valid date
    And the month field is empty
    And the applicant should get a validation error message    This field should be a number
    And the applicant inserts "01" in the month field
    And the applicant should not see the validation error any more

Verify the validation error for the duration field
    [Documentation]    -INFUND-43
    [Tags]    Applicant    Validations
    Given Applicant goes to the 'application details' question
    And the applicant inserts "0" in the duration field
    And the applicant should get a validation error message    Please enter a valid duration
    When the applicant inserts "-1" in the duration field
    And the applicant should get a validation error message    Please enter a valid duration
    And the applicant leaves the duration field empty
    Then the applicant should get a validation error message    This field should be a number
    And the Applicant inserts 01 in the duration field
    And the applicant should not see the validation error any more

Verify the validation error when the text area is empty
    [Documentation]    -INFUND-43
    [Tags]    Applicant    Validations
    Given Applicant goes to the 'project summary' question
    When the applicant clears the text area of the "Project Summary"
    Then the applicant should get a validation error message    Please enter some text

*** Keywords ***
the applicant inserts "32" in the day field
    Clear Element Text    id=application_details-startdate_day
    Input Text    id=application_details-startdate_day    32

the applicant inserts "0" in the day field
    Clear Element Text    id=application_details-startdate_day
    Input Text    id=application_details-startdate_day    0

the applicant inserts "0" in the month field
    Clear Element Text    id=application_details-startdate_month
    Input Text    id=application_details-startdate_month    0

the day field is empty
    Clear Element Text    id=application_details-startdate_day

the applicant inserts 01 in the day
    Clear Element Text    id=application_details-startdate_day
    Input Text    id=application_details-startdate_day    01

the applicant should not see the validation error any more
    sleep    1s
    Focus    css=.app-submit-btn
    Wait Until Element Is Not Visible    css=.error-message

the applicant inserts "13" in the month field
    Clear Element Text    id=application_details-startdate_month
    Input Text    id=application_details-startdate_month    13

the month field is empty
    Clear Element Text    id=application_details-startdate_month

the applicant inserts "01" in the month field
    Clear Element Text    id=application_details-startdate_month
    Input Text    id=application_details-startdate_month    09

the applicant inserts an invalid date "18-11-2015"
    Clear Element Text    id=application_details-startdate_day
    Input Text    id=application_details-startdate_day    18
    Clear Element Text    id=application_details-startdate_year
    Input Text    id=application_details-startdate_year    2015
    Clear Element Text    id=application_details-startdate_month
    Input Text    id=application_details-startdate_month    11

the applicant inserts "2016" in the Year field(valid date)
    Clear Element Text    id=application_details-startdate_year
    Input Text    id=application_details-startdate_year    2016

the Year field is empty
    Clear Element Text    id=application_details-startdate_year

the applicant inserts "-1" in the day field
    Clear Element Text    id=application_details-startdate_day
    Input Text    id=application_details-startdate_day    -1

the applicant inserts "-1" in the month field
    Clear Element Text    id=application_details-startdate_month
    Input Text    id=application_details-startdate_month    -1

the applicant clears the text area of the "Project Summary"
    #Question should be editable    css=#form-input-11 .buttonlink[name="mark_as_incomplete"]
    Clear Element Text    css=#form-input-11 .editor
    Press Key    css=#form-input-11 .editor    \\8
    Focus    css=.app-submit-btn
    Comment    Click Element    css=.fa-bold
    Sleep    2s

the applicant should get a validation error
    Focus    css=.app-submit-btn
    Wait Until Element Is Visible    css=#form-input-11 .error-message
the applicant inserts "0" in the duration field
    Clear Element Text    id=application_details-duration
    Input Text    id=application_details-duration    0

the applicant inserts "-1" in the duration field
    Clear Element Text    id=application_details-duration
    Input Text    id=application_details-duration    -1

the applicant leaves the duration field empty
    Clear Element Text    id=application_details-duration

the Applicant inserts 01 in the duration field
    Clear Element Text    id=application_details-duration
    Input Text    id=application_details-duration    1
    focus    jQuery=button:contains("Save and")

the applicant clears the application title field
    Clear Element Text    id=application_details-title

The applicant should get a validation error message
    [Arguments]    ${validation error}
    focus    jQuery=button:contains("Save and")
    Wait Until Page Contains    ${validation_error}
    Element Should Be Visible    css=.error-message

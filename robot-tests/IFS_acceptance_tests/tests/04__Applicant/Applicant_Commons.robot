*** Settings ***
Resource    ../../resources/defaultResources.robot

*** Variables ***
${project_guidance}    https://www.gov.uk/government/publications/innovate-uk-completing-your-application-project-costs-guidance

*** Keywords ***
the user should see all the Your-Finances Sections
    the user should see the element  link=Your project costs
    the user should see the element  link=Your organisation
    the user should see the element  jQuery=h3:contains("Your funding")
    the user should see the element  jQuery=h2:contains("Finance summary")

the user navigates to Your-finances page
    [Arguments]  ${Application}
    the user navigates to the page  ${DASHBOARD_URL}
    the user clicks the button/link  jQuery=.in-progress a:contains("${Application}")
    the user clicks the button/link  link=Your finances

Applicant navigates to the finances of the Robot application
    the user navigates to Your-finances page  Robot test application

log in and create new application if there is not one already with complete application details
    log in and create new application if there is not one already  Robot test application
    Mark application details as complete

log in and create a new application if there is not one already with complete application details and completed org size section
    log in and create new application if there is not one already  Robot test application
    Mark application details as complete
    Complete the org size section

Complete the org size section
    the user navigates to the page    ${DASHBOARD_URL}
    the user clicks the button/link    link=Robot test application
    the user clicks the button/link    link=Your finances
    the user clicks the button/link    link=Your organisation
    ${orgSizeReadonly}=  Run Keyword And Return Status    Element Should Be Visible   jQuery=button:contains("Edit")
    Run Keyword If    ${orgSizeReadonly}    the user clicks the button/link    jQuery=button:contains("Edit")
    the user selects the radio button    financePosition-organisationSize  ${LARGE_ORGANISATION_SIZE}
    the user enters text to a text field    jQuery=label:contains("Turnover") + input    150
    the user enters text to a text field    jQuery=label:contains("employees") + input    0
    the user moves focus to the element    jQuery=button:contains("Mark as complete")
    run keyword and ignore error without screenshots    the user clicks the button/link    jQuery=button:contains("Mark as complete")
    run keyword and ignore error without screenshots    the user clicks the button/link    link=Your finances

mark application details incomplete the user closes the browser
    Mark application details as incomplete
    the user closes the browser

Mark application details as complete
    Given the user navigates to the page  ${DASHBOARD_URL}
    And the user clicks the button/link   link=Robot test application
    the applicant completes the application details     Application details

Mark application details as incomplete
    Given the user navigates to the page  ${DASHBOARD_URL}
    And the user clicks the button/link   link=Robot test application
    the user clicks the button/link       link=Application details
    the user clicks the button/link       jQuery=button:contains("Edit")
    the user clicks the button/link       jQuery=button:contains("Save and return to application overview")
    the user should see the element       jQuery=li:contains("Application details") > .action-required

the Application details are completed
    ${STATUS}    ${VALUE}=  Run Keyword And Ignore Error Without Screenshots  page should contain element  css=img.complete[alt*="Application details"]
    Run Keyword If  '${status}' == 'FAIL'  the applicant completes the application details  Application details

the applicant completes the application details
    [Arguments]   ${Application_details}
    the user clicks the button/link       link=${Application_details}
    the user moves Application details in Edit mode
    the user clicks the button/link       jQuery=button:contains("research category")
    the user clicks the button twice      jQuery=label[for^="researchCategoryChoice"]:contains("Experimental development")
    the user clicks the button/link       jQuery=button:contains("Save")
    the user clicks the button twice      css=label[for="application.resubmission-no"]
    # those Radio buttons need to be clicked twice.
    The user enters text to a text field  id=application_details-startdate_day  18
    The user enters text to a text field  id=application_details-startdate_year  2018
    The user enters text to a text field  id=application_details-startdate_month  11
    The user enters text to a text field  css=[id="application.durationInMonths"]  20
    the user clicks the button/link       jQuery=button:contains("Mark as complete")
    the user should see the element       jQuery=button:contains("Edit")
    the user should not see the element   css=input

the user moves Application details in Edit mode
     ${status}  ${value} =  Run Keyword And Ignore Error Without Screenshots  page should contain element  css=.buttonlink[name="mark_as_incomplete"]
     Run Keyword If  '${status}' == 'PASS'  the user clicks the button/link  css=.buttonlink[name="mark_as_incomplete"]

the user fills in the Application details
    [Arguments]  ${appTitle}  ${res_category}  ${tomorrowday}  ${month}  ${nextyear}
    the user should see the element       jQuery=h1:contains("Application details")
    the user enters text to a text field  css=[id="application.name"]  ${appTitle}
    the user enters text to a text field  css=#application_details-startdate_day  ${tomorrowday}
    the user enters text to a text field  css=#application_details-startdate_month  ${month}
    the user enters text to a text field  css=#application_details-startdate_year  ${nextyear}
    the user enters text to a text field  css=[id="application.durationInMonths]  24
    the user clicks the button twice      css=label[for="application.resubmission-no"]
    the user selects Research category    ${res_category}
    the user should not see the element   link=Choose your innovation area
    The user clicks the button/link       css=button[name="mark_as_complete"]
    the user clicks the button/link       link=Application overview
    the user should see the element       jQuery=li:contains("Application details") > .task-status-complete

the user selects Research category
    [Arguments]  ${res_category}
    the user clicks the button/link   jQuery=label:contains("Research category")
    the user clicks the button twice  jQuery=label[for^="researchCategoryChoice"]:contains("${res_category}")
    the user clicks the button/link   jQuery=button:contains("Save")

the user marks the finances as complete
    [Arguments]  ${Application}  ${overheadsCost}  ${totalCosts}
    the user fills in the project costs  ${overheadsCost}  ${totalCosts}
    the user fills in the organisation information  ${Application}  ${SMALL_ORGANISATION_SIZE}
    the user checks Your Funding section        ${Application}
    the user should see all finance subsections complete
    the user clicks the button/link  link=Application overview
    the user should see the element  jQuery=li:contains("Your finances") > .task-status-complete

the user fills in the project costs
    [Arguments]  ${overheadsCost}  ${totalCosts}
    the user clicks the button/link  link=Your project costs
    the user fills in Labour
    the user fills in Overhead costs  ${overheadsCost}  ${totalCosts}
    the user fills in Material
    the user fills in Capital usage
    the user fills in Subcontracting costs
    the user fills in Travel and subsistence
    the user fills in Other costs
    the user clicks the button/link  css=label[for="stateAidAgreed"]
    the user clicks the button/link  jQuery=button:contains("Mark as complete")
    the user clicks the button/link  link=Your project costs
    the user has read only view once section is marked complete

the user has read only view once section is marked complete
    the user should not see the element   css=input
    the user should see the element     jQuery=button:contains("Edit")
    the user clicks the button/link     jQuery=a:contains("Return to finances")

the user fills in Labour
    the user clicks the button/link            jQuery=button:contains("Labour")
    the user should see the element            css=.labour-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input
    the user enters text to a text field       css=input[name^="labour-labourDaysYearly"]    230
    the user should see the element            jQuery=input.form-control[name^=labour-role]:text[value=""]:first
    the user enters text to a text field       jQuery=input.form-control[name^=labour-role]:text[value=""]:first    anotherrole
    the user enters text to a text field       jQuery=input.form-control[name^=labour-gross][value=""]:first    120000
    the user enters text to a text field       jQuery=input.form-control[name^=labour-labour][value=""]:first    100
    the user clicks the button/link            jQuery=button:contains("Labour")

the user fills in Overhead costs
    [Arguments]  ${overheadsCost}  ${totalCosts}
    run keyword if  '${overheadsCost}'=='Calculate'  the user chooses Calculate overheads option  ${totalCosts}
    run keyword if  '${overheadsCost}'=='labour costs'  the user chooses 20% overheads option
#    run keyword if  '${overheadsCost}'=='No overhead'  the user chooses No overhead costs

the user chooses Calculate overheads option
    [Arguments]  ${totalCosts}
    the user expands the section  Overhead costs
    the user clicks the button/link                         jQuery=label:contains("Calculate overheads")
    the user should see the element                         jQuery=h3:contains("Calculate overheads")
    the user uploads the file                               css=#overheadfile   ${excel_file}
    wait for autosave
    the user enters text to a text field                    css=input[name^="overheads-total"][id^="cost-overheads"]   40
    wait for autosave
    the total overhead costs should reflect rate entered    css=#total-cost  £ ${totalCosts}

the total overhead costs should reflect rate entered
    [Arguments]    ${ADMIN_TOTAL}    ${ADMIN_VALUE}
    the element should be disabled      css=#total-cost
    Textfield Value Should Be    ${ADMIN_TOTAL}    ${ADMIN_VALUE}

the user chooses 20% overheads option
    # overheads option : 20% Labour
    the user clicks the button/link    jQuery=button:contains("Overhead costs")
    the user clicks the button/link    css=[data-target="overhead-default-percentage"] label
    the user clicks the button/link    jQuery=button:contains("Overhead costs")

the user fills in Material
    the user clicks the button/link       jQuery=button:contains("Materials")
    the user should see the element       css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    the user enters text to a text field  css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    10
    the user enters text to a text field  css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    100
    the user enters text to a text field  css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test
    the user clicks the button/link       jQuery=button:contains("Materials")

the user fills in Capital usage
    the user clicks the button/link       jQuery=button:contains("Capital usage")
    the user enters text to a text field  css=textarea.form-control[name^=capital_usage-description]  some description
    Click Element                         jQuery=label:contains("New")
    the user enters text to a text field  css=.form-finances-capital-usage-depreciation  10
    the user enters text to a text field  css=.form-finances-capital-usage-npv  5000
    the user enters text to a text field  css=.form-finances-capital-usage-residual-value  25
    the user enters text to a text field  css=.form-finances-capital-usage-utilisation   100
    focus                                 css=.section-total-summary > [data-mirror^="#section-total"]
    textfield should contain              css=#capital_usage .form-row:nth-of-type(1) [readonly]  £ 4,975
    the user clicks the button/link       jQuery=button:contains("Capital usage")

the user fills in Subcontracting costs
    the user clicks the button/link       jQuery=button:contains("Subcontracting costs")
    the user enters text to a text field  css=.form-finances-subcontracting-company  SomeName
    the user enters text to a text field  css=input.form-control[name^=subcontracting-country]  Netherlands
    the user enters text to a text field  css=textarea.form-control[name^=subcontracting-role]  Quality Assurance
    the user enters text to a text field  css=input.form-control[name^=subcontracting-subcontractingCost]  1000
    the user clicks the button/link       jQuery=button:contains("Subcontracting costs")

the user fills in Travel and subsistence
    the user clicks the button/link       jQuery=button:contains("Travel and subsistence")
    the user enters text to a text field  css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    test
    the user enters text to a text field  css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    10
    the user enters text to a text field  css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    100
    the user clicks the button/link       jQuery=button:contains("Travel and subsistence")

the user fills in Other costs
    the user clicks the button/link       jQuery=button:contains("Other costs")
    the user removes prev costs if there are any
    the user enters text to a text field  css=textarea.form-control[name^=other_costs-description]  some other costs
    the user enters text to a text field  css=input.form-control[name^=other_costs-otherCost]  50
    the user clicks the button/link       jQuery=button:contains("Other costs")

the user removes prev costs if there are any
    ${STATUS}    ${VALUE}=  Run Keyword And Ignore Error Without Screenshots  page should contain element  css=table[id="other-costs-table"] tr:contains("Remove")
    Run Keyword If    '${status}' == 'PASS'    the user clicks the button/link  jQuery=table[id="other-costs-table"] tr:contains("Remove")

the academic user fills in his finances
    [Arguments]  ${application}
    the user navigates to Your-finances page  ${application}
    the academic fills in the project costs   ${application}

the academic fills in the project costs
    [Arguments]  ${application}
    the user clicks the button/link       link=Your project costs
    The user enters text to a text field  id=tsb-ref  ${application}
    The user enters text to a text field  id=incurred-staff  999.999
    The user enters text to a text field  id=travel    999.999
    The user enters text to a text field  id=other    999.999
    the user should see the element       css=#subtotal-resources[data-calculation-rawvalue^="2999"]
    The user enters text to a text field  id=investigators    999.999
    The user enters text to a text field  id=estates    999.999
    The user enters text to a text field  id=other-direct    999.999
    the user should see the element       css=#subtotal-directly-allocated[data-calculation-rawvalue^="2999"]
    The user enters text to a text field  id=indirect    999.999
    The user enters text to a text field  id=exceptions-staff    999.999
    The user enters text to a text field  id=exceptions-other-direct    999.999
    the user should see the element       css=#total[data-calculation-rawvalue^="8999"]
    the user uploads the file             css=.inputfile  ${valid_pdf}
    the user should see the element       link=testing.pdf (opens in a new window)
    the user clicks the button/link       css=#mark-all-as-complete[type="submit"]

the user fills the organisation details with Project growth table
    [Arguments]   ${Application}  ${org_size}
    the user navigates to Your-finances page                ${Application}
    the user clicks the button/link                         link=Your organisation
    the user enters text to a text field                    css=input[name$="month"]    12
    and the user enters text to a text field                css=input[name$="year"]    2016
    the user selects the radio button                       financePosition-organisationSize  ${org_size}
    the user enters text to a text field                    jQuery=td:contains("Annual turnover") + td input   5600
    the user enters text to a text field                    jQuery=td:contains("Annual profit") + td input    3000
    the user enters text to a text field                    jQuery=td:contains("Annual export") + td input    4000
    the user enters text to a text field                    jQuery=td:contains("Research and development spend") + td input    5660
    the user enters text to a text field                    jQuery=label:contains("employees") + input    0
    the user clicks the button/link                         jQuery=button:contains("Mark as complete")

the user fills in the organisation information
    [Arguments]  ${Application}  ${org_size}
    the user navigates to Your-finances page  ${Application}
    the user clicks the button/link    link=Your organisation
    ${STATUS}    ${VALUE}=  Run Keyword And Ignore Error Without Screenshots  page should contain element  jQuery=button:contains("Edit")
    Run Keyword If    '${status}' == 'PASS'    the user clicks the button/link  jQuery=button:contains("Edit")
    the user selects the radio button  financePosition-organisationSize  ${org_size}
    the user enters text to a text field    jQuery=label:contains("Turnover") + input    150
    the user enters text to a text field    jQuery=label:contains("employees") + input   3
    the user clicks the button/link    jQuery=button:contains("Mark as complete")
    the user clicks the button/link  link=Your organisation
    the user should see the element       jQuery=button:contains("Edit")
    the user has read only view once section is marked complete

the user checks Your Funding section
    [Arguments]  ${Application}
    ${Research_category_selected}=  Run Keyword And Return Status    Element Should Be Visible   link=Your funding
    Run Keyword if   '${Research_category_selected}' == 'False'     the user selects research area       ${Application}
    Run Keyword if   '${Research_category_selected}' == 'True'      the user fills in the funding information      ${Application}

the user selects research area
    [Arguments]  ${Application}
    the applicant completes the application details     application details
    And the user fills in the funding information    ${Application}

the user fills in the funding information
    [Arguments]  ${Application}
    the user navigates to Your-finances page   ${Application}
    the user clicks the button/link       link=Your funding
    the user enters text to a text field  css=[name^="finance-grantclaimpercentage"]  45
    click element                         jQuery=label:contains("No")
    the user selects the checkbox         agree-terms-page
    the user clicks the button/link       jQuery=button:contains("Mark as complete")
    the user clicks the button/link  link=Your funding
    the user should see the element       jQuery=button:contains("Edit")
    the user has read only view once section is marked complete

the user should see all finance subsections complete
    the user should see the element  css=li:nth-of-type(1) .task-status-complete
    the user should see the element  css=li:nth-of-type(2) .task-status-complete
    the user should see the element  css=li:nth-of-type(3) .task-status-complete

the user should see all finance subsections incomplete
    the user should see the element  css=li:nth-of-type(1) .action-required
    the user should see the element  css=li:nth-of-type(2) .action-required
    the user should see the element  jQuery=h3:contains("Your funding")

Remove previous rows
    [Arguments]  ${element}
    :FOR    ${i}    IN RANGE  10
    # The sleep of 200 ms is actually for speed, originally the test used "should not see the element" however that made it wait for 10 seconds on every loop.
    \  sleep    200ms
    \  ${STATUS}    ${VALUE}=    Run Keyword And Ignore Error Without Screenshots    the user should see the element    ${element}
    \  Log    ${status}
    \  Exit For Loop If  '${status}'=='FAIL'
    \  run keyword if  '${status}'=='PASS'  the user clicks the button/link  ${element}
    \  ${i} =  Set Variable  ${i + 1}

Invite a non-existing collaborator
    [Arguments]   ${email}  ${competition_name}
    the user should see the element        jQuery=h1:contains("Application overview")
    the user fills in the inviting steps   ${email}
    logout as user
    newly invited collaborator can create account and sign in   ${email}  ${competition_name}

the user is able to confirm the invite
    [Arguments]  ${email}  ${password}
    the user clicks the button/link                 jQuery=.button:contains("Continue or sign in")
    The guest user inserts user email and password  ${email}  ${password}
    The guest user clicks the log-in button
    the user should see the text in the page        Confirm your organisation
    the user clicks the button/link                 jQuery=.button:contains("Confirm and accept invitation")

Newly invited collaborator can create account and sign in
    [Arguments]    ${email}  ${competition_name}
    the user reads his email and clicks the link   ${email}  Invitation to collaborate in ${competition_name}  You will be joining as part of the organisation  2
    the user clicks the button/link    jQuery=a:contains("Yes, accept invitation")
    the user should see the element    jquery=h1:contains("Choose your organisation type")
    the user completes the new account creation   ${email}

the user completes the new account creation
    [Arguments]    ${email}
    the user selects the radio button           organisationType    radio-${PUBLIC_SECTOR_TYPE_ID}
    the user clicks the button/link             jQuery=button:contains("Continue")
    the user should see the element             jQuery=span:contains("Create your account")
    the user enters text to a text field        id=organisationSearchName    innovate
    the user should see the element             jQuery=a:contains("Back to choose your organisation type")
    the user clicks the button/link             jQuery=button:contains("Search")
    wait for autosave
    the user clicks the button/link             jQuery=a:contains("INNOVATE LTD")
    the user should see the element             jQuery=h3:contains("Organisation type")
    the user selects the checkbox               address-same
    wait for autosave
    the user clicks the button/link             jQuery=button:contains("Continue")
    the user should not see an error in the page
    the user clicks the button/link             jQuery=.button:contains("Save and continue")
    the user should be redirected to the correct page    ${SERVER}/registration/register
    the invited user fills the create account form       liam  smithson
    the user should see the text in the page     Please verify your email address
    the user reads his email and clicks the link   ${email}  Please verify your email address  Once verified you can sign into your account.
    the user should be redirected to the correct page    ${REGISTRATION_VERIFIED}
    the user clicks the button/link             link=Sign in
    the user should see the text in the page    Sign in
    the user enters text to a text field        css=input[id="username"]  ${email}
    the user enters text to a text field        css=input[id="password"]  ${correct_password}
    the user clicks the button/link             jQuery=button:contains("Sign in")

the applicant adds some content and marks this section as complete
    Focus    css=.textarea-wrapped .editor
    Input Text    css=.textarea-wrapped .editor    This is some random text
    the user clicks the button/link    name=mark_as_complete
    the user should see the element    name=mark_as_incomplete

the applicant edits the "economic benefit" question
    the user clicks the button/link    name=mark_as_incomplete
    the user should see the element    name=mark_as_complete

logged in user applies to competition
    [Arguments]  ${competition}
    the user navigates to the page  ${frontDoor}
    navigate to next page if not found  ${competition}
    the user clicks the button/link  link=${competition}
    the user clicks the button/link  link=Start new application
    the user clicks the button/link  link=Begin application

navigate to next page if not found
    [Arguments]  ${competition}
    ${STATUS}    ${VALUE}=    Run Keyword And Ignore Error Without Screenshots    Element Should Be Visible  link=${competition}
    Run Keyword If    '${status}' == 'FAIL'    the user clicks the button/link  jQuery=a:contains("Next")

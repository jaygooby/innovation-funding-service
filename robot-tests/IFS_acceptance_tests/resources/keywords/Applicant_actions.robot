*** Keywords ***
Applicant goes to the Overview page
    go to    ${APPLICATION_OVERVIEW_URL}

Applicant goes to the Application form
    Go To    ${APPLICATION_URL}

Applicant goes to the 'application details' question
    Go To    ${APPLICATION_DETAILS_URL}

Applicant goes to the 'project summary' question
    Go To    ${PROJECT_SUMMARY_URL}

Applicant goes to the 'public description' question
    Go To    ${PUBLIC_DESCRIPTION_URL}

Applicant goes to the 'scope' question
    Go To    ${SCOPE_URL}

Applicant goes to the 'business opportunity' question
    Go To    ${BUSINESS_OPPORTUNITY_URL}

Applicant goes to the 'potential market' question
    Go To    ${POTENTIAL_MARKET_URL}

Applicant goes to the 'project exploitation' question
    Go To    ${PROJECT_EXPLOITATION_URL}

Applicant goes to the 'economic benefit' question
    Go To    ${ECONOMIC_BENEFIT_URL}

Applicant goes to the 'technical approach' question
    Go To    ${TECHNICAL_APPROACH_URL}

Applicant goes to the 'innovation' question
    Go To    ${INNOVATION_URL}

Applicant goes to the 'risks' question
    Go To    ${RISKS_URL}

Applicant goes to the 'funding' question
    Go To    ${FUNDING_URL}

Applicant goes to the 'adding value' question
    Go To    ${ADDING_VALUE_URL}

Applicant edits the 'Project Summary' question
    Clear Element Text    css=#form-input-11 .editor
    Input Text    css=#form-input-11 .editor    I am a robot

Mark scope question 13 as editable
    Click Element    css=#question-13 div.textarea-wrapped.marked-as-complete.word-count div.textarea-footer > button

Mark question 11 as editable
    Click Element    css=#form-input-11 div.textarea-wrapped.marked-as-complete.word-count div.textarea-footer > button

Mark question 12 as editable
    click Element    css=#question-12 div.textarea-wrapped.marked-as-complete.word-count div.textarea-footer > button

Switch to the first browser
    Switch browser    1

Applicant is in the 'Your Finance' sub-section
    Go To    ${FINANCES}
    Click Element    Link=Your finances

Applicant goes to the scope section
    go to    ${SCOPE_SECTION_URL}

Applicant goes to the Application questions section
    Go to    ${APPLICATION_QUESTIONS_SECTION_URL}

Applicant goes to the "Your approach..." section
    Go to    ${PROJECT_URL}

Applicant goes to the "Funding" section
    Go to    ${FUNDING_URL}

the applicant is in the "Your Finances" sub-section
    Applicant is in the 'Your Finance' sub-section

the applicant is in the Finance section
    Go To    ${FINANCES}

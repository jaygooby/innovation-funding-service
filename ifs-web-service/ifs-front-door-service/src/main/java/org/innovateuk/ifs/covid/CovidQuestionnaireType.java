package org.innovateuk.ifs.covid;

public enum CovidQuestionnaireType {
    BUSINESS("business", "Are you a business?"),
    AWARD_RECIPIENT("award-recipient", "Are you an Innovate UK award recipient?"),
    CHALLENGE_TIMING("challenge-timing", "Is your challenge in the timing of your project activity?", "For example, do you need to extend the blah?"),
    CHALLENGE_CASHFLOW("challenge-cashflow", "Is your challenge in managing your cashflow, so that you can meet your project costs to continue your project activity up to £250,000 in the next [x] quarters? "),
    CHALLENGE_LARGE_FUNDING_GAP("challenge-cashflow", "Is the challenge in meeting a larger funding gap (up to somthi???n"),
    CHALLENGE_SIGNIFICANT_FUNDING_GAP("challenge-cashflow",  "Is the challnege in meeting a significant one?");

    private String url;
    private String question;
    private String hint;

    CovidQuestionnaireType(String url, String question, String hint) {
        this.url = url;
        this.question = question;
        this.hint = hint;
    }

    CovidQuestionnaireType(String url, String question) {
        this(url, question, null);
    }

    public String getUrl() {
        return url;
    }

    public String getQuestion() {
        return question;
    }

    public String getHint() {
        return hint;
    }
}

package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class CompetitionCountResourceDocs {
    public static final FieldDescriptor[] competitionCountResourceFields = {
            fieldWithPath("liveCount").description("The number of live competitions"),
            fieldWithPath("projectSetupCount").description("The number of competitions in project set up"),
            fieldWithPath("upcomingCount").description("The number of upcoming competitions"),
            fieldWithPath("feedbackReleasedCount").description("The number of competitions with feedback released"),
            fieldWithPath("nonIfsCount").description("The number of non-IFS competitions")
    };
}

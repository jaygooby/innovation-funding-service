package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class InterviewAssessorAllocateApplicationsResourceDocs {

    public static final FieldDescriptor[] interviewAssessorAllocateApplicationsResourceFields = {
            fieldWithPath("id").description("Id of the assessor"),
            fieldWithPath("name").description("Name of the assessor"),
            fieldWithPath("skillAreas").description("Skill areas of the assessor"),
    };
}

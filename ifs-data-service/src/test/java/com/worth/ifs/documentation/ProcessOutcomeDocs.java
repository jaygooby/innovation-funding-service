package com.worth.ifs.documentation;


import com.worth.ifs.assessment.builder.ProcessOutcomeResourceBuilder;
import org.springframework.restdocs.payload.FieldDescriptor;

import static com.worth.ifs.assessment.builder.ProcessOutcomeResourceBuilder.newProcessOutcomeResource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class ProcessOutcomeDocs {
    public static final FieldDescriptor[] processOutcomeFields = {
            fieldWithPath("id").description("Id of the process outcome"),
            fieldWithPath("outcome").description("the value of the outcome"),
            fieldWithPath("description").description("description of the outcome"),
            fieldWithPath("comment").description("comment associated with the process outcome"),
            fieldWithPath("outcomeType").description("the type fo the process outcome")
    };

    public static final ProcessOutcomeResourceBuilder processOutcomeResourceBuilder = newProcessOutcomeResource()
            .withId(1L)
            .withOutcome("outcome")
            .withDescription("description")
            .withComment("comment")
            .withOutcomeType("outcomeType");
}

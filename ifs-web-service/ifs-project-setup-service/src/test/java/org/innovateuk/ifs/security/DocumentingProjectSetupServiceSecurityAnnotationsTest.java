package org.innovateuk.ifs.security;

import java.util.List;

import static java.util.Collections.emptyList;

public class DocumentingProjectSetupServiceSecurityAnnotationsTest extends AbstractDocumentingWebServiceSecurityAnnotationsTest {

    @Override
    protected List<Class<?>> additionalExcludedClasses() {
        return emptyList();
    }
}

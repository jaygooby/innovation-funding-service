package org.innovateuk.ifs.competition.populator.publiccontent;


import org.innovateuk.ifs.competition.publiccontent.resource.ContentGroupResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentItemResource;
import org.innovateuk.ifs.competition.publiccontent.resource.PublicContentSectionResource;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.AbstractPublicContentGroupViewModel;
import org.innovateuk.ifs.util.CollectionFunctions;

/**
 * Abstract class to populate the generic fields needed in the view.
 * @param <M> the view model class.
 */
public abstract class AbstractPublicContentGroupViewModelPopulator<M extends AbstractPublicContentGroupViewModel> extends AbstractPublicContentSectionViewModelPopulator<M> {

    protected void populateSection(M model, PublicContentItemResource publicContentItemResource, PublicContentSectionResource section, Boolean nonIFS) {
            model.setFileEntries(CollectionFunctions.simpleToMap(section.getContentGroups(),
                    ContentGroupResource::getId, ContentGroupResource::getFileEntry));
            model.setContentGroups(section.getContentGroups());
    }
}

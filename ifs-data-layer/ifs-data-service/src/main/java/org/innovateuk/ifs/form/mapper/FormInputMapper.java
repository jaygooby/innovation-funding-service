package org.innovateuk.ifs.form.mapper;

import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.application.mapper.QuestionMapper;
import org.innovateuk.ifs.commons.mapper.BaseMapper;
import org.innovateuk.ifs.commons.mapper.GlobalMapperConfig;
import org.innovateuk.ifs.competition.mapper.CompetitionMapper;
import org.innovateuk.ifs.file.resource.FileTypeCategories;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMapSet;

@Mapper(
    config = GlobalMapperConfig.class,
    uses = {
        CompetitionMapper.class,
        FormInputResponseMapper.class,
        FormValidatorMapper.class,
        QuestionMapper.class,
        GuidanceRowMapper.class
    }
)
public abstract class FormInputMapper extends BaseMapper<FormInput, FormInputResource, Long> {

    @Mappings({
            @Mapping(target = "responses", ignore = true),
            @Mapping(target = "guidanceRows", ignore = true),
            @Mapping(target = "active", ignore = true),
    })
    @Override
    public abstract FormInput mapToDomain(FormInputResource resource);

    @Mappings({
            @Mapping(target = "allowedFileTypesSet", ignore = true)
    })
    @Override
    public abstract FormInputResource mapToResource(FormInput domain);

    public Long mapFormInputToId(FormInput object) {
        if (object == null) {
            return null;
        }
        return object.getId();
    }

    Set<FileTypeCategories> mapToFileTypes(String fileTypes) {
        if (fileTypes == null) {
            return emptySet();
        }

        return simpleMapSet(fileTypes.split(","), FileTypeCategories::valueOf);
    }

    String mapFromFileTypes(Set<FileTypeCategories> fileTypes) {
        return StringUtils.join(fileTypes, ",");
    }
}

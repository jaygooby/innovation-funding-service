package org.innovateuk.ifs.competition.mapper;

import org.innovateuk.ifs.commons.mapper.BaseMapper;
import org.innovateuk.ifs.commons.mapper.GlobalMapperConfig;
import org.innovateuk.ifs.competition.domain.GrantTermsAndConditions;
import org.innovateuk.ifs.competition.resource.GrantTermsAndConditionsResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(
        config = GlobalMapperConfig.class
)
public abstract class GrantTermsAndConditionsMapper
        extends BaseMapper<GrantTermsAndConditions, GrantTermsAndConditionsResource, Long> {

    @Mappings({
            @Mapping(source = "createdBy.name", target = "createdBy"),
            @Mapping(source = "modifiedBy.name", target = "modifiedBy")})
    @Override
    public abstract GrantTermsAndConditionsResource mapToResource(GrantTermsAndConditions domain);

}

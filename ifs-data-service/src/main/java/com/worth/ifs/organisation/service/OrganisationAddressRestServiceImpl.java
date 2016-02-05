package com.worth.ifs.organisation.service;

import com.worth.ifs.commons.service.BaseRestService;
import com.worth.ifs.organisation.resource.OrganisationAddressResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrganisationAddressRestServiceImpl extends BaseRestService implements OrganisationAddressRestService {
    @Value("${ifs.data.service.rest.organisationaddress}")
    private String restUrl;


    @Override
    public OrganisationAddressResource findOne(Long id) {
        return restGet(restUrl + "/" + id, OrganisationAddressResource.class);
    }
}
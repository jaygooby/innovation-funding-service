package com.worth.ifs.organisation.service;

import com.worth.ifs.organisation.resource.CompanyHouseBusiness;

import java.util.List;

/**
 * Interface for communication with the company house services
 */
public interface CompanyHouseRestService {
    List<CompanyHouseBusiness> searchOrganisationsByName(String name);
    CompanyHouseBusiness getOrganisationById(String organisationId);
}

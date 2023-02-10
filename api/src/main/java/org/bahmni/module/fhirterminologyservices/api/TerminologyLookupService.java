package org.bahmni.module.fhirterminologyservices.api;


import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TerminologyLookupService extends OpenmrsService {

    @Authorized(value = {"Search Terminology Server"})
    List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale);
}

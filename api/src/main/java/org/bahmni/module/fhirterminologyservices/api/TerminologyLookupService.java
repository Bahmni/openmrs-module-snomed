package org.bahmni.module.fhirterminologyservices.api;


import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.io.IOException;
import java.util.List;

public interface TerminologyLookupService extends OpenmrsService {

	List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) throws TerminologyServicesException, IOException;
}

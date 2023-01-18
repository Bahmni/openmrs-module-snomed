package org.bahmni.module.terminologyservices.api;


import org.bahmni.module.terminologyservices.utils.TerminologyServicesException;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TerminologyLookupService extends OpenmrsService {
	@Authorized()
	String getTerminologyServerBaseUrl();

	List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) throws TerminologyServicesException;
}

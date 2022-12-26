package org.bahmni.module.terminologyservices.api;

import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirTerminologyResponse;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;

import java.io.IOException;
import java.util.List;

public interface TerminologyInitiatorService extends OpenmrsService {
// change the privilege value and add it to liquibase changeset
	@Authorized(value = {"Create Terminology Services url"})
	String getTerminologyServicesServerUrl();

	List<BahmniSearchResponse> getBahmniSearchResponse(String searchTerm, Integer limit) ;

	FhirTerminologyResponse createMockFhirTerminologyResponse() ;
	String getMockTerminologyString();
}

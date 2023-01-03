package org.bahmni.module.terminologyservices.api;

import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirTerminologyResponse;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TerminologyInitiatorService extends OpenmrsService {
// change the privilege value and add it to liquibase changeset
	@Authorized(value = {"Get Terminology Services"})
	String getTerminologyServicesServerUrl();
// change method signature
	List<BahmniSearchResponse> getBahmniSearchResponse(String searchTerm, Integer limit, String locale) ;
	List<SimpleObject> getDiagnosisSearch(String searchTerm, Integer limit, String locale) ;
	List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) ;

	FhirTerminologyResponse createMockFhirTerminologyResponse() ;
	ValueSet createMockFhirTerminologyResponseValueSet() ;
	String getMockTerminologyString();
}

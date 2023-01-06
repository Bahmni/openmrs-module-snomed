package org.bahmni.module.terminologyservices.api;


import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TerminologyInitiatorService extends OpenmrsService {
// change the privilege value and add it to liquibase changeset
	@Authorized(value = {"Get Terminology Services"})
	String getTerminologyServicesServerUrl();

	List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) ;

	ValueSet createMockFhirTerminologyResponseValueSet() ;
	String getMockTerminologyString();
}

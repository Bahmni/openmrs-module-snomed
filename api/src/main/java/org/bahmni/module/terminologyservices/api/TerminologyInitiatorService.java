package org.bahmni.module.terminologyservices.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;

public interface TerminologyInitiatorService extends OpenmrsService {
// change the privilege value and add it to liquibase changeset
	@Authorized(value = {"Create Terminology Services url"})
	String getTerminologyServicesServerUrl();
}

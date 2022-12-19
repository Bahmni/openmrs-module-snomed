package org.bahmni.module.terminologyservices.api.impl;

import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.bahmni.module.terminologyservices.api.TerminologyInitiatorService;

public class TerminologyInitiatorServiceImpl extends BaseOpenmrsService implements TerminologyInitiatorService {
	
	private final static String PROP_TERMINOLOGY_SERVICES_SERVER = "bahmni.clinical.terminologyServices.serverUrlPattern";
	
	private final static String DEFAULT_TERMINOLOGY_SERVICES_SERVER_URL = "https://snomed-url";

	@Override
	public String getTerminologyServicesServerUrl() {
		String tsServerUrl = Context.getAdministrationService().getGlobalProperty(PROP_TERMINOLOGY_SERVICES_SERVER);
		if ((tsServerUrl == null) || "".equals(tsServerUrl)) {
			tsServerUrl = DEFAULT_TERMINOLOGY_SERVICES_SERVER_URL;
		}
		return tsServerUrl;
	}
	
}

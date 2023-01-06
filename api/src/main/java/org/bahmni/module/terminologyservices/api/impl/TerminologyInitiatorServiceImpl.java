package org.bahmni.module.terminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.terminologyservices.api.Constants;
import org.bahmni.module.terminologyservices.api.mapper.FhirTerminologyServicesToBahmniMapper;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.bahmni.module.terminologyservices.api.TerminologyInitiatorService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;
import java.util.stream.Collectors;

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

	@Override
	public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) {
		ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
		return 	valueSet.getExpansion().getContains().stream().map(new FhirTerminologyServicesToBahmniMapper()::mapFhirResponseValueSetToSimpleObject).collect(Collectors.toList());

	}

	@Override
	public ValueSet createMockFhirTerminologyResponseValueSet() {
		String mockString  = getMockTerminologyString();
		FhirContext ctx =  FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		return parser.parseResource(ValueSet.class, mockString);
	}

	@Override
	public String getMockTerminologyString() {
		return Constants.FHIR_TERMINOLOGY_SERVICES_MOCK_RESPONSE;
	}

}

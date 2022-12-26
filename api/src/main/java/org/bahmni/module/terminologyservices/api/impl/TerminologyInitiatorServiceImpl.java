package org.bahmni.module.terminologyservices.api.impl;

import org.bahmni.module.terminologyservices.api.Constants;
import org.bahmni.module.terminologyservices.api.mapper.FhirTerminologyServicesToBahmniMapper;
import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirTerminologyResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.bahmni.module.terminologyservices.api.TerminologyInitiatorService;

import java.io.IOException;
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
	public List<BahmniSearchResponse> getBahmniSearchResponse(String searchTerm, Integer limit) {
		FhirTerminologyResponse fhirTerminologyResponse = createMockFhirTerminologyResponse();
		return fhirTerminologyResponse.getExpansion().getContains().stream().map(new FhirTerminologyServicesToBahmniMapper()::map).collect(Collectors.toList());
	}

	@Override
	public FhirTerminologyResponse createMockFhirTerminologyResponse()  {
		String mockString  = getMockTerminologyString();
		JSONObject jsonObject = new JSONObject(mockString);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonObject.toString(), FhirTerminologyResponse.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getMockTerminologyString() {
		return Constants.FHIR_TERMINOLOGY_SERVICES_MOCK_RESPONSE;
	}

}

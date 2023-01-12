package org.bahmni.module.terminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.terminologyservices.api.Constants;
import org.bahmni.module.terminologyservices.api.mapper.FhirToBahmniMapper;
import org.bahmni.module.terminologyservices.utils.TerminologyServerUnavailableException;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.bahmni.module.terminologyservices.api.TerminologyLookupService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {
	
	private static final String PROP_TERMINOLOGY_SERVICES_SERVER = "bahmni.terminologyServices.serverBaseUrlPattern";
	
	private static final String DEFAULT_TERMINOLOGY_SERVICES_SERVER_URL = "https://snowstorm-fhir.snomedtools.org/fhir/";

	private FhirToBahmniMapper fhirToBahmniMapper;
	@Autowired
	public  TerminologyLookupServiceImpl (FhirToBahmniMapper fhirToBahmniMapper) {
		this.fhirToBahmniMapper = fhirToBahmniMapper;
	}

	@Override
	public String getTerminologyServerBaseUrl() {
		String tsServerUrl = Context.getAdministrationService().getGlobalProperty(PROP_TERMINOLOGY_SERVICES_SERVER);
		if ((tsServerUrl == null) || "".equals(tsServerUrl)) {
			tsServerUrl = DEFAULT_TERMINOLOGY_SERVICES_SERVER_URL;
		}
		return tsServerUrl;
	}

	@Override
	public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) throws TerminologyServerUnavailableException {
		String mockDiagnosis = Constants.MOCK_DIAGNOSES_SEARCH_TERM;
		if(mockDiagnosis.contains(searchTerm)) {
			ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
			return valueSet.getExpansion().getContains().stream().map(matchedItem -> fhirToBahmniMapper.mapFhirResponseValueSetToSimpleObject(matchedItem)).collect(Collectors.toList());
		} else {
			throw new TerminologyServerUnavailableException(Constants.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE);
		}
	}


	 ValueSet createMockFhirTerminologyResponseValueSet() {
		String mockString  = getMockTerminologyString();
		FhirContext ctx =  FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		return parser.parseResource(ValueSet.class, mockString);
	}


	 String getMockTerminologyString() {
		return Constants.FHIR_TERMINOLOGY_SERVICES_MOCK_RESPONSE;
	}

	public void setFhirToBahmniMapper(FhirToBahmniMapper fhirToBahmniMapper) {
		this.fhirToBahmniMapper = fhirToBahmniMapper;
	}
}

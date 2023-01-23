package org.bahmni.module.fhirterminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.fhirterminologyservices.api.BahmniConstants;
import org.bahmni.module.fhirterminologyservices.api.mapper.FhirToBahmniMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.bahmni.module.fhirterminologyservices.api.BahmniConstants.TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR;

public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {
	


	private FhirToBahmniMapper fhirToBahmniMapper;
	@Autowired
	public  TerminologyLookupServiceImpl (FhirToBahmniMapper fhirToBahmniMapper) {
		this.fhirToBahmniMapper = fhirToBahmniMapper;
	}

	@Override
	public String getTerminologyServerBaseUrl() throws TerminologyServicesException {
		String tsServerUrl = Context.getAdministrationService().getGlobalProperty(BahmniConstants.TERMINOLOGY_SERVER_URL_GLOBAL_PROP);
		if ((tsServerUrl == null) || "".equals(tsServerUrl)) {
			throw new TerminologyServicesException(TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR);
		}
		return tsServerUrl;
	}

	@Override
	public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) throws TerminologyServicesException {
		String mockDiagnosis = BahmniConstants.MOCK_DIAGNOSES_SEARCH_TERM;
		if(mockDiagnosis.contains(searchTerm)) {
			ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
			return valueSet.getExpansion().getContains().stream().map(matchedItem -> fhirToBahmniMapper.mapFhirResponseValueSetToSimpleObject(matchedItem)).collect(Collectors.toList());
		} else {
			throw new TerminologyServicesException(BahmniConstants.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE);
		}
	}


	 ValueSet createMockFhirTerminologyResponseValueSet() {
		String mockString  = getMockTerminologyString();
		FhirContext ctx =  FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		return parser.parseResource(ValueSet.class, mockString);
	}


	 String getMockTerminologyString() {
		return BahmniConstants.FHIR_TERMINOLOGY_SERVICES_MOCK_RESPONSE;
	}

	public void setFhirToBahmniMapper(FhirToBahmniMapper fhirToBahmniMapper) {
		this.fhirToBahmniMapper = fhirToBahmniMapper;
	}
}

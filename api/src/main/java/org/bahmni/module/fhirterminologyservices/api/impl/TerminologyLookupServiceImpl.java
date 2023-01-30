package org.bahmni.module.fhirterminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.fhirterminologyservices.api.mapper.FhirValueSetToDiagnosisMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {

	public static final String MOCK_DIAGNOSES_SEARCH_TERM = "Malaria";
	public static final String TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE = "TERMINOLOGY_SERVER_ERROR_MESSAGE";
	public static final String TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR = "TERMINOLOGY_SERVICES_CONFIG_INVALID";
	public static final String TERMINOLOGY_SERVER_URL_GLOBAL_PROP = "ts.fhir.baseurl";

	private FhirValueSetToDiagnosisMapper fhirValueSetToDiagnosisMapper;
	@Autowired
	public  TerminologyLookupServiceImpl (FhirValueSetToDiagnosisMapper fhirValueSetToDiagnosisMapper) {
		this.fhirValueSetToDiagnosisMapper = fhirValueSetToDiagnosisMapper;
	}

	@Override
	public String getTerminologyServerBaseUrl() throws TerminologyServicesException {
		String tsServerUrl = Context.getAdministrationService().getGlobalProperty(TERMINOLOGY_SERVER_URL_GLOBAL_PROP);
		if ((tsServerUrl == null) || "".equals(tsServerUrl)) {
			throw new TerminologyServicesException(TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR);
		}
		return tsServerUrl;
	}

	@Override
	public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) throws TerminologyServicesException, IOException {
		if(MOCK_DIAGNOSES_SEARCH_TERM.contains(searchTerm)) {
			ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
			return valueSet.getExpansion().getContains().stream().map(matchedItem -> fhirValueSetToDiagnosisMapper.mapFhirResponseValueSetToSimpleObject(matchedItem)).collect(Collectors.toList());
		} else {
			throw new TerminologyServicesException(TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE);
		}
	}


	 ValueSet createMockFhirTerminologyResponseValueSet() throws IOException {
		String mockString  = getMockTerminologyString();
		FhirContext ctx =  FhirContext.forR4();
		IParser parser = ctx.newJsonParser();
		return parser.parseResource(ValueSet.class, mockString);
	}


	 private String getMockTerminologyString() throws IOException {
		 ClassLoader classLoader = getClass().getClassLoader();
		 InputStream inputStream = classLoader.getResourceAsStream("mock/TsMockResponseJson.json");
		 BufferedInputStream bis = new BufferedInputStream(inputStream);
		 ByteArrayOutputStream buf = new ByteArrayOutputStream();
		 for (int result = bis.read(); result != -1; result = bis.read()) {
			 buf.write((byte) result);
		 }
		 return buf.toString("UTF-8");
	}
}

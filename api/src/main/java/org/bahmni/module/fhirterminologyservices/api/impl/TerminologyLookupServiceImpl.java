package org.bahmni.module.fhirterminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {

    public static final String MOCK_DIAGNOSES_SEARCH_TERM = "Malaria";
    public static final String TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE = "TERMINOLOGY_SERVER_ERROR_MESSAGE";

    private ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper;

    public TerminologyLookupServiceImpl(ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper) {
        this.vsSimpleObjectMapper = vsSimpleObjectMapper;
    }


    @Override
    public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale) throws IOException {
        if (MOCK_DIAGNOSES_SEARCH_TERM.contains(searchTerm)) {
            ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
            return vsSimpleObjectMapper.map(valueSet);
        } else {
            throw new TerminologyServicesException(TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE);
        }
    }


    ValueSet createMockFhirTerminologyResponseValueSet() throws IOException {
        String mockString = getMockTerminologyString();
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        return parser.parseResource(ValueSet.class, mockString);
    }


    private String getMockTerminologyString() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("mock/TsMockResponseJson.json");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        for (int result = bufferedInputStream.read(); result != -1; result = bufferedInputStream.read()) {
            buf.write((byte) result);
        }
        return buf.toString("UTF-8");
    }
}

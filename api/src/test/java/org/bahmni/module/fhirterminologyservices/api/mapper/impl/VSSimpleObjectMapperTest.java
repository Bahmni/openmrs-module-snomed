package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper.CONCEPT_NAME;
import static org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper.CONCEPT_UUID;
import static org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper.MATCHED_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VSSimpleObjectMapperTest {

    @InjectMocks
    private VSSimpleObjectMapper vsSimpleObjectMapper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void shouldMapFhirTerminologyContainsSetToResponseList() throws IOException {
        ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
        List<SimpleObject> simpleObjectList = vsSimpleObjectMapper.map(valueSet);
        assertNotNull(simpleObjectList);
        assertEquals(4, simpleObjectList.size());
        SimpleObject response = simpleObjectList.get(0);
        assertEquals("Malaria", response.get(CONCEPT_NAME));
        assertEquals("1", response.get(CONCEPT_UUID));
        assertEquals("Malaria", response.get(MATCHED_NAME));
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
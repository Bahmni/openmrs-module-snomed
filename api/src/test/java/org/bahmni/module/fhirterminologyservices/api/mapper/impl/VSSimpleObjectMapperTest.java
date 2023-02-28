package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
    public void shouldMapFhirTerminologyContainsSetToResponseList() throws IOException, URISyntaxException {
        ValueSet valueSet = createMockFhirTerminologyResponseValueSet();
        List<SimpleObject> simpleObjectList = vsSimpleObjectMapper.map(valueSet);
        assertNotNull(simpleObjectList);
        assertEquals(4, simpleObjectList.size());
        SimpleObject response = simpleObjectList.get(0);
        assertEquals("Malaria", response.get(VSSimpleObjectMapper.CONCEPT_NAME));
        assertEquals("1", response.get(VSSimpleObjectMapper.CONCEPT_UUID));
        assertEquals("Malaria", response.get(VSSimpleObjectMapper.MATCHED_NAME));
        assertEquals("http://dummyhost/dummysystemcode", response.get(VSSimpleObjectMapper.CONCEPT_SYSTEM));
    }

    private ValueSet createMockFhirTerminologyResponseValueSet() throws IOException, URISyntaxException {
        String mockString = getMockTerminologyString();
        return FhirContext.forR4().newJsonParser().parseResource(ValueSet.class, mockString);
    }

    private String getMockTerminologyString() throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource("mock/TsMockResponse.json").toURI());
        return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
    }

}
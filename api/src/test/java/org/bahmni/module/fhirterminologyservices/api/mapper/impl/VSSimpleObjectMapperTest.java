package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

import static org.bahmni.module.fhirterminologyservices.api.SimpleObjectConstants.*;
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
    public void shouldMapFhirTerminologyContainsSetToResponseList() {
        ValueSet valueSet = getMockValueSet();
        List<SimpleObject> simpleObjectList = vsSimpleObjectMapper.map(valueSet);
        assertNotNull(simpleObjectList);
        assertEquals(1, simpleObjectList.size());
        SimpleObject response = simpleObjectList.get(0);
        assertEquals("Malaria", response.get(CONCEPT_NAME));
        assertEquals("1", response.get(CONCEPT_UUID));
        assertEquals("Malaria", response.get(MATCHED_NAME));
    }

    private ValueSet getMockValueSet() {
        ValueSet valueSet = new ValueSet();
        ValueSet.ValueSetExpansionContainsComponent valueSetExpansionContainsComponent = new ValueSet.ValueSetExpansionContainsComponent();
        valueSetExpansionContainsComponent.setCode("1");
        valueSetExpansionContainsComponent.setSystem("http://DUMMY_TS_URL");
        valueSetExpansionContainsComponent.setDisplay("Malaria");
        ValueSet.ValueSetExpansionComponent valueSetExpansionComponent = new ValueSet.ValueSetExpansionComponent();
        valueSetExpansionComponent.addContains(valueSetExpansionContainsComponent);
        valueSet.setExpansion(valueSetExpansionComponent);
        return valueSet;
    }

}
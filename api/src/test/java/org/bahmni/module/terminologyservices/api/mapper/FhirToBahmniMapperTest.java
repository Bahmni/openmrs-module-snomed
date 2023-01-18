package org.bahmni.module.terminologyservices.api.mapper;


import org.bahmni.module.terminologyservices.api.Constants;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static org.junit.Assert.*;

public class FhirToBahmniMapperTest {

    @Test
    public void shouldMapFhirTerminologyContainsSetToResponseList() {
        ValueSet.ValueSetExpansionContainsComponent valueSetExpansionContainsComponent = new ValueSet.ValueSetExpansionContainsComponent();
        valueSetExpansionContainsComponent.setCode("195967001");
        valueSetExpansionContainsComponent.setSystem("http://snomed.info/sct");
        valueSetExpansionContainsComponent.setDisplay("Hyperreactive airway disease");
        SimpleObject response = new FhirToBahmniMapper().mapFhirResponseValueSetToSimpleObject(valueSetExpansionContainsComponent);
        assertNotNull(response);
        assertEquals("Hyperreactive airway disease", response.get(Constants.CONCEPT_NAME));
        assertEquals("195967001", response.get(Constants.CONCEPT_UUID));
        assertEquals("Hyperreactive airway disease", response.get(Constants.MATCHED_NAME));
    }
}
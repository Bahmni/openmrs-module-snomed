package org.bahmni.module.fhirterminologyservices.api.mapper;


import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static org.bahmni.module.fhirterminologyservices.api.mapper.FhirValueSetToDiagnosisMapper.*;
import static org.junit.Assert.*;

public class FhirValueSetToDiagnosisMapperTest {

    @Test
    public void shouldMapFhirTerminologyContainsSetToResponseList() {
        ValueSet.ValueSetExpansionContainsComponent valueSetExpansionContainsComponent = new ValueSet.ValueSetExpansionContainsComponent();
        valueSetExpansionContainsComponent.setCode("195967001");
        valueSetExpansionContainsComponent.setSystem("http://snomed.info/sct");
        valueSetExpansionContainsComponent.setDisplay("Hyperreactive airway disease");
        SimpleObject response = new FhirValueSetToDiagnosisMapper().mapFhirResponseValueSetToSimpleObject(valueSetExpansionContainsComponent);
        assertNotNull(response);
        assertEquals("Hyperreactive airway disease", response.get(FhirValueSetToDiagnosisMapper.CONCEPT_NAME));
        assertEquals("195967001", response.get(CONCEPT_UUID));
        assertEquals("Hyperreactive airway disease", response.get(MATCHED_NAME));
    }
}
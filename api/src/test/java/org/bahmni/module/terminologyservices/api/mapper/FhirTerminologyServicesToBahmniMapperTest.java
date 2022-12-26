package org.bahmni.module.terminologyservices.api.mapper;

import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirContains;
import org.junit.Test;

import static org.junit.Assert.*;

public class FhirTerminologyServicesToBahmniMapperTest {

    @Test
    public void shouldMapFhirTerminologyContainsSetToBahmniResponse() {
        FhirContains fhirContains = new FhirContains("http://snomed.info/sct", "195967001", "Hyperreactive airway disease");
        BahmniSearchResponse bahmniSearchResponse = new FhirTerminologyServicesToBahmniMapper().map(fhirContains);
        assertNotNull(bahmniSearchResponse);
        assertEquals("Hyperreactive airway disease", bahmniSearchResponse.getConceptName());
        assertEquals("195967001", bahmniSearchResponse.getConceptUuid());
        assertEquals("Hyperreactive airway disease", bahmniSearchResponse.getMatchedName());

    }
}
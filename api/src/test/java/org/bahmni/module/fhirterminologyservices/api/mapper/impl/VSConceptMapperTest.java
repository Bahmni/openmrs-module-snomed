package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class VSConceptMapperTest {

    @Test
    public void shouldMapFhirValueSetToConcept() throws IOException, URISyntaxException {
        ValueSet valueSet = createMockFhirTerminologyResponseValueSet("mock/TSMockResponseForConcept.json");
        Concept concept = new VSConceptMapper().map(valueSet);
        assertNotNull(concept);
        assertEquals("Malaria (disorder)", concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals("Malaria", concept.getShortNameInLocale(Context.getLocale()).getName());
    }

    @Test
    public void shouldThrowExceptionIfConceptNotFound() throws IOException, URISyntaxException {
        ValueSet valueSet = createMockFhirTerminologyResponseValueSet("mock/TSMockResponseForInvalidConcept.json");
        assertThrows(TerminologyServicesException.class, () ->
                new VSConceptMapper().map(valueSet)
        );
    }

    ValueSet createMockFhirTerminologyResponseValueSet(String filePath) throws IOException, URISyntaxException {
        String mockString = getMockTerminologyString(filePath);
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        return parser.parseResource(ValueSet.class, mockString);
    }

    private String getMockTerminologyString(String filePath) throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(filePath).toURI());
        return Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
    }

}
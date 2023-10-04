package org.bahmni.module.fhirterminologyservices.api;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class TSConceptServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static final String CONCEPT_CLASS_NAME = "Procedure";
    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;

    @Mock
    TerminologyLookupService terminologyLookupService;

    @Mock
    ConceptService conceptService;

    @Mock
    private FhirConceptSourceService conceptSourceService;

    @InjectMocks
    TSConceptService tsConceptService;

    @Mock
    private UserContext userContext;

    private ValueSet valueSet;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Context.class);
        Locale defaultLocale = new Locale("en", "GB");
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        valueSet = getMockValueSet("mock/TsMockResponseForProceduresValueSet.json");
    }

    @Test
    public void shouldCreateProcedureConceptsAndBodySiteConcepts_whenValueSetIsGiven() throws Exception {
        ConceptSource conceptSource = getMockConceptSource();
        Concept concept = getMockConcept("Removal of suture from head", "Removal of suture from head", false);
        Concept bodySiteConcept = getBodySiteConcept();

        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(conceptSource));
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(concept);
        when(conceptService.saveConcept(any(Concept.class))).thenReturn(bodySiteConcept);

        List<Concept> procedures = tsConceptService.createOrUpdateConceptsForValueSet(valueSet, "Procedure", "N/A", null);

        assertEquals(valueSet.getExpansion().getTotal(), procedures.size());
    }

    @Test
    public void shouldCreateProcedureConceptsAndBodySiteConcepts_whenValueSetIsGivenAndContextRootConceptProvided() throws Exception {
        ConceptSource conceptSource = getMockConceptSource();
        Concept concept = getMockConcept("Removal of suture from head", "Removal of suture from head", false);
        Concept procedureConcept = getMockConcept("Procedure Orders", "Procedures", true);
        Concept bodySiteConcept = getBodySiteConcept();

        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(conceptSource));
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(concept);
        when(conceptService.saveConcept(any(Concept.class))).thenReturn(bodySiteConcept);
        when(conceptService.getConceptByName("Procedure Orders")).thenReturn(procedureConcept);

        int initialBodySiteCount = procedureConcept.getSetMembers().size();
        List<Concept> procedures = tsConceptService.createOrUpdateConceptsForValueSet(valueSet, "Procedure", "N/A", "Procedure Orders");

        assertEquals(valueSet.getExpansion().getTotal(), procedures.size());
        assertEquals(initialBodySiteCount + 1, procedureConcept.getSetMembers().size());
    }


    @Test(expected = APIException.class)
    public void shouldThrowException_whenContextRootConceptNotFoundForGivenValueSet() throws Exception {
        when(conceptService.getConceptByName("Procedure Orders")).thenReturn(null);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("Context Root Concept Procedure Orders not found");

        tsConceptService.createOrUpdateConceptsForValueSet(valueSet, CONCEPT_CLASS_NAME, "N/A", "Procedure Orders");
    }

    @Test
    public void shouldThrowException_whenContextRootConceptIsNotSetForGivenValueSet() throws Exception {
        Concept procedureConcept = getMockConcept("Procedure Orders", "Procedures", false);

        when(conceptService.getConceptByName("Procedure Orders")).thenReturn(procedureConcept);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("Context Root Concept Procedure Orders should be a set");

        tsConceptService.createOrUpdateConceptsForValueSet(valueSet, CONCEPT_CLASS_NAME, "N/A", "Procedure Orders");
    }

    @Test
    public void shouldAddNewProceduresToConvSetConcept_whenNewProcedureAddedToValueSet() throws Exception {
        ValueSet oldValueSet = getMockValueSet("mock/TsMockResponseForProceduresValueSet.json");
        ConceptSource conceptSource = getMockConceptSource();
        Concept concept = getMockConcept("Removal of suture from head", "Removal of suture from head", false);
        Concept bodySiteConcept = getBodySiteConcept();

        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(conceptSource));
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(concept);
        when(conceptService.saveConcept(any(Concept.class))).thenReturn(bodySiteConcept);

        List<Concept> procedures = tsConceptService.createOrUpdateConceptsForValueSet(oldValueSet, "Procedure", "N/A", null);

        assertEquals(oldValueSet.getExpansion().getTotal(), procedures.size());
        assertEquals(oldValueSet.getExpansion().getTotal(), bodySiteConcept.getSetMembers().size());

        ValueSet newValueSet = getMockValueSet("mock/TsMockResponseForProceduresValueSetUpdated.json");
        procedures = tsConceptService.createOrUpdateConceptsForValueSet(newValueSet, "Procedure", "N/A", null);

        assertNotEquals(oldValueSet.getExpansion().getTotal(), newValueSet.getExpansion().getTotal());
        assertEquals(newValueSet.getExpansion().getTotal(), procedures.size());
        assertEquals(newValueSet.getExpansion().getTotal(), bodySiteConcept.getSetMembers().size());
    }

    private Concept getMockConcept(String longName, String conceptShortName, boolean isSet) {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName(longName, Context.getLocale());
        ConceptName shortName = new ConceptName(conceptShortName, Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setShortName(shortName);

        concept.setSet(isSet);
        return concept;
    }

    private ConceptSource getMockConceptSource() {
        ConceptSource conceptSource = new ConceptSource();
        conceptSource.setName("Dummy CS");
        conceptSource.setHl7Code("Dummy code");
        return conceptSource;
    }

    private Concept getBodySiteConcept() {
        Concept bodySiteConcept = new Concept();
        bodySiteConcept.setSet(true);
        bodySiteConcept.setFullySpecifiedName(new ConceptName("bahmni-procedures-head", Context.getLocale()));
        bodySiteConcept.setShortName(new ConceptName("Head", Context.getLocale()));
        return bodySiteConcept;
    }

    private ValueSet getMockValueSet(String filePath) throws Exception {
        Path path = Paths.get(getClass().getClassLoader()
                .getResource(filePath).toURI());
        String mockString = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
        return FhirContext.forR4().newJsonParser().parseResource(ValueSet.class, mockString);
    }
}

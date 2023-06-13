package org.bahmni.module.fhirterminologyservices.api;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.bahmni.module.fhirterminologyservices.api.TSConceptUuidResolver.DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class TSConceptUuidResolverTest {
    public static final String CONCEPT_CLASS_DIAGNOSIS = "Diagnosis";
    public static final String CONCEPT_DATATYPE_NA = "N/A";
    final String GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID = "bahmni.diagnosisSetForNewDiagnosisConcepts";
    final String UNCLASSIFIED_CONCEPT_SET_UUID = "unclassified-concept-set-uuid";
    final String MALARIA_CONCEPT_UUID = "malaria-uuid";
    final String MOCK_CONCEPT_SYSTEM = "http://dummyhost.com/systemcode";
    final String MOCK_CONCEPT_SOURCE_CODE = "CS dummy code";
    private final String TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER = "/";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;
    @Mock
    ConceptService conceptService;
    @Mock
    TerminologyLookupService terminologyLookupService;
    @InjectMocks
    TSConceptUuidResolver tsConceptUuidResolver;
    @Mock
    EmrApiProperties emrApiProperties;
    @Mock
    private FhirConceptSourceService conceptSourceService;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        Locale defaultLocale = new Locale("en", "GB");
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    // test for condition
    @Test
    public void shouldUpdateConceptUuidAndSaveNewDiagnosisAnswerConceptAndAddToUnclassifiedSetWhenConceptSourceAndReferenceCodeProvidedForCondition() {
        Concept newDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        org.openmrs.module.emrapi.conditionslist.contract.Concept concept = getBahmniConditionConcept(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        tsConceptUuidResolver.resolveConceptUuid(concept, CONCEPT_CLASS_DIAGNOSIS, unclassifiedConceptSet, CONCEPT_DATATYPE_NA);

        assertEquals(initialDiagnosisSetMembersCount + 1, unclassifiedConceptSet.getSetMembers().size());
        assertEquals(MALARIA_CONCEPT_UUID, concept.getUuid());
    }

    @Test
    public void shouldUpdateConceptUuidAndNotCreateDiagnosisAnswerConceptWhenExistingConceptSourceAndCodeProvidedForCondition() {
        Concept existingDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        org.openmrs.module.emrapi.conditionslist.contract.Concept concept = getBahmniConditionConcept(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        List<Concept> mockConceptList = getMockConceptList(true);
        when(conceptService.getConceptsByMapping(anyString(), anyString())).thenReturn(mockConceptList);
        when(conceptService.getConceptMapTypeByUuid(anyString())).thenReturn(getMockConceptMapType("sameAs"));
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        tsConceptUuidResolver.resolveConceptUuid(concept, CONCEPT_CLASS_DIAGNOSIS, unclassifiedConceptSet, CONCEPT_DATATYPE_NA);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
        assertEquals(MALARIA_CONCEPT_UUID, concept.getUuid());
    }

    @Test
    public void shouldNotUpdateConceptUuidAndNotCreateDiagnosisAnswerConceptWhenReferenceCodeNotProvidedForCondition() {
        Concept existingDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        org.openmrs.module.emrapi.conditionslist.contract.Concept concept = getBahmniConditionConcept(MOCK_CONCEPT_SYSTEM, false);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptService.getConceptByMapping(anyString(), anyString())).thenReturn(existingDiagnosisConcept);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        tsConceptUuidResolver.resolveConceptUuid(concept, CONCEPT_CLASS_DIAGNOSIS, unclassifiedConceptSet, CONCEPT_DATATYPE_NA);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
        assertEquals("coded-answer-uuid", concept.getUuid());
    }

    @Test
    public void shouldUpdateConceptUuidAndSaveNewDiagnosisAnswerConceptAndAddToUnclassifiedSetWhenConceptSourceAndReferenceCodeProvidedForDiagnosis() {
        Concept newDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        EncounterTransaction.Concept concept = getBahmniEncounterTransactionConcept(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        tsConceptUuidResolver.resolveConceptUuid(concept, CONCEPT_CLASS_DIAGNOSIS, unclassifiedConceptSet, CONCEPT_DATATYPE_NA);

        assertEquals(initialDiagnosisSetMembersCount + 1, unclassifiedConceptSet.getSetMembers().size());
        assertEquals(MALARIA_CONCEPT_UUID, concept.getUuid());
    }

    @Test
    public void shouldUpdateConceptUuidAndNotCreateDiagnosisAnswerConceptWhenExistingConceptSourceAndCodeProvidedForDiagnosis() {
        Concept existingDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        EncounterTransaction.Concept concept = getBahmniEncounterTransactionConcept(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        List<Concept> mockConceptList = getMockConceptList(true);
        when(conceptService.getConceptsByMapping(anyString(), anyString())).thenReturn(mockConceptList);
        when(conceptService.getConceptMapTypeByUuid(anyString())).thenReturn(getMockConceptMapType("sameAs"));
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        tsConceptUuidResolver.resolveConceptUuid(concept, CONCEPT_CLASS_DIAGNOSIS, unclassifiedConceptSet, CONCEPT_DATATYPE_NA);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
        assertEquals(MALARIA_CONCEPT_UUID, concept.getUuid());
    }

    @Test
    public void shouldNotUpdateConceptUuidAndNotCreateDiagnosisAnswerConceptWhenReferenceCodeNotProvidedForDiagnosis() {
        Concept existingDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        EncounterTransaction.Concept concept = getBahmniEncounterTransactionConcept(MOCK_CONCEPT_SYSTEM, false);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptService.getConceptByMapping(anyString(), anyString())).thenReturn(existingDiagnosisConcept);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        tsConceptUuidResolver.resolveConceptUuid(concept, CONCEPT_CLASS_DIAGNOSIS, unclassifiedConceptSet, CONCEPT_DATATYPE_NA);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
        assertEquals("coded-answer-uuid", concept.getUuid());
    }

    @Test
    public void shouldThrowExceptionWhenConceptNotFoundForGivenConceptId() {
        String mockConceptUuid = "mock-uuid";
        when(conceptService.getConceptByUuid(mockConceptUuid)).thenReturn(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Concept Set with uuid mock-uuid not found");
        tsConceptUuidResolver.getConceptSetByUuid(mockConceptUuid);

    }

    @Test
    public void shouldThrowExceptionWhenDefaultDiagnosisConceptSetNotFound() {
        when(emrApiProperties.getDiagnosisSets()).thenReturn(new ArrayList<>());
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Concept Set " + DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT + " not found");
        tsConceptUuidResolver.getDefaultDiagnosisConceptSet();

    }

    @Test
    public void shouldReturnConceptWithSameAsMapTypeWhenConceptListContainsSameAsMapTypeAndReferenceTermCodeAndConceptSourceIsPassed() {
        List<Concept> mockConcetplist = getMockConceptList(true);
        when(conceptService.getConceptsByMapping(anyString(), anyString())).thenReturn(mockConcetplist);
        when(conceptService.getConceptMapTypeByUuid(anyString())).thenReturn(getMockConceptMapType("sameAs"));
        Concept concept = tsConceptUuidResolver.getConceptByReferenceTermCodeAndConceptSource("dummyConceptCode", MOCK_CONCEPT_SYSTEM);
        assertNotNull(concept);
    }

    @Test
    public void shouldReturnNullWhenConceptListDoesNotContainSameAsMapTypeAndReferenceTermCodeAndConceptSourceIsPassed() {
        List<Concept> mockConcetplist = getMockConceptList(false);
        when(conceptService.getConceptsByMapping(anyString(), anyString())).thenReturn(mockConcetplist);
        when(conceptService.getConceptMapTypeByUuid(anyString())).thenReturn(getMockConceptMapType("sameAs"));
        Concept concept = tsConceptUuidResolver.getConceptByReferenceTermCodeAndConceptSource("dummyConceptCode", MOCK_CONCEPT_SYSTEM);
        assertNull(concept);
    }

    // private methods for BahmniEncounterTransaction
    private EncounterTransaction.Concept getBahmniEncounterTransactionConcept(String conceptSystem, boolean isCodedAnswerFromTerminologyServer) {
        return createBahmniEncounterTransactionConcept(conceptSystem, isCodedAnswerFromTerminologyServer);
    }

    private EncounterTransaction.Concept createBahmniEncounterTransactionConcept(String conceptSystem, boolean isCodedAnswerFromTerminologyServer) {
        String codedAnswerUuid = null;
        if (isCodedAnswerFromTerminologyServer)
            codedAnswerUuid = conceptSystem + TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER + "dummyConceptCode";
        else
            codedAnswerUuid = "coded-answer-uuid";
        return new EncounterTransaction.Concept(codedAnswerUuid);
    }


    // private methods for conidition

    private org.openmrs.module.emrapi.conditionslist.contract.Concept getBahmniConditionConcept(String conceptSystem, boolean isCodedAnswerFromTerminologyServer) {
        return createBahmniConditionConcept(conceptSystem, isCodedAnswerFromTerminologyServer);
    }

    private org.openmrs.module.emrapi.conditionslist.contract.Concept createBahmniConditionConcept(String conceptSystem, boolean isCodedAnswerFromTerminologyServer) {
        String codedAnswerUuid = null;
        String conceptName = "dummy-concept";
        if (isCodedAnswerFromTerminologyServer)
            codedAnswerUuid = conceptSystem + TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER + "dummyConceptCode";
        else
            codedAnswerUuid = "coded-answer-uuid";
        return new org.openmrs.module.emrapi.conditionslist.contract.Concept(codedAnswerUuid, conceptName);
    }

    // common private methods for diagnosis and condition
    private ConceptSource getMockedConceptSources(String name, String code) {
        ConceptSource conceptSource = new ConceptSource();
        conceptSource.setName(name);
        conceptSource.setHl7Code(code);
        return conceptSource;
    }

    private Concept getDiagnosisConcept() {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName("Malaria (disorder)", Context.getLocale());
        ConceptName shortName = new ConceptName("Malaria", Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setShortName(shortName);
        concept.setUuid(MALARIA_CONCEPT_UUID);

        return concept;
    }

    private Concept getUnclassifiedConceptSet() {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName("Unclassified", Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setSet(true);
        return concept;
    }

    private List<Concept> getMockConceptList(boolean isSameAs) {
        String mockConceptMapType = "";
        if (isSameAs) {
            mockConceptMapType = "sameAs";
        } else {
            mockConceptMapType = "narrowerThan";
        }
        String mockReferenceTermCode = "dummyConceptCode";
        ConceptSource conceptSource = getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE);

        String mockConceptName = "dummyConcept";
        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm(conceptSource, mockReferenceTermCode, mockConceptName);
        ConceptMapType conceptMapType = getMockConceptMapType(mockConceptMapType);
        List<Concept> conceptList = new ArrayList<>();
        Concept concept1 = getDiagnosisConcept();
        ConceptMap conceptMap = new ConceptMap(conceptReferenceTerm, conceptMapType);
        concept1.addConceptMapping(conceptMap);
        conceptList.add(concept1);
        return conceptList;
    }

    private ConceptMapType getMockConceptMapType(String name) {
        ConceptMapType conceptMapType = new ConceptMapType();
        conceptMapType.setName(name);
        return conceptMapType;
    }


}
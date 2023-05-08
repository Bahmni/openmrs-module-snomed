package org.bahmni.module.fhirterminologyservices.api;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class BahmniObservationAnswerConceptSaveCommandTest {
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
    BahmniObservationAnswerConceptSaveCommand bahmniObservationAnswerConceptSaveCommand;
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

    @Test
    public void shouldSaveNewAnswerConceptAndAddToMockConceptSetWhenConceptSourceAndReferenceCodeProvided() {
        String mockConceptSetUuid = "mock-concept-set-uuid";
        Concept newDiagnosisConcept = getMockAnswerConcept();
        Concept mockConceptSet = getMockConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(mockConceptSetUuid);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(any())).thenReturn(mockConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        int initialAnswersSize = mockConceptSet.getAnswers().size();

        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);
        Object value = bahmniEncounterTransaction.getObservations().stream().findFirst().get().getValue();
        LinkedHashMap observationValue = (LinkedHashMap) value;
        assertEquals(initialAnswersSize + 1, mockConceptSet.getAnswers().size());
        verify(conceptService, times(2)).saveConcept(any(Concept.class));
        assertEquals(MALARIA_CONCEPT_UUID, observationValue.get("uuid"));
    }

    @Test
    public void shouldNotCreateNewAnswerConceptWhenExistingConceptProvided() {
        String mockConceptSetUuid = "mock-concept-set-uuid";
        Concept newDiagnosisConcept = getMockAnswerConcept();
        Concept mockConceptSet = getMockConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, false);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(mockConceptSetUuid);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(any())).thenReturn(mockConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);
        int initialDiagnosisSetMembersCount = mockConceptSet.getAnswers().size();
        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);
        assertEquals(initialDiagnosisSetMembersCount, mockConceptSet.getAnswers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
    }

    @Test
    public void shouldNotCreateAnswerConceptAndAddToMockConceptSetWhenExistingConceptSourceAndCodeProvided() {
        String mockConceptSetUuid = "mock-concept-set-uuid";
        Concept existingDiagnosisConcept = getMockAnswerConcept();
        Concept mockConceptSet = getMockConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(mockConceptSetUuid);
        when(conceptService.getConceptByMapping(anyString(), anyString())).thenReturn(existingDiagnosisConcept);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(any())).thenReturn(mockConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = mockConceptSet.getAnswers().size();

        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);
        Object value = bahmniEncounterTransaction.getObservations().stream().findFirst().get().getValue();
        LinkedHashMap observationValue = (LinkedHashMap) value;

        assertEquals(initialDiagnosisSetMembersCount + 1, mockConceptSet.getAnswers().size());
        verify(conceptService, times(1)).saveConcept(any(Concept.class));
        assertEquals(MALARIA_CONCEPT_UUID, observationValue.get("uuid"));
    }

    @Test
    public void shouldNotCreateAnswerConceptAndNotAddToMockConceptSetWhenExistingConceptSourceAndCodeProvidedAndAnswerConceptAlreadyPresent() {
        String mockConceptSetUuid = "mock-concept-set-uuid";
        Concept existingDiagnosisConcept = getMockAnswerConcept();
        Concept mockConceptSet = getMockConceptSet();
        addNewAnswerToConceptSet(getMockAnswerConcept(), mockConceptSet);
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(mockConceptSetUuid);
        when(conceptService.getConceptByMapping(anyString(), anyString())).thenReturn(existingDiagnosisConcept);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(any())).thenReturn(mockConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = mockConceptSet.getAnswers().size();

        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);
        Object value = bahmniEncounterTransaction.getObservations().stream().findFirst().get().getValue();
        LinkedHashMap observationValue = (LinkedHashMap) value;

        assertEquals(initialDiagnosisSetMembersCount, mockConceptSet.getAnswers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
        assertEquals(MALARIA_CONCEPT_UUID, observationValue.get("uuid"));
    }

    @Test
    public void shouldThrowExceptionWhenConceptSourceNotFound() {
        Concept newDiagnosisConcept = getMockAnswerConcept();
        Concept mockConceptSet = getMockConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction("Some Invalid System", true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.empty());
        when(conceptService.getConceptByUuid(any())).thenReturn(mockConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("Concept Source Some Invalid System not found");

        int initialDiagnosisSetMembersCount = mockConceptSet.getSetMembers().size();

        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount, mockConceptSet.getSetMembers().size());
    }

    @Test
    public void shouldThrowExceptionWhenTerminologyServerUnavailable() {
        Concept mockConceptSet = getMockConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(any())).thenReturn(mockConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenAnswer(invocation -> {
            throw new RuntimeException("Error fetching concept details from terminology server");
        });

        int initialDiagnosisSetMembersCount = mockConceptSet.getSetMembers().size();

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Error fetching concept details from terminology server");

        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount, mockConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
    }

    private BahmniEncounterTransaction getBahmniEncounterTransaction(String conceptSystem, boolean isCodedAnswerFromTermimologyServer) {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setObservations(createBahmniObservation(conceptSystem, isCodedAnswerFromTermimologyServer));
        return bahmniEncounterTransaction;
    }

    private List<BahmniObservation> createBahmniObservation(String conceptSystem, boolean isCodedAnswerFromTermimologyServer) {
        String codedAnswerUuid = null;
        if (isCodedAnswerFromTermimologyServer) {
            codedAnswerUuid = conceptSystem + TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER + "61462000";
        } else {
            codedAnswerUuid = "coded-answer-uuid";
        }
        String mockConceptSetUuid = "mock-concept-uuid";
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setConcept(new EncounterTransaction.Concept(mockConceptSetUuid));
        bahmniObservation.setVoided(false);
        LinkedHashMap observationValue = new LinkedHashMap<>();
        LinkedHashMap<String, String> codedAnswer = new LinkedHashMap<>();
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept(codedAnswerUuid);
        codedAnswer.put("uuid", concept.getUuid());
        observationValue.put("codedAnswer", codedAnswer);
        bahmniObservation.setValue(observationValue);
        bahmniObservation.setEncounterUuid("enc-uuid-1");
        return Arrays.asList(bahmniObservation);
    }

    private ConceptSource getMockedConceptSources(String name, String code) {
        ConceptSource conceptSource = new ConceptSource();
        conceptSource.setName(name);
        conceptSource.setHl7Code(code);
        return conceptSource;
    }

    private Concept getMockAnswerConcept() {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName("Malaria (disorder)", Context.getLocale());
        ConceptName shortName = new ConceptName("Malaria", Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setShortName(shortName);
        concept.setUuid(MALARIA_CONCEPT_UUID);

        return concept;
    }

    private Concept getMockConceptSet() {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName("Mock Concept", Context.getLocale());
        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setSet(true);
        return concept;
    }

    private Concept addNewAnswerToConceptSet(Concept concept, Concept conceptSet) {
        ConceptAnswer newConceptAnswer = new ConceptAnswer();
        newConceptAnswer.setConcept(conceptSet);
        newConceptAnswer.setAnswerConcept(concept);
        conceptSet.addAnswer(newConceptAnswer);
        return conceptSet;
    }
}
package org.bahmni.module.fhirterminologyservices.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TSConceptUuidResolver {
    public static final String DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT = "Unclassified";
    public static final String GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID = "bahmni.diagnosisSetForNewDiagnosisConcepts";
    public static final String CONCEPT_CLASS_FINDINGS = "Finding";
    private static final String TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER = "/";
    private static Logger logger = LogManager.getLogger(org.bahmni.module.fhirterminologyservices.api.TSConceptUuidResolver.class);

    public AdministrationService adminService;
    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private TerminologyLookupService terminologyLookupService;
    private FhirConceptSourceService conceptSourceService;

    @Autowired
    public TSConceptUuidResolver(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService,
                                 EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService,
                                 FhirConceptSourceService conceptSourceService) {
        this.adminService = administrationService;
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.terminologyLookupService = terminologyLookupService;
        this.conceptSourceService = conceptSourceService;
    }


    protected void resolveConceptUuid(org.openmrs.module.emrapi.conditionslist.contract.Concept codedAnswer, String conceptClassName, Concept conceptSet, String conceptDatatypeName) {
        String codedAnswerUuidWithSystem = codedAnswer.getUuid();
        String updatedConceptUuid = getUpdatedConceptUuid(codedAnswerUuidWithSystem, conceptClassName, conceptSet, conceptDatatypeName);
        codedAnswer.setUuid(updatedConceptUuid);
    }


    public void resolveConceptUuid(EncounterTransaction.Concept codedAnswer, String conceptClassName, Concept conceptSet, String conceptDatatypeName) {
        String codedAnswerUuidWithSystem = codedAnswer.getUuid();
        String updatedConceptUuid = getUpdatedConceptUuid(codedAnswerUuidWithSystem, conceptClassName, conceptSet, conceptDatatypeName);
        codedAnswer.setUuid(updatedConceptUuid);
    }

    public Concept getConcept(String conceptSystem, String conceptReferenceTermCode, String conceptClassName, Concept parentConceptSet, String conceptDatatypeName) {
        return saveOrUpdateConcept(conceptClassName, parentConceptSet, conceptDatatypeName, conceptSystem, conceptReferenceTermCode);
    }

    private String getUpdatedConceptUuid(String codedAnswerUuidWithSystem, String conceptClassName, Concept conceptSet, String conceptDatatypeName) {
        int conceptCodeIndex = codedAnswerUuidWithSystem.lastIndexOf(TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER);
        boolean isConceptFromTerminologyServer = conceptCodeIndex > -1 ? true : false;
        if (!isConceptFromTerminologyServer) {
            return codedAnswerUuidWithSystem;
        }
        String conceptSystem = codedAnswerUuidWithSystem.substring(0, conceptCodeIndex);
        String conceptReferenceTermCode = codedAnswerUuidWithSystem.substring(conceptCodeIndex + 1);
        Concept concept = saveOrUpdateConcept(conceptClassName, conceptSet, conceptDatatypeName, conceptSystem, conceptReferenceTermCode);
        return concept.getUuid();
    }

    private Concept saveOrUpdateConcept(String conceptClassName, Concept conceptSet, String conceptDatatypeName, String conceptSystem, String conceptReferenceTermCode) {
        Optional<ConceptSource> conceptSourceByUrl = conceptSourceService.getConceptSourceByUrl(conceptSystem);
        ConceptSource conceptSource = conceptSourceByUrl.isPresent() ? conceptSourceByUrl.get() : null;
        if (conceptSource == null) {
            logger.error("Concept Source " + conceptSystem + " not found");
            throw new APIException("Concept Source " + conceptSystem + " not found");
        }
        Concept existingAnswerConcept = conceptService.getConceptByMapping(conceptReferenceTermCode, conceptSource.getName());
        if (existingAnswerConcept == null) {
            return getNewAnswerConcept(conceptClassName, conceptSet, conceptDatatypeName, conceptReferenceTermCode, conceptSource);
        }
        updateExistingAnswerConceptInCurrentLocale(conceptClassName, conceptSet, conceptDatatypeName, conceptReferenceTermCode, existingAnswerConcept);
        return existingAnswerConcept;
    }

    private Concept createNewConcept(String conceptReferenceTermCode, ConceptSource conceptSource, String conceptClassName, String conceptDatatypeName) {
        Concept concept = getConcept(conceptReferenceTermCode, conceptClassName, conceptDatatypeName);
        addConceptMap(concept, conceptSource, conceptReferenceTermCode);
        conceptService.saveConcept(concept);
        return concept;
    }

    private void updateExistingConcept(Concept existingAnswerConcept, String conceptReferenceTermCode, String conceptClassName, String conceptDatatypeName) {
        Concept conceptInUserLocale = getConcept(conceptReferenceTermCode, conceptClassName, conceptDatatypeName);
        conceptInUserLocale.getNames(Context.getLocale()).stream().forEach(conceptName -> existingAnswerConcept.addName(conceptName));
        conceptService.saveConcept(existingAnswerConcept);
    }

    private Concept getConcept(String referenceCode, String conceptClassName, String conceptDatatypeName) {
        Concept concept = terminologyLookupService.getConcept(referenceCode, Context.getLocale().getLanguage());
        ConceptClass conceptClass = conceptService.getConceptClassByName(conceptClassName);
        concept.setConceptClass(conceptClass);

        ConceptDatatype conceptDataType = conceptService.getConceptDatatypeByName(conceptDatatypeName);
        concept.setDatatype(conceptDataType);

        return concept;
    }

    private void addConceptMap(Concept concept, ConceptSource conceptSource, String conceptReferenceTermCode) {
        ConceptMap conceptMap = getConceptMap(concept.getName().getName(), conceptReferenceTermCode, conceptSource);
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        conceptMappings.add(conceptMap);
        concept.setConceptMappings(conceptMappings);
    }

    private ConceptMap getConceptMap(String name, String conceptReferenceTermCode, ConceptSource conceptSource) {
        ConceptReferenceTerm conceptReferenceTerm = conceptService.getConceptReferenceTermByCode(conceptReferenceTermCode, conceptSource);
        if(conceptReferenceTerm == null) {
            conceptReferenceTerm =  new ConceptReferenceTerm(conceptSource, conceptReferenceTermCode, name);
        }
        ConceptMapType sameAsConceptMapType = conceptService.getConceptMapTypeByUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
        ConceptMap conceptMap = new ConceptMap(conceptReferenceTerm, sameAsConceptMapType);
        return conceptMap;
    }

    protected void addNewMemberConceptToConceptSet(Concept memberConcept, Concept conceptSet) {
        if (conceptSet == null)
            return;
        List<Concept> setMembers = conceptSet.getSetMembers();
        Optional<Concept> optionalConcept = setMembers.stream().filter(setMember -> setMember.getUuid().equals(memberConcept.getUuid())).findFirst();
        if (!optionalConcept.isPresent()) {
            conceptSet.addSetMember(memberConcept);
            conceptService.saveConcept(conceptSet);
        }
    }

    private void addNewAnswerToConceptSet(Concept concept, Concept conceptSet) {
        ConceptAnswer newConceptAnswer = new ConceptAnswer();
        newConceptAnswer.setConcept(conceptSet);
        newConceptAnswer.setAnswerConcept(concept);
        conceptSet.addAnswer(newConceptAnswer);
        conceptService.saveConcept(conceptSet);
    }

    private boolean checkIfConceptAnswerExistsForConceptSet(Concept conceptSet, Integer conceptAnswerUuid) {
        Collection<ConceptAnswer> conceptAnswers = conceptSet.getAnswers();
        Optional<ConceptAnswer> conceptAnswer = conceptAnswers.stream().filter(c -> Objects.equals(c.getAnswerConcept().getConceptId(), conceptAnswerUuid)).findFirst();
        return conceptAnswer.isPresent();
    }

    public Concept getDefaultDiagnosisConceptSet() {
        Concept diagnosisConceptSet = null;
        Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
        Optional<Concept> optionalConcept = diagnosisSets.stream().filter(c -> c.getName().getName().equals(DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT) && !c.getRetired()).findFirst();
        if (optionalConcept.isPresent()) {
            diagnosisConceptSet = optionalConcept.get();
        }

        if (diagnosisConceptSet == null) {
            logger.error("Concept Set " + DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT + " not found");
            throw new APIException("Concept Set " + DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT + " not found");
        }
        return diagnosisConceptSet;
    }

    public Concept getConceptSetByUuid(String conceptSetUuid) {
        Concept conceptSet = conceptService.getConceptByUuid(conceptSetUuid);
        if (conceptSet == null) {
            logger.error("Concept Set with uuid " + conceptSetUuid + " not found");
            throw new APIException("Concept Set with uuid " + conceptSetUuid + " not found");
        }
        return conceptSet;
    }

    private Concept getNewAnswerConcept(String conceptClassName, Concept conceptSet, String conceptDatatypeName, String conceptReferenceTermCode, ConceptSource conceptSource) {
        Concept newAnswerConcept = createNewConcept(conceptReferenceTermCode, conceptSource, conceptClassName, conceptDatatypeName);
        if (CONCEPT_CLASS_FINDINGS.equals(conceptClassName) && !checkIfConceptAnswerExistsForConceptSet(conceptSet, newAnswerConcept.getConceptId())) {
            addNewAnswerToConceptSet(newAnswerConcept, conceptSet);
        } else {
            addNewMemberConceptToConceptSet(newAnswerConcept, conceptSet);
        }
        return newAnswerConcept;
    }

    private void updateExistingAnswerConceptInCurrentLocale(String conceptClassName, Concept conceptSet, String conceptDatatypeName, String conceptReferenceTermCode, Concept existingAnswerConcept) {
        ConceptName answerConceptNameInUserLocale = existingAnswerConcept.getFullySpecifiedName(Context.getLocale());
        if (answerConceptNameInUserLocale == null)
            updateExistingConcept(existingAnswerConcept, conceptReferenceTermCode, conceptClassName, conceptDatatypeName);
        if (CONCEPT_CLASS_FINDINGS.equals(conceptClassName) && !checkIfConceptAnswerExistsForConceptSet(conceptSet, existingAnswerConcept.getConceptId())) {
            addNewAnswerToConceptSet(existingAnswerConcept, conceptSet);
        }
    }

    public Concept getParentConceptForValueSet(String parentConceptName, String conceptShortName, String conceptClassName, String conceptDatatypeName) {
        Concept parentConcept = conceptService.getConceptByName(parentConceptName);
        if (parentConcept == null) {
            Concept concept = new Concept();
            ConceptName fullySpecifiedName = new ConceptName(parentConceptName, Context.getLocale());
            concept.setFullySpecifiedName(fullySpecifiedName);
            ConceptName shortName = new ConceptName(conceptShortName, Context.getLocale());
            concept.setShortName(shortName);
            concept.setSet(true);
            ConceptClass conceptClass = conceptService.getConceptClassByName(conceptClassName);
            concept.setConceptClass(conceptClass);
            ConceptDatatype conceptDataType = conceptService.getConceptDatatypeByName(conceptDatatypeName);
            concept.setDatatype(conceptDataType);
            return conceptService.saveConcept(concept);
        }
        return parentConcept;
    }

    public Concept getConcept(String conceptName) {
        return conceptService.getConceptByName(conceptName);
    }

}
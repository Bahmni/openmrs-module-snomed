package org.bahmni.module.fhirterminologyservices.api;


import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BahmniDiagnosisAnswerConceptSaveCommand extends TSConceptUuidResolver {
    public static final String CONCEPT_CLASS_DIAGNOSIS = "Diagnosis";
    public static final String CONCEPT_DATATYPE_NA = "N/A";


    @Autowired
    public BahmniDiagnosisAnswerConceptSaveCommand(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService, EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService, FhirConceptSourceService conceptSourceService) {
        super(administrationService, conceptService, emrApiProperties, terminologyLookupService, conceptSourceService);
    }

    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = bahmniEncounterTransaction.getBahmniDiagnoses();
        bahmniDiagnoses.stream().forEach(this::updateDiagnosisAnswerConceptUuid);
        return bahmniEncounterTransaction;
    }



    private void updateDiagnosisAnswerConceptUuid(BahmniDiagnosisRequest bahmniDiagnosis) {
        String codedConceptUuid = adminService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID);
        EncounterTransaction.Concept codedAnswer = bahmniDiagnosis.getCodedAnswer();
        if (!checkIfCodedAnswerHasUuid(codedAnswer)) {
            return;
        }
        Concept conceptSet = getConceptSetByCodedConceptUuid(codedConceptUuid);
        if (conceptSet != null) {
            resolveConceptUuid(codedAnswer, CONCEPT_CLASS_DIAGNOSIS, conceptSet, CONCEPT_DATATYPE_NA);
        }
    }

    private boolean checkIfCodedAnswerHasUuid(EncounterTransaction.Concept codedAnswer) {
        return codedAnswer != null && codedAnswer.getUuid() != null;
    }
    private Concept getConceptSetByCodedConceptUuid(String codedConceptUuid) {
        if (StringUtils.isNotBlank(codedConceptUuid)) {
            return getConceptSetByUuid(codedConceptUuid);
        } else {
            return getDefaultDiagnosisConceptSet();
        }
    }

}

package org.bahmni.module.fhirterminologyservices.api.impl;

import org.apache.commons.lang.StringUtils;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.bahmni.module.fhirterminologyservices.api.TSConceptUuidResolver;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
public class ConditionConceptSaveImpl extends TSConceptUuidResolver implements ConditionConceptSaveService {

    public static final String CONCEPT_CLASS_DIAGNOSIS = "Diagnosis";
    public static final String CONCEPT_DATATYPE_NA = "N/A";


    @Autowired
    public ConditionConceptSaveImpl(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService, EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService, FhirConceptSourceService conceptSourceService) {
        super(administrationService, conceptService, emrApiProperties, terminologyLookupService, conceptSourceService);
    }

    @Override
    public org.openmrs.module.emrapi.conditionslist.contract.Condition update(org.openmrs.module.emrapi.conditionslist.contract.Condition condition) {
        updateConditionAnswerConceptUuid(condition);
        return condition;
    }

    private void updateConditionAnswerConceptUuid(org.openmrs.module.emrapi.conditionslist.contract.Condition condition) {
        String codedConceptUuid = adminService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID);
        org.openmrs.module.emrapi.conditionslist.contract.Concept codedAnswer = condition.getConcept();
        if (codedAnswer != null && codedAnswer.getUuid() != null) {
            Concept conceptSet = null;
            if (StringUtils.isNotBlank(codedConceptUuid)) {
                conceptSet = getConceptSetByUuid(codedConceptUuid);
            } else {
                conceptSet = getDefaultDiagnosisConceptSet();
            }
            resolveConceptUuid(codedAnswer, CONCEPT_CLASS_DIAGNOSIS, conceptSet, CONCEPT_DATATYPE_NA);
        }
    }
}

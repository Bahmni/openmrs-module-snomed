package org.bahmni.module.fhirterminologyservices.api;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;

@Component
public class BahmniObservationAnswerConceptSaveCommand extends TSConceptUuidResolver {
    public static final String CONCEPT_CLASS_FINDINGS = "Finding";
    public static final String CONCEPT_DATATYPE_NA = "N/A";

    @Autowired
    public BahmniObservationAnswerConceptSaveCommand(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService, EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService, FhirConceptSourceService conceptSourceService) {
        super(administrationService, conceptService, emrApiProperties, terminologyLookupService, conceptSourceService);
    }



    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        Collection<BahmniObservation> bahmniObservations = bahmniEncounterTransaction.getObservations();
        bahmniObservations.stream().forEach(this::updateObservationAnswerConceptUuid);
        return bahmniEncounterTransaction;
    }

    private void updateObservationAnswerConceptUuid(BahmniObservation observation) {
        if (observation.getGroupMembers().size() > 0) {
            observation.getGroupMembers().stream().forEach(this::updateObservationAnswerConceptUuid);
        } else {
            Object value = observation.getValue();
            String codedConceptUuid = observation.getConcept().getUuid();
            if (StringUtils.isNotBlank(codedConceptUuid)) {
                Concept conceptSet;
                conceptSet = getConceptSetByUuid(codedConceptUuid);
                if (conceptSet != null) {
                    if (value instanceof LinkedHashMap) {
                        LinkedHashMap observationValue = (LinkedHashMap) value;
                        LinkedHashMap<String, String> codedAnswer = (LinkedHashMap) (observationValue).get("codedAnswer");
                        if (codedAnswer != null && StringUtils.isNotBlank(codedAnswer.get("uuid"))) {
                            EncounterTransaction.Concept concept = new EncounterTransaction.Concept(codedAnswer.get("uuid"));
                            resolveConceptUuid(concept, CONCEPT_CLASS_FINDINGS, conceptSet, CONCEPT_DATATYPE_NA);
                            codedAnswer.put("uuid", concept.getUuid());
                            observation.setValue(observationValue.put("codedAnswer", codedAnswer));
                        }
                    }
                }
            }
        }
    }

}

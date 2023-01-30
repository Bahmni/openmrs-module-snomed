package org.bahmni.module.fhirterminologyservices.api.mapper;


import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.stereotype.Component;

@Component
public class FhirValueSetToDiagnosisMapper {
    public static final String CONCEPT_NAME = "conceptName";
    public static final String CONCEPT_UUID = "conceptUuid";
    public static final String MATCHED_NAME = "matchedName";
    public  SimpleObject mapFhirResponseValueSetToSimpleObject(ValueSet.ValueSetExpansionContainsComponent matchedItem) {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add(CONCEPT_NAME, matchedItem.getDisplay());
        diagnosisObject.add(CONCEPT_UUID, matchedItem.getCode());
        diagnosisObject.add(MATCHED_NAME,matchedItem.getDisplay());
        return diagnosisObject;
    }
}

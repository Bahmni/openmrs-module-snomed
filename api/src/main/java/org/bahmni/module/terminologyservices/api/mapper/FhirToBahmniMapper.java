package org.bahmni.module.terminologyservices.api.mapper;


import org.bahmni.module.terminologyservices.api.BahmniConstants;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.stereotype.Component;

@Component
public class FhirToBahmniMapper {
    public  SimpleObject mapFhirResponseValueSetToSimpleObject(ValueSet.ValueSetExpansionContainsComponent matchedItem) {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add(BahmniConstants.CONCEPT_NAME, matchedItem.getDisplay());
        diagnosisObject.add(BahmniConstants.CONCEPT_UUID, matchedItem.getCode());
        diagnosisObject.add(BahmniConstants.MATCHED_NAME,matchedItem.getDisplay());
        return diagnosisObject;
    }
}

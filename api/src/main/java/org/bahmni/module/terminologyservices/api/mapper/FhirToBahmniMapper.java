package org.bahmni.module.terminologyservices.api.mapper;


import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.stereotype.Component;

@Component
public class FhirToBahmniMapper {
    public  SimpleObject mapFhirResponseValueSetToSimpleObject(ValueSet.ValueSetExpansionContainsComponent matchedItem) {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add("conceptName", matchedItem.getDisplay());
        diagnosisObject.add("conceptUuid", matchedItem.getCode());
        diagnosisObject.add("matchedName",matchedItem.getDisplay());
        return diagnosisObject;
    }
}

package org.bahmni.module.terminologyservices.api.mapper;


import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;

public class FhirTerminologyServicesToBahmniMapper {
    public SimpleObject mapFhirResponseValueSetToSimpleObject(ValueSet.ValueSetExpansionContainsComponent valueSetContains) {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add("conceptName", valueSetContains.getDisplay());
        diagnosisObject.add("conceptUuid", valueSetContains.getCode());
        diagnosisObject.add("matchedName",valueSetContains.getDisplay());
        return diagnosisObject;
    }
}

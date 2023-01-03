package org.bahmni.module.terminologyservices.api.mapper;

import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirContains;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;

public class FhirTerminologyServicesToBahmniMapper {
    public  BahmniSearchResponse map(FhirContains fhirContains) {
        return new BahmniSearchResponse(fhirContains.getDisplay(), fhirContains.getCode(), fhirContains.getDisplay());
    }
    public SimpleObject mapFhirResponseToSimpleObject(FhirContains fhirContains) {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add("conceptName", fhirContains.getDisplay());
        diagnosisObject.add("conceptUuid", fhirContains.getCode());
        diagnosisObject.add("matchedName",fhirContains.getDisplay());
        return diagnosisObject;
    }
    public SimpleObject mapFhirResponseValueSetToSimpleObject(ValueSet.ValueSetExpansionContainsComponent valueSetContains) {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add("conceptName", valueSetContains.getDisplay());
        diagnosisObject.add("conceptUuid", valueSetContains.getCode());
        diagnosisObject.add("matchedName",valueSetContains.getDisplay());
        return diagnosisObject;
    }
}

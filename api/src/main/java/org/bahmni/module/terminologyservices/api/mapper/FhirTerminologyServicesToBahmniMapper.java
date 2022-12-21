package org.bahmni.module.terminologyservices.api.mapper;

import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirContains;

public class FhirTerminologyServicesToBahmniMapper {
    public  BahmniSearchResponse map(FhirContains fhirContains) {
        return new BahmniSearchResponse(fhirContains.getDisplay(), fhirContains.getCode(), fhirContains.getDisplay());
    }
}

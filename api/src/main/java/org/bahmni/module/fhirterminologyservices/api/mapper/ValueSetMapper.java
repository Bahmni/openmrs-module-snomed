package org.bahmni.module.fhirterminologyservices.api.mapper;

import org.hl7.fhir.r4.model.ValueSet;

public interface ValueSetMapper <T>{
    T map(ValueSet valueSet);
}

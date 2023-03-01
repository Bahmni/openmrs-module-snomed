package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;
import java.util.stream.Collectors;

public class VSSimpleObjectMapper implements ValueSetMapper<List<SimpleObject>> {
    public static final String CONCEPT_NAME = "conceptName";
    public static final String CONCEPT_UUID = "conceptUuid";
    public static final String MATCHED_NAME = "matchedName";
    public static final String CONCEPT_SYSTEM = "conceptSystem";

    @Override
    public List<SimpleObject> map(ValueSet valueSet) {
        return valueSet.getExpansion().getContains().stream().map(item -> {
            SimpleObject simpleObject = new SimpleObject();
            simpleObject.add(CONCEPT_NAME, item.getDisplay());
            simpleObject.add(CONCEPT_UUID, item.getCode());
            simpleObject.add(MATCHED_NAME, item.getDisplay());
            simpleObject.add(CONCEPT_SYSTEM, item.getSystem());
            return simpleObject;
        }).collect(Collectors.toList());
    }
}

package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import org.bahmni.module.fhirterminologyservices.api.SimpleObjectConstants;
import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;
import java.util.stream.Collectors;

public class VSSimpleObjectMapper implements ValueSetMapper<List<SimpleObject>> {

    @Override
    public List<SimpleObject> map(ValueSet valueSet) {
        return valueSet.getExpansion().getContains().stream().map(item -> {
            SimpleObject simpleObject = new SimpleObject();
            simpleObject.add(SimpleObjectConstants.CONCEPT_NAME, item.getDisplay());
            simpleObject.add(SimpleObjectConstants.CONCEPT_UUID, item.getCode());
            simpleObject.add(SimpleObjectConstants.MATCHED_NAME, item.getDisplay());
            return simpleObject;
        }).collect(Collectors.toList());
    }
}

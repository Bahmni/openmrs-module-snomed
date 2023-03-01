package org.bahmni.module.fhirterminologyservices.api.mapper.impl;

import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.impl.TerminologyLookupServiceImpl;
import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;

import java.util.List;
import java.util.Optional;


public class VSConceptMapper implements ValueSetMapper<Concept> {

    private Logger logger = Logger.getLogger(this.getClass());
    private static final String FULLY_SPECIFIED_NAME = "Fully specified name";


    @Override
    public Concept map(ValueSet valueSet) {
        Concept concept = null;
        List<ValueSet.ValueSetExpansionContainsComponent> contains = valueSet.getExpansion().getContains();
        if(contains.size() > 0) {
            String preferredTerm = contains.get(0).getDisplay();
            String conceptFullySpecifiedName = null;
            List<ValueSet.ConceptReferenceDesignationComponent> designation = contains.get(0).getDesignation();
            Optional<ValueSet.ConceptReferenceDesignationComponent> fullySpecifiedNameOptional = designation.stream().filter(conceptReferenceDesignationComponent -> FULLY_SPECIFIED_NAME.equals(conceptReferenceDesignationComponent.getUse().getDisplay())).findFirst();

            if (fullySpecifiedNameOptional.isPresent()) {
                conceptFullySpecifiedName = fullySpecifiedNameOptional.get().getValue();
            }
            concept = new Concept();
            ConceptName fullySpecifiedName = new ConceptName(conceptFullySpecifiedName, Context.getLocale());
            concept.setFullySpecifiedName(fullySpecifiedName);

            ConceptName shortName = new ConceptName(preferredTerm, Context.getLocale());
            concept.setShortName(shortName);
        } else {
            logger.error("Concept with given code not found on the terminology server");
            throw new TerminologyServicesException("Concept Not Found");
        }

        return concept;
    }
}

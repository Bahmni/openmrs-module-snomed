package org.bahmni.module.fhirterminologyservices.api;

import org.apache.log4j.Logger;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TSConceptService extends TSConceptUuidResolver {

    public static final String CONV_SET = "ConvSet";
    private static Logger logger = Logger.getLogger(TSConceptService.class);

    @Autowired
    public TSConceptService(@Qualifier("adminService") AdministrationService administrationService, ConceptService conceptService, EmrApiProperties emrApiProperties, @Qualifier("fhirTsServices") TerminologyLookupService terminologyLookupService, FhirConceptSourceService conceptSourceService) {
        super(administrationService, conceptService, emrApiProperties, terminologyLookupService, conceptSourceService);
    }

    public List<Concept> createOrUpdateConceptsForValueSet(ValueSet valueSet, String conceptClassName, String conceptDatatypeName, String contextRootConceptName) {
        List<ValueSet.ValueSetExpansionContainsComponent> contains = valueSet.getExpansion().getContains();
        Concept contextRootConcept = null;
        if (contextRootConceptName != null) {
            contextRootConcept = getConcept(contextRootConceptName);
            validateContextRootConcept(contextRootConceptName, contextRootConcept);
        }
        Concept parentConceptForValueSet = getParentConceptForValueSet(valueSet.getName(), valueSet.getTitle(), CONV_SET, conceptDatatypeName);
        List<Concept> conceptList = getConceptList(conceptClassName, conceptDatatypeName, contains, parentConceptForValueSet);
        addNewMemberConceptToConceptSet(parentConceptForValueSet, contextRootConcept);
        updateParentConceptSetMembers(parentConceptForValueSet, conceptList);
        return conceptList;
    }

    private List<Concept> getConceptList(String conceptClassName, String conceptDatatypeName, List<ValueSet.ValueSetExpansionContainsComponent> expansionContainsComponents, Concept parentConceptForValueSet) {
        List<Concept> conceptList = new ArrayList<>();
        for (ValueSet.ValueSetExpansionContainsComponent valueSetExpansionContainsComponent : expansionContainsComponents) {
            String conceptSystem = valueSetExpansionContainsComponent.getSystem();
            String conceptReferenceTermCode = valueSetExpansionContainsComponent.getCode();
            Concept concept = getConcept(conceptSystem, conceptReferenceTermCode, conceptClassName, parentConceptForValueSet, conceptDatatypeName);
            addNewMemberConceptToConceptSet(concept, parentConceptForValueSet);
            conceptList.add(concept);
        }
        return conceptList;
    }

    private void validateContextRootConcept(String contextRootConceptName, Concept contextRootConcept) {
        if (contextRootConcept == null) {
            logger.error("Context Root Concept " + contextRootConceptName + " not found");
            throw new APIException("Context Root Concept " + contextRootConceptName + " not found");
        }
        if (!contextRootConcept.getSet()) {
            logger.error("Context Root Concept " + contextRootConceptName + " should be a set");
            throw new APIException("Context Root Concept " + contextRootConceptName + " should be a set");
        }
    }

    private void updateParentConceptSetMembers(Concept parentConcept, List<Concept> currentSetMembers) {
        parentConcept.getConceptSets().clear();
        currentSetMembers.stream().forEach(parentConcept :: addSetMember);
    }
}

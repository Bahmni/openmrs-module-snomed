package org.bahmni.module.fhirterminologyservices.api.task;

import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.model.FhirTask;

import java.util.List;

public interface ValueSetTask {
    FhirTask getInitialTaskResponse(List<String> valueSetIds);
    void convertValueSetsToConceptsTask(List<String> valueSetIds, String locale,
                                        String conceptClass, String conceptDatatype,
                                        String contextRoot, FhirTask task, UserContext userContext);
}
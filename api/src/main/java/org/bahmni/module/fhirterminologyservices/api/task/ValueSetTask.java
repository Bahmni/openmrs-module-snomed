package org.bahmni.module.fhirterminologyservices.api.task;

import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.TSConceptService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValueSetTask {

    private TerminologyLookupService terminologyLookupService;
    private TSConceptService tsConceptService;
    private FhirTaskDao fhirTaskDao;

    @Autowired
    public ValueSetTask(TerminologyLookupService terminologyLookupService, TSConceptService tsConceptService, FhirTaskDao fhirTaskDao) {
        this.terminologyLookupService = terminologyLookupService;
        this.tsConceptService = tsConceptService;
        this.fhirTaskDao = fhirTaskDao;
    }

    private static Logger logger = Logger.getLogger(ValueSetTask.class);

    public FhirTask getInitialTaskResponse(List<String> valueSetIds) {
        FhirTask fhirTask = new FhirTask();
        fhirTask.setStatus(FhirTask.TaskStatus.ACCEPTED);
        fhirTask.setName("Create / Update Valuesets for " + valueSetIds);
        fhirTask.setIntent(FhirTask.TaskIntent.ORDER);
        fhirTaskDao.createOrUpdate(fhirTask);
        return fhirTask;
    }

    @Async("threadPoolTaskExecutor")
    public void convertValueSetsToConceptsTask(List<String> valueSetIds, String locale, String conceptClass, String conceptDatatype, String contextRoot, Integer limit, FhirTask fhirTask, UserContext userContext) {
        FhirTask.TaskStatus taskStatus = null;
        try {
            Context.openSession();
            Context.setUserContext(userContext);
            valueSetIds.stream().forEach(valueSetId -> {
                ValueSet valueSet = terminologyLookupService.getValueSet(valueSetId, locale, limit);
                tsConceptService.createOrUpdateConceptsForValueSet(valueSet, conceptClass, conceptDatatype, contextRoot);
            });
        } catch (Exception exception) {
            taskStatus = FhirTask.TaskStatus.REJECTED;
            logger.error("Exception occurred while processing valueset ", exception);
        } finally {
            if (taskStatus == null) {
                taskStatus = FhirTask.TaskStatus.COMPLETED;
            }
            fhirTask.setStatus(taskStatus);
            fhirTaskDao.createOrUpdate(fhirTask);
            Context.closeSession();
        }
    }
}

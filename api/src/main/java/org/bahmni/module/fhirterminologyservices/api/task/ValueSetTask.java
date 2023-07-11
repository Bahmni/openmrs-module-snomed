package org.bahmni.module.fhirterminologyservices.api.task;

import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.TSConceptService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.fhir2.api.dao.FhirTaskDao;
import org.openmrs.module.fhir2.model.FhirTask;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
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
        fhirTask.setName("Create / Update Value Set");
        logger.info("Create / Update Value Sets for " + valueSetIds);
        fhirTask.setIntent(FhirTask.TaskIntent.ORDER);
        fhirTaskDao.createOrUpdate(fhirTask);
        return fhirTask;
    }

    @Async("threadPoolTaskExecutor")
    public void convertValueSetsToConceptsTask(List<String> valueSetIds, String locale, String conceptClass, String conceptDatatype, String contextRoot, FhirTask fhirTask, UserContext userContext) {
        FhirTask.TaskStatus taskStatus = null;
        try {
            Context.openSession();
            Context.setUserContext(userContext);
            valueSetIds.stream().forEach(valueSetId -> {
                Integer pageSize = RestUtil.getDefaultLimit();
                int offset = 0;
                int total = 0;
                do {
                    ValueSet valueSet = terminologyLookupService.getValueSetByPageSize(valueSetId, locale, pageSize, offset);
                    total = valueSet.getExpansion().getTotal();
                    offset += pageSize;
                    tsConceptService.createOrUpdateConceptsForValueSet(valueSet, conceptClass, conceptDatatype, contextRoot);
                } while (offset < total);
            });
        } catch (Exception exception) {
            taskStatus = FhirTask.TaskStatus.REJECTED;
            logger.error("Exception occurred while processing value sets ", exception);
        } finally {
            if (taskStatus == null) {
                taskStatus = FhirTask.TaskStatus.COMPLETED;
            }
            fhirTask.setStatus(taskStatus);
            fhirTaskDao.createOrUpdate(fhirTask);
        }
    }
}

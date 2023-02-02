package org.bahmni.module.fhirterminologyservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class TerminologyServicesActivator extends BaseModuleActivator {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void started() {
        log.info("Started Fhir Terminology Services");
    }

    @Override
    public void stopped() {
        log.info("Stopped Fhir Terminology Services");
    }

}

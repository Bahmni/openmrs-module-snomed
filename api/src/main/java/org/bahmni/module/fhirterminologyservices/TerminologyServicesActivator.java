package org.bahmni.module.fhirterminologyservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class TerminologyServicesActivator extends BaseModuleActivator {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void started() {
        log.info("Started Bahmni FHIR TS Integration module");
    }

    @Override
    public void stopped() {
        log.info("Stopped  Bahmni FHIR TS Integration module");
    }

}

package org.bahmni.module.terminologyservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class TerminologyServicesActivator extends BaseModuleActivator {
	
	private final Log log = LogFactory.getLog(this.getClass());

	@Override
	public void started() {
		log.info("Started Terminology Services");
	}

	public void shutdown() {
		log.info("Shutdown Terminology Services");
	}

}

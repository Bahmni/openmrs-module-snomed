/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

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
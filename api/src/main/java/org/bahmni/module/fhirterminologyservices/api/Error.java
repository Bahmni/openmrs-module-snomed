/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhirterminologyservices.api;

public enum Error {
    TERMINOLOGY_SERVICES_CONFIG_MISSING("could not connect to terminology server; at least 1 given global property is missing i.e. ts.fhir.baseurl, ts.fhir.diagnosissearch.valueseturl, ts.fhir.diagnosiscount.valueseturl, ts.fhir.valueset.urltemplate"),
    TERMINOLOGY_SERVER_NOT_FOUND("could not connect to terminology server; given global property 'ts.fhir.baseurl' isn't valid"),
    TERMINOLOGY_SERVER_ERROR("could not fetch results from terminology server; please contact terminology server administrator");
    public final String message;

    Error(String message) {
        this.message = message;
    }
}

package org.bahmni.module.fhirterminologyservices.api;

public enum Error {
    TERMINOLOGY_SERVICES_CONFIG_INVALID("could not connect to terminology server; at least 1 given global property isn't valid i.e. ts.fhir.diagnosissearch.valueseturl, ts.fhir.valueset.urltemplate"),
    TERMINOLOGY_SERVER_NOT_FOUND( "could not connect to terminology server; given global property 'ts.fhir.baseurl' isn't valid"),
    TERMINOLOGY_SERVICES_AT_LEAST_THREE_CHARS( "could not fetch results from terminology server; insufficient search limit(must have at least 3 characters)"),
    TERMINOLOGY_SERVER_ERROR( "could not fetch results from terminology server; please contact terminology server administrator");
    public final String message;

    Error(String message) {
        this.message = message;
    }
}

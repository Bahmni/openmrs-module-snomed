package org.bahmni.module.fhirterminologyservices.utils;

public class TerminologyServicesException extends RuntimeException {
    /**
     * Unique ID for Serialized object
     */
    private static final long serialVersionUID = 4657491283614755648L;

    public TerminologyServicesException() {
        super();
    }
    public TerminologyServicesException(String message) {
        super(message);
    }
}

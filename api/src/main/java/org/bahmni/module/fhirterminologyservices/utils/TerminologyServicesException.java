package org.bahmni.module.fhirterminologyservices.utils;

import org.bahmni.module.fhirterminologyservices.api.Error;

public class TerminologyServicesException extends RuntimeException {
    /**
     * Unique ID for Serialized object
     */
    private static final long serialVersionUID = 4657491283614755648L;
    private Error errorCode;
    public TerminologyServicesException(Error errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
    }

    public TerminologyServicesException(Error errorCode) {
        super(errorCode.message);
        this.errorCode = errorCode;
    }

    public Error getErrorCode() {
        return errorCode;
    }
}

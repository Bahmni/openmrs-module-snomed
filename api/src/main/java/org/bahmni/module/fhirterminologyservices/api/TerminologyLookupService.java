package org.bahmni.module.fhirterminologyservices.api;


import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.Concept;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TerminologyLookupService extends OpenmrsService {

    public static final String TERMINOLOGY_SERVER_BASE_URL_GLOBAL_PROP = "ts.fhir.baseurl";
    public static final String FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP = "ts.fhir.valueset.urltemplate";
    public static final String DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP = "ts.fhir.diagnosissearch.valueseturl";
    public static final String OBSERVATION_VALUE_SET_URL_GLOBAL_PROP = "ts.fhir.observation.valueset.urltemplate";
    public static final String DIAGNOSIS_COUNT_VALUE_SET_URL_GLOBAL_PROP = "ts.fhir.diagnosiscount.valueseturl";
    public static final String DIAGNOSIS_COUNT_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP = "ts.fhir.diagnosiscount.valueset.urltemplate";
    public static final String CONCEPT_DETAILS_URL_GLOBAL_PROP = "ts.fhir.conceptDetailsUrl";
    public static final String PROCEDURE_VALUESET_URL_GLOBAL_PROP = "ts.fhir.procedure.valueset.urltemplate";
    public static final String OBSERVATION_FORMAT = "json";
    public static final String SOCKET_TIMEOUT_GLOBAL_PROP = "ts.fhir.valueset.socket.timeout";
    public static final String CONNECTION_TIMEOUT_GLOBAL_PROP = "ts.fhir.valueset.connection.timeout";
    public static final String CONNECTION_REQUEST_TIMEOUT_GLOBAL_PROP = "ts.fhir.valueset.connection.request.timeout";

    @Authorized(value = {"Get Concepts"})
    List<SimpleObject> searchConcepts(String searchTerm, Integer limit, String locale);

    @Authorized(value = {"Get Concepts"})
    ValueSet searchTerminologyCodes(String terminologyCode, Integer pageSize, Integer offset, String locale);
    List<SimpleObject> searchConcepts(String valueSetUrl, String locale, String searchTerm, Integer limit);

    @Authorized(value = {"Get Concepts"})
    Concept getConcept(String referenceTermCode, String locale);

    @Authorized(value = {"Get Concepts"})
    ValueSet getValueSetByPageSize(String valueSetId, String locale, Integer pageSize, Integer offset);
}

package org.bahmni.module.fhirterminologyservices.api;


import org.bahmni.module.fhirterminologyservices.api.impl.TSPageObject;
import org.openmrs.Concept;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;

public interface TerminologyLookupService extends OpenmrsService {

    public static final String TERMINOLOGY_SERVER_BASE_URL_GLOBAL_PROP = "ts.fhir.baseurl";
    public static final String FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP = "ts.fhir.valueset.urltemplate";
    public static final String DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP = "ts.fhir.diagnosissearch.valueseturl";
    public static final String DIAGNOSIS_COUNT_VALUE_SET_URL_GLOBAL_PROP = "ts.fhir.diagnosiscount.valueseturl";
    public static final String CONCEPT_DETAILS_URL_GLOBAL_PROP = "ts.fhir.conceptDetailsUrl";

    @Authorized(value = {"Get Concepts"})
    List<SimpleObject> getResponseList(String searchTerm, Integer limit, String locale);

    @Authorized(value = {"Get Concepts"})
    Concept getConcept(String conceptId, String locale);

    //@Authorized(value = {"Get Concepts"})
    TSPageObject searchTerminologyCodes(String terminologyCode, Integer pageSize, Integer offset, String locale);
}

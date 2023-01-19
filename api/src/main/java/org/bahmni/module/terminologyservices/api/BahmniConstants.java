package org.bahmni.module.terminologyservices.api;

public class BahmniConstants {

    private BahmniConstants(){
    }
    //Global Property Keys
    public static final String TERMINOLOGY_SERVER_URL_GLOBAL_PROP = "ts.fhir.baseurl";
    public static final String DEFAULT_TERMINOLOGY_SERVER_URL_GLOBAL_PROP = "ts.fhir.defaultbaseurl";

    //Error Constants
    public static final String TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE = "TERMINOLOGY_SERVER_ERROR_MESSAGE";
    public static final String TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR = "TERMINOLOGY_SERVICES_CONFIG_INVALID";


    // Mock Constants
    public static final String MOCK_DIAGNOSES_SEARCH_TERM = "Malaria";
    public static final String FHIR_TERMINOLOGY_SERVICES_MOCK_RESPONSE = "{\"resourceType\":\"ValueSet\",\"url\":\"http://snomed.info/sct/449081005?fhir_vs\",\"expansion\":{\"total\":74,\"offset\":0,\"contains\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"61462000\",\"display\":\"Plasmodiosis\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"161416007\",\"display\":\"History of malaria\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"18342001\",\"display\":\"Algid malaria - malarial shock\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"27052006\",\"display\":\"Vivax malaria\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"21070001\",\"display\":\"Malaria by more than one parasite\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"19341001\",\"display\":\"Malaria by Plasmodium ovale\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"12845003\",\"display\":\"Malaria smear\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"1141602008\",\"display\":\"Complicated malaria\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"248437004\",\"display\":\"Malarial fever\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"27618009\",\"display\":\"Malariae malaria\"}]}}";

    //Mapping Constants
    public static final String CONCEPT_NAME = "conceptName";
    public static final String CONCEPT_UUID = "conceptUuid";
    public static final String MATCHED_NAME = "matchedName";
}

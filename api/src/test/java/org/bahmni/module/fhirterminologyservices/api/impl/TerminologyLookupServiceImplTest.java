package org.bahmni.module.fhirterminologyservices.api.impl;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.bahmni.module.fhirterminologyservices.api.mapper.impl.VSSimpleObjectMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, FhirContext.class})
public class TerminologyLookupServiceImplTest {
    @InjectMocks
    TerminologyLookupServiceImpl terminologyLookupService;
    @Mock
    private AdministrationService administrationService;
    @Mock
    private UserContext userContext;
    @Mock
    private FhirContext fhirContext;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IGenericClient iGenericClient;
    @Mock
    private ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper;

    private static List<SimpleObject> getMockSimpleObjectSingletonList(ValueSet valueSet) {
        SimpleObject simpleObject = new SimpleObject();
        ValueSet.ValueSetExpansionContainsComponent containsComponent = valueSet.getExpansion().getContainsFirstRep();
        simpleObject.add(VSSimpleObjectMapper.CONCEPT_NAME, containsComponent.getDisplay());
        simpleObject.add(VSSimpleObjectMapper.CONCEPT_UUID, containsComponent.getCode());
        simpleObject.add(VSSimpleObjectMapper.MATCHED_NAME, containsComponent.getDisplay());
        return Arrays.asList(simpleObject);
    }

    private static ValueSet getMockValueSet() {
        ValueSet valueSet = new ValueSet();
        ValueSet.ValueSetExpansionContainsComponent valueSetExpansionContainsComponent = new ValueSet.ValueSetExpansionContainsComponent();
        valueSetExpansionContainsComponent.setCode("195967001");
        valueSetExpansionContainsComponent.setSystem("http://DUMMY_TS_URL");
        valueSetExpansionContainsComponent.setDisplay("Hyperreactive airway disease");
        ValueSet.ValueSetExpansionComponent valueSetExpansionComponent = new ValueSet.ValueSetExpansionComponent();
        valueSetExpansionComponent.addContains(valueSetExpansionContainsComponent);
        valueSet.setExpansion(valueSetExpansionComponent);
        return valueSet;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Context.class);
        PowerMockito.mockStatic(FhirContext.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getLocale()).thenReturn(Locale.getDefault());
        when(FhirContext.forR4()).thenReturn(fhirContext);
    }

    @Test
    public void shouldGetMatchingTerminologies_whenDiagnosisSearchInputParametersPassed_isValid() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn("http://DUMMY_VALUESET_URL");
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn("DUMMY_VALUESET_TEMPLATE");
        ValueSet valueSet = getMockValueSet();
        List<SimpleObject> simpleObjectSingletonList = getMockSimpleObjectSingletonList(valueSet);
        when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(iGenericClient);
        when(iGenericClient.read().resource(ValueSet.class).withUrl(anyString()).execute()).thenReturn(valueSet);
        when(vsSimpleObjectMapper.map(any(ValueSet.class))).thenReturn(simpleObjectSingletonList);
        List<SimpleObject> diagnosisSearchList = terminologyLookupService.getResponseList("Asthma", 1, null);
        assertNotNull(diagnosisSearchList);
        assertEquals(1, diagnosisSearchList.size());
        SimpleObject response = diagnosisSearchList.get(0);
        assertEquals("Hyperreactive airway disease", response.get("conceptName"));
        assertEquals("195967001", response.get("conceptUuid"));
        assertEquals("Hyperreactive airway disease", response.get("matchedName"));
    }

    @Test
    public void shouldThrowTerminologyServicesAtLeastThreeCharactersValidationException_whenDiagnosisSearchInputParametersPassed_isLessThan3Characters() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn("http://DUMMY_VALUESET_URL");
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn("DUMMY_VALUESET_TEMPLATE");
        when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(iGenericClient);
        when(iGenericClient.read().resource(ValueSet.class).withUrl(anyString()).execute()).thenThrow(new InternalErrorException("Failed to call access method: java.lang.IllegalArgumentException: Search term must have at least 3 characters."));
        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupService.getResponseList("Ma", 10, null)
        );
        assertEquals("ca.uhn.fhir.rest.server.exceptions.InternalErrorException: Failed to call access method: java.lang.IllegalArgumentException: Search term must have at least 3 characters.", exception.getMessage());
    }

    @Test
    public void shouldThrowTerminologyServicesConfigInvalidException_whenValueSetUrlPassed_isEmpty() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn(null);
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn("DUMMY_VALUESET_TEMPLATE");
        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupService.getResponseList("Malaria", 10, null)
        );
        assertEquals("org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException: could not connect to terminology server; at least 1 given global property isn't valid i.e. ts.fhir.diagnosissearch.valueseturl, ts.fhir.valueset.urltemplate", exception.getMessage());
    }

    @Test
    public void shouldThrowTerminologyServicesConfigInvalidException_whenValueSetUrlTemplatePassed_isEmpty() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn("http://DUMMY_VALUESET_URL");
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn(null);
        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupService.getResponseList("Malaria", 10, null)
        );
        assertEquals("org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException: could not connect to terminology server; at least 1 given global property isn't valid i.e. ts.fhir.diagnosissearch.valueseturl, ts.fhir.valueset.urltemplate", exception.getMessage());
    }

    @Test
    public void shouldThrowTerminologyServerError_whenTerminologyServerConnection_isNotFound() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn("http://DUMMY_VALUESET_URL");
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn("DUMMY_VALUESET_TEMPLATE");
        when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(iGenericClient);
        when(iGenericClient.read().resource(ValueSet.class).withUrl(anyString()).execute()).thenThrow(new FhirClientConnectionException("Invalid Connection"));
        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupService.getResponseList("Malaria", 10, null)
        );
        assertEquals("ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException: Invalid Connection", exception.getMessage());
    }

    @Test
    public void shouldThrowTerminologyServerError_whenTerminologyServerConnection_isTimeOut() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn("http://DUMMY_VALUESET_URL");
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn("DUMMY_VALUESET_TEMPLATE");
        when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(iGenericClient);
        when(iGenericClient.read().resource(ValueSet.class).withUrl(anyString()).execute()).thenThrow(new UnclassifiedServerFailureException(502, "HTTP 502 Bad Gateway"));
        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupService.getResponseList("Malaria", 10, null)
        );
        assertEquals("ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException: HTTP 502 Bad Gateway", exception.getMessage());
    }

    @Test
    public void shouldThrowTerminologyServerError_whenTerminologyServerFunctionality_isBroken() throws Exception {
        when(administrationService.getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn("https://DUMMY_TS_URL/");
        when(administrationService.getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP)).thenReturn("http://DUMMY_VALUESET_URL");
        when(administrationService.getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP)).thenReturn("DUMMY_VALUESET_TEMPLATE");
        when(fhirContext.newRestfulGenericClient(anyString())).thenReturn(iGenericClient);
        when(iGenericClient.read().resource(ValueSet.class).withUrl(anyString()).execute()).thenThrow(new ResourceNotFoundException("Not Found"));
        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupService.getResponseList("Malaria", 10, null)
        );
        assertEquals("ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: Not Found", exception.getMessage());
    }
}
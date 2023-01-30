package org.bahmni.module.fhirterminologyservices.api.impl;

import org.bahmni.module.fhirterminologyservices.api.mapper.FhirValueSetToDiagnosisMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.io.IOException;
import java.util.List;

import static org.bahmni.module.fhirterminologyservices.api.impl.TerminologyLookupServiceImpl.*;
import static org.bahmni.module.fhirterminologyservices.api.mapper.FhirValueSetToDiagnosisMapper.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class TerminologyLookupServiceImplTest {
    @Mock
    private AdministrationService administrationService;
    @InjectMocks
    TerminologyLookupServiceImpl terminologyLookupService;
    @Mock
    private UserContext userContext;
   @Mock
    private FhirValueSetToDiagnosisMapper fhirValueSetToDiagnosisMapper;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrl() throws TerminologyServicesException {
        when(administrationService.getGlobalProperty(TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn(
                "https://DUMMY_TS_URL");
        String tsServerUrl = terminologyLookupService.getTerminologyServerBaseUrl();
        assertEquals("https://DUMMY_TS_URL", tsServerUrl);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrlDefaultValueWhenServerUrlIsNull() throws TerminologyServicesException {
        when(administrationService.getGlobalProperty(TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn(
                null);
        Exception exception = assertThrows(TerminologyServicesException.class, () -> {
            String tsServerUrl = terminologyLookupService.getTerminologyServerBaseUrl();
        });
        assertEquals(TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR, exception.getMessage());
    }
    @Test
    public void shouldGetTerminologyServicesServerUrlDefaultValueWhenServerUrlIsEmpty() throws TerminologyServicesException {
        when(administrationService.getGlobalProperty(TERMINOLOGY_SERVER_URL_GLOBAL_PROP)).thenReturn(
                "");
        Exception exception = assertThrows(TerminologyServicesException.class, () -> {
            String tsServerUrl = terminologyLookupService.getTerminologyServerBaseUrl();
        });
        assertEquals(TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR, exception.getMessage());
    }
    @Test
    public void shouldGetResponseList() throws Exception {

        when(fhirValueSetToDiagnosisMapper.mapFhirResponseValueSetToSimpleObject(any())).thenReturn( createDumbDiagnosisResponse());
        List<SimpleObject> diagnosisSearchList = terminologyLookupService.getResponseList("Malaria", 10, null);
        assertNotNull(diagnosisSearchList);
        assertEquals(10, diagnosisSearchList.size());
        SimpleObject firstResponse = diagnosisSearchList.get(0);
        SimpleObject lastResponse = diagnosisSearchList.get(9);
        assertEquals("Plasmodiosis", firstResponse.get(CONCEPT_NAME));
        assertEquals("61462000", firstResponse.get(CONCEPT_UUID));
        assertEquals("Plasmodiosis", firstResponse.get(MATCHED_NAME));
    }
    @Test
    public void shouldThrowServerDownExceptionWhenSearchTermIsOtherThanMalaria() throws Exception {

        when(fhirValueSetToDiagnosisMapper.mapFhirResponseValueSetToSimpleObject(any())).thenReturn( createDumbDiagnosisResponse());
        Exception exception = assertThrows(TerminologyServicesException.class, () -> {
            List<SimpleObject> diagnosisSearchList = terminologyLookupService.getResponseList("otherTerm", 10, null);
        });
        assertEquals(TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE, exception.getMessage());
    }
    @Test
    public void shouldCreateMockFhirTerminologyResponseUsingFhirValueSetModel() throws IOException {
        ValueSet terminologyResponseValueSet = terminologyLookupService.createMockFhirTerminologyResponseValueSet();
        assertNotNull(terminologyResponseValueSet);
        assertEquals("ValueSet", terminologyResponseValueSet.getResourceType().toString());
        assertEquals("http://snomed.info/sct/449081005?fhir_vs", terminologyResponseValueSet.getUrl());
        assertEquals(74, terminologyResponseValueSet.getExpansion().getTotal());
        assertEquals(0, terminologyResponseValueSet.getExpansion().getOffset());
        assertEquals(10, terminologyResponseValueSet.getExpansion().getContains().size());
        ValueSet.ValueSetExpansionContainsComponent containsComponent  = terminologyResponseValueSet.getExpansion().getContains().get(0);
        assertEquals("Plasmodiosis", containsComponent.getDisplay());
        assertEquals("61462000", containsComponent.getCode());
        assertEquals("http://snomed.info/sct", containsComponent.getSystem());
    }
    private SimpleObject createDumbDiagnosisResponse() {
        SimpleObject diagnosisObject = new SimpleObject();
        diagnosisObject.add(CONCEPT_NAME, "Plasmodiosis");
        diagnosisObject.add(CONCEPT_UUID, "61462000");
        diagnosisObject.add(MATCHED_NAME,"Plasmodiosis");
        return diagnosisObject;
    }
}
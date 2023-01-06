package org.bahmni.module.terminologyservices.api.impl;

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

import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class })
public class TerminologyInitiatorServiceImplTest {
    @Mock
    private AdministrationService administrationService;
    @InjectMocks
    TerminologyInitiatorServiceImpl terminologyInitiatorService;
    @Mock
    private UserContext userContext;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrl() {
        when(administrationService.getGlobalProperty("bahmni.clinical.terminologyServices.serverUrlPattern")).thenReturn(
                "https://snomed-url");
        String tsServerUrl = terminologyInitiatorService.getTerminologyServicesServerUrl();
        assertEquals("https://snomed-url", tsServerUrl);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrlDefaultValueWhenServerUrlIsNull() {
        when(administrationService.getGlobalProperty("bahmni.clinical.terminologyServices.serverUrlPattern")).thenReturn(
                null);
        String tsServerUrl = terminologyInitiatorService.getTerminologyServicesServerUrl();
        assertEquals("https://snomed-url", tsServerUrl);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrlDefaultValueWhenServerUrlIsEmpty() {
        when(administrationService.getGlobalProperty("bahmni.clinical.terminologyServices.serverUrlPattern")).thenReturn(
                "");
        String tsServerUrl = terminologyInitiatorService.getTerminologyServicesServerUrl();
        assertEquals("https://snomed-url", tsServerUrl);
    }
    @Test
    public void ShouldGetResponseList() {
        List<SimpleObject> diagnosisSearchList = terminologyInitiatorService.getResponseList("Malaria", 10, null);
        assertNotNull(diagnosisSearchList);
        assertEquals(10, diagnosisSearchList.size());
        SimpleObject firstResponse = diagnosisSearchList.get(0);
        SimpleObject lastResponse = diagnosisSearchList.get(9);
        assertEquals("Plasmodiosis", firstResponse.get("conceptName"));
        assertEquals("61462000", firstResponse.get("conceptUuid"));
        assertEquals("Plasmodiosis", firstResponse.get("matchedName"));
        assertEquals("Malariae malaria", lastResponse.get("conceptName"));
        assertEquals("27618009", lastResponse.get("conceptUuid"));
        assertEquals("Malariae malaria", lastResponse.get("matchedName"));

    }
    @Test
    public void ShouldCreateMockFhirTerminologyResponseUsingFhirValueSetModel() {
        ValueSet terminologyResponseValueSet = terminologyInitiatorService.createMockFhirTerminologyResponseValueSet();
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
}
package org.bahmni.module.terminologyservices.api.impl;

import org.bahmni.module.terminologyservices.api.mapper.FhirToBahmniMapper;
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
    private FhirToBahmniMapper fhirToBahmniMapper;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrl() {
        when(administrationService.getGlobalProperty("ts.fhir.baseurl")).thenReturn(
                "https://DUMMY_TS_URL");
        String tsServerUrl = terminologyLookupService.getTerminologyServerBaseUrl();
        assertEquals("https://DUMMY_TS_URL", tsServerUrl);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrlDefaultValueWhenServerUrlIsNull() {
        when(administrationService.getGlobalProperty("ts.fhir.baseurl")).thenReturn(
                null);
        when(administrationService.getGlobalProperty("ts.fhir.defaultbaseurl")).thenReturn(
                "https://DUMMY_DEFAULT_TS_URL");
        String tsServerUrl = terminologyLookupService.getTerminologyServerBaseUrl();
        assertEquals("https://DUMMY_DEFAULT_TS_URL", tsServerUrl);
    }
    @Test
    public void shouldGetTerminologyServicesServerUrlDefaultValueWhenServerUrlIsEmpty() {
        when(administrationService.getGlobalProperty("bahmni.terminologyServices.serverBaseUrlPattern")).thenReturn(
                "");
        when(administrationService.getGlobalProperty("ts.fhir.defaultbaseurl")).thenReturn(
                "https://DUMMY_DEFAULT_TS_URL");
        String tsServerUrl = terminologyLookupService.getTerminologyServerBaseUrl();
        assertEquals("https://DUMMY_DEFAULT_TS_URL", tsServerUrl);
    }
    @Test
    public void ShouldGetResponseList() throws Exception {

        when(fhirToBahmniMapper.mapFhirResponseValueSetToSimpleObject(any())).thenReturn( createDumbDiagnosisResponse());
        List<SimpleObject> diagnosisSearchList = terminologyLookupService.getResponseList("Malaria", 10, null);
        assertNotNull(diagnosisSearchList);
        assertEquals(10, diagnosisSearchList.size());
        SimpleObject firstResponse = diagnosisSearchList.get(0);
        SimpleObject lastResponse = diagnosisSearchList.get(9);
        assertEquals("Plasmodiosis", firstResponse.get("conceptName"));
        assertEquals("61462000", firstResponse.get("conceptUuid"));
        assertEquals("Plasmodiosis", firstResponse.get("matchedName"));
    }
    @Test
    public void ShouldCreateMockFhirTerminologyResponseUsingFhirValueSetModel() {
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
        diagnosisObject.add("conceptName", "Plasmodiosis");
        diagnosisObject.add("conceptUuid", "61462000");
        diagnosisObject.add("matchedName","Plasmodiosis");
        return diagnosisObject;
    }
}
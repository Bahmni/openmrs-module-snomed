package org.bahmni.module.terminologyservices.api.impl;

import org.bahmni.module.terminologyservices.api.TerminologyInitiatorService;
import org.bahmni.module.terminologyservices.api.model.BahmniSearchResponse;
import org.bahmni.module.terminologyservices.api.model.FhirContains;
import org.bahmni.module.terminologyservices.api.model.FhirTerminologyResponse;
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
    public void ShouldGetBahmniSearchResponse() {
        List<BahmniSearchResponse> bahmniSearchResponseList = terminologyInitiatorService.getBahmniSearchResponse("Malaria", 10, null);
        assertNotNull(bahmniSearchResponseList);
        assertEquals(10, bahmniSearchResponseList.size());
        BahmniSearchResponse firstResponse = bahmniSearchResponseList.get(0);
        BahmniSearchResponse lastResponse = bahmniSearchResponseList.get(9);
        assertEquals("Plasmodiosis", firstResponse.getMatchedName());
        assertEquals("61462000", firstResponse.getConceptUuid());
        assertEquals("Plasmodiosis", firstResponse.getConceptName());
        assertEquals("Malariae malaria", lastResponse.getMatchedName());
        assertEquals("27618009", lastResponse.getConceptUuid());
        assertEquals("Malariae malaria", lastResponse.getConceptName());

    }
    @Test
    public void ShouldGetDiagnosisSearch() {
        List<SimpleObject> diagnosisSearchList = terminologyInitiatorService.getDiagnosisSearch("Malaria", 10, null);
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
    public void ShouldCreateMockFhirTerminologyResponse() {
        String mockJsonResposeString = "{\"resourceType\":\"ValueSet\",\"url\":\"http://snomed.info/sct/449081005?fhir_vs\",\"expansion\":{\"total\":246,\"offset\":0,\"contains\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"195967001\",\"display\":\"Hyperreactive airway disease\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"160377001\",\"display\":\"FH: Asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"56968009\",\"display\":\"Wood asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"406162001\",\"display\":\"Asthma care\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"161527007\",\"display\":\"History of asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"370218001\",\"display\":\"Mild asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"281239006\",\"display\":\"Exacerbation of asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"195977004\",\"display\":\"Mixed asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"34015007\",\"display\":\"Flour asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"370221004\",\"display\":\"Severe asthma\"}]}}";
//        when(terminologyInitiatorService.getMockTerminologyString()).thenReturn(mockJsonResposeString);
        FhirTerminologyResponse fhirTerminologyResponse = terminologyInitiatorService.createMockFhirTerminologyResponse();
        assertNotNull(fhirTerminologyResponse);
        assertEquals("ValueSet", fhirTerminologyResponse.getResourceType());
        assertEquals("http://snomed.info/sct/449081005?fhir_vs", fhirTerminologyResponse.getUrl());
        assertEquals(new Integer(74), fhirTerminologyResponse.getExpansion().getTotal());
        assertEquals(new Integer(0), fhirTerminologyResponse.getExpansion().getOffset());
        assertEquals(10, fhirTerminologyResponse.getExpansion().getContains().size());
        FhirContains fhirContains  = fhirTerminologyResponse.getExpansion().getContains().get(0);
        assertEquals("Plasmodiosis", fhirContains.getDisplay());
        assertEquals("61462000", fhirContains.getCode());
        assertEquals("http://snomed.info/sct", fhirContains.getSystem());
    }
    @Test
    public void ShouldCreateMockFhirTerminologyResponseUsingFhirValueSetModel() {
        String mockJsonResposeString = "{\"resourceType\":\"ValueSet\",\"url\":\"http://snomed.info/sct/449081005?fhir_vs\",\"expansion\":{\"total\":246,\"offset\":0,\"contains\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"195967001\",\"display\":\"Hyperreactive airway disease\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"160377001\",\"display\":\"FH: Asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"56968009\",\"display\":\"Wood asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"406162001\",\"display\":\"Asthma care\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"161527007\",\"display\":\"History of asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"370218001\",\"display\":\"Mild asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"281239006\",\"display\":\"Exacerbation of asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"195977004\",\"display\":\"Mixed asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"34015007\",\"display\":\"Flour asthma\"},{\"system\":\"http://snomed.info/sct\",\"code\":\"370221004\",\"display\":\"Severe asthma\"}]}}";
//        when(terminologyInitiatorService.getMockTerminologyString()).thenReturn(mockJsonResposeString);
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
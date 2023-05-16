package org.bahmni.module.fhirterminologyservices.web.controller;

import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class TerminologyLookupControllerTest {
    @Mock
    private TerminologyLookupService terminologyLookupService;

    @InjectMocks
    private TerminologyLookupController terminologyLookupController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldBeAbleToSearchForTerminologies() throws Exception {
        String term = "Ma";
        Integer limit = 10;
        String locale = "en";
        ResponseEntity<?> searchResponse = terminologyLookupController
                .searchDiagnosis(term, limit, locale);
        verify(terminologyLookupService, times(1)).getResponseList(term, limit, locale);
    }

    @Test
    public void shouldThrowTerminologyServicesExceptionWhenTerminologyServerIsUnavailable() throws Exception {
        String term = "Me";
        Integer limit = 10;
        String locale = "en";
        when(terminologyLookupService.getResponseList("Me", 10, "en")).thenThrow(new TerminologyServicesException());
        assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupController.searchDiagnosis(term, limit, locale)
        );
    }

    @Test
    public void shouldGetPageObjectWithDescendantCodes_whenValidTerminologyCodeIsProvided() throws Exception {
        when(terminologyLookupService.searchTerminologyCodes(anyString(), anyInt(), anyInt(), anyString())).thenReturn(getMockValueSet());
        ResponseEntity<Object> responseEntity = terminologyLookupController.searchTerminologyCodes("12345", 10, 0, "en");
        Object responseObject = responseEntity.getBody();
        assertEquals(responseObject.getClass(), TSPageObject.class);
        TSPageObject pageObject = (TSPageObject)responseObject;
        assertEquals(1,pageObject.getTotal().intValue());
        assertEquals("195967001", pageObject.getCodes().get(0));
    }

    private static ValueSet getMockValueSet() {
        ValueSet valueSet = new ValueSet();
        ValueSet.ValueSetExpansionContainsComponent valueSetExpansionContainsComponent = new ValueSet.ValueSetExpansionContainsComponent();
        valueSetExpansionContainsComponent.setCode("195967001");
        valueSetExpansionContainsComponent.setSystem("http://DUMMY_TS_URL");
        valueSetExpansionContainsComponent.setDisplay("Hyperreactive airway disease");
        ValueSet.ValueSetExpansionComponent valueSetExpansionComponent = new ValueSet.ValueSetExpansionComponent();
        valueSetExpansionComponent.setTotal(1);
        valueSetExpansionComponent.addContains(valueSetExpansionContainsComponent);
        valueSet.setExpansion(valueSetExpansionComponent);
        return valueSet;
    }

}
package org.bahmni.module.fhirterminologyservices.web.controller;

import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.bahmni.module.fhirterminologyservices.api.impl.TerminologyLookupServiceImpl.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void shouldGenerateSearchResponse() throws Exception {
        String term = "Ma";
        Integer limit = 10;
        String locale = "en";
        ResponseEntity<?> searchResponse = terminologyLookupController
                .searchDiagnosis(term, limit, locale);
        verify(terminologyLookupService, times(1)).getResponseList(term, limit, locale);
    }

    @Test
    public void shouldReturnServiceUnavailableWhenTerminologyServerIsUnavailable() throws Exception {
        String term = "Me";
        Integer limit = 10;
        String locale = "en";
         when(terminologyLookupService.getResponseList("Me", 10, "en")).thenThrow(new TerminologyServicesException(TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE));
        ResponseEntity<Object> errorResponse = terminologyLookupController
                .searchDiagnosis(term, limit, locale);
        Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, errorResponse.getStatusCode());
    }

}
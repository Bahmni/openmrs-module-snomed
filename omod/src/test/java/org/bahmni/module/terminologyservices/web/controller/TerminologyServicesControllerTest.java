package org.bahmni.module.terminologyservices.web.controller;

import org.bahmni.module.terminologyservices.api.TerminologyInitiatorService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TerminologyServicesControllerTest {
    @Mock
    private TerminologyInitiatorService terminologyInitiatorService;

    @InjectMocks
    private TerminologyServicesController terminologyServicesController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGenerateSearchResponse() throws Exception {
        String term = "Ma";
        Integer limit = 10;
        String locale = "en";
        ResponseEntity<?> searchResponse = terminologyServicesController
                .searchDiagnosis(term, limit, locale);
        verify(terminologyInitiatorService, times(1)).getResponseList(term, limit, locale);
    }
    @Test
    public void shouldReturnServiceUnavailableWhenSearchedWithDifferentTerm() throws Exception {
        String term = "Me";
        Integer limit = 10;
        String locale = "en";
        ResponseEntity<Object> errorResponse = terminologyServicesController
                .searchDiagnosis(term, limit, locale);
        Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, errorResponse.getStatusCode());
    }

}
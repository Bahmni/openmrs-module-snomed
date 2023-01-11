package org.bahmni.module.terminologyservices.web.controller;

import org.bahmni.module.terminologyservices.api.TerminologyLookupService;
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
    public void shouldReturnServiceUnavailableWhenSearchedWithDifferentTerm() throws Exception {
        String term = "Me";
        Integer limit = 10;
        String locale = "en";
        ResponseEntity<Object> errorResponse = terminologyLookupController
                .searchDiagnosis(term, limit, locale);
        Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, errorResponse.getStatusCode());
    }

}
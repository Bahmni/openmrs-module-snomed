package org.bahmni.module.fhirterminologyservices.web.controller;

import org.bahmni.module.fhirterminologyservices.api.Error;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
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
    public void shouldThrowExceptionWithContextAndErrorWithMitigationWhenTerminologyServerIsUnavailable() throws Exception {
        String term = "Me";
        Integer limit = 10;
        String locale = "en";
        when(terminologyLookupService.getResponseList("Me", 10, "en")).thenThrow(new TerminologyServicesException(Error.TERMINOLOGY_SERVER_NOT_FOUND));

        Exception exception = assertThrows(TerminologyServicesException.class, () ->
                terminologyLookupController.searchDiagnosis(term, limit, locale)
        );
        assertEquals("could not connect to terminology server; given global property 'ts.fhir.baseurl' isn't valid", exception.getMessage());
    }

}
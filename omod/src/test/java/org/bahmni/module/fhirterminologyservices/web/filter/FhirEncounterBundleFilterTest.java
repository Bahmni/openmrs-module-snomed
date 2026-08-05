/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhirterminologyservices.web.filter;

import org.apache.commons.io.IOUtils;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Condition;

import javax.servlet.FilterChain;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirEncounterBundleFilterTest {

    private static final String ENCOUNTER_BUNDLE_URI = "/openmrs/ws/fhir2/R4/EncounterBundle";

    @Mock
    private ConditionConceptSaveService conditionConceptSaveService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private FhirEncounterBundleFilter filter;

    @Before
    public void setUp() {
        filter = new FhirEncounterBundleFilter() {
            @Override
            protected ConditionConceptSaveService getConditionConceptSaveService() {
                return conditionConceptSaveService;
            }
        };
    }

    @Test
    public void shouldEnsureConceptExistsForNewSnomedConditionCode() throws Exception {
        String body = "{ \"entry\": [ { \"resource\": { \"resourceType\": \"Condition\", "
                + "\"code\": { \"coding\": [ { \"system\": \"http://snomed.info/sct\", \"code\": \"225486005\" } ] } } } ] }";
        HttpServletRequest request = mockRequest("POST", ENCOUNTER_BUNDLE_URI, body);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Condition> conditionCaptor = ArgumentCaptor.forClass(Condition.class);
        verify(conditionConceptSaveService, times(1)).update(conditionCaptor.capture());
        assertEquals("http://snomed.info/sct/225486005",
                conditionCaptor.getValue().getCondition().getCoded().getUuid());
    }

    @Test
    public void shouldEnsureConceptExistsForSnomedObservationValue() throws Exception {
        String body = "{ \"entry\": [ { \"resource\": { \"resourceType\": \"Observation\", "
                + "\"valueCodeableConcept\": { \"coding\": [ { \"system\": \"http://snomed.info/sct\", \"code\": \"283111009\" } ] } } } ] }";
        HttpServletRequest request = mockRequest("POST", ENCOUNTER_BUNDLE_URI, body);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Condition> conditionCaptor = ArgumentCaptor.forClass(Condition.class);
        verify(conditionConceptSaveService, times(1)).update(conditionCaptor.capture());
        assertEquals("http://snomed.info/sct/283111009",
                conditionCaptor.getValue().getCondition().getCoded().getUuid());
    }

    @Test
    public void shouldNotEnsureConceptExistsWhenCodingSystemIsNotSnomed() throws Exception {
        String body = "{ \"entry\": [ { \"resource\": { \"resourceType\": \"Condition\", "
                + "\"code\": { \"coding\": [ { \"system\": \"http://some-other-system\", \"code\": \"12345\" } ] } } } ] }";
        HttpServletRequest request = mockRequest("POST", ENCOUNTER_BUNDLE_URI, body);

        filter.doFilter(request, response, filterChain);

        verify(conditionConceptSaveService, never()).update(any(Condition.class));
    }

    @Test
    public void shouldNotEnsureConceptExistsWhenCodingHasNoSystem() throws Exception {
        String body = "{ \"entry\": [ { \"resource\": { \"resourceType\": \"Condition\", "
                + "\"code\": { \"coding\": [ { \"code\": \"225486005\" } ] } } } ] }";
        HttpServletRequest request = mockRequest("POST", ENCOUNTER_BUNDLE_URI, body);

        filter.doFilter(request, response, filterChain);

        verify(conditionConceptSaveService, never()).update(any(Condition.class));
    }

    @Test
    public void shouldPassThroughRequestBodyUnchanged() throws Exception {
        String body = "{ \"entry\": [ { \"resource\": { \"resourceType\": \"Condition\", "
                + "\"code\": { \"coding\": [ { \"system\": \"http://snomed.info/sct\", \"code\": \"225486005\" } ] } } } ] }";
        HttpServletRequest request = mockRequest("POST", ENCOUNTER_BUNDLE_URI, body);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain, times(1)).doFilter(requestCaptor.capture(), any());
        String forwardedBody = IOUtils.toString(requestCaptor.getValue().getInputStream(), StandardCharsets.UTF_8);
        assertEquals(body, forwardedBody);
    }

    @Test
    public void shouldSkipProcessingForNonEncounterBundleRequests() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");

        filter.doFilter(request, response, filterChain);

        verifyZeroInteractions(conditionConceptSaveService);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    private HttpServletRequest mockRequest(String method, String uri, String body) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getInputStream()).thenReturn(toServletInputStream(body));
        return request;
    }

    private ServletInputStream toServletInputStream(String content) {
        final InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return is.read();
            }
        };
    }
}
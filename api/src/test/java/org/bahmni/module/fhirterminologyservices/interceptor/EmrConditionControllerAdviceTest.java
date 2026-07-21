/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhirterminologyservices.interceptor;

import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Condition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmrConditionControllerAdviceTest {

    @InjectMocks
    EmrConditionControllerAdvice emrConditionControllerAdvice;

    @Mock
    ConditionConceptSaveService conditionConceptSaveService;

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void shouldReturnTrueForPostRequestToConditionEndpoint() {
        setRequestContext("POST", "/ws/rest/v1/condition");
        assertTrue(emrConditionControllerAdvice.supports(null, null, null));
    }

    @Test
    public void shouldReturnFalseForGetRequestToConditionEndpoint() {
        setRequestContext("GET", "/ws/rest/v1/condition");
        assertFalse(emrConditionControllerAdvice.supports(null, null, null));
    }

    @Test
    public void shouldReturnFalseForPostRequestToNonConditionEndpoint() {
        setRequestContext("POST", "/ws/rest/v1/patient");
        assertFalse(emrConditionControllerAdvice.supports(null, null, null));
    }

    @Test
    public void shouldReturnSameObjectWhenAfterBodyReadCalled() {
        Object object = new Object();
        Object returnObject = emrConditionControllerAdvice.afterBodyRead(object, null, null, null, null);
        assertNotNull(returnObject);
        assertEquals(object, returnObject);
    }

    @Test
    public void shouldReturnSameObjectWhenHandleEmptyBodyCalled() {
        Object object = new Object();
        Object returnObject = emrConditionControllerAdvice.handleEmptyBody(object, null, null, null, null);
        assertNotNull(returnObject);
        assertEquals(object, returnObject);
    }

    @Test
    public void shouldCallUpdateOnServiceWhenConditionWithCodedConceptProvided() throws IOException {
        String bodyJson = "{\"condition\":{\"coded\":\"concept-uuid-123\"},\"patient\":\"patient-uuid\"}";
        HttpInputMessage httpInputMessage = buildInputMessage(bodyJson);

        when(conditionConceptSaveService.update(any(Condition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        emrConditionControllerAdvice.beforeBodyRead(httpInputMessage, null, null, null);

        verify(conditionConceptSaveService, times(1)).update(any(Condition.class));
    }

    @Test
    public void shouldNotCallUpdateOnServiceWhenNoConditionFieldPresent() throws IOException {
        String bodyJson = "{\"patient\":\"patient-uuid\",\"clinicalStatus\":\"ACTIVE\"}";
        HttpInputMessage httpInputMessage = buildInputMessage(bodyJson);

        emrConditionControllerAdvice.beforeBodyRead(httpInputMessage, null, null, null);

        verify(conditionConceptSaveService, times(0)).update(any(Condition.class));
    }

    @Test
    public void shouldNotCallUpdateOnServiceWhenConditionHasNoCodedValue() throws IOException {
        String bodyJson = "{\"condition\":{\"nonCoded\":\"some text\"},\"patient\":\"patient-uuid\"}";
        HttpInputMessage httpInputMessage = buildInputMessage(bodyJson);

        emrConditionControllerAdvice.beforeBodyRead(httpInputMessage, null, null, null);

        verify(conditionConceptSaveService, times(0)).update(any(Condition.class));
    }

    @Test
    public void shouldPassCorrectCodedUuidToService() throws IOException {
        String conceptUuid = "snomed-concept-uuid-456";
        String bodyJson = "{\"condition\":{\"coded\":\"" + conceptUuid + "\"},\"patient\":\"patient-uuid\"}";
        HttpInputMessage httpInputMessage = buildInputMessage(bodyJson);

        when(conditionConceptSaveService.update(any(Condition.class))).thenAnswer(invocation -> {
            Condition c = invocation.getArgument(0);
            assertEquals(conceptUuid, c.getCondition().getCoded().getUuid());
            return c;
        });

        emrConditionControllerAdvice.beforeBodyRead(httpInputMessage, null, null, null);
    }

    private void setRequestContext(String method, String uri) {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private HttpInputMessage buildInputMessage(String json) {
        return new MappingJacksonInputMessage(new ByteArrayInputStream(json.getBytes()), new HttpHeaders());
    }
}

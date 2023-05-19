package org.bahmni.module.fhirterminologyservices.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.conditionslist.contract.Condition;
import org.openmrs.module.emrapi.web.controller.ConditionController;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class EmrConditionControllerAdviceTest {

    @InjectMocks
    EmrConditionControllerAdvice emrConditionControllerAdvice;
    @Mock
    ConditionConceptSaveService conditionConceptSaveService;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
    }
    @Test
    public  void shouldReturnTrueWhenInterceptedMethodNameMatchesSaveMethodName() throws NoSuchMethodException {
        Method method = ConditionController.class.getMethod("save", Condition[].class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = emrConditionControllerAdvice.supports(methodParameter, null, null);
        assertTrue(supports);
    }
    @Test
    public  void shouldReturnFalseWhenInterceptedMethodNameDoesntMatcheSaveMethodName() throws NoSuchMethodException {
        Method method = ConditionController.class.getMethod("getConditionHistory", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = emrConditionControllerAdvice.supports(methodParameter, null, null);
        assertFalse(supports);
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
    public void shouldProcessHttpInputMessageBeforeReturning() throws IOException {
        HttpInputMessage httpInputMessage = createMockHttpInputMessage();
        org.openmrs.module.emrapi.conditionslist.contract.Condition[] conditionList = createMockConditionList();
        when(conditionConceptSaveService.update(any())).thenReturn(conditionList[0]);
        emrConditionControllerAdvice.beforeBodyRead(httpInputMessage, null, null, null);
        verify(conditionConceptSaveService, times(1)).update(any(org.openmrs.module.emrapi.conditionslist.contract.Condition.class));
    }

    private HttpInputMessage createMockHttpInputMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        org.openmrs.module.emrapi.conditionslist.contract.Condition[] conditions = createMockConditionList();
        return new MappingJacksonInputMessage(new ByteArrayInputStream(objectMapper.writeValueAsString(conditions).getBytes()), new HttpHeaders());
    }
    private org.openmrs.module.emrapi.conditionslist.contract.Condition[] createMockConditionList() {
        org.openmrs.module.emrapi.conditionslist.contract.Condition[] conditionList = new org.openmrs.module.emrapi.conditionslist.contract.Condition[1];
        Condition condition = new Condition();
        conditionList[0] = condition;
        return conditionList;
    }



}
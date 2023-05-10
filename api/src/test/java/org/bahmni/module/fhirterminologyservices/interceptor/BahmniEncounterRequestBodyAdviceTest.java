package org.bahmni.module.fhirterminologyservices.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.bahmnicore.web.v1_0.controller.BahmniEncounterController;
import org.bahmni.module.fhirterminologyservices.api.BahmniDiagnosisAnswerConceptSaveCommand;
import org.bahmni.module.fhirterminologyservices.api.BahmniObservationAnswerConceptSaveCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
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
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class BahmniEncounterRequestBodyAdviceTest {

    @InjectMocks
    BahmniEncounterRequestBodyAdvice bahmniEncounterRequestBodyAdvice;
    @Mock
    BahmniObservationAnswerConceptSaveCommand bahmniObservationAnswerConceptSaveCommand;
    @Mock
    BahmniDiagnosisAnswerConceptSaveCommand bahmniDiagnosisAnswerConceptSaveCommand;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
    }


    @Test
    public  void shouldReturnTrueWhenInterceptedMethodNameMatchesSaveMethodName() throws NoSuchMethodException {
        Method method = BahmniEncounterController.class.getMethod("update", BahmniEncounterTransaction.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = bahmniEncounterRequestBodyAdvice.supports(methodParameter, null, null);
        assertTrue(supports);
    }
    @Test
    public  void shouldReturnFalseWhenInterceptedMethodNameDoesntMatcheSaveMethodName() throws NoSuchMethodException {
        Method method = BahmniEncounterController.class.getMethod("find", BahmniEncounterSearchParameters.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = bahmniEncounterRequestBodyAdvice.supports(methodParameter, null, null);
        assertFalse(supports);
    }
    @Test
    public void shouldReturnSameObjectWhenAfterBodyReadCalled() {
        Object object = new Object();
        Object returnObject = bahmniEncounterRequestBodyAdvice.afterBodyRead(object, null, null, null, null);
        assertNotNull(returnObject);
        assertEquals(object, returnObject);
    }

    @Test
    public void shouldReturnSameObjectWhenHandleEmptyBodyCalled() {
        Object object = new Object();
        Object returnObject = bahmniEncounterRequestBodyAdvice.handleEmptyBody(object, null, null, null, null);
        assertNotNull(returnObject);
        assertEquals(object, returnObject);
    }
    @Test
    public void shouldProcessHttpInputMessageBeforeReturning() throws IOException {
        HttpInputMessage httpInputMessage = createMockHttpInputMessage();
        BahmniEncounterTransaction bahmniEncounterTransaction = createMockBahmniEncounterTransaction();
        when(bahmniDiagnosisAnswerConceptSaveCommand.update(any())).thenReturn(bahmniEncounterTransaction);
        when(bahmniObservationAnswerConceptSaveCommand.update(any())).thenReturn(bahmniEncounterTransaction);
        bahmniEncounterRequestBodyAdvice.beforeBodyRead(httpInputMessage,null, null, null);
        verify(bahmniDiagnosisAnswerConceptSaveCommand, times(1)).update(any(BahmniEncounterTransaction.class));
        verify(bahmniObservationAnswerConceptSaveCommand, times(1)).update(any(BahmniEncounterTransaction.class));
    }

    @Test
    public void shouldInitializeServices() {
        bahmniEncounterRequestBodyAdvice.setBahmniDiagnosisAndObservationCommand(bahmniDiagnosisAnswerConceptSaveCommand, bahmniObservationAnswerConceptSaveCommand);
        assertNotNull(bahmniEncounterRequestBodyAdvice.bahmniDiagnosisAnswerConceptSaveCommand);
        assertNotNull(bahmniEncounterRequestBodyAdvice.bahmniObservationAnswerConceptSaveCommand);
    }
    private HttpInputMessage createMockHttpInputMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        BahmniEncounterTransaction bahmniEncounterTransaction = createMockBahmniEncounterTransaction();
        return new MappingJacksonInputMessage(new ByteArrayInputStream(objectMapper.writeValueAsString(bahmniEncounterTransaction).getBytes()), new HttpHeaders());
    }
    private BahmniEncounterTransaction createMockBahmniEncounterTransaction() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setBahmniDiagnoses(new ArrayList<>());
        bahmniEncounterTransaction.setObservations(new ArrayList<>());
        return bahmniEncounterTransaction;
    }
}
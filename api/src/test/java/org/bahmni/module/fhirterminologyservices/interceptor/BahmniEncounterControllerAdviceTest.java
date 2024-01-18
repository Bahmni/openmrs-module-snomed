package org.bahmni.module.fhirterminologyservices.interceptor;

import org.bahmni.module.bahmnicore.web.v1_0.controller.BahmniEncounterController;
import org.bahmni.module.fhirterminologyservices.api.BahmniDiagnosisAnswerConceptSaveCommand;
import org.bahmni.module.fhirterminologyservices.api.BahmniObservationAnswerConceptSaveCommand;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class BahmniEncounterControllerAdviceTest {

    @InjectMocks
    BahmniEncounterControllerAdvice bahmniEncounterControllerAdvice;
    @Mock
    BahmniObservationAnswerConceptSaveCommand bahmniObservationAnswerConceptSaveCommand;
    @Mock
    BahmniDiagnosisAnswerConceptSaveCommand bahmniDiagnosisAnswerConceptSaveCommand;
    @Mock
    private AdministrationService administrationService;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
    }


    @Test
    public  void shouldReturnTrueWhenInterceptedMethodNameMatchesSaveMethodName() throws NoSuchMethodException {
        Method method = BahmniEncounterController.class.getMethod("update", BahmniEncounterTransaction.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = bahmniEncounterControllerAdvice.supports(methodParameter, null, null);
        assertTrue(supports);
    }
    @Test
    public  void shouldReturnFalseWhenInterceptedMethodNameDoesntMatcheSaveMethodName() throws NoSuchMethodException {
        Method method = BahmniEncounterController.class.getMethod("find", BahmniEncounterSearchParameters.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        boolean supports = bahmniEncounterControllerAdvice.supports(methodParameter, null, null);
        assertFalse(supports);
    }
    @Test
    public void shouldReturnSameObjectWhenAfterBodyReadCalled() {
        Object object = new Object();
        Object returnObject = bahmniEncounterControllerAdvice.afterBodyRead(object, null, null, null, null);
        assertNotNull(returnObject);
        assertEquals(object, returnObject);
    }

    @Test
    public void shouldReturnSameObjectWhenHandleEmptyBodyCalled() {
        Object object = new Object();
        Object returnObject = bahmniEncounterControllerAdvice.handleEmptyBody(object, null, null, null, null);
        assertNotNull(returnObject);
        assertEquals(object, returnObject);
    }
    @Test
    public void shouldProcessHttpInputMessageBeforeReturningIfPropertyIsEnabled() throws IOException {
        HttpInputMessage httpInputMessage = createMockHttpInputMessage();
        BahmniEncounterTransaction bahmniEncounterTransaction = createMockBahmniEncounterTransaction();
        when(administrationService.getGlobalProperty(eq("bahmni.lookupExternalTerminologyServer"))).thenReturn("true");
        when(bahmniDiagnosisAnswerConceptSaveCommand.update(any())).thenReturn(bahmniEncounterTransaction);
        when(bahmniObservationAnswerConceptSaveCommand.update(any())).thenReturn(bahmniEncounterTransaction);
        bahmniEncounterControllerAdvice.beforeBodyRead(httpInputMessage,null, null, null);
        verify(bahmniDiagnosisAnswerConceptSaveCommand, times(1)).update(any(BahmniEncounterTransaction.class));
        verify(bahmniObservationAnswerConceptSaveCommand, times(1)).update(any(BahmniEncounterTransaction.class));
    }

    @Test
    public void shouldNotProcessHttpInputMessageBeforeReturningIfPropertyIsNotEnabled() throws IOException {
        HttpInputMessage httpInputMessage = createMockHttpInputMessage();
        when(administrationService.getGlobalProperty(eq("bahmni.lookupExternalTerminologyServer"))).thenReturn("false");
        bahmniEncounterControllerAdvice.beforeBodyRead(httpInputMessage,null, null, null);
        verify(bahmniDiagnosisAnswerConceptSaveCommand, times(0)).update(any(BahmniEncounterTransaction.class));
        verify(bahmniObservationAnswerConceptSaveCommand, times(0)).update(any(BahmniEncounterTransaction.class));
    }

    @Test
    public void shouldInitializeServices() {
        bahmniEncounterControllerAdvice.setBahmniDiagnosisAndObservationCommand(bahmniDiagnosisAnswerConceptSaveCommand, bahmniObservationAnswerConceptSaveCommand);
        assertNotNull(bahmniEncounterControllerAdvice.bahmniDiagnosisAnswerConceptSaveCommand);
        assertNotNull(bahmniEncounterControllerAdvice.bahmniObservationAnswerConceptSaveCommand);
    }
    private HttpInputMessage createMockHttpInputMessage() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
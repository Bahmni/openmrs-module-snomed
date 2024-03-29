package org.bahmni.module.fhirterminologyservices.interceptor;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.BahmniDiagnosisAnswerConceptSaveCommand;
import org.bahmni.module.fhirterminologyservices.api.BahmniObservationAnswerConceptSaveCommand;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

@ControllerAdvice(assignableTypes = {org.bahmni.module.bahmnicore.web.v1_0.controller.BahmniEncounterController.class})
public class BahmniEncounterControllerAdvice implements RequestBodyAdvice {
    private static Logger logger = Logger.getLogger(BahmniEncounterControllerAdvice.class);

    BahmniObservationAnswerConceptSaveCommand bahmniObservationAnswerConceptSaveCommand;
    BahmniDiagnosisAnswerConceptSaveCommand bahmniDiagnosisAnswerConceptSaveCommand;
    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private static final String UPDATE_METHOD = "update";
    private static final String BAHMNI_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED = "bahmni.lookupExternalTerminologyServer";
    private static final boolean DEFAULT_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED = false;



    @Autowired
    public void setBahmniDiagnosisAndObservationCommand(BahmniDiagnosisAnswerConceptSaveCommand bahmniDiagnosisAnswerConceptSaveCommand, BahmniObservationAnswerConceptSaveCommand bahmniObservationAnswerConceptSaveCommand) {
        this.bahmniDiagnosisAnswerConceptSaveCommand = bahmniDiagnosisAnswerConceptSaveCommand;
        this.bahmniObservationAnswerConceptSaveCommand = bahmniObservationAnswerConceptSaveCommand;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        logger.info("In supports() method of " + getClass().getSimpleName());
        return UPDATE_METHOD.equals(methodParameter.getMethod().getName());
    }

    private boolean isExternalTerminologyServerLookupNeeded() {
        String externalTSLookupNeeded = administrationService.getGlobalProperty(BAHMNI_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED);
        return StringUtils.isNotBlank(externalTSLookupNeeded) ? Boolean.valueOf(externalTSLookupNeeded) : DEFAULT_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        if(!isExternalTerminologyServerLookupNeeded()) {
            return httpInputMessage;
        }
        logger.info("In beforeBodyRead() method of " + getClass().getSimpleName());
        InputStream body = httpInputMessage.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String bodyStr = IOUtils.toString(body, Charset.forName("UTF-8"));
        BahmniEncounterTransaction bahmniEncounterTransaction =  objectMapper
                .readValue(bodyStr, BahmniEncounterTransaction.class);
        bahmniDiagnosisAnswerConceptSaveCommand.update(bahmniEncounterTransaction);
        bahmniObservationAnswerConceptSaveCommand.update(bahmniEncounterTransaction);
        bodyStr = objectMapper.writeValueAsString(bahmniEncounterTransaction);
        return new MappingJacksonInputMessage(new ByteArrayInputStream(bodyStr.getBytes()), httpInputMessage.getHeaders());
    }


    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        logger.info("In handleEmptyBody() method of " + getClass().getSimpleName());
        return body;
    }
}
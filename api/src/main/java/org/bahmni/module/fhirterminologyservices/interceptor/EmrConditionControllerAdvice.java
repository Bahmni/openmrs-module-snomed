package org.bahmni.module.fhirterminologyservices.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.openmrs.module.emrapi.conditionslist.contract.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.http.HttpInputMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;

@ControllerAdvice(assignableTypes = {org.openmrs.module.emrapi.web.controller.ConditionController.class})
public class EmrConditionControllerAdvice implements RequestBodyAdvice {
    private static Logger logger = Logger.getLogger(EmrConditionControllerAdvice.class);

    ConditionConceptSaveService conditionConceptSaveService;

    private static final String SAVE_METHOD = "save";


    @Autowired
    public void setConditionConceptSaveService (ConditionConceptSaveService conditionConceptSaveService) {
        this.conditionConceptSaveService = conditionConceptSaveService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        logger.info("In supports() method of " + getClass().getSimpleName());
        boolean isSupported  = SAVE_METHOD.equals(methodParameter.getMethod().getName());
        return isSupported;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        InputStream body = httpInputMessage.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        String bodyStr = IOUtils.toString(body, Charset.forName("UTF-8"));
        Condition[] conditionList =  objectMapper
                .readValue(bodyStr, new TypeReference<Condition[]>() {
                });
        Arrays.stream(conditionList).forEach(condition -> conditionConceptSaveService.update(condition));
        bodyStr = objectMapper.writeValueAsString(conditionList);
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
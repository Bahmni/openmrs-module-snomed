package org.bahmni.module.fhirterminologyservices.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.openmrs.module.emrapi.conditionslist.contract.Condition;
import org.openmrs.module.emrapi.web.controller.ConditionController;
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
import java.util.List;

@ControllerAdvice
public class ConditionRequestBodyAdvice implements RequestBodyAdvice {
    ConditionConceptSaveService conditionConceptSaveService;


    @Autowired
    public void setConditionConceptSaveService (ConditionConceptSaveService conditionConceptSaveService) {
        this.conditionConceptSaveService = conditionConceptSaveService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        System.out.println("In supports() method of " + getClass().getSimpleName());
        return methodParameter.getContainingClass() == ConditionController.class;
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
    /*
    Update bodyStr as you wish
    */
        return new MappingJacksonInputMessage(new ByteArrayInputStream(bodyStr.getBytes()), httpInputMessage.getHeaders());
//        return httpInputMessage;
    }


    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
//        System.out.println("In afterBodyRead() method of " + getClass().getSimpleName());
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            String bodyContent = objectMapper.writeValueAsString(body);
//            Condition[] conditionList =  objectMapper
//                    .readValue(bodyContent, new TypeReference<Condition[]>() {
//                    });
//            Arrays.stream(conditionList).forEach(condition -> conditionConceptSaveService.update(condition));
//            return objectMapper.writeValueAsString(conditionList);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        if (body instanceof org.openmrs.module.emrapi.conditionslist.contract.Condition[]) {
//            System.out.println("transforming body");
//        }
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        System.out.println("In handleEmptyBody() method of " + getClass().getSimpleName());
        return body;
    }
}
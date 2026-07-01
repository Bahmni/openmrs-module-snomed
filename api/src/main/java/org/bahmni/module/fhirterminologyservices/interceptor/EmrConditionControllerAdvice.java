/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhirterminologyservices.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.http.HttpInputMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

@ControllerAdvice(assignableTypes = {MainResourceController.class})
public class EmrConditionControllerAdvice implements RequestBodyAdvice {
    private static Logger logger = Logger.getLogger(EmrConditionControllerAdvice.class);

    ConditionConceptSaveService conditionConceptSaveService;

    @Autowired
    public void setConditionConceptSaveService (ConditionConceptSaveService conditionConceptSaveService) {
        this.conditionConceptSaveService = conditionConceptSaveService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();
        boolean matched = "POST".equals(request.getMethod())
                && request.getRequestURI().contains("/ws/rest/v1/condition");
        logger.info("In supports() of " + getClass().getSimpleName()
                + " — " + request.getMethod() + " " + request.getRequestURI() + " → matched=" + matched);
        return matched;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {
        InputStream body = httpInputMessage.getBody();
        ObjectMapper objectMapper = new ObjectMapper();

        String bodyStr = IOUtils.toString(body, Charset.forName("UTF-8"));
        Map<String, Object> bodyMap = objectMapper.readValue(bodyStr, new TypeReference<Map<String, Object>>() {});

        Object conditionField = bodyMap.get("condition");
        if (conditionField instanceof Map) {
            Map conditionMap = (Map) conditionField;
            Object codedUuid = conditionMap.get("coded");
            if (codedUuid != null) {
                Concept codedConcept = new Concept();
                codedConcept.setUuid(codedUuid.toString());
                CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
                codedOrFreeText.setCoded(codedConcept);
                Condition condition = new Condition();
                condition.setCondition(codedOrFreeText);
                conditionConceptSaveService.update(condition);
                conditionMap.put("coded", condition.getCondition().getCoded().getUuid());
            }
        }

        bodyStr = objectMapper.writeValueAsString(bodyMap);
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
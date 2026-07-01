/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. SNOMED International. SNOMED International is a registered trademark
 * and the SNOMED International graphic logo is a trademark of SNOMED International.
 */

package org.bahmni.module.fhirterminologyservices.web.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class FhirEncounterBundleFilter implements Filter {

    private static final Logger logger = Logger.getLogger(FhirEncounterBundleFilter.class);
    private static final String SNOMED_SYSTEM = "http://snomed.info/sct";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if ("POST".equals(httpRequest.getMethod())
                && httpRequest.getRequestURI().contains("/ws/fhir2/R4/EncounterBundle")) {
            String body = IOUtils.toString(httpRequest.getInputStream(), Charset.forName("UTF-8"));
            String modifiedBody = resolveTerminologyCodes(body);
            chain.doFilter(wrapWithBody(httpRequest, modifiedBody), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {}

    @SuppressWarnings("unchecked")
    private String resolveTerminologyCodes(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> bundleMap = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> entries = (List<Map<String, Object>>) bundleMap.get("entry");
            if (entries == null) return body;

            ConditionConceptSaveService saveService = getConditionConceptSaveService();
            if (saveService == null) {
                logger.warn("ConditionConceptSaveService unavailable, skipping SNOMED resolution for EncounterBundle");
                return body;
            }

            for (Map<String, Object> entry : entries) {
                Map<String, Object> resource = (Map<String, Object>) entry.get("resource");
                if (resource == null) continue;

                String resourceType = (String) resource.get("resourceType");
                if ("Observation".equals(resourceType)) {
                    resolveObservationValueConcept(resource, saveService);
                } else if ("Condition".equals(resourceType)) {
                    resolveConditionCodeConcept(resource, saveService);
                }
            }
            return objectMapper.writeValueAsString(bundleMap);
        } catch (Exception e) {
            logger.error("Error resolving SNOMED codes in EncounterBundle, passing original body: " + e.getMessage());
            return body;
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveObservationValueConcept(Map<String, Object> resource, ConditionConceptSaveService saveService) {
        Map<String, Object> valueCodeableConcept = (Map<String, Object>) resource.get("valueCodeableConcept");
        if (valueCodeableConcept == null) return;
        List<Map<String, Object>> codings = (List<Map<String, Object>>) valueCodeableConcept.get("coding");
        if (codings == null) return;

        for (Map<String, Object> coding : codings) {
            if (coding.containsKey("system")) continue;
            Object codeObj = coding.get("code");
            if (codeObj == null) continue;

            String code = codeObj.toString();
            int lastSlash = code.lastIndexOf("/");
            if (lastSlash < 0) continue;

            // code is a full URL e.g. "http://snomed.info/sct/225057002"
            String conceptSystem = code.substring(0, lastSlash);
            String conceptCode = code.substring(lastSlash + 1);
            ensureConceptExists(code, saveService);
            coding.put("system", conceptSystem);
            coding.put("code", conceptCode);
            logger.info("Resolved Observation valueCoded: " + code + " → system=" + conceptSystem + " code=" + conceptCode);
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveConditionCodeConcept(Map<String, Object> resource, ConditionConceptSaveService saveService) {
        Map<String, Object> codeElement = (Map<String, Object>) resource.get("code");
        if (codeElement == null) return;
        List<Map<String, Object>> codings = (List<Map<String, Object>>) codeElement.get("coding");
        if (codings == null) return;

        for (Map<String, Object> coding : codings) {
            if (coding.containsKey("system")) continue;
            Object codeObj = coding.get("code");
            if (codeObj == null) continue;

            String code = codeObj.toString();
            if (!code.matches("\\d+")) continue;

            // bare SNOMED code e.g. "283111009" — construct full URL for resolution
            String fullSnomedUrl = SNOMED_SYSTEM + "/" + code;
            ensureConceptExists(fullSnomedUrl, saveService);
            coding.put("system", SNOMED_SYSTEM);
            logger.info("Resolved Condition code: " + code + " → system=" + SNOMED_SYSTEM);
        }
    }

    private void ensureConceptExists(String snomedUrl, ConditionConceptSaveService saveService) {
        try {
            Concept codedConcept = new Concept();
            codedConcept.setUuid(snomedUrl);
            CodedOrFreeText codedOrFreeText = new CodedOrFreeText();
            codedOrFreeText.setCoded(codedConcept);
            org.openmrs.Condition condition = new org.openmrs.Condition();
            condition.setCondition(codedOrFreeText);
            saveService.update(condition);
        } catch (Exception e) {
            logger.error("Failed to ensure concept exists for SNOMED URL: " + snomedUrl + " — " + e.getMessage());
        }
    }

    private ConditionConceptSaveService getConditionConceptSaveService() {
        try {
            List<ConditionConceptSaveService> services = Context.getRegisteredComponents(ConditionConceptSaveService.class);
            return services.isEmpty() ? null : services.get(0);
        } catch (Exception e) {
            logger.warn("Could not get ConditionConceptSaveService: " + e.getMessage());
            return null;
        }
    }

    private HttpServletRequestWrapper wrapWithBody(HttpServletRequest request, String body) {
        byte[] bodyBytes = body.getBytes(Charset.forName("UTF-8"));
        return new HttpServletRequestWrapper(request) {
            @Override
            public ServletInputStream getInputStream() {
                final InputStream is = new ByteArrayInputStream(bodyBytes);
                return new ServletInputStream() {
                    @Override
                    public int read() throws IOException { return is.read(); }
                };
            }
            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getInputStream()));
            }
        };
    }
}

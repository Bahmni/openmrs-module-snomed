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
            byte[] bodyBytes = IOUtils.toByteArray(httpRequest.getInputStream());
            ensureSnomedConceptsExist(bodyBytes);
            chain.doFilter(wrapWithBody(httpRequest, bodyBytes), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {}

    @SuppressWarnings("unchecked")
    private void ensureSnomedConceptsExist(byte[] bodyBytes) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> bundleMap = objectMapper.readValue(bodyBytes, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> entries = (List<Map<String, Object>>) bundleMap.get("entry");
            if (entries == null) return;

            ConditionConceptSaveService saveService = getConditionConceptSaveService();
            if (saveService == null) {
                logger.warn("ConditionConceptSaveService unavailable, skipping SNOMED concept creation for EncounterBundle");
                return;
            }

            for (Map<String, Object> entry : entries) {
                Map<String, Object> resource = (Map<String, Object>) entry.get("resource");
                if (resource == null) continue;
                if (!"Observation".equals(resource.get("resourceType"))) continue;

                Map<String, Object> valueCodeableConcept = (Map<String, Object>) resource.get("valueCodeableConcept");
                if (valueCodeableConcept == null) continue;
                List<Map<String, Object>> codings = (List<Map<String, Object>>) valueCodeableConcept.get("coding");
                if (codings == null) continue;

                for (Map<String, Object> coding : codings) {
                    Object system = coding.get("system");
                    Object code = coding.get("code");
                    if (system == null || code == null) continue;
                    if (!SNOMED_SYSTEM.equals(system.toString())) continue;

                    ensureConceptExists(system + "/" + code, saveService);
                    logger.info("Ensured SNOMED concept exists: system=" + system + " code=" + code);
                }
            }
        } catch (Exception e) {
            logger.error("Error during SNOMED concept creation for EncounterBundle, proceeding with original body: " + e.getMessage());
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

    private HttpServletRequestWrapper wrapWithBody(HttpServletRequest request, final byte[] bodyBytes) {
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
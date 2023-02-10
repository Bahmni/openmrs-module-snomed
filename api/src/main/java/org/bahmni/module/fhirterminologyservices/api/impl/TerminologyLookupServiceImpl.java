package org.bahmni.module.fhirterminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.fhirterminologyservices.api.ErrorConstants;
import org.bahmni.module.fhirterminologyservices.api.GlobalPropertyConstants;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.api.mapper.ValueSetMapper;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.hl7.fhir.r4.model.ValueSet;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;


public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {
    private ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper;

    public TerminologyLookupServiceImpl(ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper) {
        this.vsSimpleObjectMapper = vsSimpleObjectMapper;
    }


    @Override
    public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String lang) {
        try {
            String diagnosisEndPoint = getValueSetEndPoint(getDiagnosisSearchValueSetUrl(), searchTerm, getRecordLimit(limit), getLocaleLanguage(lang), false);
            ValueSet valueSet = fetchValueSet(diagnosisEndPoint);
            return vsSimpleObjectMapper.map(valueSet);
        } catch (UnsupportedEncodingException exception) {
            throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR);
        } catch (FhirClientConnectionException exception) {
            throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVER_NOT_FOUND_ERROR);
        } catch (UnclassifiedServerFailureException exception) {
            if (exception.getStatusCode() == HttpStatus.BAD_GATEWAY.value()) {
                throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVER_NOT_FOUND_ERROR);
            } else {
                throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVER_ERROR);
            }
        } catch (ResourceNotFoundException exception) {
            throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR);
        } catch (InternalErrorException exception) {
            if (exception.getMessage().contains("Search term must have at least 3 characters")) {
                throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVICES_AT_LEAST_THREE_CHARS_VALIDATION_MSG);
            } else {
                throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVER_ERROR);
            }
        }
    }

    private ValueSet fetchValueSet(String valueSetEndPoint) {
        return FhirContext.forR4().newRestfulGenericClient(getTerminologyServerBaseUrl()).read().resource(ValueSet.class).withUrl(valueSetEndPoint).execute();
    }

    private String getValueSetEndPoint(String valueSetUrl, String searchTerm, Integer recordLimit, String localeLanguage, boolean includeDesignations) throws UnsupportedEncodingException, TerminologyServicesException {
        String baseUrl = getTerminologyServerBaseUrl();
        String valueSetUrlTemplate = Context.getAdministrationService().getGlobalProperty(GlobalPropertyConstants.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP);
        if (StringUtils.isNotBlank(valueSetUrlTemplate)) {
            String relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl), encode(searchTerm), recordLimit, localeLanguage, includeDesignations);
            return baseUrl + relativeUrl;
        } else throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR);
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private String getTerminologyServerBaseUrl() {
        return Context.getAdministrationService().getGlobalProperty(GlobalPropertyConstants.TERMINOLOGY_SERVER_URL_GLOBAL_PROP);
    }

    private String getDiagnosisSearchValueSetUrl() throws TerminologyServicesException {
        String diagnosisValueSetUrl = Context.getAdministrationService().getGlobalProperty(GlobalPropertyConstants.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP);
        if (StringUtils.isNotBlank(diagnosisValueSetUrl)) return diagnosisValueSetUrl;
        else throw new TerminologyServicesException(ErrorConstants.TERMINOLOGY_SERVICES_CONFIG_INVALID_ERROR);
    }


    private Integer getRecordLimit(Integer limit) {
        return (limit != null && limit > 0) ? limit : RestUtil.getDefaultLimit();
    }

    private String getLocaleLanguage(String lang) {
        return StringUtils.isNotBlank(lang) ? lang : Context.getLocale().getLanguage();
    }
}
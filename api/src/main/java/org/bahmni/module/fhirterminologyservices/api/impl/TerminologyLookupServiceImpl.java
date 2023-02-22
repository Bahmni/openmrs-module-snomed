package org.bahmni.module.fhirterminologyservices.api.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.fhirterminologyservices.api.Error;
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
import java.util.ArrayList;
import java.util.List;


public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {
    private static Logger logger = Logger.getLogger(TerminologyLookupServiceImpl.class);
    private ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper;

    public TerminologyLookupServiceImpl(ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper) {
        this.vsSimpleObjectMapper = vsSimpleObjectMapper;
    }


    @Override
    public List<SimpleObject> getResponseList(String searchTerm, Integer limit, String lang) {
        List<SimpleObject> responseList = new ArrayList<>();
        if (StringUtils.isNotBlank(searchTerm) && searchTerm.length() > 2) {
            try {
                String diagnosisEndPoint = getValueSetEndPoint(getDiagnosisSearchValueSetUrl(), searchTerm, getRecordLimit(limit), getLocaleLanguage(lang), false);
                ValueSet valueSet = fetchValueSet(diagnosisEndPoint);
                responseList = vsSimpleObjectMapper.map(valueSet);
            } catch (Exception exception) {
                handleException(exception);
            }
        }
        return responseList;
    }

    private ValueSet fetchValueSet(String valueSetEndPoint) {
        return FhirContext.forR4().newRestfulGenericClient(getTerminologyServerBaseUrl()).read().resource(ValueSet.class).withUrl(valueSetEndPoint).execute();
    }

    private String getValueSetEndPoint(String valueSetUrl, String searchTerm, Integer recordLimit, String localeLanguage, boolean includeDesignations) throws UnsupportedEncodingException, TerminologyServicesException {
        String baseUrl = getTerminologyServerBaseUrl();
        String valueSetUrlTemplate = Context.getAdministrationService().getGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP);
        if (StringUtils.isNotBlank(valueSetUrlTemplate)) {
            String relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl), encode(searchTerm), recordLimit, localeLanguage, includeDesignations);
            return baseUrl + relativeUrl;
        } else throw new TerminologyServicesException(Error.TERMINOLOGY_SERVICES_CONFIG_INVALID);
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private String getTerminologyServerBaseUrl() {
        return Context.getAdministrationService().getGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_URL_GLOBAL_PROP);
    }

    private String getDiagnosisSearchValueSetUrl() throws TerminologyServicesException {
        String diagnosisValueSetUrl = Context.getAdministrationService().getGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP);
        if (StringUtils.isNotBlank(diagnosisValueSetUrl)) return diagnosisValueSetUrl;
        else throw new TerminologyServicesException(Error.TERMINOLOGY_SERVICES_CONFIG_INVALID);
    }


    private Integer getRecordLimit(Integer limit) {
        return (limit != null && limit > 0) ? limit : RestUtil.getDefaultLimit();
    }

    private String getLocaleLanguage(String lang) {
        return StringUtils.isNotBlank(lang) ? lang : Context.getLocale().getLanguage();
    }

    private void handleException(Exception exception) {
        Error errorCode = null;
        if (exception instanceof TerminologyServicesException)
            errorCode = ((TerminologyServicesException) exception).getErrorCode();
        else if (exception instanceof UnsupportedEncodingException)
            errorCode = Error.TERMINOLOGY_SERVICES_CONFIG_INVALID;
        else if (exception instanceof FhirClientConnectionException)
            errorCode = Error.TERMINOLOGY_SERVER_NOT_FOUND;
        else if (exception instanceof ResourceNotFoundException)
            errorCode = Error.TERMINOLOGY_SERVICES_CONFIG_INVALID;
        else if (exception instanceof UnclassifiedServerFailureException) {
            UnclassifiedServerFailureException unclassifiedServerFailureException = (UnclassifiedServerFailureException) exception;
            if (unclassifiedServerFailureException.getStatusCode() == HttpStatus.BAD_GATEWAY.value()) {
                errorCode = Error.TERMINOLOGY_SERVER_NOT_FOUND;
            } else {
                errorCode = Error.TERMINOLOGY_SERVER_ERROR;
            }
        } else errorCode = Error.TERMINOLOGY_SERVER_ERROR;

        logger.error(errorCode.message, exception);
        throw new TerminologyServicesException(errorCode, exception);
    }
}
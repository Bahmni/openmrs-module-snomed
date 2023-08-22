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
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.http.HttpStatus;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TerminologyLookupServiceImpl extends BaseOpenmrsService implements TerminologyLookupService {
    private static Logger logger = Logger.getLogger(TerminologyLookupServiceImpl.class);
    private FhirContext fhirContext = FhirContext.forR4();
    private ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper;
    private ValueSetMapper<Concept> vsConceptMapper;

    public TerminologyLookupServiceImpl(ValueSetMapper<List<SimpleObject>> vsSimpleObjectMapper, ValueSetMapper<Concept> vsConceptMapper) {
        this.vsSimpleObjectMapper = vsSimpleObjectMapper;
        this.vsConceptMapper = vsConceptMapper;
    }

    @Override
    public List<SimpleObject> searchConcepts(String searchTerm, Integer limit, String lang) {
        if (StringUtils.isBlank(searchTerm) || searchTerm.length() < 3) {
            return new ArrayList();
        }
        ValueSet valueSet = null;
        try {
            String diagnosisEndPoint = getValueSetEndPoint(getDiagnosisSearchVSUrl(), searchTerm, getRecordLimit(limit), getLocaleLanguage(lang), false);
            valueSet = fetchValueSet(diagnosisEndPoint);
        } catch (Exception exception) {
            handleException(exception);
        }
        return vsSimpleObjectMapper.map(valueSet);
    }

    @Override
    public List<SimpleObject> searchConcepts(String valueSetUrl, String lang, String searchTerm, Integer limit) {
        if (searchTerm == null) {
          searchTerm = "";
        }
        ValueSet valueSet = null;
        try {
            String valueSetEndPoint = getValueSetEndPoint(getObservationSearchVSUrl(), valueSetUrl, getLocaleLanguage(lang), OBSERVATION_FORMAT, searchTerm, getRecordLimit(limit));
            valueSet = fetchValueSet(valueSetEndPoint);
        } catch (Exception exception) {
            handleException(exception);
        }
        return vsSimpleObjectMapper.map(valueSet);
    }

    @Override
    public Concept getConcept(String conceptCode, String locale) {
        String urlParamTemplate = Context.getAdministrationService().getGlobalProperty(CONCEPT_DETAILS_URL_GLOBAL_PROP);
        String conceptUrlParam = MessageFormat.format(urlParamTemplate, conceptCode);

        ValueSet valueSet = null;
        try {
            String diagnosisEndPoint = getValueSetEndPoint(conceptUrlParam, conceptCode, 1, locale, true);
            valueSet = fetchValueSet(diagnosisEndPoint);
        } catch (Exception exception) {
            handleException(exception);
        }
        return vsConceptMapper.map(valueSet);

    }

    @Override
    public ValueSet getValueSetByPageSize(String valueSetId, String locale, Integer pageSize, Integer offset) {
        ValueSet valueSet = null;
        try {
            String valueSetUrlTemplate = getTSGlobalProperty(TerminologyLookupService.DIAGNOSIS_COUNT_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP);
            String url = MessageFormat.format(valueSetUrlTemplate, encode(getTSGlobalProperty(PROCEDURE_VALUESET_URL_GLOBAL_PROP) + valueSetId), "", locale, pageSize, offset);
            valueSet = fetchValueSet(url);
        } catch (Exception exception) {
            handleException(exception);
        }
        return valueSet;
    }

    @Override
    public ValueSet searchTerminologyCodes(String snomedCode, Integer pageSize, Integer offset, String locale){
        String baseUrl = getTSBaseUrl();
        String valueSetUrl = getTSGlobalProperty(TerminologyLookupService.DIAGNOSIS_COUNT_VALUE_SET_URL_GLOBAL_PROP);
        String valueSetUrlTemplate = getTSGlobalProperty(TerminologyLookupService.DIAGNOSIS_COUNT_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP);

        String relativeUrl = null;
        try {
            relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl), snomedCode, locale, pageSize, offset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String diagnosisEndPoint = baseUrl + relativeUrl;
        return fetchValueSet(diagnosisEndPoint);
    }

    private String getValueSetEndPoint(String valueSetUrl, String searchTerm, Integer recordLimit, String localeLanguage, boolean includeDesignations) throws UnsupportedEncodingException, TerminologyServicesException {
        String baseUrl = getTSBaseUrl();
        String valueSetUrlTemplate = getVSUrlTemplate();
        String relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl), encode(searchTerm), recordLimit, localeLanguage, includeDesignations);
        return baseUrl + relativeUrl;
    }
    private String getValueSetEndPoint(String valueSetUrlTemplate, String valueSetUrl, String localeLanguage, String format, String searchTerm, Integer limit) throws UnsupportedEncodingException, TerminologyServicesException {
        String baseUrl = getTSBaseUrl();
        String relativeUrl = MessageFormat.format(valueSetUrlTemplate, encode(valueSetUrl), localeLanguage, format, encode(searchTerm), limit);
        return baseUrl + relativeUrl;
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private String getTSBaseUrl() {
        return getTSGlobalProperty(TerminologyLookupService.TERMINOLOGY_SERVER_BASE_URL_GLOBAL_PROP);
    }

    private String getDiagnosisSearchVSUrl() throws TerminologyServicesException {
        return getTSGlobalProperty(TerminologyLookupService.DIAGNOSIS_SEARCH_VALUE_SET_URL_GLOBAL_PROP);
    }
    private String getObservationSearchVSUrl() throws TerminologyServicesException {
        return getTSGlobalProperty(TerminologyLookupService.OBSERVATION_VALUE_SET_URL_GLOBAL_PROP);
    }

    private String getVSUrlTemplate() {
        return getTSGlobalProperty(TerminologyLookupService.FHIR_VALUE_SET_URL_TEMPLATE_GLOBAL_PROP);
    }

    private String getTSGlobalProperty(String propertyName) {
        String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName);
        if (StringUtils.isBlank(propertyValue))
            throw new TerminologyServicesException();
        return propertyValue;
    }

    private ValueSet fetchValueSet(String valueSetEndPoint) {
        IRestfulClientFactory iRestfulClientFactory = fhirContext.getRestfulClientFactory();
        iRestfulClientFactory.setSocketTimeout(30*60*1000);
        return iRestfulClientFactory.newGenericClient(getTSBaseUrl()).read().resource(ValueSet.class).withUrl(valueSetEndPoint).execute();
    }

    private Integer getRecordLimit(Integer limit) {
        return (limit != null && limit > 0) ? limit : RestUtil.getDefaultLimit();
    }

    private String getLocaleLanguage(String lang) {
        return StringUtils.isNotBlank(lang) ? lang : Context.getLocale().getLanguage();
    }

    private void handleException(Exception exception) {
        Error error = Error.TERMINOLOGY_SERVER_ERROR;
        if (exception instanceof TerminologyServicesException)
            error = Error.TERMINOLOGY_SERVICES_CONFIG_MISSING;
        else if (exception instanceof UnsupportedEncodingException)
            error = Error.TERMINOLOGY_SERVICES_CONFIG_MISSING;
        else if (exception instanceof FhirClientConnectionException)
            error = Error.TERMINOLOGY_SERVER_NOT_FOUND;
        else if (exception instanceof ResourceNotFoundException)
            error = Error.TERMINOLOGY_SERVICES_CONFIG_MISSING;
        else if (exception instanceof UnclassifiedServerFailureException) {
            UnclassifiedServerFailureException unclassifiedServerFailureException = (UnclassifiedServerFailureException) exception;
            if (unclassifiedServerFailureException.getStatusCode() == HttpStatus.BAD_GATEWAY.value()) {
                error = Error.TERMINOLOGY_SERVER_NOT_FOUND;
            }
        }
        logger.error(error.message, exception);
        throw new TerminologyServicesException();
    }
}
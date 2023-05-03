package org.bahmni.module.fhirterminologyservices.web.advisor;

import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;

@Component
public class ConceptSearchInterceptor implements AfterReturningAdvice {
    private static final List<String> SAVE_ADDRESS_HIERARCY_ENTRY_METHODS = asList("getConcepts");
    private TerminologyLookupService terminologyLookupService;
    private BahmniDiagnosisService bahmniDiagnosisService;

    @Autowired
    public void setTerminologyLookupServiceAndBahmniDiagnosisService(TerminologyLookupService terminologyLookupService, BahmniDiagnosisService bahmniDiagnosisService) {
        this.terminologyLookupService = terminologyLookupService;
        this.bahmniDiagnosisService = bahmniDiagnosisService;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Exception {
        if (SAVE_ADDRESS_HIERARCY_ENTRY_METHODS.contains(method.getName())) {
            boolean externalTerminologyServerLookupNeeded = bahmniDiagnosisService.isExternalTerminologyServerLookupNeeded();
            if(externalTerminologyServerLookupNeeded) {
                returnValue = terminologyLookupService.getResponseList((String) arguments[0], (Integer) arguments[1], (String) arguments[2]);
            }
        }
    }
}

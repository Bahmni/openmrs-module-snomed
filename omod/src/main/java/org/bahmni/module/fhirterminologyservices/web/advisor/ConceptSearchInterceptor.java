package org.bahmni.module.fhirterminologyservices.web.advisor;

import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ConceptSearchInterceptor implements AfterReturningAdvice {
    private static final List<String> SAVE_ADDRESS_HIERARCY_ENTRY_METHODS = asList("getConcepts");

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Exception {
        if (SAVE_ADDRESS_HIERARCY_ENTRY_METHODS.contains(method.getName())) {
            TerminologyLookupService terminologyLookupService = getTerminologyLookupService();
            BahmniDiagnosisService bahmniDiagnosisService = getBahmniDiagnosisService();
            System.out.println("bahmniDiagnosisService hash code" + bahmniDiagnosisService.hashCode());
            System.out.println("terminologyLookupService hash code" + terminologyLookupService.hashCode());
            boolean externalTerminologyServerLookupNeeded = bahmniDiagnosisService.isExternalTerminologyServerLookupNeeded();
            if (externalTerminologyServerLookupNeeded) {
                ((ArrayList<SimpleObject>) returnValue).addAll(terminologyLookupService.getResponseList((String) arguments[0], (Integer) arguments[1], (String) arguments[2]));
            }
        }
    }


    private TerminologyLookupService getTerminologyLookupService() {
        return Context.getService(TerminologyLookupService.class);
    }

    private BahmniDiagnosisService getBahmniDiagnosisService() {
        return Context.getService(BahmniDiagnosisService.class);
    }
}

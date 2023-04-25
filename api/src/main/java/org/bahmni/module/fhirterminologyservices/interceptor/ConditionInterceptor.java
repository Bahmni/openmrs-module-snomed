package org.bahmni.module.fhirterminologyservices.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.bahmni.module.fhirterminologyservices.wrapper.ConditionInterceptorWrapper;
import org.openmrs.module.emrapi.conditionslist.contract.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

@Component
public class ConditionInterceptor extends HandlerInterceptorAdapter  {

    protected final Log log = LogFactory.getLog(getClass());

    ConditionConceptSaveService conditionConceptSaveService;


    @Autowired
    public void setConditionConceptSaveService (ConditionConceptSaveService conditionConceptSaveService) {
        this.conditionConceptSaveService = conditionConceptSaveService;
    }

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
            throws Exception  {
//        final ConditionInterceptorWrapper wrappedRequest = new ConditionInterceptorWrapper(request);
//        String body = extractPostRequestBody(wrappedRequest);
////
////
//        if(!"".equals(body))
//        {
//            List<Condition> conditionList =  new ObjectMapper()
//                .readValue(body, new TypeReference<List<Condition>>() {
//                });
//        conditionList.stream().forEach(condition -> conditionConceptSaveService.update(condition));
//        }
       return true;
    }

    private static String extractPostRequestBody(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = null;
            try {
                s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }


}

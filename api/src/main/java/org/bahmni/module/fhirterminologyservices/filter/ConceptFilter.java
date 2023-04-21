package org.bahmni.module.fhirterminologyservices.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.bahmni.module.fhirterminologyservices.api.impl.ConditionConceptSaveImpl;
import org.bahmni.module.fhirterminologyservices.wrapper.ConditionWrapper;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.conditionslist.contract.Condition;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
@Component
public class ConceptFilter  extends HandlerInterceptorAdapter  {

    protected final Log log = LogFactory.getLog(getClass());

    ConditionConceptSaveService conditionConceptSaveService;


    @Autowired
    public void setConditionConceptSaveService (ConditionConceptSaveService conditionConceptSaveService) {
        this.conditionConceptSaveService = conditionConceptSaveService;
    }

    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
            throws Exception  {
//        ConditionWrapper wrappedRequest = new ConditionWrapper(
//                (HttpServletRequest) request);
//
//        String body = IOUtils.toString(wrappedRequest.getReader());
//
//
//        if(!"".equals(body))
//        {
//            List<Condition> conditionList =  new ObjectMapper()
//                .readValue(body, new TypeReference<List<Condition>>() {
//                });
//        conditionList.stream().forEach(condition -> conditionConceptSaveService.update(condition));
//            wrappedRequest.resetInputStream(conditionList.toString().getBytes());
//
//        }
       return true;
    }


}

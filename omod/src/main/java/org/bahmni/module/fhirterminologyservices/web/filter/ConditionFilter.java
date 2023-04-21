package org.bahmni.module.fhirterminologyservices.web.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.fhirterminologyservices.api.ConditionConceptSaveService;
import org.bahmni.module.fhirterminologyservices.wrapper.ConditionWrapper;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.conditionslist.contract.Condition;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
@Component
public class ConditionFilter implements Filter {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    ConditionConceptSaveService conditionConceptSaveService;


    @Override
    public void init(FilterConfig arg0) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        log.debug("Initializing ConditionFilter");
    }

    @Override
    public void destroy() {
        log.debug("Destroying ConditionFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(conditionConceptSaveService == null) {
            ServletContext servletContext =  ((HttpServletRequest) request).getSession().getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            if(webApplicationContext != null) {
                conditionConceptSaveService = webApplicationContext.getBean(ConditionConceptSaveService.class);
            }
        }
        ConditionWrapper wrappedRequest = new ConditionWrapper(
                (HttpServletRequest) request);

        String body = IOUtils.toString(wrappedRequest.getReader());


        if(!"".equals(body))
        {ObjectMapper objectMapper = new ObjectMapper();
            List<Condition> conditionList =  objectMapper
                    .readValue(body, new TypeReference<List<Condition>>() {
                    });
            conditionList.stream().forEach(condition -> conditionConceptSaveService.update(condition));
            wrappedRequest.resetInputStream(objectMapper.writeValueAsString(conditionList).getBytes());
        }

        chain.doFilter(wrappedRequest, response);
    }
}

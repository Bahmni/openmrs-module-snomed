package org.bahmni.module.fhirterminologyservices.web.controller;

import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/terminologyServices")
public class TerminologyLookupController extends BaseRestController {

    @Autowired
    private TerminologyLookupService terminologyLookupService;

    @RequestMapping(value = "/searchDiagnosis", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> searchDiagnosis(@RequestParam(value = "term") String searchTerm, @RequestParam Integer limit,
                                                  @RequestParam(required = false) String locale) {
        try {
            return new ResponseEntity<>(terminologyLookupService.getResponseList(searchTerm, limit, locale), HttpStatus.OK);
        } catch (TerminologyServicesException exception) {
            return new ResponseEntity<>(wrapErrorResponse(null, exception.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private SimpleObject wrapErrorResponse(String code, String reason) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (reason != null && !"".equals(reason)) {
            map.put("message", reason);
        }
        if (code != null && !"".equals(code)) {
            map.put("code", code);
        }
        return (new SimpleObject()).add("error", map);
    }
}

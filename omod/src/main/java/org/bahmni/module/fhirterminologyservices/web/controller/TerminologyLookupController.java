package org.bahmni.module.fhirterminologyservices.web.controller;

import org.bahmni.module.fhirterminologyservices.utils.TerminologyServicesException;
import org.bahmni.module.fhirterminologyservices.utils.WebUtils;
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
import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;

import java.io.IOException;

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
        } catch (TerminologyServicesException | IOException e){
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null,e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}

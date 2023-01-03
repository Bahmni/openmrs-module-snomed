package org.bahmni.module.terminologyservices.web.controller;

import org.bahmni.module.terminologyservices.api.Constants;
import org.bahmni.module.terminologyservices.utils.WebUtils;
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
import org.bahmni.module.terminologyservices.api.TerminologyInitiatorService;

import static org.springframework.web.bind.annotation.ValueConstants.DEFAULT_NONE;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/terminologyServices")
public class TerminologyServicesController extends BaseRestController {
	
	@Autowired
	private TerminologyInitiatorService terminologyInitiatorService;
	
	@RequestMapping(value = "/searchDiagnosis", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> searchDiagnosis(@RequestParam(value = "term", required = true) String diagnosis,  @RequestParam Integer limit,
                                             @RequestParam(required = false, defaultValue = DEFAULT_NONE) String locale) {
        String mockDiagnosis = Constants.MOCK_DIAGNOSES_SEARCH_TERM;
        if(mockDiagnosis.contains(diagnosis)) {
            return new ResponseEntity<>(terminologyInitiatorService.getResponseList(diagnosis, limit, locale), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null,Constants.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}

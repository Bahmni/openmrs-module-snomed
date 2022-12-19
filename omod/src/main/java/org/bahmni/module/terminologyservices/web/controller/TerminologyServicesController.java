package org.bahmni.module.terminologyservices.web.controller;

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

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/terminology-services")
public class TerminologyServicesController extends BaseRestController {
	
	@Autowired
	private TerminologyInitiatorService terminologyServicesService;
	
	@RequestMapping(value = "/search-diagnosis", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> searchDiagnosis(@RequestParam(value = "diagnosis", required = true) String diagnosis) {
        String mockDiagnosis = "Malaria";
        if(mockDiagnosis.equals(diagnosis)) {
            return new ResponseEntity<>(terminologyServicesService.getTerminologyServicesServerUrl(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(terminologyServicesService.getTerminologyServicesServerUrl(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}

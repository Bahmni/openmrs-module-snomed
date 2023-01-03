package org.bahmni.module.terminologyservices.utils;

import org.bahmni.module.terminologyservices.api.Constants;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class WebUtilsTest {
    @Test
    public void shouldCreateAppropriateResponse() {
        SimpleObject response = WebUtils.wrapErrorResponse(null, Constants.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE);
        assertNotNull(response);
        LinkedHashMap errorBody  = response.get("error");
        assertNotNull(errorBody);
        assertNull(errorBody.get("code"));
        assertEquals("The Terminology server is unavailable at the moment, Please try again after sometime", errorBody.get("message"));
    }

}
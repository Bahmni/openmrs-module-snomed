package org.bahmni.module.terminologyservices.utils;

import org.bahmni.module.terminologyservices.api.Constants;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class WebUtilsTest {
    @Test
    public void shouldCreateAppropriateResponse() {
        SimpleObject response = WebUtils.wrapErrorResponse("test", Constants.TERMINOLOGY_SERVER_DOWN_ERROR_MESSAGE);
        assertNotNull(response);
        LinkedHashMap errorBody  = response.get("error");
        assertNotNull(errorBody);
        assertEquals("test",errorBody.get("code"));

        assertEquals("TERMINOLOGY_SERVER_ERROR_MESSAGE", errorBody.get("message"));
    }
    @Test
    public void shouldCreateAppropriateResponseWhenCodeAndMessageIsNull() {
        SimpleObject response = WebUtils.wrapErrorResponse(null, null);
        assertNotNull(response);
        LinkedHashMap errorBody  = response.get("error");
        assertNotNull(errorBody);
        assertNull(errorBody.get("code"));
        assertNull(errorBody.get("message"));
    }
    @Test
    public void shouldCreateAppropriateResponseWhenCodeAndMessageIsEmpty() {
        SimpleObject response = WebUtils.wrapErrorResponse("", "");
        assertNotNull(response);
        LinkedHashMap errorBody  = response.get("error");
        assertNotNull(errorBody);
        assertNull(errorBody.get("code"));
        assertNull(errorBody.get("message"));
    }

}
package org.bahmni.module.terminologyservices.utils;

import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.LinkedHashMap;

public class WebUtils {
    private WebUtils(){
    }

    public static SimpleObject wrapErrorResponse(String code, String reason) {
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

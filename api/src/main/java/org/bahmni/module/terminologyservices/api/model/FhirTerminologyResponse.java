package org.bahmni.module.terminologyservices.api.model;

import org.json.JSONObject;
import org.springframework.http.converter.json.GsonBuilderUtils;

import java.util.Map;
import java.util.stream.Collectors;

public class FhirTerminologyResponse {
    private String resourceType;
    private String url;
    private FhirExpansion expansion;

    public FhirTerminologyResponse() {
    }

    public FhirTerminologyResponse(String resourceType, String url, FhirExpansion expansion) {
        this.resourceType = resourceType;
        this.url = url;
        this.expansion = expansion;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FhirExpansion getExpansion() {
        return expansion;
    }

    public void setExpansion(FhirExpansion expansion) {
        this.expansion = expansion;
    }
}

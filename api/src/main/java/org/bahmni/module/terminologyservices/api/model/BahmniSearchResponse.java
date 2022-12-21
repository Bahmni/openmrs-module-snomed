package org.bahmni.module.terminologyservices.api.model;

public class BahmniSearchResponse {
    private String conceptName;
    private String conceptUuid;
    private String matchedName;

    public BahmniSearchResponse() {
    }

    public BahmniSearchResponse(String conceptName, String conceptUuid, String matchedName) {
        this.conceptName = conceptName;
        this.conceptUuid = conceptUuid;
        this.matchedName = matchedName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    public String getMatchedName() {
        return matchedName;
    }

    public void setMatchedName(String matchedName) {
        this.matchedName = matchedName;
    }
}

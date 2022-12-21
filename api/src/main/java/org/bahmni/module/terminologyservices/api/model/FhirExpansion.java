package org.bahmni.module.terminologyservices.api.model;

import java.util.List;

public class FhirExpansion {
    private Integer total;
    private Integer offset;
    private List<FhirContains> contains;

    public FhirExpansion() {
    }

    public FhirExpansion(Integer total, Integer offset, List<FhirContains> contains) {
        this.total = total;
        this.offset = offset;
        this.contains = contains;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<FhirContains> getContains() {
        return contains;
    }

    public void setContains(List<FhirContains> contains) {
        this.contains = contains;
    }
}

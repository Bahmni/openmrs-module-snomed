package org.bahmni.module.fhirterminologyservices.api.impl;

import java.util.List;

public class TSPageObject {
    Integer total;
    List<String> codes;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }
}
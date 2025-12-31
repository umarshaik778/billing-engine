package com.example.billing.model.audit;

import java.util.List;
import java.util.Map;

public class AuditTrail {

    private String transformationId;
    private Map<String, String> fieldMappings;
    private List<String> formulaApplications;
    private List<String> validationErrors;

    public String getTransformationId() {
        return transformationId;
    }

    public void setTransformationId(String transformationId) {
        this.transformationId = transformationId;
    }

    public Map<String, String> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(Map<String, String> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public List<String> getFormulaApplications() {
        return formulaApplications;
    }

    public void setFormulaApplications(List<String> formulaApplications) {
        this.formulaApplications = formulaApplications;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}

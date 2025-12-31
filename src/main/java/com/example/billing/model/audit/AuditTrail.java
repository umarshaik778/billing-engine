package com.example.billing.model.audit;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuditTrail {

    private String transformationId;
    private Map<String, String> fieldMappings;
    private List<String> formulaApplications;
    private List<String> validationErrors;

}

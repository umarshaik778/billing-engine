package com.example.billing.model.output;

import com.example.billing.model.audit.AuditTrail;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillOutput {

    private String accountNumber;
    private String billingDate;
    private String jurisdiction;

    private BigDecimal totalBaseCharges;
    private BigDecimal totalDue;

    private Map<String, BigDecimal> taxBreakdown;
    private Map<String, BigDecimal> deliveryCharges;

    private BigDecimal lateFee;

    private BigDecimal promoCredit;

    private List<MeterOutput> meters;

    private AuditTrail auditTrail;

}

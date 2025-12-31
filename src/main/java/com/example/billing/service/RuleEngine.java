package com.example.billing.service;

import com.example.billing.model.audit.AuditTrail;
import com.example.billing.model.input.BillInput;
import com.example.billing.model.output.BillOutput;
import com.example.billing.model.output.MeterOutput;
import com.example.billing.model.output.ReadingOutput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class RuleEngine {

    /* ================= RATES ================= */

    private static final BigDecimal TIER1_RATE = new BigDecimal("0.05");   // 0–500
    private static final BigDecimal TIER2_RATE = new BigDecimal("0.07");   // 501–2000
    private static final BigDecimal TIER3_RATE = new BigDecimal("0.09");   // 2001+

    private static final BigDecimal NY_STATE_TAX = new BigDecimal("0.08875");
    private static final BigDecimal NYC_LOCAL_TAX = new BigDecimal("0.045");
    private static final BigDecimal MTA_TAX = new BigDecimal("0.00375");

    public BillOutput applyRules(BillInput input) {

        /* ================= INIT ================= */

        BillOutput output = new BillOutput();
        AuditTrail audit = new AuditTrail();

        audit.setTransformationId(UUID.randomUUID().toString());
        audit.setFormulaApplications(new ArrayList<>());
        audit.setValidationErrors(new ArrayList<>());

        List<String> formulas = audit.getFormulaApplications();

        /* ================= BASIC MAPPING ================= */

        output.setAccountNumber(input.getHeader().getAccountNo());
        output.setBillingDate(input.getHeader().getIssueDate());
        output.setJurisdiction(input.getHeader().getJurisdiction());

        Map<String, String> mappings = new HashMap<>();
        mappings.put("header.accountNo", "accountNumber");
        mappings.put("header.issueDate", "billingDate");
        mappings.put("header.jurisdiction", "jurisdiction");
        audit.setFieldMappings(mappings);

        /* ================= RULE 1: TIERED DELIVERY ================= */

        BigDecimal tier1 = BigDecimal.ZERO;
        BigDecimal tier2 = BigDecimal.ZERO;
        BigDecimal tier3 = BigDecimal.ZERO;
        BigDecimal totalUsage = BigDecimal.ZERO;

        List<MeterOutput> meters = new ArrayList<>();

        for (BillInput.ServicePoint sp : input.getHeader().getServicePoints()) {
            for (BillInput.Meter meter : sp.getMeters()) {
                for (BillInput.Reading r : meter.getReadings()) {

                    BigDecimal usage = BigDecimal.valueOf(r.getUsageKwh());
                    totalUsage = totalUsage.add(usage);

                    /* ---- Reading Output ---- */
                    ReadingOutput ro = new ReadingOutput();
                    ro.setUsageKwh(r.getUsageKwh());

                    /* ---- Meter Output ---- */
                    MeterOutput mo = new MeterOutput(
                            meter.getId(),
                            r.getUsageKwh(),
                            r.getRateTier(),
                            BigDecimal.ZERO,
                            Collections.singletonList(ro)
                    );
                    meters.add(mo);

                    /* ---- Tier math ---- */
                    BigDecimal remaining = usage;

                    BigDecimal u1 = remaining.min(new BigDecimal("500"));
                    tier1 = tier1.add(u1.multiply(TIER1_RATE));
                    remaining = remaining.subtract(u1);

                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal u2 = remaining.min(new BigDecimal("1500"));
                        tier2 = tier2.add(u2.multiply(TIER2_RATE));
                        remaining = remaining.subtract(u2);
                    }

                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        tier3 = tier3.add(remaining.multiply(TIER3_RATE));
                    }
                }
            }
        }

        tier1 = tier1.setScale(2, RoundingMode.HALF_UP);
        tier2 = tier2.setScale(2, RoundingMode.HALF_UP);
        tier3 = tier3.setScale(2, RoundingMode.HALF_UP);

        BigDecimal baseCharges =
                tier1.add(tier2).add(tier3).setScale(2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> delivery = new HashMap<>();
        delivery.put("tier1_0_500", tier1);
        delivery.put("tier2_501_2000", tier2);
        delivery.put("tier3_2001_plus", tier3);

        output.setDeliveryCharges(delivery);
        output.setTotalBaseCharges(baseCharges);

        formulas.add("deliveryCharge_Σ_tiers");

        /* ================= RULE 2–4: TAX STACK ================= */

        BigDecimal nyStateTax =
                baseCharges.multiply(NY_STATE_TAX).setScale(2, RoundingMode.HALF_UP);

        BigDecimal nycTax = BigDecimal.ZERO;
        if ("NYC".equalsIgnoreCase(output.getJurisdiction())) {
            nycTax = baseCharges.multiply(NYC_LOCAL_TAX)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal mtaTax =
                baseCharges.multiply(MTA_TAX).setScale(2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> taxes = new HashMap<>();
        taxes.put("nyStateTax", nyStateTax);
        taxes.put("nycLocalTax", nycTax);
        taxes.put("mtaSurcharge", mtaTax);

        output.setTaxBreakdown(taxes);

        formulas.add("tax_ny_state_8.875%");
        if (nycTax.compareTo(BigDecimal.ZERO) > 0) {
            formulas.add("tax_nyc_4.5%");
            formulas.add("tax_mta_0.375%");
        }

        /* ================= RULE 5: PROMO CREDIT ================= */

        BigDecimal promoCredit = BigDecimal.ZERO;

        if (totalUsage.compareTo(new BigDecimal("10000")) > 0) {
            promoCredit =
                    baseCharges.multiply(new BigDecimal("0.02"))
                            .setScale(2, RoundingMode.HALF_UP)
                            .negate();

            formulas.add("promoCredit_usage_gt_10000_2%");
        }

        output.setPromoCredit(promoCredit);

        /* ================= RULE 6: LATE FEE ================= */

        BigDecimal lateFee = BigDecimal.ZERO;
        BigDecimal previousAmount = BigDecimal.ZERO;

        if (input.getHeader().getPreviousBalance() != null) {

            Integer overdueDays =
                    input.getHeader().getPreviousBalance().getOverdue();

            BigDecimal prev =
                    input.getHeader().getPreviousBalance().getAmount();

            previousAmount = (prev != null) ? prev : BigDecimal.ZERO;

            if (overdueDays != null && overdueDays > 30) {
                lateFee =
                        previousAmount
                                .multiply(new BigDecimal("0.015"))
                                .multiply(BigDecimal.valueOf(overdueDays))
                                .setScale(2, RoundingMode.HALF_UP);

                formulas.add("lateFee_1.5pct_per_day_" + overdueDays + "_days");
            }
        }

        output.setLateFee(lateFee);

        /* ================= FINAL TOTAL ================= */

        BigDecimal totalDue =
                baseCharges
                        .add(nyStateTax)
                        .add(nycTax)
                        .add(mtaTax)
                        .add(previousAmount)
                        .add(lateFee)
                        .add(promoCredit)
                        .setScale(2, RoundingMode.HALF_UP);

        output.setTotalDue(totalDue);
        output.setMeters(meters);
        output.setAuditTrail(audit);

        return output;
    }
}

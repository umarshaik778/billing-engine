package com.example.billing.model.output;

import java.math.BigDecimal;
import java.util.List;

public class MeterOutput {

    private String meterId;
    private double usageKwh;
    private String rateTier;
    private BigDecimal adjustment;
    private List<ReadingOutput> readings;

//    public MeterOutput() {}

    public MeterOutput(
            String meterId,
            double usageKwh,
            String rateTier,
            BigDecimal adjustment,
            List<ReadingOutput> readings
    ) {
        this.meterId = meterId;
        this.usageKwh = usageKwh;
        this.rateTier = rateTier;
        this.adjustment = adjustment;
        this.readings = readings;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public double getUsageKwh() {
        return usageKwh;
    }

    public void setUsageKwh(double usageKwh) {
        this.usageKwh = usageKwh;
    }

    public String getRateTier() {
        return rateTier;
    }

    public void setRateTier(String rateTier) {
        this.rateTier = rateTier;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public List<ReadingOutput> getReadings() {
        return readings;
    }

    public void setReadings(List<ReadingOutput> readings) {
        this.readings = readings;
    }
}

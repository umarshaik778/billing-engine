package com.example.billing.model.output;

public class ReadingOutput {

    private double usageKwh;

    public ReadingOutput() {}

    public ReadingOutput( double usageKwh) {
        this.usageKwh = usageKwh;
    }



    public double getUsageKwh() {
        return usageKwh;
    }

    public void setUsageKwh(double usageKwh) {
        this.usageKwh = usageKwh;
    }
}

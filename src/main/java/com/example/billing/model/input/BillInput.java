package com.example.billing.model.input;

import com.fasterxml.jackson.dataformat.xml.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JacksonXmlRootElement(localName = "bill")
public class BillInput {

    @JacksonXmlProperty(localName = "header")
    private Header header;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }


    public static class PreviousBalance {

        @JacksonXmlProperty(isAttribute = true)
        private int overdue;

        @JacksonXmlProperty(isAttribute = true)
        private BigDecimal amount;

        public int getOverdue() {
            return overdue;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }


    /* ================= HEADER ================= */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Header {

        private String accountNo;
        private String issueDate;
        private String jurisdiction;
        @JacksonXmlProperty(localName = "previousBalance")
        private PreviousBalance previousBalance;

        @JacksonXmlElementWrapper(localName = "servicePoints")
        @JacksonXmlProperty(localName = "servicePoint")
        private List<ServicePoint> servicePoints;

        public String getAccountNo() {
            return accountNo;
        }

        public void setAccountNo(String accountNo) {
            this.accountNo = accountNo;
        }

        public String getIssueDate() {
            return issueDate;
        }

        public void setIssueDate(String issueDate) {
            this.issueDate = issueDate;
        }

        public String getJurisdiction() {
            return jurisdiction;
        }

        public void setJurisdiction(String jurisdiction) {
            this.jurisdiction = jurisdiction;
        }

        public List<ServicePoint> getServicePoints() {
            return servicePoints;
        }

        public void setServicePoints(List<ServicePoint> servicePoints) {
            this.servicePoints = servicePoints;
        }
    }

    /* ================= SERVICE POINT ================= */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ServicePoint {

        @JacksonXmlProperty(isAttribute = true)
        private String id;

        @JacksonXmlProperty(isAttribute = true)
        private String type;

        @JacksonXmlElementWrapper(localName = "meters")
        @JacksonXmlProperty(localName = "meter")
        private List<Meter> meters;

        public List<Meter> getMeters() {
            return meters;
        }

        public void setMeters(List<Meter> meters) {
            this.meters = meters;
        }
    }

    /* ================= METER ================= */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Meter {

        @JacksonXmlProperty(isAttribute = true)
        private String id;

        @JacksonXmlElementWrapper(localName = "readings")
        @JacksonXmlProperty(localName = "reading")
        private List<Reading> readings;

        public String getId() {
            return id;
        }

        public List<Reading> getReadings() {
            return readings;
        }
    }

    /* ================= READING ================= */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Reading {

        @JacksonXmlProperty(isAttribute = true)
        private String period;

        @JacksonXmlProperty(isAttribute = true)
        private double usageKwh;

        @JacksonXmlProperty(isAttribute = true)
        private String rateTier;

        public double getUsageKwh() {
            return usageKwh;
        }

        public String getRateTier() {
            return rateTier;
        }
    }
}

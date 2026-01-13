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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class PreviousBalance {

        @JacksonXmlProperty(isAttribute = true)
        private int overdue;

        @JacksonXmlProperty(isAttribute = true)
        private BigDecimal amount;

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

    }
}

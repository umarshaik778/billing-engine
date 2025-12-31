package com.example.billing.model.output;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MeterOutput {

    private String meterId;
    private double usageKwh;
    private String rateTier;
    private BigDecimal adjustment;
    private List<ReadingOutput> readings;

}

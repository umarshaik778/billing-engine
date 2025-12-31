package com.example.billing.service;

import com.example.billing.component.ProcessingGate;
import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BillServiceTest {

    @Autowired
    BillService billService;

    @Autowired
    ProcessingGate gate;

    private BillInput sampleInput() {

        BillInput.Reading reading = new BillInput.Reading();
        reading.setRateTier("TIER1");
        reading.setUsageKwh(600);
        reading.setPeriod("2025-12-01/2025-12-16");

        BillInput.Meter meter = new BillInput.Meter();
        meter.setId("MTR1");
        meter.setReadings(Collections.singletonList(reading));

        BillInput.ServicePoint sp = new BillInput.ServicePoint();
        sp.setId("SP1");
        sp.setType("RESIDENTIAL");
        sp.setMeters(Collections.singletonList(meter));

        BillInput.Header header = new BillInput.Header();
        header.setAccountNo("ACC123");
        header.setIssueDate("12/16/2025");
        header.setJurisdiction("NYC");
        header.setServicePoints(Collections.singletonList(sp));

        BillInput input = new BillInput();
        input.setHeader(header);

        return input;
    }

    @Test
    void successFlow_createsSuccessFiles() {

        gate.enable();

        billService.process(
                sampleInput(),
                PayloadFormat.XML,
                PayloadFormat.JSON
        );

        assertTrue(Files.exists(Paths.get("data/success/input")));
        assertTrue(Files.exists(Paths.get("data/success/output")));
    }

    @Test
    void gateDisabled_movesToReplay() {

        gate.disable();

        billService.process(
                sampleInput(),
                PayloadFormat.XML,
                PayloadFormat.JSON
        );

        assertTrue(Files.exists(Paths.get("data/replay")));
    }
}


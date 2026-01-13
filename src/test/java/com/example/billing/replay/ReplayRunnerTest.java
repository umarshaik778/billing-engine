package com.example.billing.replay;

import com.example.billing.component.ProcessingGate;
import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import com.example.billing.service.BillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReplayRunnerTest {

    @Autowired
    BillService billService;

    @Autowired
    ReplayRunner runner;

    @Autowired
    ProcessingGate gate;

    private BillInput sampleInput() {

        BillInput.Reading reading = new BillInput.Reading();
        reading.setRateTier("TIER2");
        reading.setUsageKwh(1200);
        reading.setPeriod("2025-12-01/2025-12-16");

        BillInput.Meter meter = new BillInput.Meter();
        meter.setId("MTR9");
        meter.setReadings(Collections.singletonList(reading));

        BillInput.ServicePoint sp = new BillInput.ServicePoint();
        sp.setId("SP9");
        sp.setType("RESIDENTIAL");
        sp.setMeters(Collections.singletonList(meter));

        BillInput.Header header = new BillInput.Header();
        header.setAccountNo("ACC999");
        header.setIssueDate("12/16/2025");
        header.setJurisdiction("NYC");
        header.setServicePoints(Collections.singletonList(sp));

        BillInput input = new BillInput();
        input.setHeader(header);

        return input;
    }

    @Test
    void replayProcessesFilesCorrectly() throws Exception {

        gate.disable();

        billService.process(
                sampleInput(),
                PayloadFormat.XML,
                PayloadFormat.JSON
        );

        gate.enable();

        runner.run();

        assertTrue(Files.exists(Paths.get("data/success/input")));
        assertTrue(Files.exists(Paths.get("data/success/output")));
    }
}

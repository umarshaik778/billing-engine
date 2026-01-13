package com.example.billing.controller;

import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import com.example.billing.model.input.BillInput.Header;
import com.example.billing.model.input.BillInput.ServicePoint;
import com.example.billing.model.input.BillInput.Meter;
import com.example.billing.model.input.BillInput.Reading;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BillControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void processEndpointWorks() throws Exception {

        // ===== Build Reading =====
        Reading reading = new Reading();
        reading.setUsageKwh(500);
        reading.setRateTier("TIER1");

        // ===== Build Meter =====
        Meter meter = new Meter();
        meter.setReadings(Collections.singletonList(reading));

        // ===== Build Service Point =====
        ServicePoint sp = new ServicePoint();
        sp.setMeters(Collections.singletonList(meter));

        // ===== Build Header =====
        Header header = new Header();
        header.setAccountNo("ACC777");
        header.setIssueDate("12/16/2025");
        header.setJurisdiction("NYC");
        header.setServicePoints(Collections.singletonList(sp));

        // ===== Build Root =====
        BillInput input = new BillInput();
        input.setHeader(header);

        mockMvc.perform(
                        post("/bill/process")
                                .param("inputFormat", PayloadFormat.JSON.name())
                                .param("outputFormat", PayloadFormat.XML.name())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(input))
                )
                .andExpect(status().isOk());
    }
}

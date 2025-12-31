package com.example.billing.controller;

import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import com.example.billing.model.output.BillOutput;
import com.example.billing.service.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill")
public class BillController {

    private final BillService billService;

    private static final Logger requestLog =
            LoggerFactory.getLogger("REQUEST_LOG");

    private static final Logger errorLog =
            LoggerFactory.getLogger("ERROR_LOG");



    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping(
            value = "/process",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public BillOutput process(
            @RequestParam("inputFormat") PayloadFormat inputFormat,
            @RequestParam("outputFormat") PayloadFormat outputFormat,
            @RequestBody BillInput input
    ) {
        String endpoint = "POST /bill/process";
        String account = input.getHeader().getAccountNo();

        /* ================= REQUEST LOG ================= */

        requestLog.info(
                "REQUEST_RECEIVED | endpoint={} | account={} | inputFormat={} | outputFormat={} | payload={}",
                endpoint,
                account,
                inputFormat,
                outputFormat,
                input
        );

        try {
            return billService.process(input, inputFormat, outputFormat);
        } catch (Exception ex) {

            errorLog.error(
                    "REQUEST_FAILED | endpoint={} | account={}",
                    endpoint,
                    account,
                    ex
            );

            throw ex;
        }
    }
}

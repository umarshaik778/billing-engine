package com.example.billing.controller;

import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import com.example.billing.model.output.BillOutput;
import com.example.billing.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/bill/file")
public class BillFileController {

    private final BillService billService;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public BillFileController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BillOutput upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam PayloadFormat inputFormat,
            @RequestParam PayloadFormat outputFormat
    ) throws Exception {

        try (InputStream is = file.getInputStream()) {

            BillInput input =
                    inputFormat == PayloadFormat.XML
                            ? xmlMapper.readValue(is, BillInput.class)
                            : jsonMapper.readValue(is, BillInput.class);

            return billService.process(
                    input,
                    inputFormat,
                    outputFormat
            );
        }
    }
}

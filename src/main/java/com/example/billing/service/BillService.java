package com.example.billing.service;

import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import com.example.billing.model.output.BillOutput;

import java.nio.file.Path;

public interface BillService {

    BillOutput process(
            BillInput input,
            PayloadFormat inputFormat,
            PayloadFormat outputFormat
    );

    BillOutput replay(
            BillInput input,
            PayloadFormat format,
            Path processingFile
    );



}

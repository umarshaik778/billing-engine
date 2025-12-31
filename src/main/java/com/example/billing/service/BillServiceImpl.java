package com.example.billing.service;

import com.example.billing.component.ProcessingGate;
import com.example.billing.enums.PayloadFormat;
import com.example.billing.exception.InvalidBillException;
import com.example.billing.model.input.BillInput;
import com.example.billing.model.output.BillOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BillServiceImpl implements BillService {

    /* ================= LOGGERS ================= */

    private static final Logger requestLog =
            LoggerFactory.getLogger("REQUEST_LOG");

    private static final Logger successLog =
            LoggerFactory.getLogger("SUCCESS_LOG");

    private static final Logger errorLog =
            LoggerFactory.getLogger("ERROR_LOG");

    /* ================= DIRECTORIES ================= */

    private static final Path BASE = Paths.get("data");
    private static final Path PROCESSING = BASE.resolve("processing");
    private static final Path FAILED = BASE.resolve("failed");
    private static final Path REPLAY = BASE.resolve("replay");
    private static final Path SUCCESS_IN = BASE.resolve("success/input");
    private static final Path SUCCESS_OUT = BASE.resolve("success/output");

    /* ================= DEPENDENCIES ================= */

    private final RuleEngine ruleEngine = new RuleEngine();
    private final ProcessingGate gate;

    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public BillServiceImpl(ProcessingGate gate) {
        this.gate = gate;
    }

    /* ================= INIT ================= */

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(PROCESSING);
            Files.createDirectories(FAILED);
            Files.createDirectories(REPLAY);
            Files.createDirectories(SUCCESS_IN);
            Files.createDirectories(SUCCESS_OUT);

            requestLog.info("STORAGE_INITIALIZED | baseDir={}", BASE.toAbsolutePath());

        } catch (Exception ex) {
            errorLog.error("STORAGE_INIT_FAILED", ex);
            throw new IllegalStateException("Directory init failed", ex);
        }
    }

    /* ================= MAIN PROCESS ================= */

    @Override
    public BillOutput process(
            BillInput input,
            PayloadFormat inputFormat,
            PayloadFormat outputFormat
    ) {

        String account = input.getHeader().getAccountNo();
        String baseName = buildBaseName(account);
        Path processingFile = null;

        requestLog.info(
                "PROCESS_START | account={} | inputFormat={} | outputFormat={}",
                account, inputFormat, outputFormat
        );

        try {
            /* 1️⃣ Store input */
            processingFile = persistInput(baseName, input, inputFormat);

            /* 2️⃣ Gate check */
            if (!gate.isEnabled()) {

                Files.move(
                        processingFile,
                        REPLAY.resolve(processingFile.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING
                );

                requestLog.warn(
                        "PROCESSING_SKIPPED | gate=DISABLED | movedToReplay={}",
                        processingFile.getFileName()
                );

                return null;
            }

            /* 3️⃣ Apply business rules */
            BillOutput output = ruleEngine.applyRules(input);

            requestLog.info("ABOUT_TO_PERSIST_OUTPUT | account={}", account);

            /* 4️⃣ Store output */
            persistOutput(baseName, output, outputFormat);

            /* 5️⃣ Mark success */
            Files.move(
                    processingFile,
                    SUCCESS_IN.resolve(processingFile.getFileName()),
                    StandardCopyOption.ATOMIC_MOVE
            );

            successLog.info(
                    "PROCESSING_SUCCESS | account={} | totalDue={}",
                    account, output.getTotalDue()
            );

            return output;

        } catch (Exception ex) {

            errorLog.error(
                    "PROCESSING_FAILED | account={}",
                    account,
                    ex
            );

            safeMove(processingFile, FAILED);
            throw new InvalidBillException("Processing failed", ex);
        }
    }

    /* ================= REPLAY ================= */

    @Override
    public BillOutput replay(
            BillInput input,
            PayloadFormat format,
            Path processingFile
    ) {

        String account = input.getHeader().getAccountNo();

        requestLog.info(
                "REPLAY_START | account={} | file={}",
                account,
                processingFile.getFileName()
        );

        try {
            BillOutput output = ruleEngine.applyRules(input);

            requestLog.info("ABOUT_TO_PERSIST_OUTPUT2 | account={}", account);

            persistOutput(
                    account,
                    output,
                    format
            );

            Files.move(
                    processingFile,
                    SUCCESS_IN.resolve(processingFile.getFileName()),
                    StandardCopyOption.ATOMIC_MOVE
            );

            successLog.info(
                    "REPLAY_SUCCESS | account={} | file={}",
                    account,
                    processingFile.getFileName()
            );

            return output;

        } catch (Exception ex) {

            errorLog.error(
                    "REPLAY_FAILED | account={} | file={}",
                    account,
                    processingFile.getFileName(),
                    ex
            );

            safeMove(processingFile, FAILED);
            throw new InvalidBillException("Replay failed", ex);
        }
    }

    /* ================= STORAGE ================= */

    private Path persistInput(
            String base,
            BillInput input,
            PayloadFormat format
    ) throws Exception {

        String name = fileName(base, "IN", format);
        Path path = PROCESSING.resolve(name);

        if (format == PayloadFormat.XML)
            xmlMapper.writeValue(path.toFile(), input);
        else
            jsonMapper.writeValue(path.toFile(), input);

        requestLog.info(
                "INPUT_STORED | file={} | format={}",
                path.getFileName(),
                format
        );

        return path;
    }

    private void persistOutput(
            String base,
            BillOutput output,
            PayloadFormat format
    ) throws Exception {

        String name = fileName(base, "OUT", format);
        Path path = SUCCESS_OUT.resolve(name);

        if (format == PayloadFormat.XML) {
            xmlMapper.writeValue(path.toFile(), output);
        } else {
            jsonMapper.writeValue(path.toFile(), output);
        }

        successLog.info(
                "OUTPUT_STORED | file={} | path={} | format={}",
                path.getFileName(),
                path.toAbsolutePath(),
                format
        );
    }


    /* ================= UTIL ================= */

    private void safeMove(Path src, Path destDir) {
        try {
            if (src != null && Files.exists(src)) {
                Files.move(
                        src,
                        destDir.resolve(src.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING
                );

                requestLog.warn(
                        "FILE_MOVED | from={} | to={}",
                        src.getParent(),
                        destDir
                );
            }
        } catch (Exception ex) {
            errorLog.error("FILE_MOVE_FAILED | file={}", src, ex);
        }
    }

    private String buildBaseName(String acc) {
        return acc + "_" +
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String fileName(
            String base,
            String type,
            PayloadFormat format
    ) {
        return base + "_" + type +
                (format == PayloadFormat.XML ? ".xml" : ".json");
    }
}

package com.example.billing.replay;

import com.example.billing.component.ProcessingGate;
import com.example.billing.enums.PayloadFormat;
import com.example.billing.model.input.BillInput;
import com.example.billing.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.*;

@Component
public class ReplayRunner {

    private static final Logger log =
            LoggerFactory.getLogger("REPLAY_LOG");

    private static final Path REPLAY =
            Paths.get("data", "replay");

    private static final Path PROCESSING =
            Paths.get("data", "processing");

    private final BillService billService;
    private final ProcessingGate gate;

    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public ReplayRunner(
            BillService billService,
            ProcessingGate gate
    ) {
        this.billService = billService;
        this.gate = gate;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void run() {

        if (!gate.isEnabled()) {
            log.info("Replay skipped — gate disabled");
            return;
        }

        try {
            if (!Files.exists(REPLAY)) return;

            Files.list(REPLAY)
                    .filter(Files::isRegularFile)
                    .forEach(this::replayFile);

        } catch (Exception ex) {
            log.error("Replay scan failed", ex);
        }
    }

    private void replayFile(Path replayFile) {

        Path processing =
                PROCESSING.resolve(replayFile.getFileName());

        try {
            Files.move(
                    replayFile,
                    processing,
                    StandardCopyOption.ATOMIC_MOVE
            );

            PayloadFormat format =
                    replayFile.toString().endsWith(".xml")
                            ? PayloadFormat.XML
                            : PayloadFormat.JSON;

            BillInput input =
                    format == PayloadFormat.XML
                            ? xmlMapper.readValue(processing.toFile(), BillInput.class)
                            : jsonMapper.readValue(processing.toFile(), BillInput.class);

            billService.replay(input, format, processing);

            log.info("REPLAY_SUCCESS | {}", replayFile.getFileName());

        } catch (Exception ex) {
            log.error("REPLAY_FAILED | {}", replayFile.getFileName(), ex);
        }
    }
}

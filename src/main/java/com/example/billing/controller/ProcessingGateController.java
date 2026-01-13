package com.example.billing.controller;

import com.example.billing.component.ProcessingGate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gate")
public class ProcessingGateController {

    private final ProcessingGate gate;

    public ProcessingGateController(ProcessingGate gate) {
        this.gate = gate;
    }

    @PostMapping("/enable")
    public String enable() {
        gate.enable();
        return "ProcessingGate ENABLED";
    }

    @PostMapping("/disable")
    public String disable() {
        gate.disable();
        return "ProcessingGate DISABLED";
    }

    @GetMapping("/status")
    public boolean status() {
        return gate.isEnabled();
    }
}

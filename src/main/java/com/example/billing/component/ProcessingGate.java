package com.example.billing.component;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ProcessingGate {

    private final AtomicBoolean enabled = new AtomicBoolean(true);

    public boolean isEnabled() {
        return enabled.get();
    }

    public void enable() {
        enabled.set(true);
    }

    public void disable() {
        enabled.set(false);
    }
}

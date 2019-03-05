package com.github.cairoatlas.objects;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    private boolean isValid;
    private String violatedSlot;
    private Map<String, String> message = new HashMap<>();

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getViolatedSlot() {
        return violatedSlot;
    }

    public void setViolatedSlot(String violatedSlot) {
        this.violatedSlot = violatedSlot;
    }

    public Map<String, String> getMessage() {
        return message;
    }

    public void setMessage(Map<String, String> message) {
        this.message = message;
    }
}

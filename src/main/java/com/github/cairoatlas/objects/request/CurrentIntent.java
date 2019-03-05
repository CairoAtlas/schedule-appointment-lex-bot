package com.github.cairoatlas.objects.request;

import java.util.Map;

public class CurrentIntent {
    private String name;
    private Map<String, String> slots;
    private Map<String, SlotDetail> slotDetails;
    private String confirmationStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, String> slots) {
        this.slots = slots;
    }

    public Map<String, SlotDetail> getSlotDetails() {
        return slotDetails;
    }

    public void setSlotDetails(Map<String, SlotDetail> slotDetails) {
        this.slotDetails = slotDetails;
    }

    public String getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(String confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }
}

package com.github.cairoatlas.objects;

import com.github.cairoatlas.objects.response.DialogActionMessage;

public class ValidationResult {
    private boolean isValid;
    private String violatedSlot;
	private DialogActionMessage dialogActionMessage;

	public ValidationResult(final boolean isValid, final String voilatedSlot, final String contentMessage) {
		this.isValid = isValid;
		this.violatedSlot = getViolatedSlot();
		DialogActionMessage dialogActionMessage = new DialogActionMessage();
		dialogActionMessage.setContent(contentMessage);
		this.dialogActionMessage = dialogActionMessage;
	}

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

	public DialogActionMessage getDialogActionMessage() {
		return dialogActionMessage;
    }

	public void setDialogActionMessage(DialogActionMessage dialogActionMessage) {
		this.dialogActionMessage = dialogActionMessage;
    }
}

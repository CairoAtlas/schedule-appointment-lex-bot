package com.github.cairoatlas.objects.response;

public class GenericAttachmentButton {
    private String text;
    private String value;

	public GenericAttachmentButton(final String text, final String value) {
		this.text = text;
		this.value = value;
	}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package com.github.cairoatlas.objects.response;

import java.util.List;

public class GenericAttachment {

    private String title;
    private String subTitle;
    private String imageUrl;
    private String attachmentLinkUrl;
    private List<GenericAttachmentButton> buttons;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAttachmentLinkUrl() {
        return attachmentLinkUrl;
    }

    public void setAttachmentLinkUrl(String attachmentLinkUrl) {
        this.attachmentLinkUrl = attachmentLinkUrl;
    }

    public List<GenericAttachmentButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<GenericAttachmentButton> buttons) {
        this.buttons = buttons;
    }
}

package com.github.cairoatlas.objects.request;

import java.util.HashMap;
import java.util.Map;

public class LexRequest {
    private CurrentIntent currentIntent;
    private Bot bot;
    private String userId;
    private String inputTranscript;
    private String invocationSource;
    private String outputDialogMode;
    private String messageVersion;
    private Map<String, String> sessionAttributes = new HashMap<>();
    private Map<String, String> requestAttributes;

    public CurrentIntent getCurrentIntent() {
        return currentIntent;
    }

    public void setCurrentIntent(CurrentIntent currentIntent) {
        this.currentIntent = currentIntent;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInputTranscript() {
        return inputTranscript;
    }

    public void setInputTranscript(String inputTranscript) {
        this.inputTranscript = inputTranscript;
    }

    public String getInvocationSource() {
        return invocationSource;
    }

    public void setInvocationSource(String invocationSource) {
        this.invocationSource = invocationSource;
    }

    public String getOutputDialogMode() {
        return outputDialogMode;
    }

    public void setOutputDialogMode(String outputDialogMode) {
        this.outputDialogMode = outputDialogMode;
    }

    public String getMessageVersion() {
        return messageVersion;
    }

    public void setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
    }

    public Map<String, String> getSessionAttributes() {
        return sessionAttributes;
    }

    public void setSessionAttributes(Map<String, String> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }

    public Map<String, String> getRequestAttributes() {
        return requestAttributes;
    }

    public void setRequestAttributes(Map<String, String> requestAttributes) {
        this.requestAttributes = requestAttributes;
    }
}

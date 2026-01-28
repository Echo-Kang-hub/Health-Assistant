package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatResponse {
    @SerializedName("reply")
    private String reply;
    @SerializedName("suggestions")
    private List<String> suggestions;
    @SerializedName("references")
    private List<String> references;
    @SerializedName("disclaimer")
    private String disclaimer;

    public String getReply() {
        return reply;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public List<String> getReferences() {
        return references;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}

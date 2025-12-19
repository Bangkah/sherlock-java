package org.davistiba;

import com.google.gson.annotations.SerializedName;

/**
 * For JSON conversion
 */
public class Website {
    @SerializedName(value = "url")
    private String url;

    @SerializedName(value = "fingerprint")
    private String fingerprint; // HTML fingerprint (optional)

    @SerializedName(value = "category")
    private String category; // Klasifikasi website

    public String getFingerprint() {
        return fingerprint;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

}

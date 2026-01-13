package com.example.billing.enums;

public enum PayloadFormat {

    XML("xml", "application/xml"),
    JSON("json", "application/json");

    private final String extension;
    private final String mediaType;

    PayloadFormat(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMediaType() {
        return mediaType;
    }

    public static PayloadFormat fromExtension(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String lower = fileName.toLowerCase();

        if (lower.endsWith(".xml")) {
            return XML;
        }
        if (lower.endsWith(".json")) {
            return JSON;
        }

        throw new IllegalArgumentException(
                "Unsupported payload format for file: " + fileName
        );
    }
}

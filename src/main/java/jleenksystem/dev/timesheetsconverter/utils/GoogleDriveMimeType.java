package jleenksystem.dev.timesheetsconverter.utils;

public enum GoogleDriveMimeType {
	GOOGLE_DOCS("application/vnd.google-apps.document"),
    GOOGLE_SHEETS("application/vnd.google-apps.spreadsheet"),
    GOOGLE_SLIDES("application/vnd.google-apps.presentation"),
    GOOGLE_FORMS("application/vnd.google-apps.form"),
    GOOGLE_DRAWINGS("application/vnd.google-apps.drawing"),
    GOOGLE_FOLDER("application/vnd.google-apps.folder"),
    GOOGLE_FUSION_TABLE("application/vnd.google-apps.fusiontable"),
    GOOGLE_JAMBOARD("application/vnd.google-apps.jam"),
    GOOGLE_MAP("application/vnd.google-apps.map"),
    
    // Standard MIME Types
    PDF("application/pdf"),
    TEXT("text/plain"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    ZIP("application/zip"),
    MS_WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    MS_EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    MS_POWERPOINT("application/vnd.openxmlformats-officedocument.presentationml.presentation");

    private final String mimeType;

    GoogleDriveMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
		return mimeType;
	}
}

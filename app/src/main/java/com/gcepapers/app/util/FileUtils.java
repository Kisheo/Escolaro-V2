package com.gcepapers.app.util;

import java.text.DecimalFormat;

/**
 * Utility methods for file-related operations.
 */
public class FileUtils {

    private FileUtils() {}

    /** Formats a file size in bytes to a human-readable string. */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return new DecimalFormat("#,##0.#").format(
            bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /** Sanitizes a string to be used as a filename (removes invalid characters). */
    public static String sanitizeFileName(String name) {
        if (name == null) return "document.pdf";
        String safe = name.replaceAll("[^a-zA-Z0-9.\\-_\\s]", "_")
                          .replaceAll("\\s+", "_")
                          .trim();
        if (!safe.toLowerCase().endsWith(".pdf")) {
            safe += ".pdf";
        }
        return safe;
    }

    /** Extracts Google Drive file ID from a download URL. */
    public static String extractDriveFileId(String url) {
        if (url == null) return null;
        if (url.contains("id=")) {
            String id = url.substring(url.lastIndexOf("id=") + 3);
            int ampIndex = id.indexOf('&');
            if (ampIndex > 0) id = id.substring(0, ampIndex);
            return id;
        }
        return null;
    }

    /**
     * Returns a display-friendly title by removing a trailing ".pdf" extension (case-insensitive) and trimming.
     * If the input is null returns an empty string.
     */
    public static String stripPdfExtensionForDisplay(String title) {
        if (title == null) return "";
        String t = title.trim();
        if (t.length() == 0) return "";
        // Remove trailing .pdf or .PDF or mixed-case
        if (t.toLowerCase().endsWith(".pdf")) {
            t = t.substring(0, t.length() - 4).trim();
        }
        return t;
    }
}

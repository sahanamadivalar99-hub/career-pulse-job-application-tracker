package com.tracker.model;

public enum ReminderType {
    INTERVIEW("Interview Schedule", "bi-calendar-event", "text-warning"),
    FOLLOW_UP("Follow-Up", "bi-reply-all", "text-info"),
    DEADLINE("Application Deadline", "bi-alarm", "text-danger"),
    OFFER_ACCEPTANCE("Offer Response", "bi-award", "text-success"),
    OTHER("General Reminder", "bi-bell", "text-primary");

    private final String displayName;
    private final String icon;
    private final String colorClass;

    ReminderType(String displayName, String icon, String colorClass) {
        this.displayName = displayName;
        this.icon = icon;
        this.colorClass = colorClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getColorClass() {
        return colorClass;
    }
}

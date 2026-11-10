package com.tracker.model;

public enum ApplicationStatus {
    APPLIED("Applied", "badge-applied", "bg-primary"),
    IN_REVIEW("In Review", "badge-review", "bg-info"),
    INTERVIEW("Interview", "badge-interview", "bg-warning"),
    OFFER("Offer", "badge-offer", "bg-success"),
    REJECTED("Rejected", "badge-rejected", "bg-danger");

    private final String displayName;
    private final String cssClass;
    private final String bootstrapBadge;

    ApplicationStatus(String displayName, String cssClass, String bootstrapBadge) {
        this.displayName = displayName;
        this.cssClass = cssClass;
        this.bootstrapBadge = bootstrapBadge;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getBootstrapBadge() {
        return bootstrapBadge;
    }
}

package com.jobportal.model;

/**
 * Lifecycle of a job application. Stored as a readable String in the DB
 * (not a number) so the database stays understandable.
 */
public enum ApplicationStatus {
    APPLIED("Applied"),
    UNDER_REVIEW("Under Review"),
    SHORTLISTED("Shortlisted"),
    REJECTED("Rejected");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

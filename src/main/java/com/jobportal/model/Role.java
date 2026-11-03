package com.jobportal.model;

/**
 * The two roles in this portal. A user has exactly one role, chosen once
 * at registration — the login page never asks for it.
 */
public enum Role {
    SEEKER("Job Seeker"),
    POSTER("Job Poster");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

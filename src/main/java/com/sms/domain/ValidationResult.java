package com.sms.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the result of a validation check.
 * Contains a list of error messages. If the list is empty, validation passed.
 */
public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        errors.add(message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    /** Returns all errors joined by newlines, useful for dialogs. */
    public String getErrorMessage() {
        return String.join("\n", errors);
    }
}

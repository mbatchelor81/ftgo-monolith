package net.chrisrichardson.ftgo.errorhandling;

/**
 * Represents a single field-level validation error within an {@link ErrorResponse}.
 */
public class ValidationErrorDetail {

    private final String field;
    private final Object rejectedValue;
    private final String message;

    public ValidationErrorDetail(String field, Object rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public String getMessage() {
        return message;
    }
}

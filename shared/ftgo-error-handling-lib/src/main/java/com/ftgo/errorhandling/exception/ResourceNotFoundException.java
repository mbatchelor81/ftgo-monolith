package com.ftgo.errorhandling.exception;

/**
 * Thrown when a requested resource (entity) cannot be found.
 *
 * <p>Mapped to HTTP 404 Not Found by the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Object resourceId;
    private final String errorCode;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        this(resourceType, resourceId, ErrorCodes.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceType, Object resourceId, String errorCode) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.errorCode = errorCode;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

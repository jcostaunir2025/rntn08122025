package com.example.rntn.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom exception for resource not found scenarios
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    private final Map<String, Object> details;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.details = new HashMap<>();
        this.details.put("resourceName", resourceName);
        this.details.put("searchField", fieldName);
        this.details.put("searchValue", fieldValue);
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = "Resource";
        this.fieldName = "id";
        this.fieldValue = null;
        this.details = new HashMap<>();
        this.details.put("message", message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s not found with id: %s", resourceName, id));
        this.resourceName = resourceName;
        this.fieldName = "id";
        this.fieldValue = id;
        this.details = new HashMap<>();
        this.details.put("resourceName", resourceName);
        this.details.put("id", id);
    }
}


package com.arpit.StudentManagementSystem.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardised error response body returned by {@link GlobalExceptionHandler}.
 *
 * Example (single error):
 * <pre>
 * {
 *   "timestamp" : "2026-09-21 14:39:00",
 *   "status"    : 404,
 *   "error"     : "Not Found",
 *   "message"   : "Student not found with id: 5",
 *   "path"      : "/api/v1/students/5"
 * }
 * </pre>
 *
 * Example (validation errors):
 * <pre>
 * {
 *   "timestamp" : "2026-09-21 14:39:00",
 *   "status"    : 400,
 *   "error"     : "Bad Request",
 *   "message"   : "Validation failed",
 *   "path"      : "/api/v1/students",
 *   "errors"    : {
 *     "email"  : "Email must be a valid email address",
 *     "name"   : "Name is required"
 *   }
 * }
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)   // omit 'errors' field when null
public class ErrorResponse {

    /** ISO-8601 timestamp formatted as "yyyy-MM-dd HH:mm:ss" */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    /** HTTP status code, e.g. 404 */
    private final int status;

    /** HTTP reason phrase, e.g. "Not Found" */
    private final String error;

    /** Human-readable message describing the problem */
    private final String message;

    /** The request URI that triggered this error */
    private final String path;

    /**
     * Per-field validation errors.
     * Only present when {@code status == 400} and bean validation fails.
     * Key = field name, Value = constraint violation message.
     */
    private final Map<String, String> errors;

    // ── Constructors ──────────────────────────────────────────────────────────

    /** Use for all single-message errors (non-validation). */
    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.errors    = null;
    }

    /** Use for @Valid validation failures — includes per-field error map. */
    public ErrorResponse(int status, String error, String message,
                         String path, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.errors    = errors;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public LocalDateTime getTimestamp() { return timestamp; }
    public int           getStatus()    { return status;    }
    public String        getError()     { return error;     }
    public String        getMessage()   { return message;   }
    public String        getPath()      { return path;      }
    public Map<String, String> getErrors() { return errors; }
}

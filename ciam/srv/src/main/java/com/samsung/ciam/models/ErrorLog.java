package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_log")
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "uid")
    private String uid;

    @Column(name = "error_page")
    private String errorPage;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "line_number")
    private int lineNumber;

    @Column(name = "class_name")
    private String className;

    @Column(name = "type")
    private String type;  // 에러 유형 필드 추가

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "email")
    private String email;

    public ErrorLog() {}

    public ErrorLog(String message, String stackTrace, String uid, String errorPage, String methodName, int lineNumber, String className, String type,String email) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.uid = uid;
        this.errorPage = errorPage;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.className = className;
        this.type = type;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
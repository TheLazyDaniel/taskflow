package com.thelazydaniel.taskflow.common.exception;

import com.thelazydaniel.taskflow.common.dto.response.ValidationErrorResponse;
import com.thelazydaniel.taskflow.auth.exception.*;
import com.thelazydaniel.taskflow.common.dto.response.ErrorResponse;
import com.thelazydaniel.taskflow.common.util.SecurityUtils;
import com.thelazydaniel.taskflow.project.exception.*;
import com.thelazydaniel.taskflow.task.exception.*;
import com.thelazydaniel.taskflow.user.exception.UserIdNotFoundException;
import com.thelazydaniel.taskflow.user.exception.UserNameNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex,
            HttpStatus status,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    //common exceptions

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("IllegalArgumentException: {}", e.getMessage());

        return buildErrorResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    //@RequestBody exceptions

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {
        String message;
        if (e.getMessage().contains("java.time.LocalDate")) {
            message = "Invalid date format. Expected format: yyyy-MM-dd";
        } else {
            message = e.getMessage();
        }

        log.warn("HttpMessageNotReadableException: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    //@Valid exceptions

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, List<String>> fieldErrors = new HashMap<>();
        List<String> globalErrors = new ArrayList<>();

        log.debug("Validation failed: {}", ex.getMessage());

        // Collect all field errors - multiple errors per field
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                String fieldName = fieldError.getField();
                String errorMessage = error.getDefaultMessage();

                // Add multiple errors for the same field
                fieldErrors.computeIfAbsent(fieldName, k -> new ArrayList<>())
                        .add(errorMessage);

                // Log each error
                log.debug("Field '{}' rejected value '{}': {}",
                        fieldName,
                        fieldError.getRejectedValue(),
                        errorMessage);
            } else {
                // Global errors (class-level constraints)
                globalErrors.add(error.getDefaultMessage());
                log.debug("Global error: {}", error.getDefaultMessage());
            }
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                fieldErrors,
                globalErrors,
                request.getDescription(false)
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException e,
            HttpServletRequest request) {

        log.warn("ConstraintViolationException: {}", e.getMessage());

        return buildErrorResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    //@PathVariable exceptions

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariableException(
            MissingPathVariableException e,
            HttpServletRequest request
    ){
        log.warn("MissingPathVariableException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    //User exceptions

    @ExceptionHandler(UserIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserIdNotFound(
            UserIdNotFoundException e,
            HttpServletRequest request) {

        log.warn("UserIdNotFoundException: {}", e.getMessage());

        return buildErrorResponse(e,HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(UserNameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNameNotFound(
            UserNameNotFoundException e,
            HttpServletRequest request) {

        log.warn("UserNameNotFoundException: {}", e.getMessage());

        return buildErrorResponse(e,HttpStatus.NOT_FOUND,request);
    }

    //auth exceptions

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
                BadCredentialsException e,
            HttpServletRequest request) {

        return buildErrorResponse(e, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException e,
            HttpServletRequest request) {

        String username = SecurityUtils.getCurrentUsername();
        log.warn("Authorization denied for user '{}' accessing '{}' - {}",
                username, request.getRequestURI(), e.getMessage());
        // DON'T log stack trace for expected exceptions
        return buildErrorResponse(e, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(
            AccountDisabledException e,
            HttpServletRequest request) {
        log.warn("AccountDisabledException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            AccountLockedException e,
            HttpServletRequest request) {
        log.warn("AccountLockedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
            InvalidRefreshTokenException e,
            HttpServletRequest request) {
        log.warn("InvalidRefreshTokenException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(
            RefreshTokenExpiredException e,
            HttpServletRequest request) {
        log.warn("RefreshTokenExpiredException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.UNAUTHORIZED,request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException e,
            HttpServletRequest request) {
        log.warn("UnauthorizedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }
    //Project exceptions

    @ExceptionHandler(ProjectIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectIdNotFound(
            ProjectIdNotFoundException e,
            HttpServletRequest request
    ){
        log.warn("ProjectIdNotFoundException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(ProjectCannotUpdateException.class)
    public ResponseEntity<ErrorResponse> handleProjectArchived(
            ProjectCannotUpdateException e,
            HttpServletRequest request
    ){
        log.warn("(ProjectCannotUpdateException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(ProjectOperationBlockedException.class)
    public ResponseEntity<ErrorResponse> handleProjectOperationBlocked(
            ProjectOperationBlockedException e,
            HttpServletRequest request
    ){
        log.warn("ProjectArchivedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(ProjectDeletedException.class)
    public ResponseEntity<ErrorResponse> handleProjectDeleted(
            ProjectDeletedException e,
            HttpServletRequest request
    ){
        log.warn("ProjectDeletedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(ProjectAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleProjectAccessDenied(
            ProjectAccessDeniedException e,
            HttpServletRequest request
    ){
        log.warn("ProjectAccessDeniedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(ProjectCannotAcceptTaskException.class)
    public ResponseEntity<ErrorResponse> handleProjectCannotAcceptTask(
            ProjectCannotAcceptTaskException e,
            HttpServletRequest request
    ){
        log.warn("ProjectCannotAcceptTaskException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(ProjectInvalidTransitionException.class)
    public ResponseEntity<ErrorResponse> handleProjectInvalidTransition(
            ProjectInvalidTransitionException e,
            HttpServletRequest request
    ){
        log.warn("ProjectInvalidTransitionException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.BAD_REQUEST,request);
    }

    //Task exceptions

    @ExceptionHandler(TaskIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskIdNotFound(
            ProjectInvalidTransitionException e,
            HttpServletRequest request
    ){
        log.warn("TaskIdNotFoundException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.NOT_FOUND,request);
    }

    @ExceptionHandler(TaskAssigneeConflictException.class)
    public ResponseEntity<ErrorResponse> handleTaskAssigneeConflict(
            TaskAssigneeConflictException e,
            HttpServletRequest request
    ){
        log.warn("TaskAssigneeConflictException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(CreateTaskDeniedException.class)
    public ResponseEntity<ErrorResponse> handleCreateTaskDenied(
            CreateTaskDeniedException e,
            HttpServletRequest request
    ){
        log.warn("CreateTaskDeniedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(TaskInvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleTaskInvalidOperation(
            TaskInvalidOperationException e,
            HttpServletRequest request
    ){
        log.warn("TaskInvalidOperationException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException e,
            HttpServletRequest request
    ){
        log.warn("InvalidStatusTransitionException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.CONFLICT,request);
    }

    @ExceptionHandler(TaskStatusTransitionDeniedException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            TaskStatusTransitionDeniedException e,
            HttpServletRequest request
    ){
        log.warn("ITaskStatusTransitionDeniedException: {}", e.getMessage());
        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDeniedGeneric(
            AccessDeniedException e,
            HttpServletRequest request) {

        log.error("AccessDeniedException {}",
                 e.getMessage());

        return buildErrorResponse(e,HttpStatus.FORBIDDEN,request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected error processing request '{}'",
                request.getRequestURI(), e);

        return buildErrorResponse(e,HttpStatus.INTERNAL_SERVER_ERROR,request);
    }

}


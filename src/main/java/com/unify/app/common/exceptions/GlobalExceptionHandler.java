package com.unify.app.common.exceptions;

import com.unify.app.reports.domain.ReportException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Value("${unify.app-uri}")
  private String appUri;

  private URI baseUri() {
    return URI.create(appUri + "/errors/");
  }

  private URI badRequestType() {
    return baseUri().resolve("bad-request");
  }

  private URI notFoundType() {
    return baseUri().resolve("not-found");
  }

  private URI serverErrorType() {
    return baseUri().resolve("internal-server");
  }

  private URI conflictType() {
    return baseUri().resolve("conflict");
  }

  // Xử lý IllegalArgumentException (400 Bad Request)
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    log.info("Bad request at [{}]: {}", request.getRequestURI(), ex.getMessage());
    return buildProblemDetail(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        ex.getMessage(),
        badRequestType(),
        request.getRequestURI(),
        Map.of("error_category", "Validation"));
  }

  // Xử lý ReportException (409 Conflict)
  @ExceptionHandler(ReportException.class)
  public ProblemDetail handleReportException(ReportException ex, HttpServletRequest request) {
    log.info("Conflict at [{}]: {}", request.getRequestURI(), ex.getMessage());
    return buildProblemDetail(
        HttpStatus.CONFLICT,
        "Conflict",
        ex.getMessage(),
        conflictType(),
        request.getRequestURI(),
        Map.of("error_category", "Report"));
  }

  // Xử lý các ngoại lệ chung (500 Internal Server Error)
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
    return buildProblemDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "An unexpected error occurred.",
        serverErrorType(),
        request.getRequestURI(),
        Map.of("error_category", "Generic"));
  }

  //  @ExceptionHandler(Exception.class)
  //  public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
  //    log.error("Unhandled exception at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
  //    return buildProblemDetail(
  //        HttpStatus.INTERNAL_SERVER_ERROR,
  //        "Internal Server Error",
  //        ex.getMessage(),
  //        serverErrorType(),
  //        request.getRequestURI(),
  //        Map.of("error_category", "Generic"));
  //  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    log.info("Resource not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
    return buildProblemDetail(
        HttpStatus.NOT_FOUND,
        "Resource Not Found",
        ex.getMessage(),
        notFoundType(),
        request.getRequestURI(),
        Map.of("error_category", "Generic"));
  }

  @ExceptionHandler(BadRequestException.class)
  public ProblemDetail handleBadRequest(BadRequestException ex, HttpServletRequest request) {
    return buildProblemDetail(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        ex.getMessage(),
        badRequestType(),
        request.getRequestURI(),
        Map.of("error_category", "Generic"));
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull MethodArgumentNotValidException ex,
      @Nullable HttpHeaders headers,
      @Nullable HttpStatusCode status,
      @NonNull WebRequest request) {

    List<String> errors = new ArrayList<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (err) -> {
              String errMessage = err.getDefaultMessage();
              errors.add(errMessage);
            });

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request payload");
    problemDetail.setTitle("Bad Request");
    problemDetail.setType(badRequestType());
    problemDetail.setProperty("errors", errors);
    problemDetail.setProperty("error_category", "Generic");
    problemDetail.setProperty("timestamp", Instant.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
  }

  @Override
  @Nullable
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      @NonNull HttpMessageNotReadableException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Malformed JSON request");
    problem.setDetail(ex.getMostSpecificCause().getMessage());
    problem.setType(badRequestType());
    problem.setProperty("timestamp", Instant.now());
    problem.setProperty("error_category", "Generic");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(problem);
  }

  private ProblemDetail buildProblemDetail(
      HttpStatus status,
      String title,
      String detail,
      URI type,
      String instanceUri,
      Map<String, Object> extraProps) {

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    problem.setType(type);
    problem.setInstance(URI.create(instanceUri));
    problem.setProperty("timestamp", Instant.now());

    if (extraProps != null) {
      extraProps.forEach(problem::setProperty);
    }

    return problem;
  }
}

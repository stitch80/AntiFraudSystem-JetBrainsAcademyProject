package antifraud.rest;

import antifraud.dto.response.AppUserResponse;
import antifraud.rest.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice
public class AppUserAndRoleExceptionHandler {

    @ExceptionHandler({
            ObjectConflictException.class,
            SQLIntegrityConstraintViolationException.class})
    public ResponseEntity<AppUserResponse> handleConflictException () {
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ObjectNotValidException.class,
            HttpMessageConversionException.class,
            UnexpectedTypeException.class,
            ConstraintViolationException.class})
    public ResponseEntity<Void> handleBadRequestException() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<AppUserResponse> handleNotFoundException(ObjectNotFoundException ex) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<AppUserResponse> handleUnprocessableEntityException(UnprocessableEntityException ex) {
        return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
    }

}

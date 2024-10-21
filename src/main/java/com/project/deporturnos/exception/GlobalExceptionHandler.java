package com.project.deporturnos.exception;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handlerArgumentException(IllegalArgumentException ex)
    {
        return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handlerRuntimeException(RuntimeException ex)
    {
        return new ResponseEntity<>(ex.getMessage(),HttpStatus.BAD_GATEWAY);
    }






    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GeneralResponseDTO> handlerResourceNotFoundException(ResourceNotFoundException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<GeneralResponseDTO> handlerUserAlreadyExistsException(UserAlreadyExistsException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TurnoStartTimeAlreadyExistException.class)
    public ResponseEntity<GeneralResponseDTO> handlerTurnoStartTimeAlreadyExist(TurnoStartTimeAlreadyExistException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TurnoAlreadyReservedException.class)
    public ResponseEntity<GeneralResponseDTO> handlerTurnoAlreadyReserved(TurnoAlreadyReservedException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<GeneralResponseDTO> handlerInvalidEmailException(InvalidEmailException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<GeneralResponseDTO> handlerInvalidPasswordException(InvalidPasswordException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReservaAlreadyCancelledException.class)
    public ResponseEntity<GeneralResponseDTO> handlerReservaAlreadyCancelledException(ReservaAlreadyCancelledException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReservaAlreadyInProcessException.class)
    public ResponseEntity<GeneralResponseDTO> handlerReservaAlreadyInProcessException(ReservaAlreadyInProcessException ex){
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReservaAlreadyCompletedException.class)
    public ResponseEntity<GeneralResponseDTO> handlerReservaAlreadyCompletedException(ReservaAlreadyCompletedException ex){
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidReservaDateException.class)
    public ResponseEntity<GeneralResponseDTO> handlerInvalidReservaDateException(InvalidReservaDateException ex){
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CanchaNotAvailableException.class)
    public ResponseEntity<GeneralResponseDTO> handlerCanchaNotAvailableException(CanchaNotAvailableException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationEmailException.class)
    public ResponseEntity<GeneralResponseDTO> handlerVerificationEmailException(VerificationEmailException ex)
    {
        return new ResponseEntity<>(new GeneralResponseDTO(ex.getMessage()),HttpStatus.BAD_REQUEST);
    }






    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<String> handleAccessDeniedException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<String> handleForbiddenException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseBody
    public ResponseEntity<String> handleUnauthorizedException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity<String> handleAuthenticationException(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }




}

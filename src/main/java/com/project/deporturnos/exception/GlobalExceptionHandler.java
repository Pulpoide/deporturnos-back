package com.project.deporturnos.exception;

import com.project.deporturnos.entity.dto.GeneralResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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




}

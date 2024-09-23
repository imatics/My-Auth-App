package com.auth.app.exception;

import com.auth.app.model.DTO.ErrorResponseDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class ExceptionApiHandler {

	/*handler for any kind of a custom business exception */
	@ExceptionHandler(ClientException.class)
	public ResponseEntity<ErrorResponseDTO> handleClientException(ClientException e) {
		return ResponseEntity
		.status(HttpStatus.NOT_FOUND)
		.body(new ErrorResponseDTO(e.getMessage()));
	}


	/*handler for the exception thrown by the  failing @Size, @Pattern or @Min/@Max */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		return ResponseEntity
		.status(HttpStatus. NOT_ACCEPTABLE)
		.body(new ErrorResponseDTO(e.getMessage()));
	}

	/*handler for the exception thrown by the wrong Enum value */
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<ErrorResponseDTO> handleInvalidFormatException(InvalidFormatException e) {
		return ResponseEntity
		.status(HttpStatus. NOT_ACCEPTABLE)
		.body(new ErrorResponseDTO(e.getMessage()));
	}
}
package com.auth.app.exception;

import com.auth.app.model.DTO.ErrorResponseDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionApiHandler {

	/*handler for any kind of a custom business exception */
	@ExceptionHandler(ClientException.class)
	public ResponseEntity<ErrorResponseDTO> handleClientException(ClientException e) {
		return ResponseEntity
		.status(HttpStatus.NOT_ACCEPTABLE)
		.body(new ErrorResponseDTO(e.getMessage()));
	}


	/*handler for the exception thrown by the  failing @Size, @Pattern or @Min/@Max */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		Map<String, List<String>> body = new HashMap<>();
		List<String> errors = e.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
		body.put("errors", errors);
		return ResponseEntity
		.status(HttpStatus. BAD_REQUEST)
		.body(body);
	}

	/*handler for the exception thrown by the wrong Enum value */
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<ErrorResponseDTO> handleInvalidFormatException(InvalidFormatException e) {
		return ResponseEntity
		.status(HttpStatus. NOT_ACCEPTABLE)
		.body(new ErrorResponseDTO(e.getMessage()));
	}
}
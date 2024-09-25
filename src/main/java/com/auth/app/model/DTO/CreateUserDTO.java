package com.auth.app.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Data
public class CreateUserDTO {
    @NotEmpty(message = "First name can not be blank")
    @Size(min = 2, max = 50, message = "The length of first name must be between 50")
    private String firstName;

    @NotEmpty(message = "last name can not be blank")
    @Size(min = 2, max = 50, message = "The length of last name must be between 50")
    private String lastName;

    @NotEmpty(message = "email can not be blank")
    @Pattern(regexp = "^[^@]+@[^@]+\\.[^@]+$", flags = {Pattern.Flag.CASE_INSENSITIVE}, message = "Email is invalid")
    private String email;

    @NotEmpty(message = "password can not be blank")
    @Size(min = 6, max = 50, message = "The length of full name must be between 50")
    private String password;


    @NotBlank
    private String phone;
}




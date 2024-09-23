package com.auth.app.model.DTO;

import com.auth.app.model.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ProfileDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean isActive;




    public static ProfileDTO fromUserModel(User user){
        return new ProfileDTO(
           user.getId(),
                user.getFirstName(),
                user.getFirstName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive()
        );
    }
}
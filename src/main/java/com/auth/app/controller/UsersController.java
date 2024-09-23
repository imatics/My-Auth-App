package com.auth.app.controller;

import com.auth.app.model.DTO.*;
import com.auth.app.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/users")
@AllArgsConstructor
class UsersController {

    private UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<Boolean> createNewUser(@RequestBody CreateUserDTO createUserDTO){
        userService.createUser(createUserDTO, false);
        return ResponseEntity.ok().body(true);
    }


    @PostMapping("/changePassword")
    public ResponseEntity<Boolean> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
            return ResponseEntity.ok().body(userService.changePassword(changePasswordDTO));
    }


    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody LoginDTO loginModel) {
          return ResponseEntity.ok().body(userService.getToken(loginModel));
    }




    @GetMapping("/all")
    public ResponseEntity<List<ProfileDTO>> getAllUsers() {
            return ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("/user")
    public ResponseEntity<ProfileDTO> getUserByEmail(String email) {
            return ResponseEntity.ok().body(userService.getUserProfileByEmail(email));
    }



    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateUser(@RequestBody ProfileDTO profileDTO) {
            return ResponseEntity.ok().body(userService.updateUserDetails(profileDTO));
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Boolean>  deleteUser(@RequestParam("email") String email){
        return ResponseEntity.ok().body(userService.deleteUser(email));
    }


}






package com.grpc.demo.controller;

import java.util.List;

import com.grpc.demo.service.user.Response;
import com.grpc.demo.service.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.grpc.demo.service.UserClient;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserClient userClient;

    public UserController(UserClient userClient){
        this.userClient = userClient;
    }

    @PostMapping("path")
    public ResponseEntity<Response> createUser(@RequestBody User user) {
        try {
            Response response = userClient.createUser(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                .setSuccess(false)
                .setMessage(e.getMessage())
                .build()
            );
        } 
    }

    @PutMapping("path/{id}")
    public ResponseEntity<Response> updateUser(@PathVariable int id, @RequestBody User user) {
        try {
            User updateUser = user.toBuilder().setId(id).build();
            Response response = userClient.updateUser(updateUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()
            );
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteUser(@PathVariable int id){
        try {
            User user = User.newBuilder().setId(id).build();
            Response response = userClient.deleteUser(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Response.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage())
                    .build()   
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        try {
            List<User> users = userClient.listUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
}

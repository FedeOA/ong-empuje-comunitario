package com.grpc.demo.controller;

import java.util.List;

import com.grpc.demo.dto.out.ResponseDTO;
import com.grpc.demo.dto.in.UserDTO;
import com.grpc.demo.dto.out.UserResponseDTO;
import com.grpc.demo.mapper.IMapper;
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
    private final IMapper<User, UserResponseDTO> mapper;

    public UserController(UserClient userClient, IMapper<User, UserResponseDTO>  mapper){
        this.userClient = userClient;
        this.mapper = mapper;
    }

    @PostMapping()
    public ResponseEntity<ResponseDTO> createUser(@RequestBody UserDTO user) {
        try {
            Response serverResponse = userClient.createUser(user);
            ResponseDTO responseDTO = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateUser(@PathVariable int id, @RequestBody UserDTO user) {
        try {
            Response serverResponse = userClient.updateUser(user,id);
            ResponseDTO response= new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable int id){
        try {
            User user = User.newBuilder().setId(id).build();
            Response serverResponse = userClient.deleteUser(user);
            ResponseDTO response = new ResponseDTO(serverResponse.getSuccess(),serverResponse.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO(false,e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers() {
        try {
            List<User> serverUsers = userClient.listUsers();
            List<UserResponseDTO> response = mapper.mapList(serverUsers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

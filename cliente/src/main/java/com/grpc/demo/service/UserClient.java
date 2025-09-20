package com.grpc.demo.service;

import java.util.List;

import com.grpc.demo.dto.in.UserDTO;
import com.grpc.demo.service.user.*;
import org.springframework.stereotype.Service;

import com.grpc.demo.exception.GrpcClientException;
import net.devh.boot.grpc.client.inject.GrpcClient;

import static com.grpc.demo.enums.Role.levelFromName;

@Service
public class UserClient {
    
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub stub;
    
    public Response createUser(UserDTO user){
        try {

            User userServer = User
                    .newBuilder()
                    .setUsername(user.username())
                    .setEmail(user.email())
                    .setPhone(user.phone())
                    .setIsActive(true)
                    .setFirstName(user.firstName())
                    .setLastName(user.lastName())
                    .setRoleId(levelFromName(user.role())).
                    build();

            return stub.createUser(userServer);
        } catch (Exception e) {
            throw new GrpcClientException("Error para crear usuario", e);
        }
    }

    public Response updateUser(UserDTO userUpdate,int id){
        try {

            User user = User
                    .newBuilder()
                    .setId(id)
                    .setUsername(userUpdate.username())
                    .setEmail(userUpdate.email())
                    .setPhone(userUpdate.phone())
                    .setFirstName(userUpdate.firstName())
                    .setLastName(userUpdate.lastName())
                    .setRoleId(levelFromName(userUpdate.role()))
                    .build();

            return stub.updateUser(user);
        } catch (Exception e) {
            throw new GrpcClientException("Error para actualizar usuario", e);
        }
    }

    public Response deleteUser(User user){
        try {
            return stub.deleteUser(user);
        } catch (Exception e) {
            throw new GrpcClientException("Error para borrar usuario", e);
        }
    }

    public List<User> listUsers(){
        try {
            UserList list = stub.listUsers(Empty.newBuilder().build());
            return list.getUserList();
        } catch (Exception e) {
            throw new GrpcClientException("Error en la lista de usuario", e);
        }
    }
}

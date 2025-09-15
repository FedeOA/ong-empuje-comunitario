package com.grpc.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grpc.demo.exception.GrpcClientException;
import com.grpc.demo.service.Service.Empty;
import com.grpc.demo.service.Service.Response;
import com.grpc.demo.service.Service.User;
import com.grpc.demo.service.Service.UserList;
import com.grpc.demo.service.UserServiceGrpc.UserServiceBlockingStub;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class UserClient {
    
    @GrpcClient("user-service")
    private UserServiceBlockingStub stub;
    
    public Response createUser(User user){
        try {
            return stub.createUser(user);
        } catch (Exception e) {
            throw new GrpcClientException("Error para crear usuario", e);
        }
    }

    public Response updateUser(User user){
        try {
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

package com.grpc.demo.exception;

public class GrpcClientException extends RuntimeException {
    public GrpcClientException(String message) {
        super(message);
    }

    public GrpcClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.grpc.demo.mapper;


import java.util.List;
import java.util.stream.Collectors;

@FunctionalInterface
public interface IMapper<S, T> {

    T map(S source);

    default List<T> mapList(List<S> sourceList) {
        if (sourceList == null) {
            return List.of();
        }
        return sourceList.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}


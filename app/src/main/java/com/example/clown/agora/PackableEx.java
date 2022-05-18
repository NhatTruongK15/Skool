package com.example.clown.agora;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
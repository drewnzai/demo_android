package com.andrew.demo.dtos;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(){}

    public void setPassword(String password){
        this.password = password;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}

package com.example.demo.dto;
/*
DTO - Data Transfer Object
 */
public class UserRequest {
    String name;
    String email;
    String password; //¿String u otro tipo de dato?

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

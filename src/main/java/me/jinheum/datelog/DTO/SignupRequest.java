package me.jinheum.datelog.DTO;

public record SignupRequest(
    String name,
    String email,
    String password

) {}
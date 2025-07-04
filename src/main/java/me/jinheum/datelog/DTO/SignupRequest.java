package me.jinheum.datelog.dto;

public record SignupRequest(
    String name,
    String email,
    String password

) {}
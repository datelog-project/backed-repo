package me.jinheum.datelog.dto;

public record SigninResponse(
    UserInfoResponse user,
    String accessToken
) {}

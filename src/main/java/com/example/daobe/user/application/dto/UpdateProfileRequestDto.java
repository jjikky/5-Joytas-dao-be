package com.example.daobe.user.application.dto;

public record UpdateProfileRequestDto(
        String profileImage,
        String nickname
) {
}
package com.pado.domain.auth.dto.response;

public record TokenWithRefreshResponseDto (
    String accessToken,
    String refreshToken
){

}

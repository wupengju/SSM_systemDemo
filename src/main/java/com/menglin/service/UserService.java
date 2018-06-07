package com.menglin.service;

import com.menglin.dto.JWTCheckInfoDto;
import com.menglin.dto.JWTPayloadDto;
import com.menglin.dto.LoginResponseDto;
import com.menglin.dto.UserDto;

public interface UserService {
    LoginResponseDto login(String username, String signature);

    LoginResponseDto quit(String userPreviewFileFolderPath, JWTPayloadDto JWTPayloadDto);

    UserDto registerNewAccount(UserDto userDto);

    JWTPayloadDto getPayloadInfo(String jwt);

    boolean checkJWT(JWTCheckInfoDto jwtCheckInfoDto);
}

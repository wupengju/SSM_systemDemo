package com.menglin.service;

import com.menglin.Bo.UserBo;
import com.menglin.dto.JWTCheckInfo;

public interface UserService {
    UserBo login(String username, String uuid, String signature);

    boolean checkJWT(JWTCheckInfo jwtCheckInfo);
}

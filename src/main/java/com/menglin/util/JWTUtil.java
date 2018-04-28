package com.menglin.util;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.enums.ResultCodeEnum;
import com.menglin.exception.ServiceException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {

    private static Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    private static final String SECRET = "XX#$%()(#*!()!KL<><MQLMNQNQJQK sdfkjsdrow32234545df>?N<:{";
    private static final String EXP = "exp";
    private static final String PAYLOAD = "payload";

    /**
     * 生成Token:jwt
     *
     * @param object 传入的加密对象 - 放入PAYLOAD
     * @param maxAge 过期事件,单位毫秒
     * @param <T>
     * @return
     */
    public static <T> String encrypt(T object, long maxAge) throws UnsupportedEncodingException {

        // header
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        String jsonString = JSON.toJSONString(object);
        long exp = System.currentTimeMillis() + maxAge;
        System.out.println("JWTUtil 当前时间:" + new DateTime().toString("yyyy-MM-dd HH:mm:ss EE"));
        System.out.println("JWTUtil 过期时间:" + new DateTime(exp).toString("yyyy-MM-dd HH:mm:ss EE"));
        String token = JWT.create()
                .withHeader(map) // header
                .withClaim(PAYLOAD, jsonString) // 存放的内容 json
                .withClaim(EXP, new DateTime(exp).toDate()) // Token 过期时间
                .sign(Algorithm.HMAC256(SECRET)); // 密钥

        return token;
    }

    /**
     * 解密token
     *
     * @param token  jwt类型的token
     * @param classT 加密时的类型
     * @param <T>
     * @return 返回解密后的对象 - 如果token过期返回空对象
     */
    public static <T> T decrypt(String token, Class<T> classT) {
        if (token == null) {
            return null;
        }

        JWTVerifier verifier = null;
        DecodedJWT jwt = null;
        try {
            verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            jwt = verifier.verify(token); // 如果超时,直接抛出运行时异常
        } catch (JWTDecodeException e) {
            logger.info("DecodedJWT JWT fail,  token:{}, e:{}", token, e);
            throw new ServiceException(ResultCodeEnum.RESULT_CODE_UNAUTHORIZED.getState(), ResultCodeEnum.RESULT_CODE_UNAUTHORIZED.getStateInfo());
        } catch (UnsupportedEncodingException e) {
            logger.info("UnsupportedEncoding JWT fail,  token:{}, e:{}", token, e);
            throw new ServiceException(ResultCodeEnum.RESULT_CODE_UNAUTHORIZED.getState(), ResultCodeEnum.RESULT_CODE_UNAUTHORIZED.getStateInfo());
        } catch (TokenExpiredException e) {
            logger.info("JWT has Expired,  token:{}, e:{}", token, e);
            throw new ServiceException(ErrorStateEnum.TOKEN_TIMEOUT.getState(), ErrorStateEnum.TOKEN_TIMEOUT.getStateInfo());
        }

        Map<String, Claim> claims = jwt.getClaims();
        Claim exp = claims.get(EXP);
        Claim payload = claims.get(PAYLOAD);
        if (exp != null && payload != null) {
            long tokenTime = exp.asDate().getTime();
            long now = new Date().getTime();
            // 判断令牌是否已经超时
            if (tokenTime > now) {
                String jsonString = payload.asString();
                // 把json转回对象，返回
                return JSON.parseObject(jsonString, classT);
            }
        }

        return null;
    }
}

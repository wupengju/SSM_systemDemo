package com.menglin.service.impl;

import com.menglin.Bo.UserBo;
import com.menglin.common.CommonConst;
import com.menglin.dao.StudentDao;
import com.menglin.dto.JWTCheckInfo;
import com.menglin.dto.PayloadInfo;
import com.menglin.entity.Student;
import com.menglin.enums.ErrorStateEnum;
import com.menglin.exception.ServiceException;
import com.menglin.service.UserService;
import com.menglin.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

import static com.menglin.common.AssertArguments.checkNotEmpty;

@Service("userService")
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private StudentDao studentDao;

    @Override
    public UserBo login(String username, String uuid, String actSignature) {
        checkNotEmpty(username, "用户名不能为空");
        checkNotEmpty(uuid, "随机码不能为空");
        checkNotEmpty(actSignature, "签名不能为空");

        UserBo userBo;

        // 按照不同身份进行用户信息校验
        if (username.length() == 10) { // 学生
            Student student = null;
            try {
                student = studentDao.selectByUsername(username);
                userBo = validateUser(student.getId(), username, student.getPassword(), uuid, actSignature);
            } catch (UnsupportedEncodingException e) {
                logger.info("create Token fail, student:{}, username:{}", student.toString(), username);
                throw new ServiceException("创建 Token 失败");
            } catch (RuntimeException e) {
                logger.info("access sql error, username:{}", username);
                throw new ServiceException(ErrorStateEnum.DB_EXCEPTION.getState(), ErrorStateEnum.DB_EXCEPTION.getStateInfo());
            }
            return userBo;
        } else if (username.length() == 8) { // 老师

        } else {
            throw new ServiceException("用户名格式不正确，请确认后重新登录");
        }

        return null;
    }

    public boolean checkJWT(JWTCheckInfo jwtCheckInfo) {

        // 从Header中取数据
        String jwt = jwtCheckInfo.getAuthorization();
        String currentName = jwtCheckInfo.getCurrentName();
        logger.info("checkJWT, RequestMapping:{}, jwt:{}, currentName:{}", jwtCheckInfo.getRequestUrI(), jwt, currentName);

        PayloadInfo payloadInfo = null;
        try {
            // 获得加密之前的数据
            payloadInfo = JWTUtil.decrypt(jwt, PayloadInfo.class);
        } catch (ServiceException e) {
            logger.info("checkJWT fail, jwt:{}, currentName:{}", jwt, currentName);
            throw e;
        }

        // 利用name 去查询密码
        return payloadInfo != null && currentName.equals(payloadInfo.getUsername());
    }

    private UserBo validateUser(long id, String username, String password, String uuid, String actSignature) throws UnsupportedEncodingException {
        String expectedSignature = createSignature(username, uuid, password);
        if (actSignature.equals(expectedSignature)) {
            UserBo userBo = new UserBo();
            PayloadInfo payloadInfo = new PayloadInfo();
            payloadInfo.setUserId(id);
            payloadInfo.setUsername(username);
            payloadInfo.setIdentity("student");
            String jwt = JWTUtil.encrypt(payloadInfo, CommonConst.JWT_MAXAGE);
            userBo.setUsername(username);
            userBo.setJwt(jwt);
            return userBo;
        } else {
            throw new ServiceException(ErrorStateEnum.SIGNATURE_VALIDATE_FAIL.getState(), ErrorStateEnum.SIGNATURE_VALIDATE_FAIL.getStateInfo());
        }
    }

    private String createSignature(String username, String uuid, String password) {
        String signature = "123";
        return signature;
    }
}

package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.converter.user.UserReadConvert;
import com.sky.dto.user.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.user.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserReadConvert userReadConvert;

    /**
     * 微信登陆
     *
     * @param dto
     * @return
     */
    @Override
    public UserLoginVO login(UserLoginDTO dto) {

        String openid = getOpenid(dto.getCode());
        // 判断 openid 获取成功了吗
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            try {
                user = User.builder()
                        .openid(openid)
                        .name(defaultName())
                        .build();
                userMapper.insert(user);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发下别报错，重新查即可
                user = userMapper.getByOpenid(openid);
            }
        }

        // token生成
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(openid)
                .token(token)
                .build();
    }

    private String defaultName() {
        // 8位随机字母数字（小写），不依赖 openid
        String s = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);
        return "老吃家_" + s;
    }

    private String getOpenid(String code) {
        if (code == null || code.isBlank()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        Map<String, String> params = new HashMap<>();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, params);
        JSONObject obj = JSONObject.parseObject(json);

        String openid = obj.getString("openid");
        if (openid == null || openid.isBlank()) {
            // 可选：把 errcode/errmsg 打到日志里便于排查（注意别泄露敏感信息）
            // Integer errcode = obj.getInteger("errcode");
            // String errmsg = obj.getString("errmsg");
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        return openid;
    }

}

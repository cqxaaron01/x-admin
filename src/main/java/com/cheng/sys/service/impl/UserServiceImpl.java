package com.cheng.sys.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cheng.sys.entity.User;
import com.cheng.sys.mapper.UserMapper;
import com.cheng.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author aaron
 * @since 2023-04-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> login(User user) {

        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        // 根据用户名查询
        lqw.eq(User::getUsername, user.getUsername());
        User loginUser = this.baseMapper.selectOne(lqw);
        // 如果结果不为空，并且密码和传入的是匹配的，则生成token，将用户信息存入redis
        if (loginUser != null && passwordEncoder.matches(user.getPassword(), loginUser.getPassword())) {
            String key = "user:" + UUID.randomUUID();
            // 存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.HOURS);
            Map<String,Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }
        return null;
    }

    /*@Override
    public Map<String, Object> login(User user) {

        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        // 根据用户名和密码查询
        lqw.eq(User::getUsername, user.getUsername());
        lqw.eq(User::getPassword, user.getPassword());
        User loginUser = this.baseMapper.selectOne(lqw);

        // 如果结果不为空，则生成token，将用户信息存入redis
        if (loginUser != null) {
            String key = "user:" + UUID.randomUUID();

            // 存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.HOURS);


            Map<String,Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }


        return null;
    }*/

    @Override
    public Map<String, Object> getUserInfo(String token) {
        // 从redis查询token
        Object obj = redisTemplate.opsForValue().get(token);
        // 反序列化
        User user = JSON.parseObject(JSON.toJSONString(obj),User.class);
        if(user != null){
            Map<String, Object> data =  new HashMap<>();
            data.put("name",user.getUsername());
            data.put("avatar",user.getAvatar());
            List<String> roleList = this.getBaseMapper().getRoleNamesByUserId(user.getId());
            data.put("roles", roleList);
            return data;
        }
        return null;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(token);
    }

}

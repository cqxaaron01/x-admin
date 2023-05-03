package com.cheng.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cheng.common.vo.Result;
import com.cheng.sys.entity.User;
import com.cheng.sys.service.IUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author aaron
 * @since 2023-04-29
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin 解决跨域问题
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @GetMapping("/all")
    public Result<List<User>> getAllUser() {
        return Result.success("查询成功", userService.list());
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        Map<String, Object> data = userService.login(user);
        if (data != null) {
            return Result.success(data);
        }
        return Result.fail(20002, "用户名或密码错误");
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestParam("token") String token) {

        // 根据token获取用户信息 redis
        Map<String, Object> data = userService.getUserInfo(token);

        if (data != null) {
            return Result.success(data);
        }

        return Result.fail(20003, "登录信息无效，请重新登录");
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("X-Token") String token) {
        userService.logout(token);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<Map<String,Object>> getUserList(@RequestParam(value = "username",required = false) String username,
                                                  @RequestParam(value = "phone",required = false) String phone,
                                                  @RequestParam("pageNo") Long pageNO,
                                                  @RequestParam("pageSize") Long pageSize) {
        LambdaQueryWrapper<User> lqw  = new LambdaQueryWrapper<>();
        lqw.likeRight(StringUtils.hasLength(username),User::getUsername,username);
        lqw.likeRight(StringUtils.hasLength(phone),User::getPhone,phone);
        // 按用户id降序排序
//        lqw.orderByDesc(User::getId);
        Page<User> page = new Page<>(pageNO,pageSize);
        userService.page(page,lqw);

        Map<String ,Object> data = new HashMap<>();
        data.put("total",page.getTotal());
        data.put("rows",page.getRecords());
        return Result.success(data);
    }

    @PostMapping
    public Result<?> addUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return Result.success("新增用户成功");
    }

    @PutMapping
    public Result<?> updateUser(@RequestBody User user){
        user.setPassword(null);
        userService.updateById(user);
        return Result.success("修改用户成功");
    }

    @GetMapping("{id}")
    public Result<User> getUserById(@PathVariable("id") Integer id){
        User user = userService.getById(id);
        return Result.success(user);
    }

    @DeleteMapping("{id}")
    public Result<User> deleteUserById(@PathVariable("id") Integer id){
        userService.removeById(id);
        return Result.success("删除用户成功");
    }

}

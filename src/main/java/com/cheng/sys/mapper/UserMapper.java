package com.cheng.sys.mapper;

import com.cheng.sys.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author aaron
 * @since 2023-04-29
 */
public interface UserMapper extends BaseMapper<User> {

    List<String> getRoleNamesByUserId(Integer userId);
}

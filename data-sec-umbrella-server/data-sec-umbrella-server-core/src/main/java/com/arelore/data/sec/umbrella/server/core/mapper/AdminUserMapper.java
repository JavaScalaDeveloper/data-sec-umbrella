package com.arelore.data.sec.umbrella.server.core.mapper;

import com.arelore.data.sec.umbrella.server.core.entity.mysql.AdminUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理中心账号Mapper。
 *
 * @author 黄佳豪
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}

package com.arelore.data.sec.umbrella.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MySQL数据源实体类
 */
@Data
@TableName("mysql_data_source")
public class DataSource {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 数据源类型（MySQL、Oracle、SQL Server）
     */
    @TableField("data_source_type")
    private String dataSourceType;

    /**
     * 实例（域名:端口）
     */
    private String instance;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 连通性（可连接、无法连接）
     */
    private String connectivity;

    /**
     * 拓展信息（JSON字符串）
     */
    @TableField("extend_info")
    private String extendInfo;
}
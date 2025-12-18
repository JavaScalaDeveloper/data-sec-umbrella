CREATE TABLE `database_policy`
(
    `id`                   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `creator`              VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    `modifier`             VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '修改人',
    `policy_code`          VARCHAR(128) NOT NULL COMMENT '策略code',
    `policy_name`          VARCHAR(128) NOT NULL COMMENT '策略名',
    `description`          VARCHAR(255) NOT NULL DEFAULT '' COMMENT '描述',
    `sensitivity_level`    TINYINT      NOT NULL DEFAULT 1 COMMENT '敏感等级 1-5，越高代表越敏感',
    `hide_example`         TINYINT      NOT NULL DEFAULT 0 COMMENT '隐藏样例 0-否 1-是',
    `classification_rules` JSON COMMENT '分类规则',
    `rule_expression`      TEXT COMMENT '规则表达式',
    `ai_rule`              TEXT COMMENT 'AI规则',

    UNIQUE KEY `uk_policy_code` (`policy_code`),
    INDEX                  `idx_modify_time` (`modify_time`),
    INDEX                  `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库策略表';
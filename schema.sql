-- 社团活动报名管理系统 - 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS club_registration
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE club_registration;

-- 1. 用户表

CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    `password`      VARCHAR(255)    NOT NULL                 COMMENT '密码（BCrypt加密）',
    `email`         VARCHAR(100)    NOT NULL                 COMMENT '邮箱',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER'  COMMENT '角色: ROLE_USER / ROLE_ADMIN',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '软删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 分类表

CREATE TABLE IF NOT EXISTS `category` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    `name`          VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
    `description`   VARCHAR(255)    DEFAULT NULL             COMMENT '分类描述',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '软删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动分类表';

-- 3. 活动表

CREATE TABLE IF NOT EXISTS `activity` (
    `id`                    BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '活动ID',
    `title`                 VARCHAR(100)    NOT NULL                 COMMENT '活动标题',
    `description`           TEXT            DEFAULT NULL             COMMENT '活动介绍',
    `location`              VARCHAR(200)    NOT NULL                 COMMENT '活动地点',
    `start_time`            DATETIME        NOT NULL                 COMMENT '活动开始时间',
    `end_time`              DATETIME        NOT NULL                 COMMENT '活动结束时间',
    `registration_deadline` DATETIME        NOT NULL                 COMMENT '报名截止时间',
    `max_participants`      INT             NOT NULL                 COMMENT '最大报名人数',
    `current_participants`  INT             NOT NULL DEFAULT 0       COMMENT '当前报名人数',
    `category_id`           BIGINT          DEFAULT NULL             COMMENT '分类ID',
    `status`                VARCHAR(20)     NOT NULL DEFAULT 'UPCOMING' COMMENT '状态: UPCOMING/ONGOING/ENDED/CANCELLED',
    `created_by`            BIGINT          NOT NULL                 COMMENT '创建者用户ID',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`            TINYINT         NOT NULL DEFAULT 0       COMMENT '软删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 4. 报名记录表

CREATE TABLE IF NOT EXISTS `registration` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '报名记录ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `activity_id`   BIGINT          NOT NULL                 COMMENT '活动ID',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'REGISTERED' COMMENT '状态: REGISTERED/CANCELLED',
    `registered_at` DATETIME        NOT NULL                 COMMENT '报名时间',
    `cancelled_at`  DATETIME        DEFAULT NULL             COMMENT '取消时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名记录表';

-- 5. 操作日志表

CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `user_id`       BIGINT          DEFAULT NULL             COMMENT '操作人ID',
    `username`      VARCHAR(50)     DEFAULT NULL             COMMENT '操作人用户名',
    `action`        VARCHAR(50)     NOT NULL                 COMMENT '操作类型',
    `target_type`   VARCHAR(50)     DEFAULT NULL             COMMENT '目标类型',
    `target_id`     BIGINT          DEFAULT NULL             COMMENT '目标ID',
    `detail`        VARCHAR(500)    DEFAULT NULL             COMMENT '操作详情',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 初始化数据：管理员账号 (密码: admin123, BCrypt加密)

INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES
('admin', '$2a$10$z5xaQhI/8W7EJpaePOKj7ukZUREc.oN7iyCjgThAwArccJtBNiWyi', 'admin@club.com', 'ROLE_ADMIN');

-- 初始化数据：示例分类

INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
('学术讲座', '学术类讲座与分享活动', 1),
('体育运动', '体育比赛与运动类活动', 2),
('文艺演出', '文艺表演与展示活动', 3),
('志愿服务', '公益志愿类活动', 4),
('社团团建', '社团内部团建活动', 5);

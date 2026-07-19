-- 社团活动报名管理系统 - 数据库初始化脚本

SET NAMES utf8mb4;

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
    KEY `idx_created_by` (`created_by`),
    UNIQUE KEY `uk_title` (`title`)
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
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '软删除: 0未删除, 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_is_deleted` (`is_deleted`)
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
('admin', '$2a$10$z5xaQhI/8W7EJpaePOKj7ukZUREc.oN7iyCjgThAwArccJtBNiWyi', 'admin@tju.edu.cn', 'ROLE_ADMIN');

-- 初始化数据：示例分类

INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
('学术讲座', '学术类讲座与分享活动', 1),
('体育运动', '体育比赛与运动类活动', 2),
('文艺演出', '文艺表演与展示活动', 3),
('志愿服务', '公益志愿类活动', 4),
('社团团建', '社团内部团建活动', 5);

-- 初始化数据：示例活动
-- UPCOMING
INSERT INTO `activity` (`title`, `description`, `location`, `start_time`, `end_time`, `registration_deadline`, `max_participants`, `current_participants`, `category_id`, `status`, `created_by`) VALUES
('人工智能前沿技术讲座', '邀请国内外知名学者分享人工智能领域的最新研究成果与技术趋势，涵盖大语言模型、计算机视觉、强化学习等方向。', '北洋园校区 46教 A110', '2026-08-20 14:00:00', '2026-08-20 17:00:00', '2026-08-18 23:59:59', 300, 299, 1, 'UPCOMING', 1),
('2026年校园篮球联赛', '全校范围内的篮球对抗赛，以学院为单位组队参赛，采用小组赛+淘汰赛制。冠军队伍将获得奖杯与证书。', '卫津路校区 体育馆', '2026-09-10 08:00:00', '2026-10-10 18:00:00', '2026-09-05 23:59:59', 200, 89, 2, 'UPCOMING', 1),
('迎新音乐会', '由校大学生艺术团倾情演出，节目涵盖交响乐、民乐、合唱、舞蹈等多种形式，欢迎新老同学前来欣赏。', '北洋园校区 大通学生活动中心', '2026-08-15 19:00:00', '2026-08-15 21:30:00', '2026-08-13 23:59:59', 500, 412, 3, 'UPCOMING', 1),
('社区助老志愿服务活动', '组织志愿者前往周边社区为独居老人提供陪伴、打扫、代购等志愿服务，传递温暖与爱心。每次活动计入志愿服务时长。', '天津市南开区学府街道各社区', '2026-08-09 09:00:00', '2026-08-09 17:00:00', '2026-08-06 23:59:59', 50, 28, 4, 'UPCOMING', 1),

-- ONGOING
('程序设计竞赛集训营', '为期两周的算法与编程集训，覆盖数据结构、动态规划、图论等竞赛知识点，为下半年的区域赛做准备。', '北洋园校区 45教 B211', '2026-07-15 08:30:00', '2026-08-28 17:30:00', '2026-07-12 23:59:59', 80, 64, 1, 'ONGOING', 1),
('社团骨干领导力培训', '面向全校社团骨干成员的封闭式培训，内容涵盖团队管理、活动策划、沟通技巧、危机处理等模块。', '卫津路校区 大通学生活动中心 201', '2026-07-16 09:00:00', '2026-08-20 18:00:00', '2026-07-10 23:59:59', 40, 35, 5, 'ONGOING', 1),

-- ENDED
('学术论文写作工坊', '由研究生导师指导论文选题、文献综述、实验设计和写作规范，帮助同学们掌握学术写作的核心技能。', '北洋园校区 33教 B201', '2026-06-20 14:00:00', '2026-06-20 17:00:00', '2026-06-18 23:59:59', 60, 60, 1, 'ENDED', 1),
('校园马拉松赛', '一年一度的校园马拉松赛事，设置全程、半程和迷你跑三个组别，全校师生均可报名参加。', '北洋园校区 北操场', '2026-05-15 07:00:00', '2026-05-15 12:00:00', '2026-05-10 23:59:59', 1000, 856, 2, 'ENDED', 1),
('"海棠花开"校园合唱比赛', '以学院为单位参加的大型合唱比赛，每个学院选派一支队伍，曲目自选，由校内外专业评委评分。', '卫津路校区 大通学生活动中心', '2026-05-20 18:30:00', '2026-05-20 21:00:00', '2026-05-15 23:59:59', 800, 720, 3, 'ENDED', 1),
('校园环保公益行', '组织志愿者在校内和周边公园开展垃圾分类宣传、河道清理、植树护绿等环保公益活动。', '北洋园校区及周边', '2026-04-22 08:00:00', '2026-04-22 16:00:00', '2026-04-20 23:59:59', 120, 103, 4, 'ENDED', 1),
('社团招新嘉年华', '新学期社团联合招新活动，近百个社团将在太雷广场设立展位，现场展示社团风采，吸引新生加入。', '北洋园校区 太雷广场', '2026-03-05 10:00:00', '2026-03-05 17:00:00', '2026-03-03 23:59:59', 999, 999, 5, 'ENDED', 1),

-- CANCELLED
('校园歌手大赛海选', '原定于春季学期举办的校园歌手大赛海选赛，因场地装修延期，后续另行通知。', '卫津路校区 大学生活动中心', '2026-07-10 13:00:00', '2026-07-10 18:00:00', '2026-07-08 23:59:59', 200, 56, 3, 'CANCELLED', 1);

# Host: localhost  (Version 5.7.12-log)
# Date: 2018-05-20 10:14:32
# Generator: MySQL-Front 5.3  (Build 5.33)

/*!40101 SET NAMES utf8 */;

#
# Structure for table "admin"
#

DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `role_id`    BIGINT(20)  NOT NULL DEFAULT '0'
  COMMENT '角色ID',
  `username`   VARCHAR(7)  NOT NULL DEFAULT ''
  COMMENT '管理员用户名',
  `password`   VARCHAR(32) NOT NULL
  COMMENT '密码(MD5 加密后的密文)',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8
  COMMENT ='管理员信息表';

#
# Data for table "admin"
#

INSERT INTO `admin` VALUES (1, 1, 'admin', 'de59cfe94618e761ae5b7664495ec212', '2018-04-28 00:00:00', NULL);

#
# Structure for table "class_team"
#

DROP TABLE IF EXISTS `class_team`;
CREATE TABLE `class_team` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`       VARCHAR(16) NOT NULL
  COMMENT '班级名',
  `college_id` BIGINT(20)  NOT NULL
  COMMENT '学院id',
  `major_id`   BIGINT(20)  NOT NULL
  COMMENT '专业id',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_college_id` (`college_id`),
  KEY `idx_major_id` (`major_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8
  COMMENT ='班级信息表';

#
# Data for table "class_team"
#

INSERT INTO `class_team` VALUES (1, '1391401', 1, 1, 'N', 'admin', '', '2018-04-28 00:00:00', NULL),
  (2, '1391403', 1, 1, 'N', 'admin', 'admin', '2018-05-01 00:47:59', NULL);

#
# Structure for table "college"
#

DROP TABLE IF EXISTS `college`;
CREATE TABLE `college` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`        VARCHAR(64)  NOT NULL
  COMMENT '学院名称',
  `description` VARCHAR(256) NOT NULL DEFAULT ''
  COMMENT '学院描述',
  `code`        VARCHAR(64)  NOT NULL
  COMMENT '学院代码',
  `is_delete`   CHAR(1)      NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`     VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`    VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`  DATETIME              DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`  DATETIME              DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8
  COMMENT ='学院信息表';

#
# Data for table "college"
#

INSERT INTO `college` VALUES (1, '软件学院', '软件编程', '100001', 'N', 'admin', '', '2018-04-28 00:00:00', NULL),
  (2, '传媒学院', '艺术2222', '1111111222', 'N', 'admin', 'admin', '2018-05-01 12:21:23', '2018-05-01 12:22:10');

#
# Structure for table "course"
#

DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`        VARCHAR(32)  NOT NULL
  COMMENT '课程名',
  `description` VARCHAR(256) NOT NULL DEFAULT ''
  COMMENT '课程描述内容',
  `is_delete`   CHAR(1)      NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`     VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`    VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`  DATETIME              DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`  DATETIME              DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8
  COMMENT ='课程信息表';

#
# Data for table "course"
#

INSERT INTO `course` VALUES (1, '高数', '高等数学', 'N', 'admin', '', '2018-04-28 00:00:00', NULL),
  (2, 'Java', 'java编程', 'N', 'admin', 'admin', '2018-05-01 12:22:53', '2018-05-01 12:23:13');

#
# Structure for table "major"
#

DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`       VARCHAR(64) NOT NULL
  COMMENT '专业名称',
  `category`   VARCHAR(64) NOT NULL
  COMMENT '专业分类',
  `code`       VARCHAR(64) NOT NULL
  COMMENT '专业代码',
  `college_id` BIGINT(20)  NOT NULL DEFAULT '0'
  COMMENT '学院ID',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_college_id` (`college_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 32
  DEFAULT CHARSET = utf8
  COMMENT ='专业信息表';

#
# Data for table "major"
#

INSERT INTO `major` VALUES (1, '软件工程', '互联网', '10000002', 1, 'N', 'admin', '', '2018-04-28 00:00:00', NULL),
  (2, '英语+软件222', '计算机', '11111112222', 1, 'N', 'admin', 'admin', '2018-05-01 12:29:22', '2018-05-01 12:29:43'),
  (31, '英语+软件qqq', '计算机', '1111111', 1, 'N', 'admin', '', '2018-05-10 12:10:55', NULL);

#
# Structure for table "major_course"
#

DROP TABLE IF EXISTS `major_course`;
CREATE TABLE `major_course` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `major_id`   BIGINT(20)  NOT NULL
  COMMENT '专业id',
  `course_id`  BIGINT(20)  NOT NULL
  COMMENT '课程id',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_course_id` (`course_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8
  COMMENT ='专业课程信息表';

#
# Data for table "major_course"
#

INSERT INTO `major_course` VALUES (1, 1, 1, 'N', 'admin', '', '2018-04-28 00:00:00', NULL);

#
# Structure for table "notice"
#

DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`         BIGINT(20)   NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`       VARCHAR(32)  NOT NULL
  COMMENT '通知名',
  `content`    VARCHAR(512) NOT NULL DEFAULT ''
  COMMENT '通知内容',
  `url`        VARCHAR(128)          DEFAULT ''
  COMMENT '文档通知的url',
  `admin_id`   BIGINT(20)   NOT NULL DEFAULT '1'
  COMMENT '管理员id',
  `is_delete`  CHAR(1)      NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME              DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME              DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8
  COMMENT ='系统通知信息表';

#
# Data for table "notice"
#

INSERT INTO `notice` VALUES (1, '新系统发布', '新系统发布了', '', 1, 'N', 'admin', '', '2018-04-28 00:00:00', NULL);

#
# Structure for table "permission"
#

DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`        VARCHAR(32)  NOT NULL
  COMMENT '权限名称',
  `description` VARCHAR(128) NOT NULL
  COMMENT '描述',
  `is_delete`   CHAR(1)      NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`     VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`    VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`  DATETIME              DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`  DATETIME              DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT ='权限信息表';

#
# Data for table "permission"
#


#
# Structure for table "role"
#

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`        VARCHAR(32)  NOT NULL
  COMMENT '角色名称',
  `description` VARCHAR(128) NOT NULL
  COMMENT '描述',
  `is_delete`   CHAR(1)      NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`     VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`    VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`  DATETIME              DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`  DATETIME              DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8
  COMMENT ='角色信息表';

#
# Data for table "role"
#

INSERT INTO `role`
VALUES (1, 'admin', '管理员的默认角色', 'N', '', '', NULL, NULL), (2, 'teacher', '教师的默认角色', 'N', '', '', NULL, NULL),
  (3, 'student', '学生的默认角色', 'N', '', '', NULL, NULL);

#
# Structure for table "role_permission"
#

DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `role_id`       BIGINT(20)  NOT NULL
  COMMENT '角色id',
  `permission_id` BIGINT(20)  NOT NULL
  COMMENT '权限id',
  `is_delete`     CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`       VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`      VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`    DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`    DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT ='角色_权限表';

#
# Data for table "role_permission"
#


#
# Structure for table "student"
#

DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `username`      CHAR(10)    NOT NULL
  COMMENT '学生用户名',
  `password`      VARCHAR(32) NOT NULL
  COMMENT '密码(MD5 加密后的密文)',
  `name`          VARCHAR(16) NOT NULL DEFAULT ''
  COMMENT '学生姓名',
  `grade`         CHAR(4)     NOT NULL DEFAULT ''
  COMMENT '年级如2014',
  `role_id`       BIGINT(20)  NOT NULL DEFAULT '0'
  COMMENT '角色ID',
  `college_id`    BIGINT(20)  NOT NULL DEFAULT '0'
  COMMENT '学院id',
  `major_id`      BIGINT(20)  NOT NULL
  COMMENT '专业id',
  `class_team_id` BIGINT(20)  NOT NULL
  COMMENT '班级id',
  `is_delete`     CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`       VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`      VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`    DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`    DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_college_id` (`college_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_class_id` (`class_team_id`),
  KEY `idx_role_id` (`role_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 32
  DEFAULT CHARSET = utf8
  COMMENT ='学生信息表';

#
# Data for table "student"
#

INSERT INTO `student` VALUES
  (1, '2014214060', '12345', '吴鹏举', '2014', 3, 1, 1, 1, 'N', 'admin', '2014214060', '2018-04-28 00:00:00',
   '2018-05-02 13:42:43'),
  (24, '2014214050', '123', '逗比4', '2014', 3, 1, 1, 2, 'N', 'admin', 'admin', '2018-04-30 18:00:44',
   '2018-04-30 18:40:10'),
  (30, '2014214000', 'de59cfe94618e761ae5b7664495ec212', '测试学生', '2016', 3, 1, 1, 1, 'N', '2014214060', '2014214000',
   '2018-05-08 15:49:26', '2018-05-10 14:46:27'),
  (31, '2014214001', 'de59cfe94618e761ae5b7664495ec212', '测试学生1', '2015', 3, 1, 1, 1, 'N', '2014214001', '',
   '2018-05-10 11:34:57', NULL);

#
# Structure for table "student_course"
#

DROP TABLE IF EXISTS `student_course`;
CREATE TABLE `student_course` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `student_id` BIGINT(20)  NOT NULL
  COMMENT '学生id',
  `course_id`  BIGINT(20)  NOT NULL
  COMMENT '课程id',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_course_id` (`course_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8
  COMMENT ='学生课程信息表';

#
# Data for table "student_course"
#

INSERT INTO `student_course`
VALUES (1, 1, 1, 'N', 'admin', '', '2018-04-28 00:00:00', NULL), (2, 30, 1, 'N', '', '', NULL, NULL),
  (3, 24, 1, 'N', '', '', NULL, NULL), (4, 31, 1, 'N', '', '', NULL, NULL), (5, 1, 2, 'N', '', '', NULL, NULL),
  (6, 24, 2, 'N', '', '', NULL, NULL), (7, 30, 2, 'N', '', '', NULL, NULL), (8, 31, 2, 'N', '', '', NULL, NULL);

#
# Structure for table "student_task"
#

DROP TABLE IF EXISTS `student_task`;
CREATE TABLE `student_task` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `student_id` BIGINT(20)  NOT NULL
  COMMENT '学生id',
  `task_id`    BIGINT(20)  NOT NULL
  COMMENT '作业id',
  `content`    VARCHAR(512)         DEFAULT ''
  COMMENT '作答内容',
  `url`        VARCHAR(128)         DEFAULT ''
  COMMENT '作答的附件',
  `score`      VARCHAR(3)           DEFAULT ''
  COMMENT '分数',
  `comments`   VARCHAR(256)         DEFAULT ''
  COMMENT '教师评语',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_task_id` (`task_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 23
  DEFAULT CHARSET = utf8
  COMMENT ='学生作业信息表';

#
# Data for table "student_task"
#

INSERT INTO `student_task` VALUES
  (5, 1, 1, 'ppp', 'F:\\Server\\OfficeToPDF\\uploadFile\\2014214060_Java书单.md', '100', 'A+', 'N', '12345678',
      '2014214060', '2018-05-02 09:56:37', '2018-05-07 00:21:29'),
  (6, 24, 3, 'qqqq', NULL, NULL, NULL, 'N', '12345678', '', '2018-05-02 09:56:37', NULL),
  (7, 30, 1, 'aaaaa', '', '100', '', 'N', '12341234', '', NULL, NULL),
  (8, 1, 4, 'wwwww', NULL, '100', 'A+', 'N', '12341234', '12341234', '2018-05-10 12:45:02', '2018-05-10 14:26:02'),
  (9, 30, 4, NULL, 'F:\\Server\\OfficeToPDF\\uploadFile\\2014214000\\testPPt.pptx', NULL, NULL, 'N', '12341234', '2014214000', '2018-05-10 12:45:02', '2018-05-10 22:11:14'),
  (10, 31, 4, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 12:45:02', NULL),
  (11, 24, 4, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 12:45:02', NULL),
  (12, 1, 2, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:53:02', NULL),
  (13, 30, 2, NULL, 'F:\\Server\\OfficeToPDF\\uploadFile\\2014214000\\testExcel.xlsx', NULL, NULL, 'N', '12341234', '2014214000', '2018-05-10 14:53:03', '2018-05-10 17:25:57'),
  (14, 31, 2, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:53:03', NULL),
  (15, 24, 2, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:53:03', NULL),
  (16, 1, 3, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:54:55', NULL),
  (17, 30, 3, 'sssss', 'F:\\Server\\OfficeToPDF\\uploadFile\\2014214000\\testExcel.xlsx', NULL, NULL, 'N', '12341234',
       '2014214000', '2018-05-10 14:54:55', '2018-05-10 17:30:18'),
  (18, 31, 3, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:54:55', NULL),
  (19, 1, 31, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:55:10', NULL),
  (20, 30, 31, NULL, 'F:\\Server\\OfficeToPDF\\uploadFile\\2014214000\\testPPt.pptx', NULL, NULL, 'N', '12341234',
       '2014214000', '2018-05-10 14:55:10', '2018-05-10 17:30:25'),
  (21, 31, 31, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:55:10', NULL),
  (22, 24, 31, NULL, NULL, NULL, NULL, 'N', '12341234', '', '2018-05-10 14:55:11', NULL);

#
# Structure for table "task"
#

DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `name`        VARCHAR(32)  NOT NULL
  COMMENT '作业名',
  `description` VARCHAR(512) NOT NULL DEFAULT ''
  COMMENT '作业描述内容即题目信息',
  `url`         VARCHAR(128)          DEFAULT ''
  COMMENT '作业内容附件',
  `course_id`   BIGINT(20)   NOT NULL
  COMMENT '课程id',
  `teacher_id`  BIGINT(20)   NOT NULL
  COMMENT '教师id',
  `is_delete`   CHAR(1)      NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`     VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`    VARCHAR(32)  NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`  DATETIME              DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`  DATETIME              DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_teacher_id` (`teacher_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 32
  DEFAULT CHARSET = utf8
  COMMENT ='作业信息表';

#
# Data for table "task"
#

INSERT INTO `task`
VALUES (1, '高等数学', '求极限', '', 1, 1, 'N', '张三', '12341234', '2018-05-01 00:00:00', '2018-05-17 15:11:27'),
  (2, '编程作业', '四则运算', '', 1, 2, 'N', '逗比2', '张三', '2018-05-01 21:39:19', '2018-05-01 21:39:34'),
  (3, '前端编程作业', 'JavaScript', 'F:\\Server\\OfficeToPDF\\uploadFile\\12341234\\testExcel.xlsx', 2, 1, 'N', '张三',
      '12341234', '2018-05-02 01:06:09', '2018-05-10 17:25:27'),
  (4, '后端编程作业', 'Node', 'F:\\Server\\OfficeToPDF\\uploadFile\\12341234\\testExcel.xlsx', 2, 1, 'N', '12345678',
      '12341234', '2018-05-02 09:53:42', '2018-05-17 15:02:54'),
  (31, '后端编程作业222', 'Node', '', 1, 1, 'N', '12341234', '', '2018-05-10 12:42:54', NULL);

#
# Structure for table "teacher"
#

DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `username`   CHAR(8)     NOT NULL
  COMMENT '教师用户名',
  `password`   VARCHAR(32) NOT NULL
  COMMENT '密码(MD5 加密后的密文)',
  `name`       VARCHAR(16) NOT NULL DEFAULT ''
  COMMENT '教师姓名',
  `role_id`    BIGINT(20)  NOT NULL DEFAULT '0'
  COMMENT '角色ID',
  `college_id` BIGINT(20)  NOT NULL
  COMMENT '学院id',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_college_id` (`college_id`),
  KEY `idx_role_id` (`role_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 32
  DEFAULT CHARSET = utf8
  COMMENT ='教师信息表';

#
# Data for table "teacher"
#

INSERT INTO `teacher`
VALUES (1, '12345678', '123', '张三', 2, 1, 'N', 'admin', '张三', '2018-04-28 00:00:00', '2018-05-02 01:27:39'),
  (2, '01234567', '123', '逗比2', 2, 1, 'N', 'admin', 'admin', '2018-05-01 11:06:12', '2018-05-01 11:07:43'),
  (30, '12341234', 'de59cfe94618e761ae5b7664495ec212', '测试教师', 2, 1, 'N', '12341234', '12341234', '2018-05-08 15:50:29',
       '2018-05-10 14:41:03'),
  (31, '12341235', 'de59cfe94618e761ae5b7664495ec212', '测试教师', 2, 1, 'N', '12341235', '', '2018-05-10 11:35:10', NULL);

#
# Structure for table "teacher_class_team"
#

DROP TABLE IF EXISTS `teacher_class_team`;
CREATE TABLE `teacher_class_team` (
  `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `teacher_id`    BIGINT(20)  NOT NULL
  COMMENT '教师id',
  `class_team_id` BIGINT(20)  NOT NULL
  COMMENT '班级id',
  `is_delete`     CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`       VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`      VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create`    DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify`    DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_class_team_id` (`class_team_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8
  COMMENT ='教师_班级信息表';

#
# Data for table "teacher_class_team"
#

INSERT INTO `teacher_class_team`
VALUES (1, 30, 1, 'N', '', '', NULL, NULL), (2, 30, 2, 'N', '', '', NULL, NULL), (3, 31, 1, 'N', '', '', NULL, NULL),
  (4, 31, 2, 'N', '', '', NULL, NULL);

#
# Structure for table "teacher_course"
#

DROP TABLE IF EXISTS `teacher_course`;
CREATE TABLE `teacher_course` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT
  COMMENT '主键，自增',
  `teacher_id` BIGINT(20)  NOT NULL
  COMMENT '教师id',
  `course_id`  BIGINT(20)  NOT NULL
  COMMENT '课程id',
  `is_delete`  CHAR(1)     NOT NULL DEFAULT 'N'
  COMMENT '是否删除',
  `creator`    VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '创建者',
  `modifier`   VARCHAR(32) NOT NULL DEFAULT ''
  COMMENT '最后修改者',
  `gmt_create` DATETIME             DEFAULT NULL
  COMMENT '创建时间',
  `gmt_modify` DATETIME             DEFAULT NULL
  COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_course_id` (`course_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8
  COMMENT ='教师课程信息表';

#
# Data for table "teacher_course"
#

INSERT INTO `teacher_course` VALUES (1, 1, 1, 'N', 'admin', '', '2018-04-28 00:00:00', NULL);

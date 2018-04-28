create table `version_customized_property` (
  `id` unsigned bigint  not null auto_increment comment '主键，自增',
  `version_id` bigint  not null comment '版本号',
  `version_type` varchar(16)  not null comment '版本类型 FOTA/COMMON/APP',
  `name` varchar(32)  not null comment '自定义属性名',
  `value` varchar(2000)  not null comment '自定义属性值',
  `value_type` varchar(16)  not null comment '自定义属性值的属性类型 WRITE/FILE',
  `black_white_type` varchar(16)  not null comment '黑白名单 BLACK/WHITE',
  `gmt_create` datetime comment '创建时间',
  `gmt_modify` datetime comment '最后修改时间',
  `is_delete` char(1)  not null default 'N' comment '是否删除',
  `creator` varchar(32)  not null default '' comment '创建者',
  `modifier` varchar(32)  not null default '' comment '最后修改者',
  primary key (id),
  key `idx_version_id_version_type` (version_id,version_type)
) comment='版本自定义属性表';
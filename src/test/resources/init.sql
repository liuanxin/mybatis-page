
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_name` VARCHAR(32) NOT NULL COMMENT '用户名',
  `password` VARCHAR(64) NOT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) COMMENT='用户表' ENGINE=InnoDB;

DROP PROCEDURE IF EXISTS `test_user`;
DELIMITER //
CREATE PROCEDURE `test_user`(
  cnt int
)
begin
  declare i int;
  start transaction;
  set i = 1;
  while i <= cnt do
    INSERT INTO `t_user`(`user_name`, `password`)
    VALUES(repeat(round(rand() * 1000), 3), md5(repeat(round(rand() * 1000), 4)));
    set i = i + 1;
  end while;
  commit;
end//
DELIMITER ;

-- 生成 200 条随机数据
CALL test_user(200);
DROP PROCEDURE `test_user`;

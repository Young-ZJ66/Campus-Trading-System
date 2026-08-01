CREATE DATABASE IF NOT EXISTS `campus_trading` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `campus_trading`;

-- 1. sys_user (用户表)
CREATE TABLE `sys_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
  `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分余额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用, 1-正常)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. sys_news (资讯公告表)
CREATE TABLE `sys_news` (
  `news_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资讯ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `content` TEXT NOT NULL COMMENT '内容',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶(0-否, 1-是)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-下架, 1-发布)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`news_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯公告表';

-- 3. goods_category (商品分类表)
CREATE TABLE `goods_category` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID(0为顶级分类)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用, 1-正常)',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 4. goods_info (商品信息表)
CREATE TABLE `goods_info` (
  `goods_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `description` TEXT NOT NULL COMMENT '详情描述',
  `images` JSON DEFAULT NULL COMMENT '图片列表(JSON数组)',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `is_exchange` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持换物(0-否, 1-是)',
  `exchange_desc` VARCHAR(500) DEFAULT NULL COMMENT '期望换取物品描述',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-在售, 1-已售出, 2-已下架)',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`goods_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 5. goods_order (交易订单表)
CREATE TABLE `goods_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
  `trade_type` TINYINT NOT NULL DEFAULT 0 COMMENT '交易类型(0-普通购买, 1-以物换物)',
  `exchange_goods_id` BIGINT DEFAULT NULL COMMENT '交换商品ID(换物模式下使用)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态(普通购买: 1-待收货, 2-已完成, 3-已取消; 以物换物: 0-待卖家同意, 1-已同意, 2-已完成, 3-已取消, 4-已拒绝)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 6. goods_comment (留言表)
CREATE TABLE `goods_comment` (
  `comment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '留言内容',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父级留言ID(用于回复)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言表';

-- 7. user_favorite (收藏表)
CREATE TABLE `user_favorite` (
  `favorite_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `uk_user_goods` (`user_id`, `goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 8. point_goods (积分奖励物品表)
CREATE TABLE `point_goods` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `name` VARCHAR(255) NOT NULL COMMENT '名称',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '描述',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '图片',
  `points_required` INT NOT NULL COMMENT '所需积分',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-下架, 1-上架)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分奖励物品表';

-- 9. point_record (积分明细表)
CREATE TABLE `point_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `change_type` TINYINT NOT NULL COMMENT '变动类型(0-签到获取, 1-交易获取, 2-兑换消耗)',
  `change_amount` INT NOT NULL COMMENT '变动积分数(+或-)',
  `balance_after` INT NOT NULL COMMENT '变动后余额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细表';

-- 10. point_order (积分兑换订单表)
CREATE TABLE `point_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '兑换用户ID',
  `item_id` BIGINT NOT NULL COMMENT '兑换商品ID',
  `points_used` INT NOT NULL COMMENT '消耗积分',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待核销, 1-已完成',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换订单表';

-- 初始化测试数据 (密码统设为123456)
INSERT INTO `sys_user` (`student_no`, `nickname`, `password`, `phone`, `points`) VALUES 
('admin', '系统管理员', '$2a$10$x4QdatbarahhwWT8OnqfJ.lBtmK9yVFC8TNVgyoss3tkD2FBmokmW', '13800138000', 9999) /* 系统管理员 */,
('20220001', '张三', '$2a$10$VERIFi99u9UYY2EE1x1cauesvFlZyiVnZAq49PWM.keQqWWpzJCOm', '13900139001', 150) /* 用户-张三 */,
('20220002', '李四', '$2a$10$ZAFn6DH/s7R97LQ2Q3uB9ebZRgwisabXGHPYnQYEoY5ylUcb4QbYm', '13900139002', 300) /* 用户-李四 */,
('20220003', '王五', '$2a$10$15zDnjL6saIhLgqQlcMWcO8TpeMCAMH2EJow.jJ2TDT7LN3Gw.Ag6', '13900139003', 50) /* 用户-王五 */,
('20220004', '赵六', '$2a$10$T5wdOCfvytTu5s9t3tmyeuk9rXFgkyKhA9XVV2FPW0Y.FN0yEDSLa', '13900139004', 120) /* 用户-赵六 */,
('20220005', '陈七', '$2a$10$GZrCvnSsDL5G76a2QB5FQecW6FwGhlecmJo3BLgt4nstH5mZZ/ojS', '13900139005', 80) /* 用户-陈七 */,
('20220006', '周八', '$2a$10$EFaJ9br/ZMkS7pYqcnLu/.JhKTLoY/KQ6zwyrJm0ti3uWS0kL2CW6', '13900139006', 200) /* 用户-周八 */,
('20220007', '吴九', '$2a$10$jcmHJb5qwjFbO/LsiE/Y0.ZFUaG4Ii7tvAkanwGkH/fjShbbTTnIS', '13900139007', 10) /* 用户-吴九 */;

INSERT INTO `goods_category` (`name`, `sort`) VALUES 
('教材教辅', 1) /* 分类-教材教辅 */,
('数码电子', 2) /* 分类-数码电子 */,
('生活用品', 3) /* 分类-生活用品 */,
('美妆服饰', 4) /* 分类-美妆服饰 */,
('体育器材', 5) /* 分类-体育器材 */,
('其他闲置', 6) /* 分类-其他闲置 */;

-- 插入积分商品测试数据
INSERT INTO `point_goods` (`name`, `description`, `image`, `points_required`, `stock`, `status`, `create_time`) VALUES 
('校园定制帆布袋', '印有学校标志性建筑的限量版帆布袋，大容量，结实耐用。', '/uploads/帆布袋.jpg', 500, 50, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)) /* 积分商品-校园定制帆布袋 */,
('精致钥匙扣', '带有学校logo的精美金属钥匙扣，小巧实用。', '/uploads/钥匙扣.jpg', 100, 200, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)) /* 积分商品-精致钥匙扣 */,
('精美笔记套装', '高质感硬抄本配合精致水笔，适合做课堂笔记或手账。', '/uploads/精美笔记套装.jpg', 200, 100, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)) /* 积分商品-精美笔记套装 */;
INSERT INTO `sys_news` (`title`, `cover_image`, `content`, `view_count`, `is_top`, `create_time`) VALUES 
('【防骗指南】校园二手交易避坑指南，请同学们务必阅读！', 'https://via.placeholder.com/300x200?text=Security+Guide', '<p>亲爱的同学们：</p><p>近期校园内二手交易频繁，为了保障大家的财产安全，特发布此防骗指南...</p><p>1. 尽量选择校园内面交<br>2. 不要轻易脱离平台转账<br>3. 收到物品前仔细检查</p>', 1580, 1, DATE_SUB(NOW(), INTERVAL 1 DAY)) /* 资讯-防骗指南(置顶) */,
('【通知】积分商城上新啦！限量版校园帆布袋等你来兑换', 'https://via.placeholder.com/300x200?text=New+Arrivals', '<p>好消息！本周积分商城新上了印有校园风景的限量版帆布袋，只需 500 积分即可兑换，数量有限，先到先得！</p>', 856, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)) /* 资讯-积分商城上新 */,
('期末清仓季开启，让闲置物品发挥余热', 'https://via.placeholder.com/300x200?text=Clearance+Sale', '<p>马上又要到毕业季和期末季了，宿舍里带不走的书籍和生活用品，赶紧发布到平台上来吧，学弟学妹们正需要呢！</p>', 342, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)) /* 资讯-期末清仓季 */;
-- 插入商品测试数据
-- user_id 2 (张三) 发布的商品
-- user_id 3 (李四) 发布的商品
-- user_id 4 (王五) 发布的商品
-- 其他用户发布的商品
INSERT INTO `goods_info` (`user_id`, `category_id`, `title`, `description`, `images`, `original_price`, `price`, `is_exchange`, `exchange_desc`, `status`, `view_count`, `create_time`) VALUES 
(2, 1, '《高等数学》第七版 上下册', '九成新，上课没怎么翻过，里面有少量学霸笔记。不包邮，南门面交。', '["/uploads/高等数学.jpg"]', 85.00, 25.00, 0, NULL, 0, 120, DATE_SUB(NOW(), INTERVAL 5 DAY)) /* 商品-高等数学(张三) */,
(2, 3, '九成新小风扇', '宿舍桌面小风扇，风力大，带USB接口，毕业清仓带不走了。', '["/uploads/小风扇.jpg"]', 45.00, 15.00, 1, '希望能换几包抽纸或者湿巾', 0, 45, DATE_SUB(NOW(), INTERVAL 2 DAY)) /* 商品-小风扇(张三,支持换物) */,
(3, 2, '吃灰的Kindle Paperwhite 3', '考研买的，结果全用来盖泡面了。贴了膜带保护壳，无暗病。也可以换个好点的降噪耳机。', '["/uploads/KindlePaperwhite3.jpg"]', 958.00, 350.00, 1, '降噪蓝牙耳机(加钱可补差价)', 0, 356, DATE_SUB(NOW(), INTERVAL 10 DAY)) /* 商品-Kindle Paperwhite 3(李四,支持换物) */,
(3, 1, '考研英语红宝书真题解析', '23考研英语一真题解析，部分有铅笔做题痕迹，擦掉还能用。', '["/uploads/红宝书真题解析.jpg"]', 68.00, 20.00, 0, NULL, 0, 88, DATE_SUB(NOW(), INTERVAL 1 DAY)) /* 商品-红宝书真题解析(李四) */,
(4, 2, '几乎全新的罗技G304无线鼠标', '用了不到一个月，换了毒蝰所以闲置了。有包装盒，电池还有电。', '["/uploads/罗技G304.jpg"]', 189.00, 120.00, 0, NULL, 0, 210, DATE_SUB(NOW(), INTERVAL 3 DAY)) /* 商品-罗技G304(王五) */,
(5, 4, 'YSL圣罗兰口红', '仅试色，色号不合适，专柜正品带包装。', '["/uploads/口红.jpg"]', 380.00, 200.00, 0, NULL, 0, 520, DATE_SUB(NOW(), INTERVAL 1 DAY)) /* 商品-YSL口红(赵六) */,
(5, 4, '全新运动服套装', '买小了，吊牌未拆，适合160-165身高的女生。', '["/uploads/运动服套装.jpg"]', 150.00, 80.00, 1, '换大一码的或者换别的衣服', 0, 120, DATE_SUB(NOW(), INTERVAL 2 DAY)) /* 商品-运动服套装(赵六,支持换物) */,
(6, 5, '李宁篮球', '室外打过几次，弹性很好，带打气筒和球针。', '["/uploads/李宁篮球.jpg"]', 120.00, 50.00, 0, NULL, 0, 300, DATE_SUB(NOW(), INTERVAL 4 DAY)) /* 商品-李宁篮球(陈七) */,
(6, 5, '尤尼克斯羽毛球拍', '社团发的，自己不打羽毛球，全新未拆封。', '["/uploads/尤尼克斯羽毛球拍.jpg"]', 200.00, 150.00, 1, '换个好点的乒乓球拍', 0, 180, DATE_SUB(NOW(), INTERVAL 6 DAY)) /* 商品-尤尼克斯羽毛球拍(陈七,支持换物) */,
(7, 3, '小米台灯', '光线柔和，可调色温，毕业带不走了。', '["/uploads/小米台灯.jpg"]', 149.00, 60.00, 0, NULL, 0, 250, DATE_SUB(NOW(), INTERVAL 8 DAY)) /* 商品-小米台灯(周八) */,
(7, 3, '大号收纳箱', '买多了，全新没用过，很结实。', '["/uploads/大号收纳箱.jpg"]', 30.00, 15.00, 0, NULL, 0, 90, DATE_SUB(NOW(), INTERVAL 2 DAY)) /* 商品-大号收纳箱(周八) */,
(8, 2, 'AirPods Pro 一代', '换二代了，出掉一代。箱说全，带几个保护套，电池还算耐用。', '["/uploads/AirPods Pro 一代.jpg"]', 1999.00, 600.00, 0, NULL, 0, 880, DATE_SUB(NOW(), INTERVAL 1 DAY)) /* 商品-AirPods Pro 一代(吴九) */,
(8, 6, '折叠自行车', '买来在校园里骑的，很方便，毕业出。', '["/uploads/折叠自行车.jpeg"]', 350.00, 120.00, 1, '可以换个机械键盘', 0, 420, DATE_SUB(NOW(), INTERVAL 3 DAY)) /* 商品-折叠自行车(吴九,支持换物) */;

-- 插入订单测试数据
-- 订单1: 王五(4) 购买了 张三(2) 的 高数书(1)
INSERT INTO `goods_order` (`order_no`, `buyer_id`, `seller_id`, `goods_id`, `amount`, `trade_type`, `exchange_goods_id`, `status`, `create_time`) VALUES 
('OD1688880001A1B2', 4, 2, 1, 25.00, 0, NULL, 2, DATE_SUB(NOW(), INTERVAL 4 DAY)) /* 订单-王五购买张三的高数书(已完成) */;
-- 将商品1状态改为已售出
UPDATE `goods_info` SET `status` = 1 WHERE `goods_id` = 1;

-- 订单2: 张三(2) 想用 小风扇(2) 换 李四(3) 的 考研英语(4) （假设李四的考研英语支持换物）
UPDATE `goods_info` SET `is_exchange` = 1, `exchange_desc` = '换点日用品也行' WHERE `goods_id` = 4;
INSERT INTO `goods_order` (`order_no`, `buyer_id`, `seller_id`, `goods_id`, `amount`, `trade_type`, `exchange_goods_id`, `status`, `create_time`) VALUES 
('OD1688880002C3D4', 2, 3, 4, 0.00, 1, 2, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)) /* 订单-张三用小风扇换李四的红宝书(待卖家同意) */;

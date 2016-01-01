START TRANSACTION;
# Const tables
INSERT INTO Levels (level_id, description) VALUES
  (1, '普通'), (2, '特色'), (3, '高档');
INSERT INTO Locations (location_id, city) VALUES
  (1,'北京'),(2,'上海'),(3,'新加坡'),(4,'马来西亚'),(5,'日本'),(6,'纽约'),(7, '洛杉矶'), (8, '旧金山');
INSERT INTO Languages (language_id, code, description) VALUES
  (1, 'cn', '中文'),
  (2, 'en', '英文'),
  (3, 'jp', '日语');
INSERT INTO BillingMethods (billing_method_id, description) VALUES
  (1, 'VISA / Master / AE'),
  (2, 'Paypal'),
  (3, 'Alipay');
INSERT INTO Interests (interest_id, interest_name) VALUES
  (1, '浪漫'),
  (2, '商务'),
  (3, '家庭');
COMMIT;

START TRANSACTION;
# Create a guide 1
INSERT INTO Users (user_id, user_name, email, password) VALUES
  (101, 'guide1', 'guide1@a.com', SHA2('guide1pass', 256));
INSERT INTO Guides (guide_id, user_id, description, level_id, max_persons, citizenship, has_car, score) VALUES
  (101, 101, '导游一', 2, 4, 'US', true, 3.7);
SET @new_guide_id = 101;
INSERT INTO GuideReservations (guide_id, reserved_date, location_id) VALUES
  (101, 20151215, 7),
  (@new_guide_id, 20151216, 7),
  (@new_guide_id, 20151217, 8),
  (@new_guide_id, 20151218, 8);
INSERT INTO GuideLanguages (guide_id, language_id) VALUES
  (@new_guide_id, 1), (@new_guide_id, 2);
INSERT INTO GuideLocations (guide_id, location_id, price_usd, price_cny, is_effective) VALUES
  (@new_guide_id, 6, 500.0, 4000.0, true),
  (@new_guide_id, 6, 400.0, 3000.0, false),
  (@new_guide_id, 7, 400.0, 3000.0, true),
  (@new_guide_id, 8, 400.0, 3000.0, true);
INSERT INTO GuideBillingMethods (guide_id, billing_method_id) VALUES
  (@new_guide_id, 1),
  (@new_guide_id, 2);

# Create guide 2
INSERT INTO Users (user_id, user_name, email, password) VALUES
  (102, 'guide2', 'guide2@a.com', SHA2('guide2pass', 256));
INSERT INTO Guides (guide_id, user_id, description, level_id, max_persons, citizenship, has_car, score) VALUES
  (102, 102, '导游2', 3, 10, 'US', true, 4.2);
SET @new_guide_id = 102;
INSERT INTO GuideReservations (guide_id, reserved_date, location_id) VALUES
  (@new_guide_id, 20151217, 7),
  (@new_guide_id, 20151218, 7);
INSERT INTO GuideLanguages (guide_id, language_id) VALUES
  (@new_guide_id, 1), (@new_guide_id, 2);
INSERT INTO GuideLocations (guide_id, location_id, price_usd, price_cny, is_effective) VALUES
  (@new_guide_id, 6, 600.0, 4500.0, true),
  (@new_guide_id, 7, 500.0, 3600.0, true),
  (@new_guide_id, 8, 500.0, 3600.0, true);
INSERT INTO GuideBillingMethods (guide_id, billing_method_id) VALUES
  (@new_guide_id, 1),
  (@new_guide_id, 2);
COMMIT;

/*
 * Initialize city data.
 */
START TRANSACTION;

INSERT INTO Spots (city_id, spot_id, name, hours, score, interests) VALUES
  (8, 1, '金门大桥', 2, 0.9, '浪漫'),
  (8, 2, '九曲花街', 1, 0.9, '浪漫'),
  (8, 3, '渔人码头', 2, 0.9, ''),
  (8, 4, '旧金山艺术宫', 1, 0.8, ''),
  (8, 5, '旧金山唐人街', 2, 0.7, ''),
  (8, 6, '联合广场', 1, 0.7, ''),
  (8, 7, '双子峰', 2, 0.7, ''),
  (8, 8, '恶魔岛', 4, 0.8, ''),
  (8, 9, '金门公园', 7, 0.8, ''),
  (8, 10, '旧金山市政厅', 2, 0.6, '浪漫|商务'),
  (8, 11, 'Castro St', 2, 0.7, ''),
  (8, 12, '阿拉莫广场', 2, 0.7, '');


INSERT INTO GuideCities (guide_id, city_id) VALUES
  (1, 1), (1, 2), (1, 3),
  (2, 1), (2, 4), (2, 5),
  (3, 1), (3, 2), (3, 3), (3, 4), (3, 5),
  (4, 1), (4, 2),
  (5, 1), (5, 3),
  (6, 2), (6, 3),
  (7, 2), (7, 3), (7, 4);

INSERT INTO Guides (guide_id, score, interests) VALUES
  (1, 0.9, '浪漫'),
  (2, 0.8, '浪漫|商务'),
  (3, 0.7, '浪漫|商务|家庭'),
  (4, 0.6, '商务'),
  (5, 0.5, '家庭'),
  (6, 0.4, '浪漫|家庭'),
  (7, 0.3, '商务|家庭');

INSERT INTO GuideReservations (guide_id, reserved_date, is_permanent) VALUES
  (1, 20151225, true),
  (3, 20151226, true), (3, 20151227, true),
  (4, 20151226, true),
  (7, 20151225, true);

COMMIT;

/*
 * 以下为production真实数据。
 */
START TRANSACTION;
# Real data below:
INSERT INTO Cities (city_id, city_name, cn_name, suggest, min) VALUES
  (1, 'San Diego', '圣地亚哥', 2, 1),
  (2, 'Los Angels', '洛杉矶', 3, 2),
  (3, 'Las Vegas', '拉斯维加斯', 3, 2),
  (4, 'Phoenix', '菲尼克斯', 1, 1),
  (5, 'Salt Lake City', '盐湖城', 2, 1),
  (6, 'Reno', '雷诺', 2, 1),
  (7, 'Sacramento', '萨克拉门托', 1, 1),
  (8, 'San Francisco', '旧金山', 3, 1),
  (9, 'Portland', '波特兰', 1, 1),
  (10, 'Seattle', '西雅图', 2, 1),
  (11, 'Vancouvor', '温哥华', 2, 1),
  (12, 'Chicago', '芝加哥', 3, 2),
  (13, 'New York', '纽约', 4, 2),
  (14, 'Washington', '华盛顿', 2, 1),
  (15, 'Philadelphia', '费城', 2, 1),
  (16, 'Boston', '波士顿', 2, 1),
  (17, 'Miami', '迈阿密', 4, 2),
  (18, 'Atlanta', '亚特兰大', 2, 1),
  (19, 'Orlando', '奥兰多', 4, 2),
  (20, 'Houston', '休斯顿', 3, 2),
  (21, 'Denver', '丹佛', 2, 1),
  (22, 'New Orleans', '新奥尔良', 2, 1);

INSERT INTO CityConnections (from_city_id, to_city_id, distance, hours) VALUES
  (1, 2, 120, 2),
  (2, 3, 269, 5),
  (3, 4, 297, 5),
  (3, 5, 421, 7),
  (3, 6, 448, 8),
  (5, 6, 518, 9),
  (6, 7, 135, 3),
  (7, 8, 90, 2),
  (2, 8, 383, 7),
  (8, 9, 636, 11),
  (9, 10, 173, 3),
  (7, 2, 386, 7),
  (10, 11, 143, 3);

INSERT INTO CityAliases (city_id, alias) VALUES
  (1, 'San Diego'),
  (1, 'SD'),
  (1, 'SDYG'),
  (1, 'SHIDIYAGE'),
  (1, 'SAN'),
  (2, 'Los Angels'),
  (2, 'LA'),
  (2, 'LUOSANJI'),
  (2, 'LDJ'),
  (2, 'LAX'),
  (3, 'Las Vegas'),
  (3, 'LV'),
  (3, 'LSWJS'),
  (3, 'LaSiWeiJiaSi'),
  (4, 'Phoenix'),
  (4, 'FNKS'),
  (4, 'FeiNiKeSi'),
  (5, 'Salt Lake City'),
  (5, 'SLC'),
  (5, 'YHC'),
  (5, 'YanHuCheng'),
  (6, 'Reno'),
  (6, 'LN'),
  (6, 'LeiNuo'),
  (7, 'Sacramento'),
  (7, 'SKLMT'),
  (7, 'SaKeLaMenTuo'),
  (8, 'San Francisco'),
  (8, 'SF'),
  (8, 'JJS'),
  (8, 'JIUJINGSHAN'),
  (8, 'SFO'),
  (9, 'Portland'),
  (9, 'BTL'),
  (9, 'BoTeLan'),
  (10, 'Seattle'),
  (10, 'XYT'),
  (10, 'XIYATU'),
  (10, 'SEA'),
  (11, 'Vancouvor'),
  (11, 'WGH'),
  (11, 'WenGeHua'),
  (12, 'Chicago'),
  (12, 'ZHIJIAGE'),
  (12, 'ZHJG'),
  (12, 'ORD'),
  (13, 'New York'),
  (13, 'NY'),
  (13, 'NiuYue'),
  (14, 'Washington'),
  (14, 'HSD'),
  (14, 'HuaShengDun'),
  (14, 'DC'),
  (15, 'Philadelphia'),
  (15, 'FC'),
  (15, 'FeiCheng'),
  (16, 'Boston'),
  (16, 'BSD'),
  (16, 'BoShiDun'),
  (17, 'Miami'),
  (17, 'MAM'),
  (17, 'MaiAMi'),
  (18, 'Atlanta'),
  (18, 'YTLD'),
  (18, 'YaTeLanDa'),
  (19, 'Orlando'),
  (19, 'ALD'),
  (19, 'AoLanDuo'),
  (20, 'Houston'),
  (20, 'XSD'),
  (20, 'XiuSiDun'),
  (21, 'Denver'),
  (21, 'DF'),
  (21, 'DanFo'),
  (22, 'New Orleans'),
  (22, 'NO'),
  (22, 'XAEL'),
  (22, 'XinAoErLiang');

INSERT INTO Users (user_id, user_name) VALUES
  (200, '董琪'),
  (201, '郝峰'),
  (202, 'tony'),
  (203, 'david lu'),
  (204, 'Eric'),
  (205, '沈哲浩'),
  (206, '杨光'),
  (207, 'David pan'),
  (208, 'danver 赵'),
  (209, 'Danny '),
  (210, '杜新'),
  (211, '杨飞'),
  (212, '小吴 '),
  (213, 'mark'),
  (214, '李龙祥Max'),
  (215, '小萱'),
  (216, '贺雷'),
  (217, 'Helen'),
  (218, '董毛磊'),
  (219, '范伟'),
  (220, '牛健'),
  (221, 'Jason南'),
  (222, '钟毅'),
  (223, 'tony张'),
  (224, '四海接送'),
  (225, 'tony'),
  (226, '陈导'),
  (227, '刘小姐'),
  (228, 'frank'),
  (229, '杨导'),
  (230, '张导'),
  (231, '侯导'),
  (232, '林导'),
  (233, '文森'),
  (234, '陈斌'),
  (235, '王导'),
  (236, '王悦'),
  (237, '鲁杰'),
  (238, '凯文'),
  (239, 'zoey'),
  (240, '柏导'),
  (241, '许导'),
  (242, '宋导'),
  (243, '吴师傅'),
  (244, 'Jason张'),
  (245, '陈导'),
  (246, 'frank 宋'),
  (247, '王导'),
  (248, '李毅'),
  (249, '金导');

INSERT INTO Guides (guide_id, user_id, description, max_persons, has_car, score, interests, phone) VALUES
  (200, 200, '旧金山导游', 7, False, 9.00, '浪漫|商务|家庭', 4155280389),
  (201, 201, '旧金山导游', 15, False, 9.00, '家庭|商务', 5108574829),
  (202, 202, '旧金山导游', 7, False, 7.00, '浪漫|家庭', 8163388888),
  (203, 203, '旧金山导游', 5, False, 7.00, '高尔夫|家庭|浪漫', 6264562143),
  (204, 204, '旧金山导游', 7, False, 8.00, '高端礼宾车', 5105857097),
  (205, 205, '旧金山导游', 7, False, 6.00, '摄影|拍照', 5105857097),
  (206, 206, '旧金山导游', 7, False, 6.00, '浪漫|家庭', 4086561788),
  (207, 207, '旧金山导游', 60, False, 8.00, '历史|商务|家庭', 4154898122),
  (208, 208, '旧金山导游', 60, False, 8.00, '商务|家庭', 4155833252),
  (209, 209, '旧金山导游', 7, False, 7.00, '家庭|浪漫', 4156303880),
  (210, 210, '洛杉矶导游', 15, False, 9.00, '浪漫|家庭|商务', 6269057979),
  (211, 211, '洛杉矶导游', 7, False, 8.00, '浪漫|家庭|商务', 6264174274),
  (212, 212, '洛杉矶导游', 7, False, 8.00, '浪漫|家庭', 6267828475),
  (213, 213, '洛杉矶导游', 15, False, 8.00, '商务|家庭|浪漫', 6262025526),
  (214, 214, '洛杉矶导游', 7, False, 9.00, '商务|家庭|浪漫', 6268185575),
  (215, 215, '洛杉矶导游', 7, False, 8.00, '商务|家庭', 6265596581),
  (216, 216, '洛杉矶导游', 7, False, 8.00, '商务|家庭', 6263805826),
  (217, 217, '洛杉矶导游', 7, False, 8.00, '浪漫|家庭', 6262838629),
  (218, 218, '拉斯维加斯导游', 7, False, 8.00, '浪漫|家庭|商务', 6264105769),
  (219, 219, '拉斯维加斯导游', 15, False, 9.00, '浪漫|家庭|商务', 7025757377),
  (220, 220, '休斯顿导游', 15, False, 9.00, '浪漫|家庭|商务', 7133020568),
  (221, 221, '盐湖城黄石导游', 15, False, 9.00, '浪漫|家庭|商务', 9173618884),
  (222, 222, '盐湖城黄石导游', 60, False, 9.00, '浪漫|家庭|商务', 5105419756),
  (223, 223, '盐湖城黄石导游', 60, False, 9.00, '浪漫|家庭|商务', 8018194885),
  (224, 224, '芝加哥导游', 7, False, 8.00, '浪漫|家庭|商务', 3128238688),
  (225, 225, '芝加哥导游', 7, False, 8.00, '浪漫|家庭|商务', 3124830858),
  (226, 226, '芝加哥导游', 7, False, 8.00, '浪漫|家庭|商务', 3125327923),
  (227, 227, '芝加哥导游', 7, False, 8.00, '浪漫|家庭|商务', 3128434257),
  (228, 228, '芝加哥导游', 7, False, 8.00, '浪漫|家庭|商务', 6265921201),
  (229, 229, '纽约导游', 7, False, 8.00, '浪漫|家庭|商务', 7182197299),
  (230, 230, '纽约导游', 7, False, 8.00, '浪漫|家庭|商务', 7186691534),
  (231, 231, '纽约导游', 7, False, 8.00, '浪漫|家庭|商务', 3478595988),
  (232, 232, '纽约导游', 7, False, 8.00, '浪漫|家庭|商务', 3476662445),
  (233, 233, '纽约导游', 7, False, 8.00, '浪漫|家庭|商务', 9176670098),
  (234, 234, '纽约导游', 7, False, 8.00, '浪漫|家庭|商务', 2067345096),
  (235, 235, '华盛顿导游', 7, False, 8.00, '浪漫|家庭|商务', 7038906113),
  (236, 236, '华盛顿导游', 7, False, 8.00, '浪漫|家庭|商务', 6145961652),
  (237, 237, '华盛顿导游', 7, False, 8.00, '浪漫|家庭|商务', 3017301777),
  (238, 238, '华盛顿导游', 7, False, 8.00, '浪漫|家庭|商务', 2129616201),
  (239, 239, '休斯顿导游', 7, False, 8.00, '浪漫|家庭|商务', 4694261315),
  (240, 240, '休斯顿导游', 7, False, 8.00, '浪漫|家庭|商务', 2819196750),
  (241, 241, '休斯顿导游', 7, False, 8.00, '浪漫|家庭|商务', 8324758284),
  (242, 242, '休斯顿导游', 7, False, 8.00, '浪漫|家庭|商务', 8326134629),
  (243, 243, '休斯顿导游', 7, False, 8.00, '浪漫|家庭|商务', 3467048735),
  (244, 244, '西雅图导游', 7, False, 8.00, '浪漫|家庭|商务', 2062298077),
  (245, 245, '西雅图导游', 7, False, 8.00, '浪漫|家庭|商务', 2064682169),
  (246, 246, '西雅图导游', 7, False, 8.00, '浪漫|家庭|商务', 4252838189),
  (247, 247, '西雅图导游', 7, False, 8.00, '浪漫|家庭|商务', 2069796993),
  (248, 248, '西雅图导游', 7, False, 8.00, '浪漫|家庭|商务', 2064277218),
  (249, 249, '西雅图导游', 7, False, 8.00, '浪漫|家庭|商务', 2069540949);

INSERT INTO GuideCities (guide_id, city_id) VALUES
 (200, 2),
 (200, 8),
 (200, 1),
 (201, 2),
 (201, 8),
 (201, 1),
 (202, 8),
 (202, 1),
 (203, 8),
 (204, 8),
 (205, 8),
 (206, 8),
 (207, 8),
 (208, 8),
 (209, 8),
 (210, 2),
 (211, 2),
 (212, 2),
 (213, 2),
 (214, 2),
 (215, 2),
 (216, 2),
 (217, 2),
 (218, 3),
 (219, 3),
 (220, 20),
 (221, 5),
 (222, 5),
 (223, 5),
 (224, 12),
 (225, 12),
 (226, 12),
 (227, 12),
 (228, 12),
 (229, 13),
 (230, 13),
 (231, 13),
 (232, 13),
 (233, 13),
 (234, 13),
 (235, 14),
 (236, 14),
 (237, 14),
 (238, 14),
 (239, 20),
 (240, 20),
 (241, 20),
 (242, 20),
 (243, 20),
 (244, 10),
 (245, 10),
 (246, 10),
 (247, 10),
 (248, 10),
 (249, 10);

COMMIT;

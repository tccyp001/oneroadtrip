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

INSERT INTO Cities (city_id, city_name, suggest, min, max) VALUES
  (1, 'San Diego', 2, 1, 3),
  (2, 'Los Angels', 3, 2, 5),
  (3, 'Las Vegas', 2, 1, 3),
  (4, 'Phoenix', 1, 1, 2),
  (5, 'Salt Lake City', 4, 2, 6),
  (6, 'Reno', 2, 1, 5),
  (7, 'Sacramento', 1, 1, 2),
  (8, 'San Francisco', 2, 1, 4),
  (9, 'Portland', 1, 1, 2),
  (10, 'Seattle', 2, 1, 4),
  (11, 'Vancouvor', 2, 1, 4);

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

INSERT INTO CityConnections
(from_city_id, to_city_id, distance, hours) VALUES
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

# 还没用上
INSERT INTO CityAliases (city_id, alias) VALUES
  (2, 'LA'),
  (2, 'LSJ'),
  (7, 'SKLMT'),
  (1, 'SD'),
  (1, 'SDYG'),
  (8, 'SF'),
  (8, 'JJS');


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

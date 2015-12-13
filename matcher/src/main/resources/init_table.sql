START TRANSACTION;

# Const tables
INSERT INTO Levels (level, description) VALUES
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

# Create a guide 1
INSERT INTO Users (user_name, email, password) VALUES
  ('guide1', 'guide1@a.com', SHA2('guide1pass', 256));
INSERT INTO Guides (user_id, description, level, max_persons, citizenship, has_car, score) VALUES
  (LAST_INSERT_ID(), '导游一', 2, 4, 'US', true, 3.7);
SET @new_guide_id = LAST_INSERT_ID();
INSERT INTO GuideReservations (guide_id, reserved_date, location_id) VALUES
  (@new_guide_id, 20151215, 7),
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
INSERT INTO Users (user_name, email, password) VALUES
  ('guide2', 'guide2@a.com', SHA2('guide2pass', 256));
INSERT INTO Guides (user_id, description, level, max_persons, citizenship, has_car, score) VALUES
  (LAST_INSERT_ID(), '导游2', 3, 10, 'US', true, 4.2);
SET @new_guide_id = LAST_INSERT_ID();
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
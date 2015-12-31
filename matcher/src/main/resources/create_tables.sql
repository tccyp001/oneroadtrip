/*
 * password: one-way-hash
 */
CREATE TABLE Users(
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_name VARCHAR(100),
  email VARCHAR(100),
  password CHAR(64),
  photo_id BIGINT
) DEFAULT CHARSET=utf8;
CREATE INDEX UsersUserName ON Users(user_name);

/*
 * About user expiration: 对于一个用户而言，有可能对应有多个tokens，有些过期了，有些没有。
 * 但是有且只有一个is_expired=false
 *
 * is_expired: client端可以显式的设置这个域来废弃这个token，这有可能发生在更新token的情况下。
 */
CREATE TABLE Tokens (
  token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(40),
  user_id BIGINT,
  expired_ts TIMESTAMP,
  is_expired BOOLEAN
) DEFAULT CHARSET=utf8;
CREATE INDEX TokensToken ON Tokens(token);

CREATE TABLE Cities(
  city_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  city_name VARCHAR(100),
  cn_name VARCHAR(100),
  suggest INT,
  min INT
) DEFAULT CHARSET=utf8;
CREATE INDEX CitiesCityName On Cities(city_name);

CREATE TABLE CityConnections(
  city_connection_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  from_city_id BIGINT,
  to_city_id BIGINT,
  distance INT,
  hours INT
) DEFAULT CHARSET=utf8;
CREATE INDEX CityConnectionsFromCityId ON CityConnections(from_city_id);
CREATE INDEX CityConnectionsToCityId ON CityConnections(to_city_id);

CREATE TABLE CityAliases(
  city_alias_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  city_id BIGINT,
  alias VARCHAR(20)
) DEFAULT CHARSET=utf8;
CREATE INDEX CityAliasesCityId ON CityAliases(city_id);

CREATE TABLE Spots (
  spot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  city_id BIGINT,
  name VARCHAR(20),
  description VARCHAR(200),
  hours INT,
  score FLOAT,
  interests VARCHAR(100)
) DEFAULT CHARSET=utf8;
CREATE INDEX SpotsCityId ON Spots(city_id);

/*
 * 每个用户都有可能同时是一个导游。
 *
 * citizenship: US or CN.
 */
CREATE TABLE Guides (
	guide_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	user_id BIGINT,
	description VARCHAR(1000),
	level_id BIGINT,
	max_persons INT,
	citizenship VARCHAR(2),
	has_car BOOLEAN,
	score FLOAT,
  location_id BIGINT,  -- DEPRECATING...
  interests VARCHAR(100)
) DEFAULT CHARSET=utf8;
CREATE INDEX GuidesUserId ON Guides(user_id);

/*
 * reserved_date: 因为导游空闲时间是以天为单位的，所以INT比较简洁（20151210 -- Dec. 10, 2015)
 */
CREATE TABLE GuideReservations (
  reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	guide_id BIGINT,
	reserved_date INT,
  is_permanent BOOLEAN,
  insert_time TIMESTAMP,
  location_id BIGINT  -- DEPRECATING...
) DEFAULT CHARSET=utf8;
CREATE INDEX GuideReservationsGuideId ON GuideReservations(guide_id);

CREATE TABLE GuideCities (
  guide_city_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guide_id BIGINT,
  city_id BIGINT
) DEFAULT CHARSET=utf8;
CREATE INDEX GuideCitiesGuideId ON GuideCities(guide_id);
CREATE INDEX GuideCitiesCityIt ON GuideCities(city_id);

CREATE TABLE GuideBillingMethods (
  guide_billing_method_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guide_id BIGINT,
  billing_method_id BIGINT
) DEFAULT CHARSET=utf8;

/*
 * guide_location_id: 里面含价格和旅游点
 * reservation_id: 里面含导游被预定的日期
 */
CREATE TABLE Orders (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  guide_id BIGINT,
  guide_location_id BIGINT,
  reservation_id BIGINT
) DEFAULT CHARSET=utf8;
CREATE INDEX OrdersUserId ON Orders(user_id);
CREATE INDEX OrdersGuideId ON Orders(guide_id);

CREATE TABLE Photos(
  photo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content BLOB
) DEFAULT CHARSET=utf8;

-- Const tables --

/*
 * 目前Locations我们比较粗的用城市表示
 *
 * 以后再加更多的，譬如用多边形画个区域之类。
 */
CREATE TABLE Locations (
  location_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  city VARCHAR(20),
  region VARCHAR(20),
  zipcode INT
) DEFAULT CHARSET=utf8;

CREATE TABLE Levels (
  level_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  description VARCHAR(100)
) DEFAULT CHARSET=utf8;

CREATE TABLE Languages (
  language_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code CHAR(2),
  description VARCHAR(100)
) DEFAULT CHARSET=utf8;

CREATE TABLE BillingMethods (
  billing_method_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  description VARCHAR(100)  -- VISA / Master / AE / 银联 / Paypal / Alipay
) DEFAULT CHARSET=utf8;

CREATE TABLE Interests (
  interest_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  interest_name VARCHAR(20)
) DEFAULT CHARSET=utf8;

-- DEPRECATING...
CREATE TABLE GuideLanguages (
  guide_language_id INT PRIMARY KEY AUTO_INCREMENT,
  guide_id BIGINT,
  language_id BIGINT
) DEFAULT CHARSET=utf8;
CREATE INDEX GuideLanguagesGuideId ON GuideLanguages(guide_id);
CREATE INDEX GuideLanguagesLanguageId ON GuideLanguages(language_id);

/*
 * 导游可以提供的旅游地点
 *
 * is_effective: 导游应该可以更改他们某个地点的旅游价格，但是同一个导游同一地点，
 *               任何时候都只能有一个有效.
 */
CREATE TABLE GuideLocations (
  guide_location_id BIGINT PRIMARY KEY AUTO_INCREMENT,
	guide_id BIGINT,
	location_id BIGINT,
	price_usd FLOAT,
	price_cny FLOAT,
	is_effective BOOLEAN
) DEFAULT CHARSET=utf8;
CREATE INDEX GuideLocationsGuideId ON GuideLocations(guide_id);
CREATE INDEX GuideLocationsLocationId ON GuideLocations(location_id);

CREATE TABLE Users(
  user_id INT PRIMARY KEY,
  user_name VARCHAR(100),
  email VARCHAR(100),
  password VARCHAR(100), --Possible to be empty
  photo_id VARCHAR(100),  -- index of the photo
) DEFAULT CHARSET=utf8;

CREATE TABLE Tokens (
  token VARCHAR(100) PRIMARY KEY,
  user_id INT,  -- 对于一个用户而言，有可能对应有多个tokens，有些过期了，有些没有。但是有且只有一个is_expired=false
  expired_ts TIMESTAMP,
  is_expired BOOLEAN,  -- client端可以显式的设置这个域来废弃这个token，这有可能发生在更新token的情况下。
) DEFAULT CHARSET=utf8;

-- 每个用户都有可能同时是一个导游。
CREATE TABLE Guides (
	guide_id INT PRIMARY KEY,  -- Equals to user_id
	description VARCHAR(1000),
	level INT,
	max_persons INT,
	citizenship VARCHAR(2),  -- US or CN
	has_car BOOLEAN,
	score FLOAT
);

CREATE TABLE GuideReservations (
  reservation_id INT PRIMARY KEY,
	guide_id INT,
	reserved_date INT,  -- 因为导游空闲时间是以天为单位的，所以INT比较简洁（20151210 -- Dec. 10, 2015)
	location_id INT,  -- 当天所在位置
);
CREATE INDEX ON GuideReservations(guide_id);

CREATE TABLE GuideLanguages (
  guide_id INT PRIMARY KEY,
  language_id INT
);

-- 导游可以提供的旅游地点
CREATE TABLE GuideLocations (
  guide_location_id INT PRIMARY KEY,
	guide_id INT,
	location_id INT，
	price_usd FLOAT,
	price_cny FLOAT,
	is_effective BOOLEAN,  -- 导游应该可以更改他们某个地点的旅游价格，但是同一个导游同一地点，任何时候都只能有一个有效
);
CREATE INDEX ON GuideLocations(guide_id);
CREATE INDEX ON GuideLocations(location_id);

CREATE TABLE GuideBillingMethods (
  guide_id INT PRIMARY KEY,
  billing_method_id INT
);

CREATE TABLE Orders (
  order_id INT PRIMARY KEY,
  user_id INT,
  guide_id INT,
  guide_location_id INT,  -- 里面含价格和旅游点
  reservation_id INT,  -- 里面含导游被预定的日期
);
CREATE INDEX ON Orders(user_id);
CREATE INDEX ON Orders(guide_id);

CREATE TABLE Photos(
  photo_id VARCHAR(100) PRIMARY KEY,
  content BLOB
);

-- Const tables --
-- 目前Locations我们比较粗的用城市表示
CREATE TABLE Locations (
  location_id INT PRIMARY KEY,
  city VARCHAR(20),
  region VARCHAR(20),
  zipcode INT
  -- 以后再加更多的，譬如用多边形画个区域之类。
);

CREATE TABLE Levels (
  level INT PRIMARY KEY,
  description VARCHAR(100)
);

CREATE TABLE Languages (
  language_id INT PRIMARY KEY,
  code CHAR(2),
  description VARCHAR(100)
);

CREATE TABLE BillingMethods (
  billing_method_id INT PRIMARY KEY,
  description VARCHAR(100)  -- VISA / Master / AE / 银联 / Paypal / Alipay
);

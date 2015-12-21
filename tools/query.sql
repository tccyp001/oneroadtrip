SELECT
  Guides.guide_id,
  GuideReservations.reserved_date
FROM
  Guides
  INNER JOIN GuideLocations USING(guide_id)
  INNER JOIN Locations USING(location_id)
  INNER JOIN GuideReservations USING(guide_id)
WHERE
  Guides.max_persons > 3
  AND Locations.city = '纽约'
  AND GuideLocations.is_effective = true
  AND GuideReservations.reserved_date >= 20151210 AND GuideReservations.reserved_date < 20151217;


SELECT
  Guides.guide_id,
  Users.user_name,
  Guides.description,
  Guides.level,
  Guides.max_persons,
  Guides.citizenship,
  Guides.has_car,
  Guides.score,
  GuideLocations.price_usd,
  GuideLocations.price_cny
FROM
  Guides
  INNER JOIN Users USING(user_id)
  INNER JOIN GuideLocations USING(guide_id)
  INNER JOIN Locations USING(location_id)
WHERE
  Guides.max_persons > 3
  AND Locations.city = '纽约'
  AND GuideLocations.is_effective = true;

SELECT
  id,
  col1,
  4 + 4,
  SUBSTR(col1, 3, 4),
  'alma' AS vvv,
  XXX(4, 0.3),
  CONCAT((col2 + 4) * 5, 'alma') AS xpr
FROM table1
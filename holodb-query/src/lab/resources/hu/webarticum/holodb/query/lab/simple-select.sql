SELECT id, col1, SUBSTR(col1, 3, 4), CONCAT((col2 + 4) * 5, 'alma') AS xpr
FROM table1
WHERE id=2
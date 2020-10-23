SELECT col1, CONCAT((col2 + 4) * 5, 'alma')
FROM table1
LEFT JOIN table2 t2 ON t1.t2_id = t2.id
WHERE t1.col1 IS NOT NULL
ORDER BY col2 ASC, col3, col4 DESC
LIMIT 10
OFFSET 50
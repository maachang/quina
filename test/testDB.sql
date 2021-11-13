
DROP TABLE IF EXISTS TestTable;
CREATE TABLE TestTable(id INT not null PRIMARY KEY, NAME VARCHAR(32), grade INT);

INSERT INTO TestTable(id, name, grade) VALUES(1001, '山田太郎', 1);
INSERT INTO TestTable(id, name, grade) VALUES(2001, '太田隆', 2);
INSERT INTO TestTable(id, name, grade) VALUES(3001, '林敦子', 3);
INSERT INTO TestTable(id, name, grade) VALUES(3003, '市川次郎', 3);


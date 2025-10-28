DROP TABLE IF EXISTS donations;

CREATE TABLE donations (
  id INT NOT NULL AUTO_INCREMENT,
  description VARCHAR(255),
  quantity INT,
  is_deleted TINYINT(1),
  created_at DATETIME,
  created_by INT,
  updated_at DATETIME,
  updated_by INT,
  category_id INT,
  user_id INT,
  PRIMARY KEY (id),
  KEY created_by (created_by),
  KEY updated_by (updated_by),
  KEY category_id (category_id),
  KEY user_id (user_id),
  CONSTRAINT donations_ibfk_1 FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT donations_ibfk_2 FOREIGN KEY (updated_by) REFERENCES users(id),
  CONSTRAINT donations_ibfk_3 FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT donations_ibfk_4 FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO donations (id, description, quantity, is_deleted, created_at, created_by, updated_at, updated_by, category_id, user_id) VALUES
(4, 'arroz gallo', 111, 0, '2025-10-03 22:05:10', 2, NULL, NULL, 1, NULL),
(5, 'rompecabezas', 155, 0, '2025-10-15 21:50:43', 2, NULL, NULL, 3, NULL),
(6, 'bananas', 100, 0, '2025-10-15 21:50:54', 2, NULL, NULL, 1, NULL),
(7, 'remeras', 111, 0, '2025-10-15 21:51:05', 2, NULL, NULL, 2, NULL),
(8, 'pantalon', 111, 0, '2025-10-15 21:51:20', 2, NULL, NULL, 2, NULL),
(9, 'lapices', 111, 0, '2025-10-15 21:51:30', 2, NULL, NULL, 4, NULL);

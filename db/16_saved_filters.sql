DROP TABLE IF EXISTS saved_filters;

CREATE TABLE saved_filters (
  id INT NOT NULL AUTO_INCREMENT,
  end_date DATETIME(6),
  filter_deleted TINYINT(1) NOT NULL DEFAULT 0,
  is_deleted TINYINT(1) NOT NULL DEFAULT 0,
  name VARCHAR(255),
  start_date DATETIME(6),
  category_id INT,
  user_id INT,
  PRIMARY KEY (id),
  KEY category_id (category_id),
  KEY user_id (user_id),
  CONSTRAINT FK_saved_filters_category FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT FK_saved_filters_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS voluntaries;

CREATE TABLE voluntaries (
  id INT NOT NULL AUTO_INCREMENT,
  organization_id INT,
  voluntary_id INT,
  name VARCHAR(200) NOT NULL,
  last_name VARCHAR(200) NOT NULL,
  phone VARCHAR(200) NOT NULL,
  email VARCHAR(200) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO voluntaries (id, organization_id, voluntary_id, name, last_name, phone, email) VALUES
(1, 2, 1, 'Juan', 'Lopez', '112321453', 'juanlopez45@gmail.com'),
(2, 3, 2, 'Juan', 'Lopez', '1162519312', 'juan_lopez@hotmail.com');

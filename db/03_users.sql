DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255),
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  phone VARCHAR(255),
  password_hash VARCHAR(255),
  email VARCHAR(255),
  is_active TINYINT(1),
  created_at DATETIME,
  role_id INT,
  PRIMARY KEY (id),
  KEY role_id (role_id),
  CONSTRAINT users_ibfk_1 FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (id, username, first_name, last_name, phone, password_hash, email, is_active, created_at, role_id) VALUES
(2,'FedeOA','Federico','Acosta','1162519312','$2b$12$xGRyt27LTCce0JzuNEoMoud8Haaw0dOJn9mjjH6mQAWAXp.692fay','fedeacosta07@hotmail.com',1,'2025-09-29 22:28:25',1),
(3,'usuario2','Usuario','Dos','1192223311','$2b$12$FkkGN71s1mLjsMfSSYifDuF6RCE7hIyUWErI1Oz.P3S0glGaNGUYq','usuario2@hotmail.com',1,'2025-10-04 00:44:26',2),
(4,'usuario3','usuario3','usuario3','1189222331','$2b$12$4f5BD5xpCP1OFMtpz2ybR.dDMMdO9jF4WBaFgKKQHYf1zZqB68cn2','usuario3@hotmail.com',1,'2025-10-05 18:43:36',3),
(5,'usuario4','usuario4','usuario4','1182839212','$2b$12$KJ6EEialCg.okjDhp8cFbeQrThC.Jdv4a6DOTizqIZY1pKLCEwnVK','usuario4@hotmail.com',1,'2025-10-05 18:46:14',4);

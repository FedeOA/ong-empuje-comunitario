DROP TABLE IF EXISTS event_filter;

CREATE TABLE event_filter (
  id BIGINT NOT NULL AUTO_INCREMENT,
  distribution VARCHAR(255),
  end_date DATETIME(6),
  name VARCHAR(255),
  start_date DATETIME(6),
  username VARCHAR(255),
  user_id INT NOT NULL,
  PRIMARY KEY (id),
  KEY user_id (user_id),
  CONSTRAINT FKt82e2ksg8xj1q6688cr2805fl FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO event_filter (id, distribution, end_date, name, start_date, username, user_id) VALUES
(6,'YES','2025-11-18 00:00:00.000000','filtro3','2025-10-18 00:00:00.000000','FedeOA',2),
(9,'YES','0000-00-00 00:00:00.000000','filtro12','0000-00-00 00:00:00.000000','FedeOA',2),
(10,'YES','2025-11-18 00:00:00.000000','filtro1','2025-10-18 00:00:00.000000','FedeOA',2),
(11,'YES','0000-00-00 00:00:00.000000','filtro17','0000-00-00 00:00:00.000000','FedeOA',2),
(14,'BOTH','0000-00-00 00:00:00.000000','Usuario3AmbosSinFecha','0000-00-00 00:00:00.000000','usuario3',2),
(15,'BOTH','0000-00-00 00:00:00.000000','FedeOAAmbosSinFecha','0000-00-00 00:00:00.000000','FedeOA',2),
(20,'YES','0000-00-00 00:00:00.000000','Usuario3SISinFecha','0000-00-00 00:00:00.000000','usuario3',2),
(22,'YES','0000-00-00 00:00:00.000000','usuario2SinFechaConEventos','0000-00-00 00:00:00.000000','usuario2',2),
(23,'BOTH','0000-00-00 00:00:00.000000','usuario4SinFechaAmbos','0000-00-00 00:00:00.000000','usuario4',2),
(24,'YES','2025-10-21 00:00:00.000000','usuario3Si20Del10','2025-10-20 00:00:00.000000','usuario3',2),
(25,'YES','0000-00-00 00:00:00.000000','Usuario4SinFechaConDon','0000-00-00 00:00:00.000000','usuario4',2),
(26,'BOTH','0000-00-00 00:00:00.000000','usuario3','0000-00-00 00:00:00.000000','usuario3',4);

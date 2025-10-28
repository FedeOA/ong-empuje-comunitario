DROP TABLE IF EXISTS user_events;

CREATE TABLE user_events (
  id INT NOT NULL AUTO_INCREMENT,
  registration_date DATETIME,
  user_id INT,
  event_id INT,
  PRIMARY KEY (id),
  KEY user_id (user_id),
  KEY event_id (event_id),
  CONSTRAINT user_events_ibfk_1 FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT user_events_ibfk_2 FOREIGN KEY (event_id) REFERENCES events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user_events (id, registration_date, user_id, event_id) VALUES
(54, '2025-10-20 02:22:28', 4, 65),
(55, '2025-10-20 02:22:32', 5, 65),
(56, '2025-10-20 02:22:36', 3, 65),
(70, '2025-10-22 00:09:40', 2, 72),
(71, '2025-10-22 00:37:37', 5, 73);

DROP TABLE IF EXISTS voluntary_events;

CREATE TABLE voluntary_events (
  id INT NOT NULL AUTO_INCREMENT,
  registration_date DATETIME,
  voluntary_id INT,
  event_id INT,
  PRIMARY KEY (id),
  KEY voluntary_id (voluntary_id),
  KEY event_id (event_id),
  CONSTRAINT voluntary_events_ibfk_1 FOREIGN KEY (voluntary_id) REFERENCES voluntaries(id),
  CONSTRAINT voluntary_events_ibfk_2 FOREIGN KEY (event_id) REFERENCES events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

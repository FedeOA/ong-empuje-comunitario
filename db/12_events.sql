DROP TABLE IF EXISTS events;

CREATE TABLE events (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  description VARCHAR(200) NOT NULL,
  event_datetime DATETIME,
  remote_id INT,
  organization_id INT DEFAULT 1,
  is_published TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY organization_id (organization_id),
  CONSTRAINT events_ibfk_1 FOREIGN KEY (organization_id) REFERENCES organizations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO events (id, name, description, event_datetime, remote_id, organization_id, is_published) VALUES
(65, 'evento 3', 'evento 3', '2025-10-20 19:12:00', NULL, 1, 0),
(70, 'sss', 'sssss', '2025-10-20 19:11:00', NULL, 1, 0),
(71, 'Evento 2', 'evento 3', '2025-11-20 19:14:00', NULL, 1, 1),
(72, 'asd', 'asd', '2025-11-20 19:34:00', NULL, 1, 1),
(73, 'fede', 'dadasda', '2025-11-20 19:57:00', NULL, 1, 1);

DROP TABLE IF EXISTS event_donations;

CREATE TABLE event_donations (
  id INT NOT NULL AUTO_INCREMENT,
  quantity_used INT,
  event_id INT,
  donation_id INT,
  PRIMARY KEY (id),
  KEY event_id (event_id),
  KEY donation_id (donation_id),
  CONSTRAINT event_donations_ibfk_1 FOREIGN KEY (event_id) REFERENCES events(id),
  CONSTRAINT event_donations_ibfk_2 FOREIGN KEY (donation_id) REFERENCES donations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO event_donations (id, quantity_used, event_id, donation_id) VALUES
(1, 2, 70, 4),
(2, 2, 70, 5),
(3, 2, 71, 5),
(4, 2, 71, 6),
(5, 2, 72, 4);

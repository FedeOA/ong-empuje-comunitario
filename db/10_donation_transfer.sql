DROP TABLE IF EXISTS donation_transfer;

CREATE TABLE donation_transfer (
  id INT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6) NOT NULL,
  organization_id INT NOT NULL,
  processed BIT(1) NOT NULL,
  received BIT(1) NOT NULL,
  request_id INT DEFAULT NULL,
  transfer_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY transfer_id_unique (transfer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

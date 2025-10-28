DROP TABLE IF EXISTS donation_offers;

CREATE TABLE donation_offers (
  id INT NOT NULL AUTO_INCREMENT,
  available BIT(1) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6),
  offer_id INT NOT NULL,
  organization_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY offer_id_unique (offer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

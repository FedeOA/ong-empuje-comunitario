DROP TABLE IF EXISTS donation_offer_item;

CREATE TABLE donation_offer_item (
  id INT NOT NULL AUTO_INCREMENT,
  category_id INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  description VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  offer_id INT NOT NULL,
  PRIMARY KEY (id),
  KEY offer_id (offer_id),
  CONSTRAINT FKlxpbk09a6093sqb1pwe853e5k FOREIGN KEY (offer_id) REFERENCES donation_offers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

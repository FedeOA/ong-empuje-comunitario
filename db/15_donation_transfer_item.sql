DROP TABLE IF EXISTS donation_transfer_item;

CREATE TABLE donation_transfer_item (
  id INT NOT NULL AUTO_INCREMENT,
  category_id INT NOT NULL,
  create_at DATETIME(6) NOT NULL,
  description VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  transfer_id INT NOT NULL,
  PRIMARY KEY (id),
  KEY transfer_id (transfer_id),
  CONSTRAINT FKohh9nin5cvhvudbuyk8tbd4pr FOREIGN KEY (transfer_id)
    REFERENCES donation_transfer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

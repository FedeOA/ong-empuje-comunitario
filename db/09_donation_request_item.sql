DROP TABLE IF EXISTS donation_request_items;

CREATE TABLE donation_request_items (
  id INT NOT NULL AUTO_INCREMENT,
  category_id INT DEFAULT NULL,
  description VARCHAR(255) DEFAULT NULL,
  organization_id INT DEFAULT NULL,
  request_id INT DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK_donation_request (organization_id, request_id),
  CONSTRAINT FK_donation_request FOREIGN KEY (organization_id, request_id)
    REFERENCES donation_requests (organization_id, request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO donation_request_items (id, category_id, description, organization_id, request_id) VALUES
(1, 1, 'sadasds', 1, 987185);

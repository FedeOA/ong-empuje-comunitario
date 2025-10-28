DROP TABLE IF EXISTS donation_requests;

CREATE TABLE donation_requests (
  organization_id INT NOT NULL,
  request_id INT NOT NULL,
  is_deleted BIT(1) DEFAULT NULL,
  PRIMARY KEY (organization_id, request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO donation_requests (organization_id, request_id, is_deleted) VALUES
(1, 987185, b'1');

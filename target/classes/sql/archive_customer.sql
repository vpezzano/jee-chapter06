DROP PROCEDURE archive_customer;
DELIMITER //
CREATE PROCEDURE archive_customer(p_email VARCHAR(255))
BEGIN
  INSERT INTO customer_archive SELECT * FROM customer WHERE email = p_email;
  DELETE FROM customer WHERE email = p_email;
  COMMIT;
END //
DELIMITER ;
ALTER TABLE CUSTOMER DROP FOREIGN KEY FK_CUSTOMER_address_fk
DROP TABLE ADDRESS
DROP TABLE CUSTOMER
DROP TABLE BOOK
DROP TABLE CD
DELETE FROM SEQUENCE WHERE SEQ_NAME = 'SEQ_GEN'

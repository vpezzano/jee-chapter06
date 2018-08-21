CREATE TABLE `customer_archive` (
 `ID` bigint(20) NOT NULL,
 `AGE` int(11) DEFAULT NULL,
 `EMAIL` varchar(255) DEFAULT NULL,
 `FIRSTNAME` varchar(255) DEFAULT NULL,
 `GENDER` varchar(255) DEFAULT NULL,
 `LASTNAME` varchar(255) DEFAULT NULL,
 `address_fk` bigint(20) NOT NULL,
 PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
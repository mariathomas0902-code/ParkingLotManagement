-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: parking_management
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `customer_id` int NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'Test Customer','9876543210','test@example.com');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_spaces`
--

DROP TABLE IF EXISTS `parking_spaces`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_spaces` (
  `slot_id` int NOT NULL,
  `supported_type` enum('BIKE','CAR','TRUCK') NOT NULL,
  `status` enum('AVAILABLE','OCCUPIED') NOT NULL DEFAULT 'AVAILABLE',
  PRIMARY KEY (`slot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_spaces`
--

LOCK TABLES `parking_spaces` WRITE;
/*!40000 ALTER TABLE `parking_spaces` DISABLE KEYS */;
INSERT INTO `parking_spaces` VALUES (101,'BIKE','AVAILABLE'),(102,'BIKE','AVAILABLE'),(201,'CAR','AVAILABLE'),(202,'CAR','AVAILABLE'),(301,'TRUCK','AVAILABLE');
/*!40000 ALTER TABLE `parking_spaces` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `parking_transactions`
--

DROP TABLE IF EXISTS `parking_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parking_transactions` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `registration_number` varchar(30) NOT NULL,
  `slot_id` int NOT NULL,
  `entry_time` datetime NOT NULL,
  `exit_time` datetime DEFAULT NULL,
  `parking_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `transaction_status` enum('ACTIVE','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`ticket_id`),
  KEY `fk_transaction_vehicle` (`registration_number`),
  KEY `fk_transaction_space` (`slot_id`),
  CONSTRAINT `fk_transaction_space` FOREIGN KEY (`slot_id`) REFERENCES `parking_spaces` (`slot_id`),
  CONSTRAINT `fk_transaction_vehicle` FOREIGN KEY (`registration_number`) REFERENCES `vehicles` (`registration_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parking_transactions`
--

LOCK TABLES `parking_transactions` WRITE;
/*!40000 ALTER TABLE `parking_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `parking_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicles` (
  `registration_number` varchar(30) NOT NULL,
  `vehicle_type` enum('BIKE','CAR','TRUCK') NOT NULL,
  `customer_id` int NOT NULL,
  PRIMARY KEY (`registration_number`),
  KEY `fk_vehicle_customer` (`customer_id`),
  CONSTRAINT `fk_vehicle_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicles`
--

LOCK TABLES `vehicles` WRITE;
/*!40000 ALTER TABLE `vehicles` DISABLE KEYS */;
INSERT INTO `vehicles` VALUES ('KL05AB1234','CAR',1);
/*!40000 ALTER TABLE `vehicles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-29 19:43:22

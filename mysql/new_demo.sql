-- MySQL dump 10.11
--
-- Host: localhost    Database: demo
-- ------------------------------------------------------
-- Server version	5.0.67-community-nt

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `completeexperiences`
--

DROP TABLE IF EXISTS `completeexperiences`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `completeexperiences` (
  `visitorHost` varchar(30) default NULL,
  `instanceName` varchar(60) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `completeexperiences`
--

LOCK TABLES `completeexperiences` WRITE;
/*!40000 ALTER TABLE `completeexperiences` DISABLE KEYS */;
/*!40000 ALTER TABLE `completeexperiences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `environmentmap`
--

DROP TABLE IF EXISTS `environmentmap`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `environmentmap` (
  `pointId` varchar(60) default NULL,
  `x` int(3) default NULL,
  `y` int(3) default NULL,
  `photoURL` varchar(60) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `environmentmap`
--

LOCK TABLES `environmentmap` WRITE;
/*!40000 ALTER TABLE `environmentmap` DISABLE KEYS */;
/*!40000 ALTER TABLE `environmentmap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friends`
--

DROP TABLE IF EXISTS `friends`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `friends` (
  `hostA` varchar(30) default NULL,
  `hostB` varchar(30) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `friends`
--

LOCK TABLES `friends` WRITE;
/*!40000 ALTER TABLE `friends` DISABLE KEYS */;
/*!40000 ALTER TABLE `friends` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `installations`
--

DROP TABLE IF EXISTS `installations`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `installations` (
  `instanceName` varchar(60) default NULL,
  `host` varchar(30) default NULL,
  `port` int(5) default NULL,
  `requiresBT` int(1) default NULL,
  `requiresFaceCamera` int(1) default NULL,
  `requiresMainCamera` int(1) default NULL,
  `capacity` int(3) default NULL,
  `freeCapacity` int(3) default NULL,
  `description` varchar(200) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `installations`
--

LOCK TABLES `installations` WRITE;
/*!40000 ALTER TABLE `installations` DISABLE KEYS */;
/*!40000 ALTER TABLE `installations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `noninstallations`
--

DROP TABLE IF EXISTS `noninstallations`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `noninstallations` (
  `poiId` varchar(60) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `noninstallations`
--

LOCK TABLES `noninstallations` WRITE;
/*!40000 ALTER TABLE `noninstallations` DISABLE KEYS */;
/*!40000 ALTER TABLE `noninstallations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `routes`
--

DROP TABLE IF EXISTS `routes`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `routes` (
  `a` varchar(60) default NULL,
  `b` varchar(60) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `routes`
--

LOCK TABLES `routes` WRITE;
/*!40000 ALTER TABLE `routes` DISABLE KEYS */;
/*!40000 ALTER TABLE `routes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `storylines`
--

DROP TABLE IF EXISTS `storylines`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `storylines` (
  `instanceName` varchar(60) default NULL,
  `prerequisiteInstanceName` varchar(60) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `storylines`
--

LOCK TABLES `storylines` WRITE;
/*!40000 ALTER TABLE `storylines` DISABLE KEYS */;
/*!40000 ALTER TABLE `storylines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visitors`
--

DROP TABLE IF EXISTS `visitors`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `visitors` (
  `host` varchar(30) default NULL,
  `name` varchar(60) default NULL,
  `hasBT` tinyint(1) default NULL,
  `hasFaceCamera` tinyint(1) default NULL,
  `hasMainCamera` tinyint(1) default NULL,
  `age` int(3) default NULL,
  `lastKnownPosition` varchar(60) default NULL,
  `photoData` blob,
  `screenWidth` int(4) default NULL,
  `screenHeight` int(4) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `visitors`
--

LOCK TABLES `visitors` WRITE;
/*!40000 ALTER TABLE `visitors` DISABLE KEYS */;
/*!40000 ALTER TABLE `visitors` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2008-10-31 18:45:37

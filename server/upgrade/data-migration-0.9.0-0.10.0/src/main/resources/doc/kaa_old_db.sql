-- MySQL dump 10.14  Distrib 5.5.48-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: kaa
-- ------------------------------------------------------
-- Server version	5.5.48-MariaDB-1~precise-wsrep

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
-- Table structure for table `abstract_structure`
--

DROP TABLE IF EXISTS `abstract_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `abstract_structure` (
  `id` bigint(20) NOT NULL,
  `activated_time` bigint(20) DEFAULT NULL,
  `activated_username` varchar(255) DEFAULT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `deactivated_time` bigint(20) DEFAULT NULL,
  `deactivated_username` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `endpoint_count` bigint(20) DEFAULT NULL,
  `last_modify_time` bigint(20) DEFAULT NULL,
  `modified_username` varchar(255) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `optimistic_lock` bigint(20) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  `endpoint_group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_t5uv4cntp4vvigky5mtkxovde` (`application_id`),
  KEY `FK_g23m1sg61brt3fovtnkxq2opk` (`endpoint_group_id`),
  CONSTRAINT `FK_g23m1sg61brt3fovtnkxq2opk` FOREIGN KEY (`endpoint_group_id`) REFERENCES `endpoint_group` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_t5uv4cntp4vvigky5mtkxovde` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abstract_structure`
--

LOCK TABLES `abstract_structure` WRITE;
/*!40000 ALTER TABLE `abstract_structure` DISABLE KEYS */;
INSERT INTO `abstract_structure` VALUES (1,1461825539156,'admin',1461825539132,'admin',0,NULL,'Generated',0,1461825539138,NULL,1,'ACTIVE',1,1,1),(2,1461825542298,'devuser',1461825542285,'devuser',1461825542888,'devuser','Generated',0,1461825542288,NULL,1,'DEPRECATED',2,1,1),(3,1461825542892,'devuser',1461825542780,'devuser',0,NULL,'Base endpoint activation configuration',0,1461825542785,NULL,2,'ACTIVE',1,1,1),(4,1461825543196,'devuser',1461825543099,'devuser',0,NULL,'Active devices configuration',0,1461825543105,NULL,1,'ACTIVE',1,1,2),(5,1461825543462,'devuser',1461825543353,'devuser',0,NULL,'Profile filter for active devices',0,1461825543353,NULL,1,'ACTIVE',1,1,2),(6,1461825543710,'devuser',1461825543617,'devuser',0,NULL,'Inactive devices configuration',0,1461825543622,NULL,1,'ACTIVE',1,1,3),(7,1461825543918,'devuser',1461825543829,'devuser',0,NULL,'Profile filter for inactive devices',0,1461825543829,NULL,1,'ACTIVE',1,1,3),(8,1461825544422,'admin',1461825544417,'admin',0,NULL,'Generated',0,1461825544418,NULL,1,'ACTIVE',1,2,4),(9,1461825545609,'devuser',1461825545596,'devuser',1461825545860,'devuser','Generated',0,1461825545598,NULL,1,'DEPRECATED',2,2,4),(10,1461825545862,'devuser',1461825545770,'devuser',0,NULL,'Base configuration demo configuration',0,1461825545780,NULL,2,'ACTIVE',1,2,4),(11,1461825546140,'admin',1461825546130,'admin',0,NULL,'Generated',0,1461825546131,NULL,1,'ACTIVE',1,3,5),(12,1461825547826,'admin',1461825547816,'admin',0,NULL,'Generated',0,1461825547821,NULL,1,'ACTIVE',1,4,6),(13,1461825549439,'admin',1461825549433,'admin',0,NULL,'Generated',0,1461825549434,NULL,1,'ACTIVE',1,5,7),(14,1461825551338,'admin',1461825551329,'admin',0,NULL,'Generated',0,1461825551334,NULL,1,'ACTIVE',1,6,8),(15,1461825552494,'admin',1461825552489,'admin',0,NULL,'Generated',0,1461825552490,NULL,1,'ACTIVE',1,7,9),(16,1461825553241,'devuser',1461825553228,'devuser',1461825553684,'devuser','Generated',0,1461825553233,NULL,1,'DEPRECATED',2,7,9),(17,1461825553691,'devuser',1461825553609,'devuser',0,NULL,'Base verifiers demo configuration',0,1461825553610,NULL,2,'ACTIVE',1,7,9),(18,1461825554081,'admin',1461825554069,'admin',0,NULL,'Generated',0,1461825554069,NULL,1,'ACTIVE',1,8,10),(19,1461825555305,'admin',1461825555288,'admin',0,NULL,'Generated',0,1461825555291,NULL,1,'ACTIVE',1,9,11),(20,1461825557545,'admin',1461825557533,'admin',0,NULL,'Generated',0,1461825557533,NULL,1,'ACTIVE',1,10,12),(21,1461825559290,'admin',1461825559281,'admin',0,NULL,'Generated',0,1461825559282,NULL,1,'ACTIVE',1,11,13),(22,1461825561590,'admin',1461825561583,'admin',0,NULL,'Generated',0,1461825561588,NULL,1,'ACTIVE',1,12,14),(23,1461825561681,'admin',1461825561673,'admin',0,NULL,'Generated',0,1461825561678,NULL,1,'ACTIVE',1,13,15),(24,1461825562883,'admin',1461825562866,'admin',0,NULL,'Generated',0,1461825562871,NULL,1,'ACTIVE',1,14,16),(25,1461825564079,'admin',1461825564065,'admin',0,NULL,'Generated',0,1461825564065,NULL,1,'ACTIVE',1,15,17),(26,1461825565517,'devuser',1461825565496,'devuser',1461825565881,'devuser','Generated',0,1461825565514,NULL,1,'DEPRECATED',2,15,17),(27,1461825565882,'devuser',1461825565801,'devuser',0,NULL,'Base city guide configuration',0,1461825565807,NULL,2,'ACTIVE',1,15,17),(28,1461825566166,'devuser',1461825566048,'devuser',0,NULL,'City guide configuration for Atlanta city',0,1461825566081,NULL,1,'ACTIVE',1,15,18),(29,1461825566342,'devuser',1461825566281,'devuser',0,NULL,'Profile filter for Atlanta city',0,1461825566281,NULL,1,'ACTIVE',1,15,18),(30,1461825566569,'devuser',1461825566472,'devuser',0,NULL,'City guide configuration for Amsterdam city',0,1461825566505,NULL,1,'ACTIVE',1,15,19),(31,1461825566721,'devuser',1461825566650,'devuser',0,NULL,'Profile filter for Amsterdam city',0,1461825566650,NULL,1,'ACTIVE',1,15,19),(32,1461825567789,'admin',1461825567780,'admin',0,NULL,'Generated',0,1461825567781,NULL,1,'ACTIVE',1,16,20);
/*!40000 ALTER TABLE `abstract_structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_authority`
--

DROP TABLE IF EXISTS `admin_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_authority` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authority` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_pglc99q1ld4mr9nrsx8yfl53b` (`user_id`),
  CONSTRAINT `FK_pglc99q1ld4mr9nrsx8yfl53b` FOREIGN KEY (`user_id`) REFERENCES `admin_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_authority`
--

LOCK TABLES `admin_authority` WRITE;
/*!40000 ALTER TABLE `admin_authority` DISABLE KEYS */;
INSERT INTO `admin_authority` VALUES (1,'KAA_ADMIN',1),(2,'TENANT_ADMIN',2),(3,'TENANT_DEVELOPER',3);
/*!40000 ALTER TABLE `admin_authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_properties`
--

DROP TABLE IF EXISTS `admin_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fqn` varchar(255) DEFAULT NULL,
  `rawConfiguration` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_properties`
--

LOCK TABLES `admin_properties` WRITE;
/*!40000 ALTER TABLE `admin_properties` DISABLE KEYS */;
INSERT INTO `admin_properties` VALUES (1,'org.kaaproject.kaa.server.admin.services.entity.gen.SmtpMailProperties','BKaa <admin@localhost.localdomain>\0localhost2\0��\0\0\0\0\0\0'),(2,'org.kaaproject.kaa.server.admin.services.entity.gen.GeneralProperties','Kaa*http://localhost:8080');
/*!40000 ALTER TABLE `admin_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_user`
--

DROP TABLE IF EXISTS `admin_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enabled` bit(1) NOT NULL,
  `firstName` varchar(255) DEFAULT NULL,
  `lastName` varchar(255) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `passwordResetHash` varchar(255) DEFAULT NULL,
  `tempPassword` bit(1) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_lvod9bfm438ex1071ku1glb70` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_user`
--

LOCK TABLES `admin_user` WRITE;
/*!40000 ALTER TABLE `admin_user` DISABLE KEYS */;
INSERT INTO `admin_user` VALUES (1,'',NULL,NULL,NULL,'$2a$10$Xm8MhS7vEmdDYNrGkhOd5edoM1CoRRZHsZ0fNX6YfC.e0.9uoM9V2',NULL,'\0','kaa'),(2,'','Tenant','Admin','admin@demoproject.org','$2a$10$HMGmjx5VfFrLYOck4Rx5CezRoWCHfp.FdsHylbsZRybyD34EyJ.c2',NULL,'\0','admin'),(3,'','Tenant','Developer','devuser@demoproject.org','$2a$10$sHDumr1dcrx.65HDSnkRvu6B8do.z/7t3X6dmp0nUFpGuh7mFiECC',NULL,'\0','devuser');
/*!40000 ALTER TABLE `admin_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application`
--

DROP TABLE IF EXISTS `application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application` (
  `id` bigint(20) NOT NULL,
  `application_token` varchar(255) DEFAULT NULL,
  `credentials_service` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `tenant_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9sl087nu585d3i0rexu115ut2` (`tenant_id`,`name`),
  UNIQUE KEY `UK_6t3emgtt4qh9n03nshofv9od6` (`application_token`),
  CONSTRAINT `FK_8oamqms10799ijgmh8qb88yut` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application`
--

LOCK TABLES `application` WRITE;
/*!40000 ALTER TABLE `application` DISABLE KEYS */;
INSERT INTO `application` VALUES (1,'32617499555992350725','Internal','Endpoint activation demo',7,1),(2,'43390267759495641709',NULL,'Configuration demo',3,1),(3,'73477667674951214230',NULL,'Notification demo',3,1),(4,'81510689285127116757',NULL,'Android notification demo',3,1),(5,'82635305199158071549',NULL,'Data collection demo',1,1),(6,'08667319577302871977',NULL,'Event demo',1,1),(7,'15499309807562370770',NULL,'User verifiers demo',3,1),(8,'48232077647668273225',NULL,'Zeppelin data analytics demo',1,1),(9,'24660441575077886557',NULL,'Storm data analytics demo',1,1),(10,'85752489253032294384',NULL,'Cassandra data analytics demo',1,1),(11,'21327944277721354899',NULL,'Spark data analytics demo',1,1),(12,'06715269635438144161',NULL,'GPIO control master',1,1),(13,'91511939299233629805',NULL,'GPIO control slave',1,1),(14,'91832476309660881037',NULL,'Cell monitor',1,1),(15,'10162668818569537434',NULL,'City guide',7,1),(16,'47379174857733852823',NULL,'Photo frame',1,1);
/*!40000 ALTER TABLE `application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application_event_family_map`
--

DROP TABLE IF EXISTS `application_event_family_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application_event_family_map` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  `events_class_family_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_o03hwph2m2111h4ld2psmvg5p` (`application_id`),
  KEY `FK_gvsyw3r82yby0synnr9u2sg05` (`events_class_family_id`),
  CONSTRAINT `FK_gvsyw3r82yby0synnr9u2sg05` FOREIGN KEY (`events_class_family_id`) REFERENCES `events_class_family` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_o03hwph2m2111h4ld2psmvg5p` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application_event_family_map`
--

LOCK TABLES `application_event_family_map` WRITE;
/*!40000 ALTER TABLE `application_event_family_map` DISABLE KEYS */;
INSERT INTO `application_event_family_map` VALUES (1,1461825551602,'devuser',1,6,1),(2,1461825552741,'devuser',1,7,2),(3,1461825562052,'devuser',1,12,3),(4,1461825562236,'devuser',1,13,3),(5,1461825568087,'devuser',1,16,4);
/*!40000 ALTER TABLE `application_event_family_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `application_event_map`
--

DROP TABLE IF EXISTS `application_event_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application_event_map` (
  `id` bigint(20) NOT NULL,
  `action` varchar(255) DEFAULT NULL,
  `fqn` varchar(255) DEFAULT NULL,
  `events_class_id` bigint(20) NOT NULL,
  `application_event_family_map_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_i4f2pub85yeu4hvtnq0wbdth4` (`events_class_id`),
  KEY `FK_ff2h9p3e5dhh6y6vooa4mbad1` (`application_event_family_map_id`),
  CONSTRAINT `FK_ff2h9p3e5dhh6y6vooa4mbad1` FOREIGN KEY (`application_event_family_map_id`) REFERENCES `application_event_family_map` (`id`),
  CONSTRAINT `FK_i4f2pub85yeu4hvtnq0wbdth4` FOREIGN KEY (`events_class_id`) REFERENCES `events_class` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application_event_map`
--

LOCK TABLES `application_event_map` WRITE;
/*!40000 ALTER TABLE `application_event_map` DISABLE KEYS */;
INSERT INTO `application_event_map` VALUES (1,'BOTH','org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest',1,1),(2,'BOTH','org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoResponse',3,1),(3,'BOTH','org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest',4,1),(4,'BOTH','org.kaaproject.kaa.demo.verifiersdemo.MessageEvent',5,2),(5,'SOURCE','org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest',7,3),(6,'SINK','org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse',8,3),(7,'SOURCE','org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest',9,3),(8,'SINK','org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest',7,4),(9,'SOURCE','org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse',8,4),(10,'SINK','org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest',9,4),(11,'BOTH','org.kaaproject.kaa.demo.photoframe.DeviceInfoRequest',10,5),(12,'BOTH','org.kaaproject.kaa.demo.photoframe.DeviceInfoResponse',12,5),(13,'BOTH','org.kaaproject.kaa.demo.photoframe.AlbumListRequest',13,5),(14,'BOTH','org.kaaproject.kaa.demo.photoframe.AlbumListResponse',15,5),(15,'BOTH','org.kaaproject.kaa.demo.photoframe.PlayAlbumRequest',16,5),(16,'BOTH','org.kaaproject.kaa.demo.photoframe.StopRequest',17,5),(17,'BOTH','org.kaaproject.kaa.demo.photoframe.PlayInfoRequest',18,5),(18,'BOTH','org.kaaproject.kaa.demo.photoframe.PlayInfoResponse',21,5);
/*!40000 ALTER TABLE `application_event_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `base_schems`
--

DROP TABLE IF EXISTS `base_schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `base_schems` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  `ctl_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_server_pf_schems_app_id` (`application_id`),
  KEY `fk_server_pf_schems_ctl_id` (`ctl_id`),
  CONSTRAINT `fk_server_pf_schems_app_id` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_server_pf_schems_ctl_id` FOREIGN KEY (`ctl_id`) REFERENCES `ctl` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `base_schems`
--

LOCK TABLES `base_schems` WRITE;
/*!40000 ALTER TABLE `base_schems` DISABLE KEYS */;
INSERT INTO `base_schems` VALUES (1,1461825539072,'admin',NULL,'Generated',0,1,1),(2,1461825539216,'admin',NULL,'Generated',0,1,1),(3,1461825542657,'devuser','Server profile schema describing activation application profile','Endpoint activation server profile schema',1,1,2),(4,1461825544390,'admin',NULL,'Generated',0,2,1),(5,1461825544446,'admin',NULL,'Generated',0,2,1),(6,1461825546107,'admin',NULL,'Generated',0,3,1),(7,1461825546158,'admin',NULL,'Generated',0,3,1),(8,1461825547798,'admin',NULL,'Generated',0,4,1),(9,1461825547839,'admin',NULL,'Generated',0,4,1),(10,1461825549424,'admin',NULL,'Generated',0,5,1),(11,1461825549457,'admin',NULL,'Generated',0,5,1),(12,1461825551311,'admin',NULL,'Generated',0,6,1),(13,1461825551353,'admin',NULL,'Generated',0,6,1),(14,1461825552475,'admin',NULL,'Generated',0,7,1),(15,1461825552514,'admin',NULL,'Generated',0,7,1),(16,1461825554058,'admin',NULL,'Generated',0,8,1),(17,1461825554089,'admin',NULL,'Generated',0,8,1),(18,1461825555274,'admin',NULL,'Generated',0,9,1),(19,1461825555332,'admin',NULL,'Generated',0,9,1),(20,1461825557505,'admin',NULL,'Generated',0,10,1),(21,1461825557576,'admin',NULL,'Generated',0,10,1),(22,1461825559272,'admin',NULL,'Generated',0,11,1),(23,1461825559299,'admin',NULL,'Generated',0,11,1),(24,1461825561577,'admin',NULL,'Generated',0,12,1),(25,1461825561601,'admin',NULL,'Generated',0,12,1),(26,1461825561666,'admin',NULL,'Generated',0,13,1),(27,1461825561695,'admin',NULL,'Generated',0,13,1),(28,1461825562851,'admin',NULL,'Generated',0,14,1),(29,1461825562899,'admin',NULL,'Generated',0,14,1),(30,1461825564042,'admin',NULL,'Generated',0,15,1),(31,1461825564113,'admin',NULL,'Generated',0,15,1),(32,1461825565673,'devuser','Profile schema describing city guide application profile','City guide profile schema',1,15,3),(33,1461825567771,'admin',NULL,'Generated',0,16,1),(34,1461825567804,'admin',NULL,'Generated',0,16,1);
/*!40000 ALTER TABLE `base_schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `changes`
--

DROP TABLE IF EXISTS `changes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `changes` (
  `id` bigint(20) NOT NULL,
  `configuration_id` bigint(20) DEFAULT NULL,
  `configuration_version` int(11) DEFAULT NULL,
  `endpoint_group_id` bigint(20) DEFAULT NULL,
  `profile_filter_id` bigint(20) DEFAULT NULL,
  `topic_id` bigint(20) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `changes`
--

LOCK TABLES `changes` WRITE;
/*!40000 ALTER TABLE `changes` DISABLE KEYS */;
INSERT INTO `changes` VALUES (1,1,1,1,NULL,NULL,'ADD_CONF'),(2,2,2,1,NULL,NULL,'ADD_CONF'),(3,3,2,1,NULL,NULL,'ADD_CONF'),(4,4,2,2,NULL,NULL,'ADD_CONF'),(5,NULL,0,2,5,NULL,'ADD_PROF'),(6,6,2,3,NULL,NULL,'ADD_CONF'),(7,NULL,0,3,7,NULL,'ADD_PROF'),(8,8,1,4,NULL,NULL,'ADD_CONF'),(9,9,2,4,NULL,NULL,'ADD_CONF'),(10,10,2,4,NULL,NULL,'ADD_CONF'),(11,11,1,5,NULL,NULL,'ADD_CONF'),(12,NULL,0,5,NULL,1,'ADD_TOPIC'),(13,NULL,0,5,NULL,2,'ADD_TOPIC'),(14,12,1,6,NULL,NULL,'ADD_CONF'),(15,NULL,0,6,NULL,3,'ADD_TOPIC'),(16,NULL,0,6,NULL,4,'ADD_TOPIC'),(17,13,1,7,NULL,NULL,'ADD_CONF'),(18,14,1,8,NULL,NULL,'ADD_CONF'),(19,15,1,9,NULL,NULL,'ADD_CONF'),(20,16,2,9,NULL,NULL,'ADD_CONF'),(21,17,2,9,NULL,NULL,'ADD_CONF'),(22,18,1,10,NULL,NULL,'ADD_CONF'),(23,19,1,11,NULL,NULL,'ADD_CONF'),(24,20,1,12,NULL,NULL,'ADD_CONF'),(25,21,1,13,NULL,NULL,'ADD_CONF'),(26,22,1,14,NULL,NULL,'ADD_CONF'),(27,23,1,15,NULL,NULL,'ADD_CONF'),(28,24,1,16,NULL,NULL,'ADD_CONF'),(29,25,1,17,NULL,NULL,'ADD_CONF'),(30,26,2,17,NULL,NULL,'ADD_CONF'),(31,27,2,17,NULL,NULL,'ADD_CONF'),(32,28,2,18,NULL,NULL,'ADD_CONF'),(33,NULL,0,18,29,NULL,'ADD_PROF'),(34,30,2,19,NULL,NULL,'ADD_CONF'),(35,NULL,0,19,31,NULL,'ADD_PROF'),(36,32,1,20,NULL,NULL,'ADD_CONF');
/*!40000 ALTER TABLE `changes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuration`
--

DROP TABLE IF EXISTS `configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration` (
  `configuration_body` longblob,
  `configuration_schems_version` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `configuration_schems_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_smcmof238to6x3ta7enujgd8m` (`configuration_schems_id`),
  CONSTRAINT `FK_q1y4xb3dkmkvadurm574ago0j` FOREIGN KEY (`id`) REFERENCES `abstract_structure` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_smcmof238to6x3ta7enujgd8m` FOREIGN KEY (`configuration_schems_id`) REFERENCES `configuration_schems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuration`
--

LOCK TABLES `configuration` WRITE;
/*!40000 ALTER TABLE `configuration` DISABLE KEYS */;
INSERT INTO `configuration` VALUES ('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Ö+q\\u0006aýC¿{×¦èÞ§Ð\"}}',1,1,1),('{\"active\":false,\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"y\\u001811\\nÏN\\u0019¹²\\u0016¡\"}}',2,2,4),('{\"active\":false,\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"y\\u001811\\nÏN\\u0019¹²\\u0016¡\"}}',2,3,4),('{\"active\":{\"boolean\":true},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"~\\u0005C12RLS±1ãùki@\"}}',2,4,4),('{\"active\":{\"boolean\":false},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"òýca@KÜP*ý\\u0016;R\\u001F\"}}',2,6,4),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u000Ehªî£K§o\\u001A\\u0005àé\"}}',1,8,5),('{\"AddressList\":null,\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Jç]:¶@\\u0002>n^ÿ\\u0010×Ù\"}}',2,9,8),('{\"AddressList\":{\"array\":[{\"label\":\"Kaa website\",\"url\":\"http://www.kaaproject.org\",\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"à\\u0001§ïyIã»g?ßòõ,\\u0017\"}},{\"label\":\"Kaa GitHub repository\",\"url\":\"https://github.com/kaaproject/kaa\",\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"}\\u001A\\u001A\\u0013£DÅ»\\be\\u0016We\\bð\"}},{\"label\":\"Kaa docs\",\"url\":\"http://docs.kaaproject.org/display/KAA/Kaa+IoT+Platform+Home\",\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"¢\\\\æf»J\\u0019Ý´Jù\\\"\"}},{\"label\":\"Kaa configuration design reference\",\"url\":\"http://docs.kaaproject.org/display/KAA/Configuration\",\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"!ÙpQ\\u0018iNySj§$\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Jç]:¶@\\u0002>n^ÿ\\u0010×Ù\"}}',2,10,8),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"&\\u0018\\u0012F\\\\F@ø{ê$\\t1\\u0011ë\"}}',1,11,9),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"?3\\u000F®A§oÝU\\u001C\\u0001\"}}',1,12,13),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\" ªØ5Ç\\\\I­®aa\\u0000þ½\"}}',1,13,17),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"nKJJ3ZG\\u0015äã\'\\tì\"}}',1,14,21),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"`SÂ,íîGI¢ÛþOEX\"}}',1,15,24),('{\"twitterKaaVerifierToken\":null,\"facebookKaaVerifierToken\":null,\"googleKaaVerifierToken\":null,\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"K}½`}Of{\\u0018þ{ñ¢\\r\"}}',2,16,27),('{\"twitterKaaVerifierToken\":{\"string\":\"28824347509112399479\"},\"facebookKaaVerifierToken\":{\"string\":\"46211075959774832208\"},\"googleKaaVerifierToken\":{\"string\":\"98934406053807597448\"},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"K}½`}Of{\\u0018þ{ñ¢\\r\"}}',2,17,27),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u001B-DîûBLÜ/r7G\\u0010\"}}',1,18,28),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u0007Æ¨ÇÿO(·&¶\\u0005\\u0015\\u0006y+\"}}',1,19,32),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u0011Ù\\u0013<N\\u001D ·\\u0001)\\u0000lD\"}}',1,20,36),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u001B¾@M\\u0019sN,Æ`\\\\@tL\\u0001\"}}',1,21,40),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Ð]\\u00042ª2M±¯\\b[¿ÿ\\u000E\"}}',1,22,44),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ê¼-½\\u0016A\\u0018{ \\u000EHÇñ\"}}',1,23,47),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Í,BFË¼2²æøàF\\u0019\"}}',1,24,50),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"«XÎÂ{mLÄ¿VX,\\u0015n¦\"}}',1,25,54),('{\"availableAreas\":[],\"areas\":[],\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"lixÕ§ÕC÷¼\\u0017U¨Æ\"}}',2,26,57),('{\"availableAreas\":[{\"name\":\"North America\",\"availableCities\":[\"Atlanta\"],\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"¼$\\u0004pÔrL\\u000F¢wÚËÈ>\"}},{\"name\":\"Europe\",\"availableCities\":[\"Amsterdam\"],\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u001BwIüèL¡Ë*ðìè0\"}}],\"areas\":[],\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"lixÕ§ÕC÷¼\\u0017U¨Æ\"}}',2,27,57),('{\"availableAreas\":{\"org.kaaproject.configuration.unchangedT\":\"unchanged\"},\"areas\":{\"array\":[{\"name\":{\"string\":\"North America\"},\"cities\":{\"array\":[{\"name\":{\"string\":\"Atlanta\"},\"places\":{\"array\":[{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"HOTEL\"},\"title\":{\"string\":\"Omni Atlanta Hotel at CNN Center\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-f/06/fd/ef/93/double-queen-guest-room.jpg\"},\"description\":{\"string\":\"Near Georgia Aquarium, Popular upscale hotel in Atlanta, Upscale\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.759376},\"longitude\":{\"double\":-84.394991},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ùº^îàM,¥ËcMÐå\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"LÎ#¸BF§\\u000Bû\\\\ø&Uá\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"HOTEL\"},\"title\":{\"string\":\"InterContinental Buckhead Atlanta\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-f/06/2b/14/6e/hotel-exterior-of-intercontine.jpg\"},\"description\":{\"string\":\"Popular luxury hotel in Atlanta, Top-tier hotel, Other travelers love this hotel\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.845817},\"longitude\":{\"double\":-84.36787},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"NmðVJM§²ÕµÉÆ¢[ý\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"­H¸¤4DYºÃ\\u001Eè\\u0006\\u0005\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"HOTEL\"},\"title\":{\"string\":\"Loews Atlanta Hotel\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-f/06/2d/7c/28/grand-king-room.jpg\"},\"description\":{\"string\":\"Good choice for travelers who love local culture, Offers free wifi, Popular elegant hotel in Atlanta\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.782991},\"longitude\":{\"double\":-84.38332},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ÌùYvÌKB²¨§±ðÂx\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"§³ÙóÎ¶N\\u0018PFÅ\\u001E\\u000BV\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"SHOP\"},\"title\":{\"string\":\"Atlantic Station\"},\"photoUrl\":{\"string\":\"http://images.atlanta.net/WebImage.ashx?mode=logo&accountnum=00128166\"},\"description\":{\"string\":\"The Atlantic Station neighborhood in Atlanta is the national model for smart growth and sustainable development. Picture a community with unsurpassed architectural quality, a fusion of functionality and finesse that combines an attractive mix of affordable, middle-income and up-scale housing with world-class restaurants, theaters and retailers. \"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.792771},\"longitude\":{\"double\":-84.396187},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"/xn¯7óJ\\u0016¾\\u000E¢é\\u0002j\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"%ýA¤´m0oÿÈ\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"SHOP\"},\"title\":{\"string\":\"Buckhead Atlanta\"},\"photoUrl\":{\"string\":\"http://images.atlanta.net/WebImage.ashx?mode=logo&accountnum=00095518\"},\"description\":{\"string\":\"Buckhead Atlanta is a luxury shopping/dining entertainment, office, and residential district spanning six blocks in the heart of Buckhead Atlanta.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.839611},\"longitude\":{\"double\":-84.381425},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"7;ñRMA@\\b¾\\u0006LÀJ\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"mAÛÚu\\u0014C^´»Úå\\u0017Ñ[\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"SHOP\"},\"title\":{\"string\":\"City of East Point\"},\"photoUrl\":{\"string\":\"http://images.atlanta.net/WebImage.ashx?mode=logo&accountnum=015858\"},\"description\":{\"string\":\"East Point is one of Georgia\'s fastest growing cities. Accessibility is one of the many advantages that the City of East Point offers. Its location provides easy access to public transportation, major highways, Atlanta\'s Hartsfield-Jackson Airport and the world-renowned attractions, dining and shopping of Atlanta.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.68088},\"longitude\":{\"double\":-84.442149},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"pÖºORMV¶@\\u001Añ9ütº\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"¬\\u001Cãá\\fKb6°B\\u0002¯X\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"MUSEUM\"},\"title\":{\"string\":\"Atlanta Contemporary Art Center\"},\"photoUrl\":{\"string\":\"http://upload.wikimedia.org/wikipedia/commons/0/0e/Atlanta_Contemporary_Art_Center.jpg\"},\"description\":{\"string\":\"Local, national, and international contemporary art; education geared toward working artists and collectors of art\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.762106},\"longitude\":{\"double\":-84.391468},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ÈU ÕÙHvìî\\u0006l\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Q!6GÐ°Ô9|ù\\u000E\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"MUSEUM\"},\"title\":{\"string\":\"Atlanta Cyclorama & Civil War Museum\"},\"photoUrl\":{\"string\":\"http://upload.wikimedia.org/wikipedia/commons/6/6f/Atlanta_Cyclorama.jpg\"},\"description\":{\"string\":\"Displays pictures and artifacts from the Civil War and houses a massive cylindrical panoramic painting of the Battle of Atlanta.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.734158},\"longitude\":{\"double\":-84.371064},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"2°%\\rÕ\\u001EE·Áè\\u001D¥Eêí\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"M8+ø\\\\\\\"Kl§\\u0010¿j4y\\u0003\'\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"MUSEUM\"},\"title\":{\"string\":\"Atlanta History Center\"},\"photoUrl\":{\"string\":\"http://upload.wikimedia.org/wikipedia/commons/8/8a/Swan_Coach_House.jpg\"},\"description\":{\"string\":\"History of Atlanta and Georgia; includes the Centennial Olympic Games Museum and one of the nation\'s most complete Civil War exhibitions.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.84282},\"longitude\":{\"double\":-84.38573},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\fß\\u0001§UøF\\u0005©.ªG@=\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"up²D¥C\'¯ÜÆÛë+\\u0014\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"RESTAURANT\"},\"title\":{\"string\":\"Fandangles\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-l/04/2d/46/fa/fandangles.jpg\"},\"description\":{\"string\":\"Located inside the Sheraton Atlanta Hotel, Fandangles promotes health-conscious Southern evolution food. The menu includes Southern Comforts like the 24-oz Bone-in Rib-eye and The Roasted Corn and Chicken Chowder to items that are a bit lighter fare like our locally sourced House Smoked Trout or our Quinoa Kale salad. We offer an uniquely...\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.759269},\"longitude\":{\"double\":-84.383103},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"06$-\\u0014cH\\u0000µ\\u001AeÜ3Á¥\\u0004\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ÇFø\\u001A\\u001A\\u001CMò·æÜ®0\\u0004±\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"RESTAURANT\"},\"title\":{\"string\":\"Canoe\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-l/01/fa/1f/43/canoe.jpg\"},\"description\":{\"string\":\"Canoe is located in Atlanta\'s historic Vinings area on the Chattahoochee River where Buckhead meets Vinings. Canoe\'s beautiful historic riverside setting makes it the perfect spot for weddings, receptions and private celebrations. Its original cuisine and distinctive design have already been featured in Bon Appetit, Food And Wine, Gourmet, The Wine Spectator and The New York Times.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.860061},\"longitude\":{\"double\":-84.455461},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"¹W`ç\\u0012F\\u0000TtBÃï¢N\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ûø%ÛR\\u0002N\\u0016¥Üv´ì]ò¥\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"RESTAURANT\"},\"title\":{\"string\":\"Bone\'s Restaurant\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-l/01/23/67/77/bones.jpg\"},\"description\":{\"string\":\"Bone’s opened its doors in 1979 with a mission to provide only the finest service, steaks and seafood. Since that time, we\'re proud to have been recognized as the best steakhouse in Atlanta -- and by many, as the best steakhouse in America. Prime beef, fresh seafood, and Maine lobster are served along with regional specialties from our Southern roots.  Known for business lunches and business dinners, Bone’s provides private party rooms and personalized menus to accommodate fine dining experiences and special occasions.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":33.842361},\"longitude\":{\"double\":-84.371015},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"¼¢øü$UL2Ð\\r\\u001F¿A\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"GqâLC4ÈÎÄ\\b^ñ\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"µ%¸HõMeäkFz\\\"\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Dß\\fAøBw_5RfJ\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"]NUHûXNS\\u000F:×«,**\"}}',2,28,57),('{\"availableAreas\":{\"org.kaaproject.configuration.unchangedT\":\"unchanged\"},\"areas\":{\"array\":[{\"name\":{\"string\":\"Europe\"},\"cities\":{\"array\":[{\"name\":{\"string\":\"Amsterdam\"},\"places\":{\"array\":[{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"HOTEL\"},\"title\":{\"string\":\"Marriott Amsterdam\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-f/06/fd/e7/c4/deluxe-guest-room.jpg\"},\"description\":{\"string\":\"Known for its colourful tulips and peaceful canals, Netherland\'s capital charms every visitor. In the midst of this beautiful city, Amsterdam Marriott Hotel welcomes guests with exceptional service. Just across from Leidseplein, the city centre hotel is ideally located for dining and entertainment as well as fashionable shopping.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.363291},\"longitude\":{\"double\":4.880761},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"bAÓ´sNo´O\\u0001­xh\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\fé\\u0003eKeægÛ®5Iú\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"HOTEL\"},\"title\":{\"string\":\"Renaissance Amsterdam Hotel\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-f/06/fe/03/b6/king-guest-room.jpg\"},\"description\":{\"string\":\"Just a short walk from Central Station and situated in the historic city centre of charming Amsterdam, the Renaissance Amsterdam Hotel is the top spot to be after a top-to-bottom redesign. Our contemporary hotel in Amsterdam offers a warm and welcoming lobby, renovated guest accommodations featuring Renaissance Bedding, broadband internet access and HDTV.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.378064},\"longitude\":{\"double\":4.894593},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"2¸F/gwI¡Äk5¸`\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"óß<!­Fä»£\\nr°âç\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"HOTEL\"},\"title\":{\"string\":\"Crowne Plaza Amsterdam City Centre\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-f/06/66/df/f2/crowne-plaza-amsterdam.jpg\"},\"description\":{\"string\":\"Book a Club Room or Suite at the Crowne Plaza Amsterdam City Centre hotel and gain access to the exclusive club lounge. Club guests enjoy browsing complimentary newspapers over breakfast, and return to use complimentary high-speed Internet and evening snacks and drinks.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.377476},\"longitude\":{\"double\":4.895767},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"½û£\\u0011«G´\\u001DU«Ù¤\\u0019i\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"»\\u000E_@DFæÇf\\u0010ââR\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"SHOP\"},\"title\":{\"string\":\"Van Stapele Koekmakerij\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/05/9a/c0/d5/van-stapele-koekmakerij.jpg\"},\"description\":{\"string\":\"The cosy, warm atmosphere of our shop will briefly take you back to the Amsterdam of olden days. The aroma of freshly baked cookies slowly drifts towards you as soft piano music fills the air, and light reflected through crystal chandeliers sparkles down on rows of delicious cookies.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.368953},\"longitude\":{\"double\":4.888423},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"l\\u001D¶,êO\\\\bgýgPd²\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"NÕgS>E¡²­Îp¼o/\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"SHOP\"},\"title\":{\"string\":\"Bonebakker Jeweler Amsterdam\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/06/63/b6/82/bonebakker-jeweler-amsterdam.jpg\"},\"description\":{\"string\":\"Bonebakker’s Jewellery store is situated in the monumental Van Baerle Shopping Gallery in the charming Museum Quarter in Amsterdam.  Bonebakker has created an attractive, sophisticated venue for connoisseurs and lovers of jewellery within the luxurious atmosphere of the Conservatorium Hotel. Blending the delicate ornaments of the 19th century Art-Nouveau palace with state-of-the art interior features, Bonebakker Jeweller evokes an elegant atmosphere in an intimate setting.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.358783},\"longitude\":{\"double\":4.878607},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"z,5K»ó@me/ig,B\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"lÉF-~D\\u0001 \\u001D3Ë&ª:\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"SHOP\"},\"title\":{\"string\":\"Chocolatl\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/02/5d/42/55/chocolatl-store.jpg\"},\"description\":{\"string\":\"Chocolatl is a specialty retail chocolate shop located in Amsterdam, Netherlands. Opened in December 2010, we like to think of the shop as a kind of  “chocolate gallery” through which we seek to promote and make available high quality chocolate from around the world.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.370568},\"longitude\":{\"double\":4.880011},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"3Z&¯FÂ¡k\\u0010²\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u0018³\\u001DÄ\\u0013Fy¦\'GE\\u0004nhx\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"MUSEUM\"},\"title\":{\"string\":\"The Rijksmuseum (National Museum)\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/00/1c/d3/57/view-from-museumplein.jpg\"},\"description\":{\"string\":\"The Rijksmuseum is the museum of the Netherlands. The completely renovated Rijksmuseum tells the story of the Netherlands from the Middle Ages to the 20th century. Including works by Rembrandt, Vermeer, Frans Hals, and more! Most famous is Rembrandt\'s masterpiece the Night Watch. A new display of the collection, a renewed building, new public facilities, a revamped garden and a new Asian Pavilion. Open daily from 9 am to 5 pm.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.360517},\"longitude\":{\"double\":4.881597},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ãÇÍóå)Be ?\\u001B*¡ñ\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u00014\\u0018JbE&ëª7îî\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"MUSEUM\"},\"title\":{\"string\":\"Anne Frank House (Anne Frankhuis)\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/01/58/c2/a4/caption.jpg\"},\"description\":{\"string\":\"A Museum with a Story. Visit the hiding place where Anne Frank wrote her diary during the Second World War. For more than two years, Anne Frank lived secretively with the other people in hiding in the back part of her father’s office building at # 263 Prinsengracht. \"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.375208},\"longitude\":{\"double\":4.883997},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"AíÌzÇðH*%W V\\u0005)\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"\\u0017\\u001A\\f\\u0003â\\\"Oü¸ï£Ç\\\\¾G\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"MUSEUM\"},\"title\":{\"string\":\"Van Gogh Museum\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/01/8b/bf/c1/museum.jpg\"},\"description\":{\"string\":\"The world\'s largest collection from the Dutch painter Vincent van Gogh (1853-1890) features more than 200 paintings and 600 drawings. \"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.358419},\"longitude\":{\"double\":4.881055},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"n¸Üs¼E?¥©P8\\u0003\\u000F\\t­\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"h\\u0018¦@J\\u000FI\\u0015¥\\u001Fbîbl\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"RESTAURANT\"},\"title\":{\"string\":\"Restaurant Johannes\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-l/06/2d/9f/ad/restaurant-johannes.jpg\"},\"description\":{\"string\":\"Chef Tommy den Hartog, restaurant manager Aline Mannes and their team are looking forward to welcoming you at Johannes were they found the perfect place to share their enthusiasm.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.3676},\"longitude\":{\"double\":4.887301},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Kéý\\nxHÇ«Î\\\"S\\u001B¶]Q\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"É¸îØf³CS#ø|O\\u0019ëµ\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"RESTAURANT\"},\"title\":{\"string\":\"Lombardo\'s\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/05/af/d1/b9/lombardo-s.jpg\"},\"description\":{\"string\":\"It\'s all about taste!! Thats our motto... The Best Burgers, sandwiches, salads, freshly made juices...And damn good coffee! Served with a smile and a whole lot of love! We are located near the Rijksmuseum in the vibrant Spiegel quarter. We are near the corner of Kerkstraat and N. Spiegelstraat, careful you might miss it!! It\'s cozy at Lombardo\'s...So seating is always first come first served. Come in for the 4 years uncontested BEST burgers in Amsterdam!!\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.363524},\"longitude\":{\"double\":4.888665},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"çò8fNxRGl^Qf\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"®ËMÇIh¹6õùõíw\"}},{\"category\":{\"org.kaaproject.kaa.demo.cityguide.Category\":\"RESTAURANT\"},\"title\":{\"string\":\"Bord\'Eau\"},\"photoUrl\":{\"string\":\"http://media-cdn.tripadvisor.com/media/photo-s/04/4f/4b/af/bord-eau.jpg\"},\"description\":{\"string\":\"Recently Michelin star awarded Bord\'Eau offers a culinary adventure directed by Executive Chef Richard van Oostenbrugge and Sous Chef Thomas Groot. Exceptional ingredients are blended to create symphonies of flavour, all complemented by the finest wines from around the globe. The intimacy of the salon-like setting, the classical simplicity of the decor and the grace and attentiveness of the service suggest high style and privilege overlooking the Amstel River.\"},\"location\":{\"org.kaaproject.kaa.demo.cityguide.Location\":{\"latitude\":{\"double\":52.3676},\"longitude\":{\"double\":4.894391},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ÔÁjT$JFT×4Ü\\u0014`Á\"}}},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"ÁÃ\\u001Bâ?OÌ¼6-EÂÃÐê\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\">­¾EI~@ß¬ªøpéÆ\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"Ä\\b\\u0019g@\\u0018¦ây\\u0011\\u001CÛ\\u000F\"}}]},\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"oðâøüJ¾°=MnÓ\"}}',2,30,57),('{\"__uuid\":{\"org.kaaproject.configuration.uuidT\":\"w\\u001C\\\"óD\\tQeR´È¹\"}}',1,32,58);
/*!40000 ALTER TABLE `configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuration_schems`
--

DROP TABLE IF EXISTS `configuration_schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `configuration_schems` (
  `base_schems` longtext,
  `override_schems` longtext,
  `protocol_schems` longtext,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_6c1w1hvw3794uenuce20voli9` FOREIGN KEY (`id`) REFERENCES `schems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuration_schems`
--

LOCK TABLES `configuration_schems` WRITE;
/*!40000 ALTER TABLE `configuration_schems` DISABLE KEYS */;
INSERT INTO `configuration_schems` VALUES ('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',1),('{\"type\":\"record\",\"name\":\"DeviceType\",\"namespace\":\"org.kaaproject.kaa.demo.activation\",\"fields\":[{\"name\":\"active\",\"type\":\"boolean\",\"by_default\":false},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"DeviceType\",\"namespace\":\"org.kaaproject.kaa.demo.activation\",\"fields\":[{\"name\":\"active\",\"type\":[\"boolean\",{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}],\"by_default\":false},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"DeviceType\",\"namespace\":\"org.kaaproject.kaa.demo.activation\",\"fields\":[{\"name\":\"active\",\"type\":[\"boolean\",{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}],\"by_default\":false},{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',4),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',5),('{\"type\":\"record\",\"name\":\"SampleConfiguration\",\"namespace\":\"org.kaaproject.kaa.demo.configuration\",\"fields\":[{\"name\":\"AddressList\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Link\",\"fields\":[{\"name\":\"label\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"displayName\":\"Site label\"},{\"name\":\"url\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"displayName\":\"Site URL\"},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Site address\"}},\"null\"],\"displayName\":\"URLs list\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"SampleConfiguration\",\"namespace\":\"org.kaaproject.kaa.demo.configuration\",\"fields\":[{\"name\":\"AddressList\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Link\",\"fields\":[{\"name\":\"label\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}],\"displayName\":\"Site label\"},{\"name\":\"url\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Site URL\"},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Site address\"}},\"null\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"URLs list\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"SampleConfiguration\",\"namespace\":\"org.kaaproject.kaa.demo.configuration\",\"fields\":[{\"name\":\"AddressList\",\"type\":[{\"type\":\"enum\",\"name\":\"resetT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"reset\"]},{\"type\":\"array\",\"items\":[{\"type\":\"record\",\"name\":\"Link\",\"fields\":[{\"name\":\"label\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}],\"displayName\":\"Site label\"},{\"name\":\"url\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Site URL\"},{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Site address\"},\"org.kaaproject.configuration.uuidT\"]},\"null\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"URLs list\"},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\",\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]},\"org.kaaproject.kaa.demo.configuration.Link\"]}]}}',8),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',9),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',13),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',17),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',21),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',24),('{\"type\":\"record\",\"name\":\"KaaVerifiersTokens\",\"namespace\":\"org.kaaproject.kaa.demo.verifiersdemo\",\"fields\":[{\"name\":\"twitterKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"facebookKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"googleKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"KaaVerifiersTokens\",\"namespace\":\"org.kaaproject.kaa.demo.verifiersdemo\",\"fields\":[{\"name\":\"twitterKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\",{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}]},{\"name\":\"facebookKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\",\"org.kaaproject.configuration.unchangedT\"]},{\"name\":\"googleKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\",\"org.kaaproject.configuration.unchangedT\"]},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"KaaVerifiersTokens\",\"namespace\":\"org.kaaproject.kaa.demo.verifiersdemo\",\"fields\":[{\"name\":\"twitterKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\",{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}]},{\"name\":\"facebookKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\",\"org.kaaproject.configuration.unchangedT\"]},{\"name\":\"googleKaaVerifierToken\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\",\"org.kaaproject.configuration.unchangedT\"]},{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',27),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',28),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',32),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',36),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',40),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',44),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',47),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',50),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',54),('{\"type\":\"record\",\"name\":\"CityGuideConfig\",\"namespace\":\"org.kaaproject.kaa.demo.cityguide\",\"fields\":[{\"name\":\"availableAreas\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"AvailableArea\",\"fields\":[{\"name\":\"name\",\"type\":\"string\",\"displayName\":\"Area name\",\"displayPrompt\":\"Enter area name\"},{\"name\":\"availableCities\",\"type\":{\"type\":\"array\",\"items\":\"string\"},\"displayName\":\"Available cities\"},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Available area\"}},\"displayName\":\"Available areas\"},{\"name\":\"areas\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Area\",\"fields\":[{\"name\":\"name\",\"type\":\"string\",\"displayName\":\"Area name\",\"displayPrompt\":\"Enter area name\",\"inputType\":\"plain\"},{\"name\":\"cities\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"City\",\"fields\":[{\"name\":\"name\",\"type\":\"string\",\"displayName\":\"City name\",\"displayPrompt\":\"Enter city name\"},{\"name\":\"places\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Place\",\"fields\":[{\"name\":\"category\",\"type\":{\"type\":\"enum\",\"name\":\"Category\",\"symbols\":[\"HOTEL\",\"SHOP\",\"MUSEUM\",\"RESTAURANT\"]},\"displayName\":\"Category\",\"displayPrompt\":\"Select place category\",\"weight\":0.30000001192092896,\"keyIndex\":1,\"displayNames\":[\"Hotel\",\"Shop\",\"Museum\",\"Restaurant\"]},{\"name\":\"title\",\"type\":\"string\",\"displayName\":\"Title\",\"displayPrompt\":\"Enter place title\",\"weight\":0.699999988079071,\"keyIndex\":2,\"inputType\":\"plain\"},{\"name\":\"photoUrl\",\"type\":\"string\",\"displayName\":\"Photo URL\",\"displayPrompt\":\"Enter place photo url\",\"inputType\":\"plain\"},{\"name\":\"description\",\"type\":\"string\",\"displayName\":\"Description\",\"displayPrompt\":\"Enter place description\",\"inputType\":\"plain\"},{\"name\":\"location\",\"type\":{\"type\":\"record\",\"name\":\"Location\",\"fields\":[{\"name\":\"latitude\",\"type\":\"double\",\"displayName\":\"Latitude\",\"displayPrompt\":\"Enter latitude\"},{\"name\":\"longitude\",\"type\":\"double\",\"displayName\":\"Longitude\",\"displayPrompt\":\"Enter longitude\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Location\"},\"displayName\":\"Location\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"City place\"}},\"displayName\":\"Places\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}},\"displayName\":\"Cities\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}},\"displayName\":\"Areas\",\"overrideStrategy\":\"append\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"City guide config\"}','{\"type\":\"record\",\"name\":\"CityGuideConfig\",\"namespace\":\"org.kaaproject.kaa.demo.cityguide\",\"fields\":[{\"name\":\"availableAreas\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"AvailableArea\",\"fields\":[{\"name\":\"name\",\"type\":[\"string\",{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}],\"displayName\":\"Area name\",\"displayPrompt\":\"Enter area name\"},{\"name\":\"availableCities\",\"type\":[{\"type\":\"array\",\"items\":\"string\"},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Available cities\"},{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Available area\"}},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Available areas\"},{\"name\":\"areas\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Area\",\"fields\":[{\"name\":\"name\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Area name\",\"displayPrompt\":\"Enter area name\",\"inputType\":\"plain\"},{\"name\":\"cities\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"City\",\"fields\":[{\"name\":\"name\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"City name\",\"displayPrompt\":\"Enter city name\"},{\"name\":\"places\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Place\",\"fields\":[{\"name\":\"category\",\"type\":[{\"type\":\"enum\",\"name\":\"Category\",\"symbols\":[\"HOTEL\",\"SHOP\",\"MUSEUM\",\"RESTAURANT\"]},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Category\",\"displayPrompt\":\"Select place category\",\"weight\":0.30000001192092896,\"keyIndex\":1,\"displayNames\":[\"Hotel\",\"Shop\",\"Museum\",\"Restaurant\"]},{\"name\":\"title\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Title\",\"displayPrompt\":\"Enter place title\",\"weight\":0.699999988079071,\"keyIndex\":2,\"inputType\":\"plain\"},{\"name\":\"photoUrl\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Photo URL\",\"displayPrompt\":\"Enter place photo url\",\"inputType\":\"plain\"},{\"name\":\"description\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Description\",\"displayPrompt\":\"Enter place description\",\"inputType\":\"plain\"},{\"name\":\"location\",\"type\":[{\"type\":\"record\",\"name\":\"Location\",\"fields\":[{\"name\":\"latitude\",\"type\":[\"double\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Latitude\",\"displayPrompt\":\"Enter latitude\"},{\"name\":\"longitude\",\"type\":[\"double\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Longitude\",\"displayPrompt\":\"Enter longitude\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Location\"},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Location\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"City place\"}},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Places\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Cities\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Areas\",\"overrideStrategy\":\"append\"},{\"name\":\"__uuid\",\"type\":[\"org.kaaproject.configuration.uuidT\",\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"City guide config\"}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"CityGuideConfig\",\"namespace\":\"org.kaaproject.kaa.demo.cityguide\",\"fields\":[{\"name\":\"availableAreas\",\"type\":[{\"type\":\"enum\",\"name\":\"resetT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"reset\"]},{\"type\":\"array\",\"items\":[{\"type\":\"record\",\"name\":\"AvailableArea\",\"fields\":[{\"name\":\"name\",\"type\":[\"string\",{\"type\":\"enum\",\"name\":\"unchangedT\",\"namespace\":\"org.kaaproject.configuration\",\"symbols\":[\"unchanged\"]}],\"displayName\":\"Area name\",\"displayPrompt\":\"Enter area name\"},{\"name\":\"availableCities\",\"type\":[\"org.kaaproject.configuration.resetT\",{\"type\":\"array\",\"items\":\"string\"},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Available cities\"},{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Available area\"},\"org.kaaproject.configuration.uuidT\"]},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Available areas\"},{\"name\":\"areas\",\"type\":[\"org.kaaproject.configuration.resetT\",{\"type\":\"array\",\"items\":[{\"type\":\"record\",\"name\":\"Area\",\"fields\":[{\"name\":\"name\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Area name\",\"displayPrompt\":\"Enter area name\",\"inputType\":\"plain\"},{\"name\":\"cities\",\"type\":[\"org.kaaproject.configuration.resetT\",{\"type\":\"array\",\"items\":[{\"type\":\"record\",\"name\":\"City\",\"fields\":[{\"name\":\"name\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"City name\",\"displayPrompt\":\"Enter city name\"},{\"name\":\"places\",\"type\":[\"org.kaaproject.configuration.resetT\",{\"type\":\"array\",\"items\":[{\"type\":\"record\",\"name\":\"Place\",\"fields\":[{\"name\":\"category\",\"type\":[{\"type\":\"enum\",\"name\":\"Category\",\"symbols\":[\"HOTEL\",\"SHOP\",\"MUSEUM\",\"RESTAURANT\"]},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Category\",\"displayPrompt\":\"Select place category\",\"weight\":0.30000001192092896,\"keyIndex\":1,\"displayNames\":[\"Hotel\",\"Shop\",\"Museum\",\"Restaurant\"]},{\"name\":\"title\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Title\",\"displayPrompt\":\"Enter place title\",\"weight\":0.699999988079071,\"keyIndex\":2,\"inputType\":\"plain\"},{\"name\":\"photoUrl\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Photo URL\",\"displayPrompt\":\"Enter place photo url\",\"inputType\":\"plain\"},{\"name\":\"description\",\"type\":[\"string\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Description\",\"displayPrompt\":\"Enter place description\",\"inputType\":\"plain\"},{\"name\":\"location\",\"type\":[{\"type\":\"record\",\"name\":\"Location\",\"fields\":[{\"name\":\"latitude\",\"type\":[\"double\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Latitude\",\"displayPrompt\":\"Enter latitude\"},{\"name\":\"longitude\",\"type\":[\"double\",\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Longitude\",\"displayPrompt\":\"Enter longitude\"},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\",\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"Location\"},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Location\"},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\",\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"City place\"},\"org.kaaproject.configuration.uuidT\"]},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Places\"},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\",\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]},\"org.kaaproject.configuration.uuidT\"]},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Cities\"},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\",\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]},\"org.kaaproject.configuration.uuidT\"]},\"org.kaaproject.configuration.unchangedT\"],\"displayName\":\"Areas\",\"overrideStrategy\":\"append\"},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\",\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}],\"displayName\":\"City guide config\"},\"org.kaaproject.kaa.demo.cityguide.Place\",\"org.kaaproject.kaa.demo.cityguide.AvailableArea\",\"org.kaaproject.kaa.demo.cityguide.Location\",\"org.kaaproject.kaa.demo.cityguide.Area\",\"org.kaaproject.kaa.demo.cityguide.City\"]}]}}',57),('{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":[{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"null\"],\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}','{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"deltaT\",\"namespace\":\"org.kaaproject.configuration\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"type\":\"record\",\"name\":\"Configuration\",\"namespace\":\"org.kaaproject.kaa.schema.base\",\"fields\":[{\"name\":\"__uuid\",\"type\":{\"type\":\"fixed\",\"name\":\"uuidT\",\"namespace\":\"org.kaaproject.configuration\",\"size\":16},\"displayName\":\"Record Id\",\"fieldAccess\":\"read_only\"}]}]}]}}',58);
/*!40000 ALTER TABLE `configuration_schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ctl`
--

DROP TABLE IF EXISTS `ctl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ctl` (
  `id` bigint(20) NOT NULL,
  `body` longtext,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `default_record` longtext,
  `version` int(11) DEFAULT NULL,
  `metainfo_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ctl_unique_constraint` (`metainfo_id`,`version`),
  CONSTRAINT `fk_ctl_metainfo_id` FOREIGN KEY (`metainfo_id`) REFERENCES `ctl_metainfo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ctl`
--

LOCK TABLES `ctl` WRITE;
/*!40000 ALTER TABLE `ctl` DISABLE KEYS */;
INSERT INTO `ctl` VALUES (1,'{\n    \"type\": \"record\",\n    \"name\": \"EmptyData\",\n    \"namespace\": \"org.kaaproject.kaa.schema.system\",\n    \"version\": 1,\n    \"dependencies\": [],\n    \"displayName\": \"Empty Data\",\n    \"description\": \"Auto generated\",\n    \"fields\": []\n}',1461825539044,'admin','',1,1),(2,'{\n  \"type\" : \"record\",\n  \"name\" : \"ActivationProfile\",\n  \"namespace\" : \"org.kaaproject.kaa.demo\",\n  \"fields\" : [ {\n    \"name\" : \"active\",\n    \"type\" : [ \"boolean\", \"null\" ],\n    \"by_default\" : false\n  } ],\n  \"version\" : 1,\n  \"dependencies\" : [ ]\n}',1461825542560,NULL,'{\"active\":{\"boolean\":false}}',1,2),(3,'{\n  \"type\" : \"record\",\n  \"name\" : \"CityGuideProfile\",\n  \"namespace\" : \"org.kaaproject.kaa.demo.cityguide.profile\",\n  \"fields\" : [ {\n    \"name\" : \"area\",\n    \"type\" : [ {\n      \"type\" : \"string\",\n      \"avro.java.string\" : \"String\"\n    }, \"null\" ]\n  }, {\n    \"name\" : \"city\",\n    \"type\" : [ {\n      \"type\" : \"string\",\n      \"avro.java.string\" : \"String\"\n    }, \"null\" ]\n  } ],\n  \"version\" : 1\n}',1461825565610,NULL,'{\"area\":null,\"city\":null}',1,3);
/*!40000 ALTER TABLE `ctl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ctl_dependency`
--

DROP TABLE IF EXISTS `ctl_dependency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ctl_dependency` (
  `parent_id` bigint(20) NOT NULL,
  `child_id` bigint(20) NOT NULL,
  PRIMARY KEY (`parent_id`,`child_id`),
  KEY `FK_csknwf0ihtl6tyswpfdvxl2ox` (`child_id`),
  CONSTRAINT `FK_csknwf0ihtl6tyswpfdvxl2ox` FOREIGN KEY (`child_id`) REFERENCES `ctl` (`id`),
  CONSTRAINT `FK_qf0hx7ehr412ubkwtvjkc25g1` FOREIGN KEY (`parent_id`) REFERENCES `ctl` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ctl_dependency`
--

LOCK TABLES `ctl_dependency` WRITE;
/*!40000 ALTER TABLE `ctl_dependency` DISABLE KEYS */;
/*!40000 ALTER TABLE `ctl_dependency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ctl_metainfo`
--

DROP TABLE IF EXISTS `ctl_metainfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ctl_metainfo` (
  `id` bigint(20) NOT NULL,
  `fqn` varchar(255) DEFAULT NULL,
  `application_id` bigint(20) DEFAULT NULL,
  `tenant_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ctl_metainfo_unique_constraint` (`fqn`,`tenant_id`,`application_id`),
  KEY `fk_ctl_metainfo_app_id` (`application_id`),
  KEY `fk_ctl_metainfo_tenant_id` (`tenant_id`),
  CONSTRAINT `fk_ctl_metainfo_app_id` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`),
  CONSTRAINT `fk_ctl_metainfo_tenant_id` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ctl_metainfo`
--

LOCK TABLES `ctl_metainfo` WRITE;
/*!40000 ALTER TABLE `ctl_metainfo` DISABLE KEYS */;
INSERT INTO `ctl_metainfo` VALUES (2,'org.kaaproject.kaa.demo.ActivationProfile',1,1),(3,'org.kaaproject.kaa.demo.cityguide.profile.CityGuideProfile',15,1),(1,'org.kaaproject.kaa.schema.system.EmptyData',NULL,NULL);
/*!40000 ALTER TABLE `ctl_metainfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `endpoint_group`
--

DROP TABLE IF EXISTS `endpoint_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `endpoint_group` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `endpoint_count` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `weight` int(11) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_tp701e8bmnvi01nu4kv8hfloh` (`weight`,`application_id`),
  UNIQUE KEY `UK_eqa8f921it9bw7otht0nu1qev` (`name`,`application_id`),
  KEY `FK_6cot0d89oyq27hhhnpbn1daqh` (`application_id`),
  CONSTRAINT `FK_6cot0d89oyq27hhhnpbn1daqh` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `endpoint_group`
--

LOCK TABLES `endpoint_group` WRITE;
/*!40000 ALTER TABLE `endpoint_group` DISABLE KEYS */;
INSERT INTO `endpoint_group` VALUES (1,1461825538984,'admin',NULL,0,'All',0,0,1),(2,1461825543004,'devuser','Active device endpoint group',0,'Active device group',0,1,1),(3,1461825543531,'devuser','Inactive device endpoint group',0,'Inactive device group',0,2,1),(4,1461825544381,'admin',NULL,0,'All',0,0,2),(5,1461825546099,'admin',NULL,0,'All',0,0,3),(6,1461825547790,'admin',NULL,0,'All',0,0,4),(7,1461825549421,'admin',NULL,0,'All',0,0,5),(8,1461825551309,'admin',NULL,0,'All',0,0,6),(9,1461825552469,'admin',NULL,0,'All',0,0,7),(10,1461825554050,'admin',NULL,0,'All',0,0,8),(11,1461825555264,'admin',NULL,0,'All',0,0,9),(12,1461825557495,'admin',NULL,0,'All',0,0,10),(13,1461825559262,'admin',NULL,0,'All',0,0,11),(14,1461825561568,'admin',NULL,0,'All',0,0,12),(15,1461825561661,'admin',NULL,0,'All',0,0,13),(16,1461825562827,'admin',NULL,0,'All',0,0,14),(17,1461825564029,'admin',NULL,0,'All',0,0,15),(18,1461825565962,'devuser','Atlanta endpoint group',0,'North America/Atlanta',0,1,15),(19,1461825566398,'devuser','Amsterdam endpoint group',0,'Europe/Amsterdam',0,2,15),(20,1461825567762,'admin',NULL,0,'All',0,0,16);
/*!40000 ALTER TABLE `endpoint_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `endpoint_group_topic`
--

DROP TABLE IF EXISTS `endpoint_group_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `endpoint_group_topic` (
  `topic_id` bigint(20) NOT NULL,
  `endpoint_group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`topic_id`,`endpoint_group_id`),
  KEY `FK_rj5nireuqqcr19cl770aivx5u` (`endpoint_group_id`),
  CONSTRAINT `FK_dk8mhmwthtwyie2xco1u90e19` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`),
  CONSTRAINT `FK_rj5nireuqqcr19cl770aivx5u` FOREIGN KEY (`endpoint_group_id`) REFERENCES `endpoint_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `endpoint_group_topic`
--

LOCK TABLES `endpoint_group_topic` WRITE;
/*!40000 ALTER TABLE `endpoint_group_topic` DISABLE KEYS */;
INSERT INTO `endpoint_group_topic` VALUES (1,5),(2,5),(3,6),(4,6);
/*!40000 ALTER TABLE `endpoint_group_topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_schems_versions`
--

DROP TABLE IF EXISTS `event_schems_versions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_schems_versions` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `schems` longtext,
  `version` int(11) DEFAULT NULL,
  `events_class_family_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_gwutixvuphw1m53qrwnhojsfh` (`events_class_family_id`),
  CONSTRAINT `FK_gwutixvuphw1m53qrwnhojsfh` FOREIGN KEY (`events_class_family_id`) REFERENCES `events_class_family` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_schems_versions`
--

LOCK TABLES `event_schems_versions` WRITE;
/*!40000 ALTER TABLE `event_schems_versions` DISABLE KEYS */;
INSERT INTO `event_schems_versions` VALUES (1,1461825551282,'admin','[\n{\n \"namespace\": \"org.kaaproject.kaa.schema.sample.event.thermo\",\n \"type\": \"record\",\n \"classType\": \"event\",\n \"name\": \"ThermostatInfoRequest\",\n \"fields\": []\n},\n{\n \"namespace\": \"org.kaaproject.kaa.schema.sample.event.thermo\",\n \"type\": \"record\",\n \"classType\": \"object\",\n \"name\": \"ThermostatInfo\",\n \"fields\": [\n    {\"name\": \"degree\", \"type\": [\"int\", \"null\"]},\n    {\"name\": \"targetDegree\", \"type\": [\"int\", \"null\"]},\n    {\"name\": \"isSetManually\", \"type\": [\"boolean\", \"null\"]}\n ]\n},\n{\n \"namespace\": \"org.kaaproject.kaa.schema.sample.event.thermo\",\n \"type\": \"record\",\n \"classType\": \"event\",\n \"name\": \"ThermostatInfoResponse\",\n \"fields\": [\n    {\"name\": \"thermostatInfo\", \"type\": [\"org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfo\", \"null\"]}\n  ]\n},\n{\n \"namespace\": \"org.kaaproject.kaa.schema.sample.event.thermo\",\n \"type\": \"record\",\n \"classType\": \"event\",\n \"name\": \"ChangeDegreeRequest\",\n \"fields\": [\n     {\"name\": \"degree\", \"type\": [\"int\", \"null\"]}\n ]\n}\n]',1,1),(2,1461825552450,'admin','[\n  {\n    \"type\":\"record\",\n    \"name\":\"MessageEvent\",\n    \"namespace\":\"org.kaaproject.kaa.demo.verifiersdemo\",\n    \"fields\":[\n      {\n        \"name\":\"message\",\n        \"type\":[\n          {\n            \"type\":\"string\",\n            \"avro.java.string\":\"String\"\n          },\n          \"null\"\n        ]\n      }\n    ],\n    \"classType\":\"event\"\n  }\n]\n',1,2),(3,1461825561552,'admin','[\n    {\n        \"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\n        \"type\":\"record\",\n        \"classType\":\"object\",\n        \"name\":\"GpioStatus\",\n        \"fields\":[\n            {\n                \"name\":\"id\",\n                \"type\":\"int\"\n            },\n            {\n                \"name\":\"status\",\n                \"type\":\"boolean\"\n            }\n        ]\n    },\n    {\n        \"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\n        \"type\":\"record\",\n        \"classType\":\"event\",\n        \"name\":\"DeviceInfoRequest\",\n        \"fields\":[\n\n        ]\n    },\n    {\n        \"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\n        \"type\":\"record\",\n        \"classType\":\"event\",\n        \"name\":\"DeviceInfoResponse\",\n        \"fields\":[\n            {\n                \"name\":\"model\",\n                \"type\":\"string\"\n            },\n            {\n                \"name\":\"deviceName\",\n                \"type\":\"string\"\n            },\n            {\n                \"name\":\"gpioStatus\",\n                \"type\":{\n                    \"type\":\"array\",\n                    \"items\":\"org.kaaproject.kaa.examples.gpiocontrol.GpioStatus\"\n                }\n            }\n        ]\n    },\n    {\n        \"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\n        \"type\":\"record\",\n        \"classType\":\"event\",\n        \"name\":\"GpioToggleRequest\",\n        \"fields\":[\n            {\n                \"name\":\"gpio\",\n                \"type\":\"org.kaaproject.kaa.examples.gpiocontrol.GpioStatus\"\n            }\n        ]\n    }\n]',1,3),(4,1461825567732,'admin','[\n   {\n      \"type\":\"record\",\n      \"name\":\"DeviceInfoRequest\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"DeviceInfo\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"model\",\n            \"type\":[\n               {\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n               },\n               \"null\"\n            ]\n         },\n         {\n            \"name\":\"manufacturer\",\n            \"type\":[\n               {\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n               },\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"object\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"DeviceInfoResponse\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"deviceInfo\",\n            \"type\":[\n               \"DeviceInfo\",\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"AlbumListRequest\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"AlbumInfo\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"title\",\n            \"type\":[\n               {\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n               },\n               \"null\"\n            ]\n         },\n         {\n            \"name\":\"imageCount\",\n            \"type\":[\n               \"int\",\n               \"null\"\n            ]\n         },\n         {\n            \"name\":\"bucketId\",\n            \"type\":[\n               {\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n               },\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"object\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"AlbumListResponse\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"albumList\",\n            \"type\":[\n               {\n                  \"type\":\"array\",\n                  \"items\":\"AlbumInfo\"\n               },\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"PlayAlbumRequest\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"bucketId\",\n            \"type\":[\n               {\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n               },\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"StopRequest\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"PlayInfoRequest\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n\n      ],\n      \"classType\":\"event\"\n   },\n   {\n      \"type\":\"enum\",\n      \"name\":\"PlayStatus\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"symbols\":[\n         \"PLAYING\",\n         \"STOPPED\"\n      ],\n      \"classType\":\"object\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"PlayInfo\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"currentAlbumInfo\",\n            \"type\":[       \n               \"AlbumInfo\",\n               \"null\"\n            ]\n         },\n         {\n            \"name\":\"status\",\n            \"type\":[\n               \"PlayStatus\",\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"object\"\n   },\n   {\n      \"type\":\"record\",\n      \"name\":\"PlayInfoResponse\",\n      \"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\n      \"fields\":[\n         {\n            \"name\":\"playInfo\",\n            \"type\":[\n               \"PlayInfo\",\n               \"null\"\n            ]\n         }\n      ],\n      \"classType\":\"event\"\n   }\n]',1,4);
/*!40000 ALTER TABLE `event_schems_versions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events_class`
--

DROP TABLE IF EXISTS `events_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `events_class` (
  `id` bigint(20) NOT NULL,
  `fqn` varchar(255) DEFAULT NULL,
  `schems` longtext,
  `type` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  `events_class_family_id` bigint(20) NOT NULL,
  `tenant_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_253bwlh0opf0w5xx0hutipp78` (`events_class_family_id`),
  KEY `FK_e46pa13gxjvs1p97r1xsn2kdk` (`tenant_id`),
  CONSTRAINT `FK_253bwlh0opf0w5xx0hutipp78` FOREIGN KEY (`events_class_family_id`) REFERENCES `events_class_family` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_e46pa13gxjvs1p97r1xsn2kdk` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events_class`
--

LOCK TABLES `events_class` WRITE;
/*!40000 ALTER TABLE `events_class` DISABLE KEYS */;
INSERT INTO `events_class` VALUES (1,'org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoRequest','{\"type\":\"record\",\"name\":\"ThermostatInfoRequest\",\"namespace\":\"org.kaaproject.kaa.schema.sample.event.thermo\",\"fields\":[],\"classType\":\"event\"}','EVENT',1,1,1),(2,'org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfo','{\"type\":\"record\",\"name\":\"ThermostatInfo\",\"namespace\":\"org.kaaproject.kaa.schema.sample.event.thermo\",\"fields\":[{\"name\":\"degree\",\"type\":[\"int\",\"null\"]},{\"name\":\"targetDegree\",\"type\":[\"int\",\"null\"]},{\"name\":\"isSetManually\",\"type\":[\"boolean\",\"null\"]}],\"classType\":\"object\"}','OBJECT',1,1,1),(3,'org.kaaproject.kaa.schema.sample.event.thermo.ThermostatInfoResponse','{\"type\":\"record\",\"name\":\"ThermostatInfoResponse\",\"namespace\":\"org.kaaproject.kaa.schema.sample.event.thermo\",\"fields\":[{\"name\":\"thermostatInfo\",\"type\":[{\"type\":\"record\",\"name\":\"ThermostatInfo\",\"fields\":[{\"name\":\"degree\",\"type\":[\"int\",\"null\"]},{\"name\":\"targetDegree\",\"type\":[\"int\",\"null\"]},{\"name\":\"isSetManually\",\"type\":[\"boolean\",\"null\"]}],\"classType\":\"object\"},\"null\"]}],\"classType\":\"event\"}','EVENT',1,1,1),(4,'org.kaaproject.kaa.schema.sample.event.thermo.ChangeDegreeRequest','{\"type\":\"record\",\"name\":\"ChangeDegreeRequest\",\"namespace\":\"org.kaaproject.kaa.schema.sample.event.thermo\",\"fields\":[{\"name\":\"degree\",\"type\":[\"int\",\"null\"]}],\"classType\":\"event\"}','EVENT',1,1,1),(5,'org.kaaproject.kaa.demo.verifiersdemo.MessageEvent','{\"type\":\"record\",\"name\":\"MessageEvent\",\"namespace\":\"org.kaaproject.kaa.demo.verifiersdemo\",\"fields\":[{\"name\":\"message\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"event\"}','EVENT',1,2,1),(6,'org.kaaproject.kaa.examples.gpiocontrol.GpioStatus','{\"type\":\"record\",\"name\":\"GpioStatus\",\"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"status\",\"type\":\"boolean\"}],\"classType\":\"object\"}','OBJECT',1,3,1),(7,'org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoRequest','{\"type\":\"record\",\"name\":\"DeviceInfoRequest\",\"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\"fields\":[],\"classType\":\"event\"}','EVENT',1,3,1),(8,'org.kaaproject.kaa.examples.gpiocontrol.DeviceInfoResponse','{\"type\":\"record\",\"name\":\"DeviceInfoResponse\",\"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\"fields\":[{\"name\":\"model\",\"type\":\"string\"},{\"name\":\"deviceName\",\"type\":\"string\"},{\"name\":\"gpioStatus\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"GpioStatus\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"status\",\"type\":\"boolean\"}],\"classType\":\"object\"}}}],\"classType\":\"event\"}','EVENT',1,3,1),(9,'org.kaaproject.kaa.examples.gpiocontrol.GpioToggleRequest','{\"type\":\"record\",\"name\":\"GpioToggleRequest\",\"namespace\":\"org.kaaproject.kaa.examples.gpiocontrol\",\"fields\":[{\"name\":\"gpio\",\"type\":{\"type\":\"record\",\"name\":\"GpioStatus\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"status\",\"type\":\"boolean\"}],\"classType\":\"object\"}}],\"classType\":\"event\"}','EVENT',1,3,1),(10,'org.kaaproject.kaa.demo.photoframe.DeviceInfoRequest','{\"type\":\"record\",\"name\":\"DeviceInfoRequest\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[],\"classType\":\"event\"}','EVENT',1,4,1),(11,'org.kaaproject.kaa.demo.photoframe.DeviceInfo','{\"type\":\"record\",\"name\":\"DeviceInfo\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"model\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"manufacturer\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"object\"}','OBJECT',1,4,1),(12,'org.kaaproject.kaa.demo.photoframe.DeviceInfoResponse','{\"type\":\"record\",\"name\":\"DeviceInfoResponse\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"deviceInfo\",\"type\":[{\"type\":\"record\",\"name\":\"DeviceInfo\",\"fields\":[{\"name\":\"model\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"manufacturer\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"object\"},\"null\"]}],\"classType\":\"event\"}','EVENT',1,4,1),(13,'org.kaaproject.kaa.demo.photoframe.AlbumListRequest','{\"type\":\"record\",\"name\":\"AlbumListRequest\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[],\"classType\":\"event\"}','EVENT',1,4,1),(14,'org.kaaproject.kaa.demo.photoframe.AlbumInfo','{\"type\":\"record\",\"name\":\"AlbumInfo\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"title\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"imageCount\",\"type\":[\"int\",\"null\"]},{\"name\":\"bucketId\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"object\"}','OBJECT',1,4,1),(15,'org.kaaproject.kaa.demo.photoframe.AlbumListResponse','{\"type\":\"record\",\"name\":\"AlbumListResponse\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"albumList\",\"type\":[{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"AlbumInfo\",\"fields\":[{\"name\":\"title\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"imageCount\",\"type\":[\"int\",\"null\"]},{\"name\":\"bucketId\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"object\"}},\"null\"]}],\"classType\":\"event\"}','EVENT',1,4,1),(16,'org.kaaproject.kaa.demo.photoframe.PlayAlbumRequest','{\"type\":\"record\",\"name\":\"PlayAlbumRequest\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"bucketId\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"event\"}','EVENT',1,4,1),(17,'org.kaaproject.kaa.demo.photoframe.StopRequest','{\"type\":\"record\",\"name\":\"StopRequest\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[],\"classType\":\"event\"}','EVENT',1,4,1),(18,'org.kaaproject.kaa.demo.photoframe.PlayInfoRequest','{\"type\":\"record\",\"name\":\"PlayInfoRequest\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[],\"classType\":\"event\"}','EVENT',1,4,1),(19,'org.kaaproject.kaa.demo.photoframe.PlayStatus','{\"type\":\"enum\",\"name\":\"PlayStatus\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"symbols\":[\"PLAYING\",\"STOPPED\"],\"classType\":\"object\"}','OBJECT',1,4,1),(20,'org.kaaproject.kaa.demo.photoframe.PlayInfo','{\"type\":\"record\",\"name\":\"PlayInfo\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"currentAlbumInfo\",\"type\":[{\"type\":\"record\",\"name\":\"AlbumInfo\",\"fields\":[{\"name\":\"title\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"imageCount\",\"type\":[\"int\",\"null\"]},{\"name\":\"bucketId\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"object\"},\"null\"]},{\"name\":\"status\",\"type\":[{\"type\":\"enum\",\"name\":\"PlayStatus\",\"symbols\":[\"PLAYING\",\"STOPPED\"],\"classType\":\"object\"},\"null\"]}],\"classType\":\"object\"}','OBJECT',1,4,1),(21,'org.kaaproject.kaa.demo.photoframe.PlayInfoResponse','{\"type\":\"record\",\"name\":\"PlayInfoResponse\",\"namespace\":\"org.kaaproject.kaa.demo.photoframe\",\"fields\":[{\"name\":\"playInfo\",\"type\":[{\"type\":\"record\",\"name\":\"PlayInfo\",\"fields\":[{\"name\":\"currentAlbumInfo\",\"type\":[{\"type\":\"record\",\"name\":\"AlbumInfo\",\"fields\":[{\"name\":\"title\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]},{\"name\":\"imageCount\",\"type\":[\"int\",\"null\"]},{\"name\":\"bucketId\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}],\"classType\":\"object\"},\"null\"]},{\"name\":\"status\",\"type\":[{\"type\":\"enum\",\"name\":\"PlayStatus\",\"symbols\":[\"PLAYING\",\"STOPPED\"],\"classType\":\"object\"},\"null\"]}],\"classType\":\"object\"},\"null\"]}],\"classType\":\"event\"}','EVENT',1,4,1);
/*!40000 ALTER TABLE `events_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events_class_family`
--

DROP TABLE IF EXISTS `events_class_family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `events_class_family` (
  `id` bigint(20) NOT NULL,
  `class_name` varchar(255) DEFAULT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `namespace` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_lsk6eeeeh7flqgyy4jvysk5qu` (`tenant_id`),
  CONSTRAINT `FK_lsk6eeeeh7flqgyy4jvysk5qu` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events_class_family`
--

LOCK TABLES `events_class_family` WRITE;
/*!40000 ALTER TABLE `events_class_family` DISABLE KEYS */;
INSERT INTO `events_class_family` VALUES (1,'ThermostatEventClassFamily',1461825550529,'admin',NULL,'Thermostat Event Class Family','org.kaaproject.kaa.schema.sample.event.thermo',1),(2,'VerifiersDemoEventClassFamily',1461825552071,'admin',NULL,'Verifiers Demo Event Class Family','org.kaaproject.kaa.demo.verifiersdemo',1),(3,'RemoteControlECF',1461825560452,'admin',NULL,'Remote Control Event Class Family','org.kaaproject.kaa.examples.gpiocontrol',1),(4,'PhotoFrameEventClassFamily',1461825566942,'admin',NULL,'Photo Frame Event Class Family','org.kaaproject.kaa.demo.photoframe',1);
/*!40000 ALTER TABLE `events_class_family` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `header_structure`
--

DROP TABLE IF EXISTS `header_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `header_structure` (
  `LogAppender_id` bigint(20) NOT NULL,
  `structure_field` varchar(255) NOT NULL,
  KEY `FK_qtu8w8xl5bhbminn6wsdca5w7` (`LogAppender_id`),
  CONSTRAINT `FK_qtu8w8xl5bhbminn6wsdca5w7` FOREIGN KEY (`LogAppender_id`) REFERENCES `log_appender` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `header_structure`
--

LOCK TABLES `header_structure` WRITE;
/*!40000 ALTER TABLE `header_structure` DISABLE KEYS */;
INSERT INTO `header_structure` VALUES (1,'KEYHASH'),(1,'TIMESTAMP'),(1,'TOKEN'),(1,'VERSION'),(9,'TIMESTAMP'),(10,'TIMESTAMP'),(11,'TIMESTAMP'),(15,'KEYHASH'),(15,'TIMESTAMP'),(15,'TOKEN'),(15,'VERSION');
/*!40000 ALTER TABLE `header_structure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequences`
--

DROP TABLE IF EXISTS `hibernate_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequences` (
  `sequence_name` varchar(255) DEFAULT NULL,
  `sequence_next_hi_value` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequences`
--

LOCK TABLES `hibernate_sequences` WRITE;
/*!40000 ALTER TABLE `hibernate_sequences` DISABLE KEYS */;
INSERT INTO `hibernate_sequences` VALUES ('tenant',1),('kaa_user',1),('application',1),('endpoint_group',1),('ctl_metainfo',1),('ctl',1),('base_schems',1),('schems',1),('abstract_structure',1),('history',1),('changes',1),('sdk_token',1),('topic',1),('plugin',1),('events_class_family',1),('event_schems_versions',1),('events_class',1),('application_event_family_map',1),('application_event_map',1);
/*!40000 ALTER TABLE `hibernate_sequences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `id` bigint(20) NOT NULL,
  `last_modify_time` bigint(20) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  `changes_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_eyxergb1sbprhre3qut0023fi` (`application_id`),
  KEY `FK_ha9grmepjf4bqbme96tcl6f6j` (`changes_id`),
  CONSTRAINT `FK_eyxergb1sbprhre3qut0023fi` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_ha9grmepjf4bqbme96tcl6f6j` FOREIGN KEY (`changes_id`) REFERENCES `changes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
INSERT INTO `history` VALUES (1,1461825539166,1,1,1),(2,1461825542303,2,1,2),(3,1461825542899,3,1,3),(4,1461825543199,4,1,4),(5,1461825543468,5,1,5),(6,1461825543713,6,1,6),(7,1461825543920,7,1,7),(8,1461825544427,1,2,8),(9,1461825545611,2,2,9),(10,1461825545866,3,2,10),(11,1461825546142,1,3,11),(12,1461825547006,2,3,12),(13,1461825547478,3,3,13),(14,1461825547829,1,4,14),(15,1461825548909,2,4,15),(16,1461825549092,3,4,16),(17,1461825549440,1,5,17),(18,1461825551340,1,6,18),(19,1461825552503,1,7,19),(20,1461825553242,2,7,20),(21,1461825553694,3,7,21),(22,1461825554082,1,8,22),(23,1461825555309,1,9,23),(24,1461825557552,1,10,24),(25,1461825559290,1,11,25),(26,1461825561592,1,12,26),(27,1461825561685,1,13,27),(28,1461825562891,1,14,28),(29,1461825564082,1,15,29),(30,1461825565521,2,15,30),(31,1461825565885,3,15,31),(32,1461825566169,4,15,32),(33,1461825566344,5,15,33),(34,1461825566571,6,15,34),(35,1461825566726,7,15,35),(36,1461825567790,1,16,36);
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kaa_user`
--

DROP TABLE IF EXISTS `kaa_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kaa_user` (
  `id` bigint(20) NOT NULL,
  `authority` varchar(255) DEFAULT NULL,
  `external_uid` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `tenant_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kf6art3d9xcj4sy91qegysgv` (`tenant_id`,`external_uid`),
  CONSTRAINT `FK_nwwouvn5xp4svu0yvf0du1scj` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kaa_user`
--

LOCK TABLES `kaa_user` WRITE;
/*!40000 ALTER TABLE `kaa_user` DISABLE KEYS */;
INSERT INTO `kaa_user` VALUES (1,'TENANT_ADMIN','2','admin',1),(2,'TENANT_DEVELOPER','3','devuser',1);
/*!40000 ALTER TABLE `kaa_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `log_appender`
--

DROP TABLE IF EXISTS `log_appender`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_appender` (
  `confirm_delivery` bit(1) DEFAULT NULL,
  `max_log_schems_version` int(11) DEFAULT NULL,
  `min_log_schems_version` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_is9fjprh3841xd5vtbhe53av2` FOREIGN KEY (`id`) REFERENCES `plugin` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `log_appender`
--

LOCK TABLES `log_appender` WRITE;
/*!40000 ALTER TABLE `log_appender` DISABLE KEYS */;
INSERT INTO `log_appender` VALUES ('',2147483647,1,1),('',2147483647,1,6),('',2147483647,1,7),('',2147483647,1,8),('',2147483647,1,9),('',2147483647,1,10),('',2147483647,1,11),('',2147483647,1,12),('',2147483647,1,15);
/*!40000 ALTER TABLE `log_appender` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `log_schems`
--

DROP TABLE IF EXISTS `log_schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log_schems` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_qy3uck42ennaufnw01xluusrc` FOREIGN KEY (`id`) REFERENCES `schems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `log_schems`
--

LOCK TABLES `log_schems` WRITE;
/*!40000 ALTER TABLE `log_schems` DISABLE KEYS */;
INSERT INTO `log_schems` VALUES (3),(7),(11),(15),(19),(20),(23),(26),(30),(31),(34),(35),(38),(39),(42),(43),(46),(49),(52),(53),(56),(60);
/*!40000 ALTER TABLE `log_schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_schems`
--

DROP TABLE IF EXISTS `notification_schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_schems` (
  `type` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_jxs923lnfincmt0htt0hf8fpw` FOREIGN KEY (`id`) REFERENCES `schems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_schems`
--

LOCK TABLES `notification_schems` WRITE;
/*!40000 ALTER TABLE `notification_schems` DISABLE KEYS */;
INSERT INTO `notification_schems` VALUES (0,2),(0,6),(0,10),(0,12),(0,14),(0,16),(0,18),(0,22),(0,25),(0,29),(0,33),(0,37),(0,41),(0,45),(0,48),(0,51),(0,55),(0,59);
/*!40000 ALTER TABLE `notification_schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plugin`
--

DROP TABLE IF EXISTS `plugin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `plugin` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `plugin_class_name` varchar(255) DEFAULT NULL,
  `plugin_type_name` varchar(255) DEFAULT NULL,
  `raw_configuration` longblob,
  `application_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_9urxlnistpbq4ppw4i6x5d9hj` (`application_id`),
  CONSTRAINT `FK_9urxlnistpbq4ppw4i6x5d9hj` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plugin`
--

LOCK TABLES `plugin` WRITE;
/*!40000 ALTER TABLE `plugin` DISABLE KEYS */;
INSERT INTO `plugin` VALUES (1,1461825550228,'devuser','Log appender used to deliver log records from data collection application to local mongo db instance','Data collection log appender','org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender','MongoDB','localhost��\0\0kaa\0<\0��\0�N\0\0\0\0\0\0\0\0',5),(2,1461825551855,'devuser',NULL,'Trustful verifier','org.kaaproject.kaa.server.verifiers.trustful.verifier.TrustfulUserVerifier','Trustful verifier','',6),(3,1461825553305,'devuser',NULL,'Twitter verifier','org.kaaproject.kaa.server.verifiers.twitter.verifier.TwitterUserVerifier','Twitter verifier','201Y9gbsMeGPetye1w9kkNvNMidg4Pwh51o7SQlhd3RL6inNF3VxixBURAJDZc494uSISF7yOyJjc\nvhttps://api.twitter.com/1.1/account/verify_credentials.json',7),(4,1461825553374,'devuser',NULL,'Facebook verifier','org.kaaproject.kaa.server.verifiers.facebook.verifier.FacebookUserVerifier','Facebook verifier',' 1557997434440423@8ff17981ea0cdad3fe387a55c91aa71b\n',7),(5,1461825553439,'devuser',NULL,'Google+ verifier','org.kaaproject.kaa.server.verifiers.gplus.verifier.GplusUserVerifier','Google+ verifier','\n��',7),(6,1461825554726,'devuser','Panel per row Cassandra log appender.','Panel per row appender','org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender','Cassandra','localhost��\0logs\Zpanel_per_row\0zoneIdzone_id\0\0timestamptimestamp\0\0\0panelIdpanel_id\0\0\npower\npower\0\0\0timestamp\0\0\0\0\0\0\0\0\0',8),(7,1461825554816,'devuser','Zone per row Cassandra log appender.','Zone per row appender','org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender','Cassandra','localhost��\0logszone_per_row\0zoneIdzone_Id\0\0timestamptimestamp\0\0\0panelIdpanel_Id\0\0\npower\npower\0\0\0timestamp\0\0\0\0\0\0\0\0\0',8),(8,1461825556885,'devuser','Storm data analytics demo log appender','Storm data analytics demo log appender','org.kaaproject.kaa.server.appenders.flume.appender.FlumeLogAppender','Flume','\0\0\0\0\0\0localhost��\0',9),(9,1461825558679,'devuser','Sensor per row Cassandra log appender.','sensor_per_row','org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender','Cassandra','localhost��\0kaasensor_per_row\0sensorIdsensor_Id\0\0\0timestampts\0\0\0\nmodel\nmodel\0\0\0regionregion\0\0\0\nvalue\nvalue\0\0\n\0\0event_json\0\0\0\0event_binary\0\0\0ts\0\0\0\0\0\0\0\0',10),(10,1461825558846,'devuser','Sensor per date Cassandra log appender.','sensor_per_date','org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender','Cassandra','localhost��\0kaasensor_per_date\0sensorIdsensor_id\0\Z\0YYYYMMdddate\0\0\0timestampts\0\0\0\nmodel\nmodel\0\0\0regionregion\0\0\0\nvalue\nvalue\0\0\0ts\0\0\0\0\0\0\0\0',10),(11,1461825558973,'devuser','Sensor per region Cassandra log appender.','sensor_per_region','org.kaaproject.kaa.server.appenders.cassandra.appender.CassandraLogAppender','Cassandra','localhost��\0kaa\"sensor_per_region\n\0regionregion\0\Z\0YYYY/MM/ddHH:mmdate\0\0\0timestampts\0\0\0sensorIdsensor_id\0\0\nvalue\nvalue\0\0\0ts\0sensor_id\0\0\0��z\0\0\0\0\0',10),(12,1461825559995,'devuser','Flume log appender','Flume appender','org.kaaproject.kaa.server.appenders.flume.appender.FlumeLogAppender','Flume','\0\0\0\0\0\0localhost�n\0',11),(13,1461825561939,'devuser',NULL,'Trustful verifier','org.kaaproject.kaa.server.verifiers.trustful.verifier.TrustfulUserVerifier','Trustful verifier','',12),(14,1461825562113,'devuser',NULL,'Trustful verifier','org.kaaproject.kaa.server.verifiers.trustful.verifier.TrustfulUserVerifier','Trustful verifier','',13),(15,1461825563550,'devuser','Log appender used to deliver log records from cell monitor application to local mongo db instance','Cell monitor log appender','org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender','MongoDB','localhost��\0\0kaa\0<\0��\0�N\0\0\0\0\0\0\0\0',14),(16,1461825568153,'devuser',NULL,'Trustful verifier','org.kaaproject.kaa.server.verifiers.trustful.verifier.TrustfulUserVerifier','Trustful verifier','',16);
/*!40000 ALTER TABLE `plugin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_filter`
--

DROP TABLE IF EXISTS `profile_filter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_filter` (
  `profile_filter_body` varchar(255) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `endpoint_schems_id` bigint(20) DEFAULT NULL,
  `server_schems_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_44uw0i94fh2vk4llys30fdppb` (`endpoint_schems_id`),
  KEY `FK_pigj778qg4rknexptifpl7bik` (`server_schems_id`),
  CONSTRAINT `FK_44uw0i94fh2vk4llys30fdppb` FOREIGN KEY (`endpoint_schems_id`) REFERENCES `profile_schems` (`id`),
  CONSTRAINT `FK_pigj778qg4rknexptifpl7bik` FOREIGN KEY (`server_schems_id`) REFERENCES `server_profile_schems` (`id`),
  CONSTRAINT `FK_t2juyogk8wd20g5x54oafrn9e` FOREIGN KEY (`id`) REFERENCES `abstract_structure` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_filter`
--

LOCK TABLES `profile_filter` WRITE;
/*!40000 ALTER TABLE `profile_filter` DISABLE KEYS */;
INSERT INTO `profile_filter` VALUES ('#sp.active != null and #sp.active == true',5,NULL,3),('#sp.active == null or #sp.active == false',7,NULL,3),('(area == null or area.equals(\"North America\")) and (city == null or city.equals(\"Atlanta\"))',29,32,NULL),('(area == null or area.equals(\"Europe\")) and (city == null or city.equals(\"Amsterdam\"))',31,32,NULL);
/*!40000 ALTER TABLE `profile_filter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_schems`
--

DROP TABLE IF EXISTS `profile_schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_schems` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_rwt514hdwy7bnib4kwo2dh9ny` FOREIGN KEY (`id`) REFERENCES `base_schems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_schems`
--

LOCK TABLES `profile_schems` WRITE;
/*!40000 ALTER TABLE `profile_schems` DISABLE KEYS */;
INSERT INTO `profile_schems` VALUES (1),(4),(6),(8),(10),(12),(14),(16),(18),(20),(22),(24),(26),(28),(30),(32),(33);
/*!40000 ALTER TABLE `profile_schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schems`
--

DROP TABLE IF EXISTS `schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schems` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `endpoint_count` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `schems` longtext,
  `version` int(11) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_no26f6n6hwan47ijhnxxp7isu` (`application_id`),
  CONSTRAINT `FK_no26f6n6hwan47ijhnxxp7isu` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schems`
--

LOCK TABLES `schems` WRITE;
/*!40000 ALTER TABLE `schems` DISABLE KEYS */;
INSERT INTO `schems` VALUES (1,1461825539085,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,1),(2,1461825539220,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,1),(3,1461825539245,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,1),(4,1461825542262,'devuser','Configuration schema describing active and inactive devices used by city guide application',0,'Endpoint activation configuration schema','{\n  \"type\" : \"record\",\n  \"name\" : \"DeviceType\",\n  \"namespace\" : \"org.kaaproject.kaa.demo.activation\",\n  \"fields\" : [ {\n    \"name\" : \"active\",\n    \"type\" : \"boolean\",\n    \"by_default\": false\n  } ]\n}\n',2,1),(5,1461825544397,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,2),(6,1461825544447,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,2),(7,1461825544456,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,2),(8,1461825545575,'devuser','Default configuration schema for the configuration demo application',0,'ConfigurationDemo schema','{\n  \"type\":\"record\",\n  \"name\":\"SampleConfiguration\",\n  \"namespace\":\"org.kaaproject.kaa.demo.configuration\",\n  \"fields\":[\n    {\n      \"name\":\"AddressList\",\n      \"type\":[\n        {\n          \"type\":\"array\",\n          \"items\":{\n            \"type\":\"record\",\n            \"name\":\"Link\",\n            \"fields\":[\n              {\n                \"name\":\"label\",\n                \"type\":{\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n                },\n                \"displayName\":\"Site label\"\n              },\n              {\n                \"name\":\"url\",\n                \"type\":{\n                  \"type\":\"string\",\n                  \"avro.java.string\":\"String\"\n                },\n                \"displayName\":\"Site URL\"\n              }\n            ],\n            \"displayName\":\"Site address\"\n          }\n        },\n        \"null\"\n      ],\n      \"displayName\":\"URLs list\"\n    }\n  ]\n}\n',2,2),(9,1461825546111,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,3),(10,1461825546161,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,3),(11,1461825546177,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,3),(12,1461825546777,'devuser','Notification schema of a sample notification',0,'Notification schema','{\n  \"type\":\"record\",\n  \"name\":\"SampleNotification\",\n  \"namespace\":\"org.kaaproject.kaa.demo.notification\",\n  \"fields\":[\n    {\n      \"name\":\"message\",\n      \"type\":[\n        {\n          \"type\":\"string\",\n          \"avro.java.string\":\"String\"\n        },\n        \"null\"\n      ]\n    }\n  ],\n  \"displayName\":\"Sample notification to demonstrate API\"\n}\n',2,3),(13,1461825547799,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,4),(14,1461825547840,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,4),(15,1461825547849,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,4),(16,1461825548711,'devuser','Notification schema of a sample notification',0,'Notification schema','{\n  \"type\": \"record\",\n  \"name\": \"Notification\",\n  \"namespace\": \"org.kaaproject.kaa.schema.example\",\n  \"fields\": [\n    {\n      \"name\": \"message\",\n      \"type\": [\n        {\n          \"type\": \"string\",\n          \"avro.java.string\": \"String\"\n        },\n        \"null\"\n      ]\n    },\n    {\n      \"name\": \"image\",\n      \"type\": [\n        {\n          \"type\": \"string\",\n          \"avro.java.string\": \"String\"\n        },\n        \"null\"\n      ]\n    }\n  ]\n}',2,4),(17,1461825549425,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,5),(18,1461825549457,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,5),(19,1461825549460,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,5),(20,1461825550141,'devuser','Log schema describing incoming logs',0,'Log schema','{\n  \"type\":\"record\",\n  \"name\":\"LogData\",\n  \"namespace\":\"org.kaaproject.kaa.schema.sample.logging\",\n  \"fields\":[\n    {\n      \"name\":\"level\",\n      \"type\":{\n        \"type\":\"enum\",\n        \"name\":\"Level\",\n        \"symbols\":[\n          \"KAA_DEBUG\",\n          \"KAA_ERROR\",\n          \"KAA_FATAL\",\n          \"KAA_INFO\",\n          \"KAA_TRACE\",\n          \"KAA_WARN\"\n        ]\n      }\n    },\n    {\n      \"name\":\"tag\",\n      \"type\":\"string\"\n    },\n    {\n      \"name\":\"message\",\n      \"type\":\"string\"\n    },\n    {\n      \"name\" : \"timeStamp\",\n      \"type\" : \"long\"\n    }\n  ]\n}',2,5),(21,1461825551318,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,6),(22,1461825551353,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,6),(23,1461825551360,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,6),(24,1461825552480,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,7),(25,1461825552515,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,7),(26,1461825552526,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,7),(27,1461825553181,'devuser','Configuration schema for the default Kaa verifiers tokens',0,'KaaVerifiersTokens schema','{\n  \"type\":\"record\",\n  \"name\":\"KaaVerifiersTokens\",\n  \"namespace\":\"org.kaaproject.kaa.demo.verifiersdemo\",\n  \"fields\":[\n    {\n      \"name\":\"twitterKaaVerifierToken\",\n      \"type\":[\n        {\n          \"type\":\"string\",\n          \"avro.java.string\":\"String\"\n        },\n        \"null\"\n      ]\n    },\n    {\n      \"name\":\"facebookKaaVerifierToken\",\n      \"type\":[\n        {\n          \"type\":\"string\",\n          \"avro.java.string\":\"String\"\n        },\n        \"null\"\n      ]\n    },\n    {\n      \"name\":\"googleKaaVerifierToken\",\n      \"type\":[\n        {\n          \"type\":\"string\",\n          \"avro.java.string\":\"String\"\n        },\n        \"null\"\n      ]\n    }\n  ]\n}\n',2,7),(28,1461825554061,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,8),(29,1461825554096,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,8),(30,1461825554100,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,8),(31,1461825554664,'devuser','Zeppelin data analytics demo Power report log schema',0,'Power report','{\n    \"type\":\"record\",\n    \"name\":\"PowerReport\",\n    \"namespace\":\"org.kaaproject.kaa.sample\",\n    \"fields\":[\n        {\n            \"name\":\"timestamp\",\n            \"type\":\"long\"\n        },\n        {\n            \"name\":\"zoneId\",\n            \"type\":{\n                \"type\":\"string\",\n                \"avro.java.string\":\"String\"\n            }\n        },\n        {\n            \"name\":\"panelId\",\n            \"type\":{\n                \"type\":\"string\",\n                \"avro.java.string\":\"String\"\n            }\n        },\n        {\n            \"name\":\"power\",\n            \"type\":\"double\"\n        }\n    ]\n}\n',2,8),(32,1461825555275,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,9),(33,1461825555333,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,9),(34,1461825555352,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,9),(35,1461825556799,'devuser','Storm demo log schema',0,'Power report','{\n    \"type\":\"record\",\n    \"name\":\"PowerReport\",\n    \"namespace\":\"org.kaaproject.kaa.examples.powerplant\",\n    \"fields\":[\n        {\n            \"name\":\"timestamp\",\n            \"type\":\"long\"\n        },\n        {\n            \"name\":\"samples\",\n            \"type\":{\n                \"type\":\"array\",\n                \"items\":{\n                    \"type\":\"record\",\n                    \"name\":\"PowerSample\",\n                    \"namespace\":\"org.kaaproject.kaa.examples.powerplant\",\n                    \"fields\":[\n                        {\n                            \"name\":\"zoneId\",\n                            \"type\":\"int\"\n                        },\n                        {\n                            \"name\":\"panelId\",\n                            \"type\":\"int\"\n                        },\n                        {\n                            \"name\":\"power\",\n                            \"type\":\"double\"\n                        }\n                    ]\n                }\n            }\n        }\n    ]\n}\n',2,9),(36,1461825557515,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,10),(37,1461825557577,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,10),(38,1461825557582,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,10),(39,1461825558598,'devuser','Log schema describing incoming logs',0,'SensorData','{\n    \"type\":\"record\",\n    \"name\":\"SensorData\",\n    \"namespace\":\"org.kaaproject.kaa.sample\",\n    \"fields\":[\n        {\n            \"name\":\"sensorId\",\n            \"type\":\"string\"\n        },\n        {\n            \"name\":\"model\",\n            \"type\":\"string\"\n        },\n        {\n            \"name\":\"region\",\n            \"type\":\"string\"\n        },\n        {\n            \"name\":\"value\",\n            \"type\":\"float\"\n        }\n    ]\n}',2,10),(40,1461825559274,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,11),(41,1461825559300,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,11),(42,1461825559311,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,11),(43,1461825559936,'devuser','Spark data analytics demo log schema',0,'Power report','{\n    \"type\":\"record\",\n    \"name\":\"PowerReport\",\n    \"namespace\":\"org.kaaproject.kaa.examples.powerplant\",\n    \"fields\":[\n        {\n            \"name\":\"timestamp\",\n            \"type\":\"long\"\n        },\n        {\n            \"name\":\"samples\",\n            \"type\":{\n                \"type\":\"array\",\n                \"items\":{\n                    \"type\":\"record\",\n                    \"name\":\"PowerSample\",\n                    \"namespace\":\"org.kaaproject.kaa.examples.powerplant\",\n                    \"fields\":[\n                        {\n                            \"name\":\"zoneId\",\n                            \"type\":\"int\"\n                        },\n                        {\n                            \"name\":\"panelId\",\n                            \"type\":\"int\"\n                        },\n                        {\n                            \"name\":\"power\",\n                            \"type\":\"double\"\n                        }\n                    ]\n                }\n            }\n        }\n    ]\n}\n',2,11),(44,1461825561578,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,12),(45,1461825561602,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,12),(46,1461825561609,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,12),(47,1461825561668,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,13),(48,1461825561700,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,13),(49,1461825561708,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,13),(50,1461825562857,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,14),(51,1461825562903,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,14),(52,1461825562915,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,14),(53,1461825563498,'devuser','Log schema describing cell monitor record with information about current cell location, signal strength and phone gps location.',0,'Cell monitor log schema','{\n   \"type\":\"record\",\n   \"name\":\"CellMonitorLog\",\n   \"namespace\":\"org.kaaproject.kaa.demo.cellmonitor\",\n   \"fields\":[\n      {\n         \"name\":\"logTime\",\n         \"type\":\"long\"\n      },\n      {\n         \"name\":\"networkOperatorCode\",\n         \"type\":\"int\"\n      },\n      {\n         \"name\":\"networkOperatorName\",\n         \"type\":{\n            \"type\":\"string\",\n            \"avro.java.string\":\"String\"\n         }\n      },\n      {\n         \"name\":\"gsmCellId\",\n         \"type\":\"int\"\n      },\n      {\n         \"name\":\"gsmLac\",\n         \"type\":\"int\"\n      },\n      {\n         \"name\":\"signalStrength\",\n         \"type\":\"int\"\n      },\n      {\n         \"name\":\"phoneGpsLocation\",\n         \"type\":{\n            \"type\":\"record\",\n            \"name\":\"Location\",\n            \"fields\":[\n               {\n                  \"name\":\"latitude\",\n                  \"type\":\"double\"\n               },\n               {\n                  \"name\":\"longitude\",\n                  \"type\":\"double\"\n               }\n            ]\n         }\n      }\n   ]\n}',2,14),(54,1461825564052,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,15),(55,1461825564113,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,15),(56,1461825564125,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,15),(57,1461825565460,'devuser','Configuration schema describing cities and places used by city guide application',0,'City guide configuration schema','{\n  \"type\" : \"record\",\n  \"name\" : \"CityGuideConfig\",\n  \"namespace\" : \"org.kaaproject.kaa.demo.cityguide\",\n  \"fields\" : [ {\n    \"name\" : \"availableAreas\",\n    \"type\" : {\n      \"type\" : \"array\",\n      \"items\" : {\n        \"type\" : \"record\",\n        \"name\" : \"AvailableArea\",\n        \"fields\" : [ {\n          \"name\" : \"name\",\n          \"type\" : \"string\",\n          \"displayName\" : \"Area name\",\n          \"displayPrompt\" : \"Enter area name\"\n        }, {\n          \"name\" : \"availableCities\",\n          \"type\" : {\n            \"type\" : \"array\",\n            \"items\" : \"string\"\n          },\n          \"displayName\" : \"Available cities\"\n        } ],\n        \"displayName\" : \"Available area\"\n      }\n    },\n    \"displayName\" : \"Available areas\"\n  }, {\n    \"name\" : \"areas\",\n    \"type\" : {\n      \"type\" : \"array\",\n      \"items\" : {\n        \"type\" : \"record\",\n        \"name\" : \"Area\",\n        \"fields\" : [ {\n          \"name\" : \"name\",\n          \"type\" : \"string\",\n          \"displayName\" : \"Area name\",\n          \"displayPrompt\" : \"Enter area name\",\n          \"inputType\" : \"plain\"\n        }, {\n          \"name\" : \"cities\",\n          \"type\" : {\n            \"type\" : \"array\",\n            \"items\" : {\n              \"type\" : \"record\",\n              \"name\" : \"City\",\n              \"fields\" : [ {\n                \"name\" : \"name\",\n                \"type\" : \"string\",\n                \"displayName\" : \"City name\",\n                \"displayPrompt\" : \"Enter city name\"\n              }, {\n                \"name\" : \"places\",\n                \"type\" : {\n                  \"type\" : \"array\",\n                  \"items\" : {\n                    \"type\" : \"record\",\n                    \"name\" : \"Place\",\n                    \"fields\" : [ {\n                      \"name\" : \"category\",\n                      \"type\" : {\n                        \"type\" : \"enum\",\n                        \"name\" : \"Category\",\n                        \"symbols\" : [ \"HOTEL\", \"SHOP\", \"MUSEUM\", \"RESTAURANT\" ]\n                      },\n                      \"displayName\" : \"Category\",\n                      \"displayPrompt\" : \"Select place category\",\n                      \"weight\" : 0.30000001192092896,\n                      \"keyIndex\" : 1,\n                      \"displayNames\" : [ \"Hotel\", \"Shop\", \"Museum\", \"Restaurant\" ]\n                    }, {\n                      \"name\" : \"title\",\n                      \"type\" : \"string\",\n                      \"displayName\" : \"Title\",\n                      \"displayPrompt\" : \"Enter place title\",\n                      \"weight\" : 0.699999988079071,\n                      \"keyIndex\" : 2,\n                      \"inputType\" : \"plain\"\n                    }, {\n                      \"name\" : \"photoUrl\",\n                      \"type\" : \"string\",\n                      \"displayName\" : \"Photo URL\",\n                      \"displayPrompt\" : \"Enter place photo url\",\n                      \"inputType\" : \"plain\"\n                    }, {\n                      \"name\" : \"description\",\n                      \"type\" : \"string\",\n                      \"displayName\" : \"Description\",\n                      \"displayPrompt\" : \"Enter place description\",\n                      \"inputType\" : \"plain\"\n                    }, {\n                      \"name\" : \"location\",\n                      \"type\" : {\n                        \"type\" : \"record\",\n                        \"name\" : \"Location\",\n                        \"fields\" : [ {\n                          \"name\" : \"latitude\",\n                          \"type\" : \"double\",\n                          \"displayName\" : \"Latitude\",\n                          \"displayPrompt\" : \"Enter latitude\"\n                        }, {\n                          \"name\" : \"longitude\",\n                          \"type\" : \"double\",\n                          \"displayName\" : \"Longitude\",\n                          \"displayPrompt\" : \"Enter longitude\"\n                        } ],\n                        \"displayName\" : \"Location\"\n                      },\n                      \"displayName\" : \"Location\"\n                    } ],\n                    \"displayName\" : \"City place\"\n                  }\n                },\n                \"displayName\" : \"Places\"\n              } ]\n            }\n          },\n          \"displayName\" : \"Cities\"\n        } ]\n      }\n    },\n    \"displayName\" : \"Areas\",\n    \"overrideStrategy\":\"append\"\n  } ],\n  \"displayName\" : \"City guide config\"\n}',2,15),(58,1461825567773,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Configuration\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,16),(59,1461825567804,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Notification\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,16),(60,1461825567812,'admin',NULL,0,'Generated','{\n    \"type\": \"record\",\n    \"name\": \"Log\",\n    \"namespace\": \"org.kaaproject.kaa.schema.base\",\n    \"fields\": []\n}',1,16);
/*!40000 ALTER TABLE `schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sdk_token`
--

DROP TABLE IF EXISTS `sdk_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sdk_token` (
  `id` bigint(20) NOT NULL,
  `configuration_schems_version` int(11) DEFAULT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `default_verifier_token` varchar(255) DEFAULT NULL,
  `endpoint_count` int(11) DEFAULT NULL,
  `log_schems_version` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `notification_schems_version` int(11) DEFAULT NULL,
  `profile_schems_version` int(11) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_jlbhe1m3k30f7y6pymylegf3p` (`application_id`),
  CONSTRAINT `FK_jlbhe1m3k30f7y6pymylegf3p` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sdk_token`
--

LOCK TABLES `sdk_token` WRITE;
/*!40000 ALTER TABLE `sdk_token` DISABLE KEYS */;
INSERT INTO `sdk_token` VALUES (1,2,1461825544140,'devuser',NULL,0,1,NULL,1,0,'7SbuQENOsunG1joR58hLDLoNvqY',1),(2,2,1461825545944,'devuser',NULL,0,1,NULL,1,0,'ZJyeFywCv7lv7XITXlwHpkZwFMo',2),(3,1,1461825549205,'devuser',NULL,0,1,NULL,2,0,'PLZZ0Erj5tyJfNy_qxGgp59HTbo',3),(4,1,1461825549268,'devuser',NULL,0,1,NULL,2,0,'FGMjC8ayctXgea_Fy75BVWuphaI',4),(5,1,1461825550333,'devuser',NULL,0,2,NULL,1,0,'pBzrHw-nMiTyEFq2-VRnz3wL-Go',5),(6,1,1461825551911,'devuser','98517974622127974493',0,1,NULL,1,0,'tiFq5zPninjPs9jWwRh-KyvqU-8',6),(7,2,1461825553767,'devuser',NULL,0,1,NULL,1,0,'r7qNVC2I6jXSV2JP5NbKzez_bOg',7),(8,1,1461825554920,'devuser',NULL,0,2,NULL,1,0,'WeCejkHhirnyYXv3KZ1xvAEe6P4',8),(9,1,1461825556993,'devuser',NULL,0,2,NULL,1,0,'EattdVoSCptjr4X2LnCGhHEt_rE',9),(10,1,1461825559071,'devuser',NULL,0,2,NULL,1,0,'wcknkllgn6TIg3OjUlumWqcZSCw',10),(11,1,1461825560103,'devuser',NULL,0,2,NULL,1,0,'MGyvT9L3uN7QIdkgN2ybUz5iCC4',11),(12,1,1461825562334,'devuser','37829401801592754521',0,1,NULL,1,0,'6a4aVRjhxJ6P1HEbdJ5XYJMRCKo',12),(13,1,1461825562406,'devuser','69402418612762382487',0,1,NULL,1,0,'jZMrieHfgZDuFKWHbJP-CwxtWd4',13),(14,1,1461825563623,'devuser',NULL,0,2,NULL,1,0,'O5ujRyR6AzcMbofZMh1ElWjXzQE',14),(15,2,1461825566793,'devuser',NULL,0,1,NULL,1,1,'zGynEctvWnZhhuWD02rnAhikz5Y',15),(16,1,1461825568231,'devuser','17786535462961380534',0,1,NULL,1,0,'E-FWf1OrIzbo5XAACVXNkTg1avQ',16);
/*!40000 ALTER TABLE `sdk_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sdkprofile_aefmapids`
--

DROP TABLE IF EXISTS `sdkprofile_aefmapids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sdkprofile_aefmapids` (
  `SdkProfile_id` bigint(20) NOT NULL,
  `aefMapIds` varchar(255) DEFAULT NULL,
  KEY `FK_ko8wc5lu38r1hgagjmv8w4j4t` (`SdkProfile_id`),
  CONSTRAINT `FK_ko8wc5lu38r1hgagjmv8w4j4t` FOREIGN KEY (`SdkProfile_id`) REFERENCES `sdk_token` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sdkprofile_aefmapids`
--

LOCK TABLES `sdkprofile_aefmapids` WRITE;
/*!40000 ALTER TABLE `sdkprofile_aefmapids` DISABLE KEYS */;
INSERT INTO `sdkprofile_aefmapids` VALUES (6,'1'),(7,'2'),(12,'3'),(13,'4'),(16,'5');
/*!40000 ALTER TABLE `sdkprofile_aefmapids` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `server_profile_schems`
--

DROP TABLE IF EXISTS `server_profile_schems`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `server_profile_schems` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_6wjj3y9qvefue8ncc4nrdo24k` FOREIGN KEY (`id`) REFERENCES `base_schems` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `server_profile_schems`
--

LOCK TABLES `server_profile_schems` WRITE;
/*!40000 ALTER TABLE `server_profile_schems` DISABLE KEYS */;
INSERT INTO `server_profile_schems` VALUES (2),(3),(5),(7),(9),(11),(13),(15),(17),(19),(21),(23),(25),(27),(29),(31),(34);
/*!40000 ALTER TABLE `server_profile_schems` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenant`
--

DROP TABLE IF EXISTS `tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tenant` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_dcxf3ksi0gyn1tieeq0id96lm` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant`
--

LOCK TABLES `tenant` WRITE;
/*!40000 ALTER TABLE `tenant` DISABLE KEYS */;
INSERT INTO `tenant` VALUES (1,'Demo Tenant');
/*!40000 ALTER TABLE `tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `topic`
--

DROP TABLE IF EXISTS `topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topic` (
  `id` bigint(20) NOT NULL,
  `created_time` bigint(20) DEFAULT NULL,
  `created_username` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `sequence_number` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `application_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1xe10jw4ag452iy0sxi6t01jw` (`name`,`application_id`),
  KEY `FK_67pt395diacy1os1c4h08gm51` (`application_id`),
  CONSTRAINT `FK_67pt395diacy1os1c4h08gm51` FOREIGN KEY (`application_id`) REFERENCES `application` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `topic`
--

LOCK TABLES `topic` WRITE;
/*!40000 ALTER TABLE `topic` DISABLE KEYS */;
INSERT INTO `topic` VALUES (1,1461825546923,'devuser','Sample mandatory topic to demonstrate notifications API','Sample mandatory topic',1,'MANDATORY',3),(2,1461825547402,'devuser','Sample optional topic to demonstrate notifications API','Sample optional topic',1,'OPTIONAL',3),(3,1461825548834,'devuser','Sample mandatory topic to demonstrate notifications API','Sample mandatory topic',1,'MANDATORY',4),(4,1461825549021,'devuser','Sample optional topic to demonstrate notifications API','Sample optional topic',1,'OPTIONAL',4);
/*!40000 ALTER TABLE `topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_verifier`
--

DROP TABLE IF EXISTS `user_verifier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_verifier` (
  `verifier_token` varchar(255) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_ghpj2j19qtpvhllhjwgcrwmtd` FOREIGN KEY (`id`) REFERENCES `plugin` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_verifier`
--

LOCK TABLES `user_verifier` WRITE;
/*!40000 ALTER TABLE `user_verifier` DISABLE KEYS */;
INSERT INTO `user_verifier` VALUES ('98517974622127974493',2),('28824347509112399479',3),('46211075959774832208',4),('98934406053807597448',5),('37829401801592754521',13),('69402418612762382487',14),('17786535462961380534',16);
/*!40000 ALTER TABLE `user_verifier` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-08-01  3:23:12

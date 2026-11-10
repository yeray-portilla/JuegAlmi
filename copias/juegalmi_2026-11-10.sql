/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.4.13-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: app
-- ------------------------------------------------------
-- Server version	11.4.13-MariaDB-ubu2404

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `alquiler`
--

DROP TABLE IF EXISTS `alquiler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `alquiler` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `usuario_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_655BED39DB38439E` (`usuario_id`),
  CONSTRAINT `FK_655BED39DB38439E` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alquiler`
--

LOCK TABLES `alquiler` WRITE;
/*!40000 ALTER TABLE `alquiler` DISABLE KEYS */;
INSERT INTO `alquiler` VALUES
(1,43.2631000,-2.9350000,'finalizado','2026-10-01','2026-10-08',2),
(2,43.2631000,-2.9350000,'activo','2026-10-30','2026-11-06',3),
(3,43.2631000,-2.9350000,'pendiente','2026-11-10','2026-12-10',4),
(4,43.2631000,-2.9350000,'pendiente','2026-11-10','2026-11-17',2),
(5,43.2631000,-2.9350000,'finalizado','2026-11-10','2026-11-17',2),
(6,0.0000000,0.0000000,'finalizado','2026-11-06','2026-11-13',1),
(7,43.2792793,-2.9451691,'activo','2026-11-09','2026-11-19',1),
(8,37.4219983,-122.0840000,'activo','2026-11-09','2026-11-11',1);
/*!40000 ALTER TABLE `alquiler` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compra`
--

DROP TABLE IF EXISTS `compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `compra` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL,
  `fecha_compra` date NOT NULL,
  `usuario_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_9EC131FFDB38439E` (`usuario_id`),
  CONSTRAINT `FK_9EC131FFDB38439E` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compra`
--

LOCK TABLES `compra` WRITE;
/*!40000 ALTER TABLE `compra` DISABLE KEYS */;
INSERT INTO `compra` VALUES
(1,43.2631000,-2.9350000,'2026-09-15',2),
(2,43.2631000,-2.9350000,'2026-10-02',3),
(3,43.2631000,-2.9350000,'2026-10-28',2),
(4,43.2631000,-2.9350000,'2026-11-04',2),
(5,43.2631000,-2.9350000,'2026-11-04',2),
(6,0.0000000,0.0000000,'2026-11-06',1),
(7,43.2792793,-2.9451691,'2026-11-09',1);
/*!40000 ALTER TABLE `compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consola`
--

DROP TABLE IF EXISTS `consola`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `consola` (
  `modelo` varchar(255) NOT NULL,
  `almacenamiento` varchar(255) NOT NULL,
  `dimensiones` varchar(255) NOT NULL,
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_316E0BAFBF396750` FOREIGN KEY (`id`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consola`
--

LOCK TABLES `consola` WRITE;
/*!40000 ALTER TABLE `consola` DISABLE KEYS */;
INSERT INTO `consola` VALUES
('CFI-2000','1 TB SSD','358 x 216 x 96 mm',9),
('HEG-001','64 GB','102 x 242 x 13,9 mm',10),
('RRT-00001','1 TB SSD','301 x 151 x 151 mm',11);
/*!40000 ALTER TABLE `consola` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departamento`
--

DROP TABLE IF EXISTS `departamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `departamento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `descripcion` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departamento`
--

LOCK TABLES `departamento` WRITE;
/*!40000 ALTER TABLE `departamento` DISABLE KEYS */;
INSERT INTO `departamento` VALUES
(1,'Consolas','Reparación de consolas de sobremesa y portátiles: lectores, ventilación, mandos y fuentes de alimentación.'),
(2,'Móviles','Reparación de teléfonos: pantallas, baterías, conectores de carga y daños por agua.'),
(3,'Tablets','Reparación de tablets: cristales, baterías, conectores y software.');
/*!40000 ALTER TABLE `departamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `desarrolladora`
--

DROP TABLE IF EXISTS `desarrolladora`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `desarrolladora` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `desarrolladora`
--

LOCK TABLES `desarrolladora` WRITE;
/*!40000 ALTER TABLE `desarrolladora` DISABLE KEYS */;
INSERT INTO `desarrolladora` VALUES
(1,'Nintendo EPD'),
(2,'FromSoftware'),
(3,'Santa Monica Studio'),
(4,'CD Projekt Red'),
(5,'Team Cherry'),
(6,'Larian Studios'),
(7,'EA Vancouver');
/*!40000 ALTER TABLE `desarrolladora` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dispositivo_cliente`
--

DROP TABLE IF EXISTS `dispositivo_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispositivo_cliente` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `modelo` varchar(255) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `marca_id` int(11) DEFAULT NULL,
  `departamento_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_379C6419DB38439E` (`usuario_id`),
  KEY `IDX_379C641981EF0041` (`marca_id`),
  KEY `IDX_379C64195A91C08D` (`departamento_id`),
  CONSTRAINT `FK_379C64195A91C08D` FOREIGN KEY (`departamento_id`) REFERENCES `departamento` (`id`),
  CONSTRAINT `FK_379C641981EF0041` FOREIGN KEY (`marca_id`) REFERENCES `marca` (`id`),
  CONSTRAINT `FK_379C6419DB38439E` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dispositivo_cliente`
--

LOCK TABLES `dispositivo_cliente` WRITE;
/*!40000 ALTER TABLE `dispositivo_cliente` DISABLE KEYS */;
INSERT INTO `dispositivo_cliente` VALUES
(1,'PlayStation 5',2,5,1),
(2,'Galaxy S24',3,2,2),
(3,'Tab P12',4,4,3),
(7,'18 pro',2,1,2),
(8,'nintendo switxh',1,7,1),
(9,'smash',1,9,2),
(10,'ifone 155',1,1,2),
(11,'18',6,1,2);
/*!40000 ALTER TABLE `dispositivo_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doctrine_migration_versions`
--

DROP TABLE IF EXISTS `doctrine_migration_versions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctrine_migration_versions`
--

LOCK TABLES `doctrine_migration_versions` WRITE;
/*!40000 ALTER TABLE `doctrine_migration_versions` DISABLE KEYS */;
INSERT INTO `doctrine_migration_versions` VALUES
('App\\Migrations\\Version20261103105936','2026-11-03 11:00:01',385),
('App\\Migrations\\Version20261103112237','2026-11-03 11:22:45',769),
('App\\Migrations\\Version20261103112501','2026-11-03 11:25:07',23),
('App\\Migrations\\Version20261103115959','2026-11-03 12:00:14',24),
('App\\Migrations\\Version20261104081149','2026-11-04 08:11:57',85),
('App\\Migrations\\Version20261105191751','2026-11-05 19:19:00',387),
('App\\Migrations\\Version20261105192349','2026-11-05 19:24:26',88);
/*!40000 ALTER TABLE `doctrine_migration_versions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `foto`
--

DROP TABLE IF EXISTS `foto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `foto` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `producto_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_EADC3BE57645698E` (`producto_id`),
  CONSTRAINT `FK_EADC3BE57645698E` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `foto`
--

LOCK TABLES `foto` WRITE;
/*!40000 ALTER TABLE `foto` DISABLE KEYS */;
INSERT INTO `foto` VALUES
(1,'Portada Zelda TOTK','https://juegalmi-shop.duckdns.org/fotos/productos/the-legends-of-zelda-tears-of-the-kingdom.jpg',1),
(2,'Portada EA FC 25','https://juegalmi-shop.duckdns.org/fotos/productos/fifa-25.jpeg',2),
(3,'Portada Elden Ring','https://juegalmi-shop.duckdns.org/fotos/productos/elden-ring.jpg',3),
(4,'Portada God of War','https://juegalmi-shop.duckdns.org/fotos/productos/god-of-war-ragnarok.jpg',4),
(5,'Portada Mario Kart 8','https://juegalmi-shop.duckdns.org/fotos/productos/mario-kart-8.jpeg',5),
(6,'Portada Cyberpunk','https://juegalmi-shop.duckdns.org/fotos/productos/cyberpunk.jpeg',6),
(7,'Portada Hollow Knight','https://juegalmi-shop.duckdns.org/fotos/productos/hollow-knight.jpeg',7),
(8,'Portada BG3','https://juegalmi-shop.duckdns.org/fotos/productos/baldurs-gate-3.jpg',8),
(9,'PS5 Slim frontal','https://juegalmi-shop.duckdns.org/fotos/productos/play-5.jpg',9),
(10,'Switch OLED','https://juegalmi-shop.duckdns.org/fotos/productos/switch-oled.jpg',10),
(11,'Xbox Series X','https://juegalmi-shop.duckdns.org/fotos/productos/xbox-series-x.jpeg',11),
(12,'iPad 10 generación','https://juegalmi-shop.duckdns.org/fotos/productos/ipad-10.jpeg',12),
(13,'Galaxy Tab S9','https://juegalmi-shop.duckdns.org/fotos/productos/galaxy-tab-s9-fe.jpg',13),
(14,'Lenovo Tab P12','https://juegalmi-shop.duckdns.org/fotos/productos/lenovo-tab-p12.jpg',14),
(15,'iPhone 15','https://juegalmi-shop.duckdns.org/fotos/productos/iphone-15.jpg',15),
(16,'Galaxy S24','https://juegalmi-shop.duckdns.org/fotos/productos/galaxy-s24.jpg',16),
(17,'Pixel 8','https://juegalmi-shop.duckdns.org/fotos/productos/pixel-8.jpg',17);
/*!40000 ALTER TABLE `foto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `foto_tienda`
--

DROP TABLE IF EXISTS `foto_tienda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `foto_tienda` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  `titulo` varchar(255) NOT NULL,
  `tienda_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_F3174F6719BA6D46` (`tienda_id`),
  CONSTRAINT `FK_F3174F6719BA6D46` FOREIGN KEY (`tienda_id`) REFERENCES `tienda` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `foto_tienda`
--

LOCK TABLES `foto_tienda` WRITE;
/*!40000 ALTER TABLE `foto_tienda` DISABLE KEYS */;
INSERT INTO `foto_tienda` VALUES
(1,'https://juegalmi-shop.duckdns.org/fotos/tiendas/tienda-fuera-gran-via.jpg','Fachada de JuegAlmi Gran Via',1),
(2,'https://juegalmi-shop.duckdns.org/fotos/tiendas/tienda-dentro-gran-via.jpg','Interior de JuegAlmi Gran Via',1),
(3,'https://juegalmi-shop.duckdns.org/fotos/tiendas/tienda-fuera-deusto.png','Fachada de JuegAlmi Deusto',2),
(4,'https://juegalmi-shop.duckdns.org/fotos/tiendas/tienda-fuera-baraka.png','Fachada de JuegAlmi Barakaldo',3),
(5,'https://juegalmi-shop.duckdns.org/fotos/tiendas/tienda-dentro-baraka.png','Taller de JuegAlmi Barakaldo',3);
/*!40000 ALTER TABLE `foto_tienda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `genero`
--

DROP TABLE IF EXISTS `genero`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `genero` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `genero`
--

LOCK TABLES `genero` WRITE;
/*!40000 ALTER TABLE `genero` DISABLE KEYS */;
INSERT INTO `genero` VALUES
(1,'Aventura'),
(2,'RPG'),
(3,'Deportes'),
(4,'Acción'),
(5,'Carreras'),
(6,'Plataformas'),
(7,'Estrategia');
/*!40000 ALTER TABLE `genero` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `linea_alquiler`
--

DROP TABLE IF EXISTS `linea_alquiler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `linea_alquiler` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `precio_total` decimal(10,2) NOT NULL,
  `alquiler_id` int(11) NOT NULL,
  `videojuego_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_8DA2E1A05A921E97` (`alquiler_id`),
  KEY `IDX_8DA2E1A082925A85` (`videojuego_id`),
  CONSTRAINT `FK_8DA2E1A05A921E97` FOREIGN KEY (`alquiler_id`) REFERENCES `alquiler` (`id`),
  CONSTRAINT `FK_8DA2E1A082925A85` FOREIGN KEY (`videojuego_id`) REFERENCES `videojuego` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `linea_alquiler`
--

LOCK TABLES `linea_alquiler` WRITE;
/*!40000 ALTER TABLE `linea_alquiler` DISABLE KEYS */;
INSERT INTO `linea_alquiler` VALUES
(1,8.00,1,3),
(2,10.00,2,4),
(3,6.00,2,5),
(4,25.00,3,8),
(5,12.50,4,1),
(6,12.50,5,1),
(7,12.50,6,1),
(8,20.00,7,4),
(9,7.50,8,1);
/*!40000 ALTER TABLE `linea_alquiler` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `linea_compra`
--

DROP TABLE IF EXISTS `linea_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `linea_compra` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cantidad` int(11) NOT NULL,
  `precio_unidad` decimal(10,2) NOT NULL,
  `compra_id` int(11) NOT NULL,
  `producto_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_42111654F2E704D7` (`compra_id`),
  KEY `IDX_421116547645698E` (`producto_id`),
  CONSTRAINT `FK_421116547645698E` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`),
  CONSTRAINT `FK_42111654F2E704D7` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `linea_compra`
--

LOCK TABLES `linea_compra` WRITE;
/*!40000 ALTER TABLE `linea_compra` DISABLE KEYS */;
INSERT INTO `linea_compra` VALUES
(1,1,59.99,1,1),
(2,2,69.99,1,2),
(3,1,479.99,2,9),
(4,1,49.99,2,5),
(5,1,859.00,3,15),
(6,1,14.99,3,7),
(7,1,859.00,4,15),
(8,1,479.99,4,9),
(9,1,859.00,5,15),
(10,1,479.99,5,9),
(11,1,859.00,6,15),
(12,1,499.99,7,11);
/*!40000 ALTER TABLE `linea_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `marca`
--

DROP TABLE IF EXISTS `marca`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `marca` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `es_fabricante` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `marca`
--

LOCK TABLES `marca` WRITE;
/*!40000 ALTER TABLE `marca` DISABLE KEYS */;
INSERT INTO `marca` VALUES
(1,'Apple',1),
(2,'Samsung',1),
(3,'Google',1),
(4,'Lenovo',1),
(5,'Sony',1),
(6,'Microsoft',1),
(7,'Nintendo',1),
(8,'Xiaomi',1),
(9,'Huawei',1),
(10,'Otra',1),
(11,'Electronic Arts',0),
(12,'Bandai Namco',0),
(13,'CD Projekt',0),
(14,'Team Cherry',0),
(15,'Larian Studios',0);
/*!40000 ALTER TABLE `marca` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messenger_messages`
--

DROP TABLE IF EXISTS `messenger_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750` (`queue_name`,`available_at`,`delivered_at`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messenger_messages`
--

LOCK TABLES `messenger_messages` WRITE;
/*!40000 ALTER TABLE `messenger_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `messenger_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pegi`
--

DROP TABLE IF EXISTS `pegi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pegi` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pegi`
--

LOCK TABLES `pegi` WRITE;
/*!40000 ALTER TABLE `pegi` DISABLE KEYS */;
INSERT INTO `pegi` VALUES
(1,'PEGI 3'),
(2,'PEGI 7'),
(3,'PEGI 12'),
(4,'PEGI 16'),
(5,'PEGI 18');
/*!40000 ALTER TABLE `pegi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `peticion_reparacion`
--

DROP TABLE IF EXISTS `peticion_reparacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `peticion_reparacion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) NOT NULL,
  `url_foto` varchar(255) DEFAULT NULL,
  `latitud` decimal(10,7) DEFAULT NULL,
  `longitud` decimal(10,7) DEFAULT NULL,
  `fecha` date NOT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `motivo_rechazo` varchar(255) DEFAULT NULL,
  `usuario_id` int(11) NOT NULL,
  `dispositivo_cliente_id` int(11) DEFAULT NULL,
  `tienda_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_81844BAADB38439E` (`usuario_id`),
  KEY `IDX_81844BAA8D9E2C34` (`dispositivo_cliente_id`),
  KEY `IDX_81844BAA19BA6D46` (`tienda_id`),
  CONSTRAINT `FK_81844BAA19BA6D46` FOREIGN KEY (`tienda_id`) REFERENCES `tienda` (`id`),
  CONSTRAINT `FK_81844BAA8D9E2C34` FOREIGN KEY (`dispositivo_cliente_id`) REFERENCES `dispositivo_cliente` (`id`),
  CONSTRAINT `FK_81844BAADB38439E` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `peticion_reparacion`
--

LOCK TABLES `peticion_reparacion` WRITE;
/*!40000 ALTER TABLE `peticion_reparacion` DISABLE KEYS */;
INSERT INTO `peticion_reparacion` VALUES
(1,'La consola se sobrecalienta y el ventilador hace mucho ruido.','https://example.com/reparaciones/ps5-ventilador.jpg',43.2631000,-2.9350000,'2026-09-19','aceptada',NULL,2,1,1),
(2,'Pantalla rota tras una caída, el táctil no responde en la parte baja.','https://example.com/reparaciones/s24-pantalla.jpg',43.2631000,-2.9350000,'2026-10-24','aceptada',NULL,3,2,2),
(3,'La tablet no carga, posible fallo del conector USB-C.',NULL,43.2631000,-2.9350000,'2026-11-02','pendiente',NULL,4,3,3),
(4,'La carcasa lateral esta rota y quiero cambiarla.',NULL,43.2631000,-2.9350000,'2026-10-15','rechazada','No disponemos de recambio de carcasa para este modelo.',2,1,2),
(17,'no sonido','https://juegalmi-shop.duckdns.org/uploads/reparaciones/8f590e0f0929cd43c34567e4ab9d5f0e.jpg',43.2612613,-2.9442973,'2026-11-06','aceptada',NULL,2,7,1),
(18,'no me va la bateria','https://juegalmi-shop.duckdns.org/uploads/reparaciones/e0aa1258368198e774dd5b0a81a53273.jpg',43.2792793,-2.9451691,'2026-11-09','aceptada',NULL,1,8,2),
(20,'hola','https://juegalmi-shop.duckdns.org/uploads/reparaciones/5f1cdc84499ef4193331a06bfaccfd2d.jpg',43.2792793,-2.9451691,'2026-11-09','rechazada','Si',1,9,1),
(21,'jj','https://juegalmi-shop.duckdns.org/uploads/reparaciones/7952004d2002a0c5f05054080bff85d2.jpg',43.2792793,-2.9451691,'2026-11-09','pendiente',NULL,1,9,2),
(22,'nono','https://juegalmi-shop.duckdns.org/uploads/reparaciones/38414a3ea01da63166db5d92db7e2622.jpg',37.4219983,-122.0840000,'2026-11-09','pendiente',NULL,1,10,1),
(23,'No va el sonido','https://juegalmi-shop.duckdns.org/uploads/reparaciones/f71a7b154d7257cd763392c173c45b4d.jpg',37.4219983,-122.0840000,'2026-11-10','rechazada','mentira',6,11,1);
/*!40000 ALTER TABLE `peticion_reparacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plataforma`
--

DROP TABLE IF EXISTS `plataforma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `plataforma` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plataforma`
--

LOCK TABLES `plataforma` WRITE;
/*!40000 ALTER TABLE `plataforma` DISABLE KEYS */;
INSERT INTO `plataforma` VALUES
(1,'PlayStation 5'),
(2,'Nintendo Switch'),
(3,'Xbox Series X|S'),
(4,'PC'),
(5,'PlayStation 4');
/*!40000 ALTER TABLE `plataforma` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `stock` int(11) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `fecha_lanzamiento` date DEFAULT NULL,
  `descripcion` longtext DEFAULT NULL,
  `tipo` varchar(255) NOT NULL,
  `marca_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_A7BB061581EF0041` (`marca_id`),
  CONSTRAINT `FK_A7BB061581EF0041` FOREIGN KEY (`marca_id`) REFERENCES `marca` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES
(1,'The Legend of Zelda: Tears of the Kingdom',15,59.99,'2023-05-12','Link explora los cielos y las profundidades de Hyrule en esta secuela de Breath of the Wild.','videojuego',7),
(2,'EA Sports FC 25',30,69.99,'2024-09-27','La nueva entrega del simulador de fútbol con HyperMotionV y modo Rush.','videojuego',11),
(3,'Elden Ring',12,59.99,'2022-02-25','RPG de acción en mundo abierto creado por FromSoftware y George R. R. Martin.','videojuego',12),
(4,'God of War Ragnarök',10,69.99,'2022-11-09','Kratos y Atreus se enfrentan al fin de los tiempos en la mitología nórdica.','videojuego',5),
(5,'Mario Kart 8 Deluxe',25,49.99,'2017-04-28','El juego de carreras de Nintendo con todos los circuitos y personajes.','videojuego',7),
(6,'Cyberpunk 2077',18,39.99,'2020-12-10','RPG de acción ambientado en Night City, con la expansión Phantom Liberty.','videojuego',13),
(7,'Hollow Knight',20,14.99,'2017-02-24','Metroidvania de aventura en el reino subterráneo de Hallownest.','videojuego',14),
(8,'Baldur\'s Gate 3',14,59.99,'2023-08-03','RPG por turnos basado en Dungeons & Dragons, con cooperativo de 4 jugadores.','videojuego',15),
(9,'PlayStation 5 Slim',6,479.99,'2023-11-10','Versión rediseñada y más compacta de PS5 con lector de discos.','consola',5),
(10,'Nintendo Switch OLED',12,349.99,'2021-10-08','Consola híbrida con pantalla OLED de 7 pulgadas.','consola',7),
(11,'Xbox Series X',5,499.99,'2020-11-10','La consola más potente de Microsoft con 1 TB SSD.','consola',6),
(12,'iPad 10.ª generación',10,399.00,'2022-10-26','Tablet de 10,9 pulgadas con chip A14 Bionic y USB-C.','tablet',1),
(13,'Galaxy Tab S9',7,799.00,'2023-08-11','Tablet Android premium con pantalla AMOLED y S Pen incluido.','tablet',2),
(14,'Lenovo Tab P12',11,349.00,'2023-05-01','Tablet de 12,7 pulgadas ideal para multimedia y estudio.','tablet',4),
(15,'iPhone 15',6,859.00,'2023-09-22','iPhone con Dynamic Island, cámara de 48 MP y USB-C.','telefono',1),
(16,'Galaxy S24',13,809.00,'2024-01-31','Teléfono con Galaxy AI, pantalla de 6,2 pulgadas y 50 MP.','telefono',2),
(17,'Pixel 8',8,549.00,'2023-10-12','Teléfono con Tensor G3 y 7 años de actualizaciones.','telefono',3);
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reparacion`
--

DROP TABLE IF EXISTS `reparacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `reparacion` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `estado` varchar(255) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date DEFAULT NULL,
  `precio_estimado` decimal(10,2) NOT NULL,
  `precio_final` decimal(10,2) DEFAULT NULL,
  `peticion_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQ_42114EBBCBE7106` (`peticion_id`),
  CONSTRAINT `FK_42114EBBCBE7106` FOREIGN KEY (`peticion_id`) REFERENCES `peticion_reparacion` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reparacion`
--

LOCK TABLES `reparacion` WRITE;
/*!40000 ALTER TABLE `reparacion` DISABLE KEYS */;
INSERT INTO `reparacion` VALUES
(1,'completada','2026-09-20','2026-09-27',60.00,55.00,1),
(2,'en_proceso','2026-10-25',NULL,120.00,NULL,2),
(5,'completada','2026-11-06','2026-11-06',100.00,20.00,17),
(6,'en_proceso','2026-11-09',NULL,15.00,NULL,18);
/*!40000 ALTER TABLE `reparacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tablet`
--

DROP TABLE IF EXISTS `tablet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tablet` (
  `modelo` varchar(255) NOT NULL,
  `pulgadas` decimal(4,1) NOT NULL,
  `dimensiones` varchar(255) NOT NULL,
  `duracion_bateria` int(11) NOT NULL,
  `almacenamiento` varchar(255) NOT NULL,
  `conector` varchar(255) NOT NULL,
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_1A239782BF396750` FOREIGN KEY (`id`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tablet`
--

LOCK TABLES `tablet` WRITE;
/*!40000 ALTER TABLE `tablet` DISABLE KEYS */;
INSERT INTO `tablet` VALUES
('A2696',10.9,'248,6 x 179,5 x 7 mm',10,'64 GB','USB-C',12),
('SM-X710',11.0,'254,3 x 165,8 x 5,9 mm',14,'128 GB','USB-C',13),
('TB370FU',12.7,'293,3 x 191,6 x 6,9 mm',12,'128 GB','USB-C',14);
/*!40000 ALTER TABLE `tablet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telefono`
--

DROP TABLE IF EXISTS `telefono`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `telefono` (
  `modelo` varchar(255) NOT NULL,
  `pulgadas` decimal(4,1) NOT NULL,
  `dimensiones` varchar(255) NOT NULL,
  `duracion_bateria` int(11) NOT NULL,
  `almacenamiento` varchar(255) NOT NULL,
  `conector` varchar(255) NOT NULL,
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_C1E70A7FBF396750` FOREIGN KEY (`id`) REFERENCES `producto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telefono`
--

LOCK TABLES `telefono` WRITE;
/*!40000 ALTER TABLE `telefono` DISABLE KEYS */;
INSERT INTO `telefono` VALUES
('A3090',6.1,'147,6 x 71,6 x 7,8 mm',20,'128 GB','USB-C',15),
('SM-S921B',6.2,'147 x 70,6 x 7,6 mm',24,'256 GB','USB-C',16),
('GPJ41',6.2,'150,5 x 70,8 x 8,9 mm',24,'128 GB','USB-C',17);
/*!40000 ALTER TABLE `telefono` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tienda`
--

DROP TABLE IF EXISTS `tienda`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tienda` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `historia` longtext NOT NULL,
  `latitud` decimal(10,7) NOT NULL,
  `longitud` decimal(10,7) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tienda`
--

LOCK TABLES `tienda` WRITE;
/*!40000 ALTER TABLE `tienda` DISABLE KEYS */;
INSERT INTO `tienda` VALUES
(1,'GameZone Bilbao Centro','Gran Vía de Don Diego López de Haro, 30, 48009 Bilbao','Nuestra tienda insignia, abierta en 2010 en plena Gran Vía de Bilbao. Empezó como un pequeño local de videojuegos de segunda mano a pocos pasos de la Ría y hoy ofrece venta, alquiler y reparación. ¡Ongi etorri!',43.2631000,-2.9350000),
(2,'GameZone Deusto','Avenida de las Universidades, 24, 48007 Bilbao','Abierta en 2015 para dar servicio al barrio de Deusto y a su comunidad universitaria. Especializada en reparaciones de consolas y alquiler de videojuegos, con una zona de juego para los estudiantes.',43.2712000,-2.9450000),
(3,'GameZone Barakaldo','Avenida Libertad, 12, 48901 Barakaldo','Nuestra tienda más reciente, inaugurada en 2021 en Barakaldo. Destaca por su zona de pruebas de consolas y su taller de móviles y tablets.',43.2965000,-2.9895000);
/*!40000 ALTER TABLE `tienda` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tienda_departamento`
--

DROP TABLE IF EXISTS `tienda_departamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tienda_departamento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tienda_id` int(11) NOT NULL,
  `departamento_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_6B588B719BA6D46` (`tienda_id`),
  KEY `IDX_6B588B75A91C08D` (`departamento_id`),
  CONSTRAINT `FK_6B588B719BA6D46` FOREIGN KEY (`tienda_id`) REFERENCES `tienda` (`id`),
  CONSTRAINT `FK_6B588B75A91C08D` FOREIGN KEY (`departamento_id`) REFERENCES `departamento` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tienda_departamento`
--

LOCK TABLES `tienda_departamento` WRITE;
/*!40000 ALTER TABLE `tienda_departamento` DISABLE KEYS */;
INSERT INTO `tienda_departamento` VALUES
(1,1,1),
(2,1,2),
(3,1,3),
(4,2,1),
(5,2,2),
(6,3,2),
(7,3,3);
/*!40000 ALTER TABLE `tienda_departamento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_usuario` varchar(180) NOT NULL,
  `roles` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`roles`)),
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `api_token` varchar(64) DEFAULT NULL,
  `expiracion_token` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQ_IDENTIFIER_NOMBRE_USUARIO` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES
(1,'admin','[\"ROLE_ADMIN\"]','$2y$13$M16rPI5VsBtZmFMKRRUbj.wwbXbZEAvr2GJ1qX.1dFylmBeNazx6K','admin@tienda.com','8af237389ba1b088e97d0510edefc7c83420eaf6ce6ae036b2a080f108d5a1de','2026-12-05'),
(2,'laura','[\"ROLE_USER\"]','$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','laura@correo.com','2c003cfbb29918b09f0ce4c6ac2904da6b6e34ef1d42ed8b5b6dc1efb22da069','2026-12-04'),
(3,'marc','[\"ROLE_USER\"]','$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','marc@correo.com','6d191686739c8b621533b89554c0d6adb9b08af50f66157a5e1ab40e7f2ee939','2026-12-06'),
(4,'sofia','[\"ROLE_USER\"]','$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi','sofia@correo.com',NULL,NULL),
(5,'porti','[\"ROLE_USER\"]','$2y$13$7fQkl5uxOjkRVbE4TZxXAOpbw6qDtQK1RnrJ1Jxwnd9nRFUptmxim','portillaacabado@almi.eus','7af81f2d76026503933802768bec8804bd49a22e5f6fb6cf64d1b23a16c28333','2026-12-06'),
(6,'aimar','[\"ROLE_USER\"]','$2y$13$8H./llD3te9IAORx/6GuZ.5xN/Q5ioQYQn/JyruFbTnBT6xEb.UJS','aimar@almi.eus','757bac98ae5625f8b830c80f530cd16ecefc01da21257173423b844373fa128d','2026-12-10'),
(7,'jugador67','[\"ROLE_USER\"]','$2y$13$bu9BJ1zrbKzKqAjg1EKxN.v081YzFrUc2raX/FIob2xSVDK/k/L86','aimar@gmail.com','6a8a771e717a23acfc2c6f289f54a18132454b96009c5cb677b989d8d057cba8','2026-12-10');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videojuego`
--

DROP TABLE IF EXISTS `videojuego`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `videojuego` (
  `precio_diario` decimal(8,2) NOT NULL,
  `precio_semanal` decimal(8,2) NOT NULL,
  `precio_mensual` decimal(8,2) NOT NULL,
  `copias_alquiler` int(11) NOT NULL,
  `pegi_id` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_AA5E6DFADD019E4A` (`pegi_id`),
  CONSTRAINT `FK_AA5E6DFABF396750` FOREIGN KEY (`id`) REFERENCES `producto` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_AA5E6DFADD019E4A` FOREIGN KEY (`pegi_id`) REFERENCES `pegi` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videojuego`
--

LOCK TABLES `videojuego` WRITE;
/*!40000 ALTER TABLE `videojuego` DISABLE KEYS */;
INSERT INTO `videojuego` VALUES
(2.50,10.00,25.00,2,3,1),
(2.50,10.00,25.00,5,1,2),
(2.00,8.00,20.00,3,4,3),
(2.50,10.00,25.00,2,5,4),
(1.50,6.00,15.00,6,1,5),
(1.50,6.00,15.00,3,5,6),
(1.00,4.00,10.00,4,2,7),
(2.50,10.00,25.00,3,5,8);
/*!40000 ALTER TABLE `videojuego` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videojuego_desarrolladora`
--

DROP TABLE IF EXISTS `videojuego_desarrolladora`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `videojuego_desarrolladora` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `videjouego_id` int(11) NOT NULL,
  `desarrolladora_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_5E9C855F80E511AE` (`videjouego_id`),
  KEY `IDX_5E9C855F2F0267C4` (`desarrolladora_id`),
  CONSTRAINT `FK_5E9C855F2F0267C4` FOREIGN KEY (`desarrolladora_id`) REFERENCES `desarrolladora` (`id`),
  CONSTRAINT `FK_5E9C855F80E511AE` FOREIGN KEY (`videjouego_id`) REFERENCES `videojuego` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videojuego_desarrolladora`
--

LOCK TABLES `videojuego_desarrolladora` WRITE;
/*!40000 ALTER TABLE `videojuego_desarrolladora` DISABLE KEYS */;
INSERT INTO `videojuego_desarrolladora` VALUES
(1,1,1),
(2,2,7),
(3,3,2),
(4,4,3),
(5,5,1),
(6,6,4),
(7,7,5),
(8,8,6);
/*!40000 ALTER TABLE `videojuego_desarrolladora` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videojuego_genero`
--

DROP TABLE IF EXISTS `videojuego_genero`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `videojuego_genero` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `videojuego_id` int(11) NOT NULL,
  `genero_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_88A29E6182925A85` (`videojuego_id`),
  KEY `IDX_88A29E61BCE7B795` (`genero_id`),
  CONSTRAINT `FK_88A29E6182925A85` FOREIGN KEY (`videojuego_id`) REFERENCES `videojuego` (`id`),
  CONSTRAINT `FK_88A29E61BCE7B795` FOREIGN KEY (`genero_id`) REFERENCES `genero` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videojuego_genero`
--

LOCK TABLES `videojuego_genero` WRITE;
/*!40000 ALTER TABLE `videojuego_genero` DISABLE KEYS */;
INSERT INTO `videojuego_genero` VALUES
(1,1,1),
(2,1,4),
(3,2,3),
(4,3,2),
(5,3,4),
(6,4,4),
(7,4,1),
(8,5,5),
(9,6,2),
(10,6,4),
(11,7,1),
(12,7,6),
(13,8,2),
(14,8,7);
/*!40000 ALTER TABLE `videojuego_genero` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `videojuego_plataforma`
--

DROP TABLE IF EXISTS `videojuego_plataforma`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `videojuego_plataforma` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `videojuego_id` int(11) NOT NULL,
  `plataforma_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_DDDABEE82925A85` (`videojuego_id`),
  KEY `IDX_DDDABEEEB90E430` (`plataforma_id`),
  CONSTRAINT `FK_DDDABEE82925A85` FOREIGN KEY (`videojuego_id`) REFERENCES `videojuego` (`id`),
  CONSTRAINT `FK_DDDABEEEB90E430` FOREIGN KEY (`plataforma_id`) REFERENCES `plataforma` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `videojuego_plataforma`
--

LOCK TABLES `videojuego_plataforma` WRITE;
/*!40000 ALTER TABLE `videojuego_plataforma` DISABLE KEYS */;
/*!40000 ALTER TABLE `videojuego_plataforma` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'app'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-11-10 11:20:14

-- MySQL dump 10.13  Distrib 8.0.44, for Linux (x86_64)
--
-- Host: localhost    Database: lada_cms
-- ------------------------------------------------------
-- Server version	8.0.44-0ubuntu0.24.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `DATABASECHANGELOG`
--

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `AUTHOR` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `FILENAME` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int NOT NULL,
  `EXECTYPE` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `MD5SUM` varchar(35) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `COMMENTS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TAG` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `LIQUIBASE` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `CONTEXTS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `LABELS` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOG`
--

LOCK TABLES `DATABASECHANGELOG` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOG` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOG` VALUES ('001-1-create-projects-table','Lex','file:database/migrations/001-initial-schema.yaml','2025-12-09 19:55:55',1,'EXECUTED','9:8c2180696fc34ab30241e7dc670995b4','createTable tableName=projects','',NULL,'4.23.0',NULL,NULL,'5299355304'),('001-2-create-project-videos-table','Lex','file:database/migrations/001-initial-schema.yaml','2025-12-09 19:55:55',2,'EXECUTED','9:11beb831b09de15f92e1b39518836801','createTable tableName=project_videos','',NULL,'4.23.0',NULL,NULL,'5299355304'),('001-3-create-project-partners-table','Lex','file:database/migrations/001-initial-schema.yaml','2025-12-09 19:55:55',3,'EXECUTED','9:740584c17ed2b3c3ba48010bd6547622','createTable tableName=project_partners','',NULL,'4.23.0',NULL,NULL,'5299355304'),('001-4-add-foreign-keys','Lex','file:database/migrations/001-initial-schema.yaml','2025-12-09 19:55:55',4,'EXECUTED','9:d326c9c33cffc3328d0c884263f6c2c3','addForeignKeyConstraint baseTableName=project_videos, constraintName=fk_project_videos_project, referencedTableName=projects; addForeignKeyConstraint baseTableName=project_partners, constraintName=fk_project_partners_project, referencedTableName=p...','',NULL,'4.23.0',NULL,NULL,'5299355304'),('002-1-create-team-members-table','Lex','file:database/migrations/002-team-member-project-roles.yaml','2025-12-09 19:55:55',5,'EXECUTED','9:a687030fb77fda0a8c5629bcb6e0b9c6','createTable tableName=team_members','',NULL,'4.23.0',NULL,NULL,'5299355304'),('002-2-create-team-member-project-roles-table','Lex','file:database/migrations/002-team-member-project-roles.yaml','2025-12-09 19:55:55',6,'EXECUTED','9:56c3806e8dc34995cede46a4b9e2e3fd','createTable tableName=team_member_project_roles; addUniqueConstraint constraintName=uk_team_member_project, tableName=team_member_project_roles','',NULL,'4.23.0',NULL,NULL,'5299355304'),('002-3-add-foreign-keys-for-roles','Lex','file:database/migrations/002-team-member-project-roles.yaml','2025-12-09 19:55:55',7,'EXECUTED','9:d34a4711d88ae0f36093f402e0c0958b','addForeignKeyConstraint baseTableName=team_member_project_roles, constraintName=fk_tmpr_team_member, referencedTableName=team_members; addForeignKeyConstraint baseTableName=team_member_project_roles, constraintName=fk_tmpr_project, referencedTable...','',NULL,'4.23.0',NULL,NULL,'5299355304'),('002-4-create-indexes','Lex','file:database/migrations/002-team-member-project-roles.yaml','2025-12-09 19:55:56',8,'EXECUTED','9:a0bdf609f88c8cc683c6b03ebc2bb76a','createIndex indexName=idx_team_members_active, tableName=team_members; createIndex indexName=idx_team_members_sort_order, tableName=team_members; createIndex indexName=idx_tmpr_member_id, tableName=team_member_project_roles; createIndex indexName=...','',NULL,'4.23.0',NULL,NULL,'5299355304'),('003-1-insert-test-team-members','Lex','file:database/migrations/003-seed-data.yaml','2025-12-09 19:55:56',9,'EXECUTED','9:a855ca041d67fcf58309820463e7d275','insert tableName=team_members; insert tableName=team_members; insert tableName=team_members','',NULL,'4.23.0',NULL,NULL,'5299355304'),('003-2-insert-test-projects','Lex','file:database/migrations/003-seed-data.yaml','2025-12-09 19:55:56',10,'EXECUTED','9:bc2d458df457ed6e4cf6f6fe1be30929','insert tableName=projects; insert tableName=projects; insert tableName=projects','',NULL,'4.23.0',NULL,NULL,'5299355304'),('003-3-insert-team-member-project-roles','Lex','file:database/migrations/003-seed-data.yaml','2025-12-09 19:55:56',11,'EXECUTED','9:0dbd9cce064e3caebd19d7930878b318','insert tableName=team_member_project_roles; insert tableName=team_member_project_roles; insert tableName=team_member_project_roles; insert tableName=team_member_project_roles; insert tableName=team_member_project_roles; insert tableName=team_membe...','',NULL,'4.23.0',NULL,NULL,'5299355304'),('004-1-create-easyPages-table','Lex','file:database/migrations/004-create-easyPages-table.yaml','2025-12-09 21:29:59',14,'EXECUTED','9:b7e558613f42f4437992fbd6a7698a47','createTable tableName=easyPages; createIndex indexName=idx_pages_slug, tableName=easyPages; createIndex indexName=idx_pages_published, tableName=easyPages','',NULL,'4.23.0',NULL,NULL,'5304998897'),('005-1-create-publication-categories-table','Lex','file:database/migrations/005-create-publication-categories.yaml','2025-12-09 21:36:34',15,'EXECUTED','9:f6af786b0e84efeb55036a8f9c8f6bc2','createTable tableName=publication_categories','',NULL,'4.23.0',NULL,NULL,'5305394000'),('005-2-insert-default-categories','Lex','file:database/migrations/005-create-publication-categories.yaml','2025-12-09 21:36:34',16,'EXECUTED','9:86dc2fb8cebd048a8ff35be6044996ec','insert tableName=publication_categories; insert tableName=publication_categories; insert tableName=publication_categories; insert tableName=publication_categories; insert tableName=publication_categories; insert tableName=publication_categories; i...','',NULL,'4.23.0',NULL,NULL,'5305394000'),('004-1-create-users-table','Lex','file:database/migrations/004-create-users-table.yaml','2025-12-09 23:51:00',17,'EXECUTED','9:3dc801955188231f473e16b5ae795ece','createTable tableName=users','',NULL,'4.23.0',NULL,NULL,'5313460397'),('004-2-insert-admin-user','Lex','file:database/migrations/004-create-users-table.yaml','2025-12-09 23:51:00',18,'EXECUTED','9:9dc744c10380d9650fb83742f727ad4e','insert tableName=users','',NULL,'4.23.0',NULL,NULL,'5313460397');
/*!40000 ALTER TABLE `DATABASECHANGELOG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATABASECHANGELOGLOCK`
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOGLOCK`
--

LOCK TABLES `DATABASECHANGELOGLOCK` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOGLOCK` VALUES (1,_binary '\0',NULL,NULL);
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gallery_items`
--

DROP TABLE IF EXISTS `gallery_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gallery_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `media_type` enum('PHOTO','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `published` bit(1) NOT NULL,
  `sort_order` int NOT NULL,
  `thumbnail_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `video_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `year` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gallery_items`
--

LOCK TABLES `gallery_items` WRITE;
/*!40000 ALTER TABLE `gallery_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `gallery_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gallery_media`
--

DROP TABLE IF EXISTS `gallery_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gallery_media` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_primary` bit(1) NOT NULL,
  `media_type` enum('PHOTO','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `original_file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `gallery_item_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfnd1bbb938i1ny92535tervx6` (`gallery_item_id`),
  CONSTRAINT `FKfnd1bbb938i1ny92535tervx6` FOREIGN KEY (`gallery_item_id`) REFERENCES `gallery_items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gallery_media`
--

LOCK TABLES `gallery_media` WRITE;
/*!40000 ALTER TABLE `gallery_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `gallery_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `media_files`
--

DROP TABLE IF EXISTS `media_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `media_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint NOT NULL,
  `file_type` enum('AUDIO','DOCUMENT','IMAGE','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_primary` bit(1) NOT NULL,
  `mime_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int NOT NULL,
  `uploaded_at` datetime(6) NOT NULL,
  `photo_item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8v8juiqi7nwafxrq20vq1q9i` (`photo_item_id`),
  CONSTRAINT `FK8v8juiqi7nwafxrq20vq1q9i` FOREIGN KEY (`photo_item_id`) REFERENCES `photo_gallery_items` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `media_files`
--

LOCK TABLES `media_files` WRITE;
/*!40000 ALTER TABLE `media_files` DISABLE KEYS */;
INSERT INTO `media_files` VALUES (58,'главная.jpg','b4a868ac-17a7-4ebb-b76a-c0723038a1ab.jpg',225189,'IMAGE',_binary '\0','image/jpeg',12,'2025-12-13 12:16:36.268047',20),(59,'DSC_5661.JPG','b52f7a74-9852-4008-9fa5-f36d18cdc985.jpg',648661,'IMAGE',_binary '\0','image/jpeg',1,'2025-12-13 18:10:55.314694',20);
/*!40000 ALTER TABLE `media_files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `easyPages`
--

DROP TABLE IF EXISTS `easyPages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `easyPages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `slug` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `page_type` enum('ABOUT','CONTACT','CUSTOM','GALLERY','PATRONS','PROJECTS') COLLATE utf8mb4_unicode_ci NOT NULL,
  `meta_description` text COLLATE utf8mb4_unicode_ci,
  `featured_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `published` bit(1) DEFAULT b'0',
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `idx_pages_slug` (`slug`),
  KEY `idx_pages_published` (`published`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `easyPages`
--

LOCK TABLES `easyPages` WRITE;
/*!40000 ALTER TABLE `easyPages` DISABLE KEYS */;
INSERT INTO `easyPages` VALUES (1,'О нас','<p>Информация о нашей организации будет здесь.</p>','about','ABOUT','Информация о нашей организации, миссии и ценностях',NULL,_binary '','2025-12-09 18:30:05.000000','2025-12-09 18:30:05.000000'),(2,'Наши проекты','<p>Наши проекты и инициативы будут отображены здесь.</p>','projects','PROJECTS','Наши текущие и завершенные проекты и инициативы',NULL,_binary '','2025-12-09 18:30:05.000000','2025-12-09 18:30:05.000000'),(3,'Галерея','<p>Фотографии и видео наших мероприятий будут здесь.</p>','gallery','GALLERY','Фотографии и видео с наших мероприятий и проектов',NULL,_binary '','2025-12-09 18:30:05.000000','2025-12-09 18:30:05.000000'),(4,'Меценатам','<p>Информация для меценатов и партнеров будет здесь.</p>','patrons','PATRONS','Информация для меценатов и партнеров организации',NULL,_binary '','2025-12-09 18:30:05.000000','2025-12-09 18:30:05.000000'),(5,'Контакты','<p>Контактная информация и форма обратной связи будут здесь.</p>','contact','CONTACT','Контактная информация и форма обратной связи',NULL,_binary '','2025-12-09 18:30:05.000000','2025-12-09 18:30:05.000000'),(6,'Пример страницы','<h2>Добро пожаловать на пример страницы!</h2>\n<p>Это демонстрационная страница, созданная автоматически при первом запуске приложения.</p>\n\n<h3>Что вы можете делать:</h3>\n<ul>\n    <li>Создавать новые страницы через административную панель</li>\n    <li>Редактировать содержимое страниц с помощью WYSIWYG редактора</li>\n    <li>Публиковать и снимать с публикации страницы</li>\n    <li>Управлять пользователями и их правами доступа</li>\n</ul>\n\n<div class=\"alert alert-info\">\n    <strong>Совет:</strong> Для начала работы перейдите в административную панель\n    и создайте свои собственные страницы!\n</div>\n','primer-stranicy','CUSTOM','Пример страницы для демонстрации функциональности CMS системы',NULL,_binary '','2025-12-09 18:30:05.000000','2025-12-09 18:30:05.000000');
/*!40000 ALTER TABLE `easyPages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `photo_gallery_items`
--

DROP TABLE IF EXISTS `photo_gallery_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_gallery_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `published` bit(1) NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `year` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_gallery_items`
--

LOCK TABLES `photo_gallery_items` WRITE;
/*!40000 ALTER TABLE `photo_gallery_items` DISABLE KEYS */;
INSERT INTO `photo_gallery_items` VALUES (20,'2025-12-13 12:16:36.198341','application.properties',_binary '\0','фото','2025-12-13 18:10:55.347338',2025);
/*!40000 ALTER TABLE `photo_gallery_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `photo_item_categories`
--

DROP TABLE IF EXISTS `photo_item_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `photo_item_categories` (
  `photo_item_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  PRIMARY KEY (`photo_item_id`,`category_id`),
  KEY `FKgw04u42972k3jo0alaev4kh0i` (`category_id`),
  CONSTRAINT `FKgw04u42972k3jo0alaev4kh0i` FOREIGN KEY (`category_id`) REFERENCES `publication_categories` (`id`),
  CONSTRAINT `FKgwc1p44tuwi312yk8r3b7gngf` FOREIGN KEY (`photo_item_id`) REFERENCES `photo_gallery_items` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `photo_item_categories`
--

LOCK TABLES `photo_item_categories` WRITE;
/*!40000 ALTER TABLE `photo_item_categories` DISABLE KEYS */;
INSERT INTO `photo_item_categories` VALUES (20,1),(20,2),(20,3),(20,4),(20,5),(20,6),(20,7);
/*!40000 ALTER TABLE `photo_item_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_articles`
--

DROP TABLE IF EXISTS `project_articles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_articles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `featured_image_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_keywords` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `og_image_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `published_date` datetime(6) DEFAULT NULL,
  `short_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `slug` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int NOT NULL,
  `status` enum('ARCHIVED','DRAFT','PUBLISHED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `view_count` int NOT NULL,
  `project_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_belj8fi1s2bh5kr003wli1dqq` (`slug`),
  KEY `FK3j2jksbl0twqivodt29t6wx1n` (`project_id`),
  CONSTRAINT `FK3j2jksbl0twqivodt29t6wx1n` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_articles`
--

LOCK TABLES `project_articles` WRITE;
/*!40000 ALTER TABLE `project_articles` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_articles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_images`
--

DROP TABLE IF EXISTS `project_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `added_at` datetime(6) NOT NULL,
  `alt_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `caption` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_featured` bit(1) NOT NULL,
  `sort_order` int NOT NULL,
  `media_file_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlujcr3u7dap0i5q71owmmy9fp` (`media_file_id`),
  KEY `FKoej10untas4roy2rqxcmbdj42` (`project_id`),
  CONSTRAINT `FKlujcr3u7dap0i5q71owmmy9fp` FOREIGN KEY (`media_file_id`) REFERENCES `media_files` (`id`),
  CONSTRAINT `FKoej10untas4roy2rqxcmbdj42` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_images`
--

LOCK TABLES `project_images` WRITE;
/*!40000 ALTER TABLE `project_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_partners`
--

DROP TABLE IF EXISTS `project_partners`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_partners` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `partner_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_person` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `active` bit(1) DEFAULT b'1',
  `added_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_project_partners_project` (`project_id`),
  CONSTRAINT `fk_project_partners_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_partners`
--

LOCK TABLES `project_partners` WRITE;
/*!40000 ALTER TABLE `project_partners` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_partners` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_videos`
--

DROP TABLE IF EXISTS `project_videos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_videos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `youtube_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `video_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `video_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_main` bit(1) DEFAULT b'0',
  `duration_seconds` int DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `added_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_project_videos_project` (`project_id`),
  CONSTRAINT `fk_project_videos_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_videos`
--

LOCK TABLES `project_videos` WRITE;
/*!40000 ALTER TABLE `project_videos` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_videos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `short_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_description` text COLLATE utf8mb4_unicode_ci,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','ANNUAL','ARCHIVED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int DEFAULT '0',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `event_date` date DEFAULT NULL,
  `show_description` bit(1) DEFAULT b'1',
  `show_photos` bit(1) DEFAULT b'1',
  `show_videos` bit(1) DEFAULT b'1',
  `show_team` bit(1) DEFAULT b'1',
  `show_participation` bit(1) DEFAULT b'1',
  `show_partners` bit(1) DEFAULT b'1',
  `show_related` bit(1) DEFAULT b'1',
  `sections_order` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `featured_image_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `og_image_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meta_keywords` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects`
--

LOCK TABLES `projects` WRITE;
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
INSERT INTO `projects` VALUES (1,'Фестиваль Весны 2024','festival-vesny-2024','Ежегодный весенний фестиваль с концертами и мастер-классами','Большой весенний фестиваль, включающий музыкальные выступления, художественные мастер-классы и культурные мероприятия для всей семьи.','Центральный парк культуры и отдыха','Фестивали','ACTIVE',1,'2024-05-15','2024-05-17',NULL,_binary '',_binary '',_binary '',_binary '',_binary '',_binary '',_binary '',NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-09 19:55:56.000000','2025-12-09 19:55:56.000000'),(2,'Конкурс молодых талантов','konkurs-molodyh-talantov','Ежегодный конкурс для выявления и поддержки молодых дарований','Конкурс направлен на поиск и поддержку талантливой молодежи в различных творческих направлениях: музыка, танцы, изобразительное искусство.','Дворец культуры','Конкурсы','ACTIVE',2,NULL,NULL,'2024-06-10',_binary '',_binary '',_binary '',_binary '',_binary '',_binary '',_binary '',NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-09 19:55:56.000000','2025-12-09 19:55:56.000000'),(3,'Благотворительный сбор для детского дома','blagotvoritelnyj-sbor-detskij-dom','Сбор средств и вещей для детского дома №3','Организация сбора одежды, игрушек, школьных принадлежностей и средств для детского дома.','Городской центр волонтеров','Благотворительность','ANNUAL',3,NULL,NULL,NULL,_binary '',_binary '',_binary '',_binary '',_binary '',_binary '',_binary '',NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-09 19:55:56.000000','2025-12-09 19:55:56.000000');
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `publication_categories`
--

DROP TABLE IF EXISTS `publication_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `publication_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `publication_categories`
--

LOCK TABLES `publication_categories` WRITE;
/*!40000 ALTER TABLE `publication_categories` DISABLE KEYS */;
INSERT INTO `publication_categories` VALUES (1,'Главная','Публикации на главной странице','2025-12-09 21:36:34.000000',NULL),(2,'О нас','Публикации на странице \'О нас\'','2025-12-09 21:36:34.000000',NULL),(3,'Наши проекты','Публикации в разделе проектов','2025-12-09 21:36:34.000000',NULL),(4,'Галерея','Публикации в галерее','2025-12-09 21:36:34.000000',NULL),(5,'Новости','Новостные публикации','2025-12-09 21:36:34.000000',NULL),(6,'Мероприятия','Публикации о мероприятиях','2025-12-09 21:36:34.000000',NULL),(7,'Партнеры','Публикации о партнерах','2025-12-09 21:36:34.000000',NULL);
/*!40000 ALTER TABLE `publication_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ROLE_ADMIN','Администратор системы','2025-12-09 18:50:21'),(2,'ROLE_EDITOR','Редактор контента','2025-12-09 18:50:21'),(3,'ROLE_USER','Обычный пользователь','2025-12-09 18:50:21');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_member_project_roles`
--

DROP TABLE IF EXISTS `team_member_project_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_member_project_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_member_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  `role` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `joined_date` date DEFAULT NULL,
  `responsibilities` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_member_project` (`team_member_id`,`project_id`),
  KEY `idx_tmpr_member_id` (`team_member_id`),
  KEY `idx_tmpr_project_id` (`project_id`),
  CONSTRAINT `fk_tmpr_project` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tmpr_team_member` FOREIGN KEY (`team_member_id`) REFERENCES `team_members` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_member_project_roles`
--

LOCK TABLES `team_member_project_roles` WRITE;
/*!40000 ALTER TABLE `team_member_project_roles` DISABLE KEYS */;
INSERT INTO `team_member_project_roles` VALUES (1,1,1,'Руководитель проекта','2025-12-09 19:55:56.000000',NULL,NULL),(2,1,2,'Член жюри','2025-12-09 19:55:56.000000',NULL,NULL),(3,2,1,'Координатор','2025-12-09 19:55:56.000000',NULL,NULL),(4,2,2,'Руководитель проекта','2025-12-09 19:55:56.000000',NULL,NULL),(5,3,1,'Волонтер','2025-12-09 19:55:56.000000',NULL,NULL),(6,3,3,'Волонтер','2025-12-09 19:55:56.000000',NULL,NULL);
/*!40000 ALTER TABLE `team_member_project_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_member_projects`
--

DROP TABLE IF EXISTS `team_member_projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_member_projects` (
  `team_member_id` bigint NOT NULL,
  `project_id` bigint NOT NULL,
  PRIMARY KEY (`team_member_id`,`project_id`),
  KEY `FKjce2vypsxq0sdgldn2t5cmboy` (`project_id`),
  CONSTRAINT `FKagtiuv17oeawdv9e88rdoueu1` FOREIGN KEY (`team_member_id`) REFERENCES `team_members` (`id`),
  CONSTRAINT `FKjce2vypsxq0sdgldn2t5cmboy` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_member_projects`
--

LOCK TABLES `team_member_projects` WRITE;
/*!40000 ALTER TABLE `team_member_projects` DISABLE KEYS */;
/*!40000 ALTER TABLE `team_member_projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `team_members`
--

DROP TABLE IF EXISTS `team_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `team_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `position` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `avatar_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `active` bit(1) DEFAULT b'1',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `social_links` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_team_members_active` (`active`),
  KEY `idx_team_members_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `team_members`
--

LOCK TABLES `team_members` WRITE;
/*!40000 ALTER TABLE `team_members` DISABLE KEYS */;
INSERT INTO `team_members` VALUES (1,'Иванов Иван Иванович','Руководитель проектов','Опыт работы 10 лет в организации мероприятий',NULL,1,_binary '','ivanov@example.com','+7 (999) 123-45-67',NULL,'2025-12-09 19:55:56.000000','2025-12-09 19:55:56.000000'),(2,'Петрова Мария Сергеевна','Координатор мероприятий','Специалист по организации культурных событий',NULL,2,_binary '','petrova@example.com','+7 (999) 765-43-21',NULL,'2025-12-09 19:55:56.000000','2025-12-09 19:55:56.000000'),(3,'Сидоров Алексей Петрович','Волонтер','Активный участник волонтерских программ',NULL,3,_binary '','sidorov@example.com',NULL,NULL,'2025-12-09 19:55:56.000000','2025-12-09 19:55:56.000000');
/*!40000 ALTER TABLE `team_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`user_id`,`role`),
  CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,'ADMIN'),(1,'EDITOR'),(1,'USER'),(4,'EDITOR'),(4,'USER'),(5,'USER'),(7,'ADMIN'),(7,'EDITOR'),(7,'USER');
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` bit(1) DEFAULT b'1',
  `created_at` datetime(6) NOT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$hYRuMgFAmV.hzlcx6ITT6Ow21OtGiwJxtPwLu083qIdvGF3H5IqwW','admin@lada.ru',_binary '','2025-12-09 23:51:00.000000','2025-12-09 20:51:00'),(4,'editor','$2a$10$yhzsDHxN79x47vZlaaxmE.SgSeyqIWOOnf4Bw.Ibybz9JmXlcbeOW','editor@lada-org.ru',_binary '','2025-12-09 20:53:08.000000','2025-12-09 20:53:07'),(5,'user','$2a$10$BfuDzfgzNegkE4xwUw5gCuJ6x.UURH31SqTYp8LATCsj6elasCgzO','user@lada-org.ru',_binary '','2025-12-09 20:53:08.000000','2025-12-09 20:53:07'),(7,'Алексей','$2a$10$w0cxId1Vxf9fZrErPHwpD.9p1jPEaVfmzSkJRMHxUWVg2mUEA8Ude','vasickin@mail.ru',_binary '','2025-12-10 16:46:48.618891','2025-12-10 16:46:48');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-13 21:37:08

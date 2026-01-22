# Настройка MySQL для Community CMS

## 1. Установка MySQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server



# Создаем инструкцию по настройке
cat > MYSQL_SETUP.md << 'EOF'
# MySQL Configuration Setup

## 1. Database Setup
```bash
# Connect to MySQL as root
sudo mysql -u root -p

# Create database and user
CREATE DATABASE lada_cms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'lada_user'@'localhost' IDENTIFIED BY 'your_secure_password_here';
GRANT ALL PRIVILEGES ON lada_cms.* TO 'lada_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```
## Зайти в базу данных:
```jshelllanguage
mysql -u lada_user -p lada_cms
```
 ## Пароль:
```shell
LadaCMS2025
```

## Проверь MySQL:
### 1. Просмотр списка всех таблиц в базе данных
#### sql
```shell
SHOW TABLES;
```

### 2. Просмотр структуры таблицы (столбцы, типы данных)
#### sql
```shell
DESCRIBE имя_таблицы;
-- или сокращенно:
DESC имя_таблицы;
-- или более подробно:
SHOW COLUMNS FROM имя_таблицы;
```
### 3. Просмотр содержимого таблицы (данных)
#### Выбор всех данных:
```shell
SELECT * FROM имя_таблицы;
```
#### Выбор конкретных столбцов:
```shell
SELECT column1, column2 FROM имя_таблицы;
```

#### С ограничением количества строк:
```shell
SELECT * FROM имя_таблицы LIMIT 10; -- первые 10 строк
```



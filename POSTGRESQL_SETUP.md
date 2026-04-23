# Настройка PostgreSQL для Community CMS

## 1. Установка PostgreSQL

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install -y postgresql-16 postgresql-contrib-16
```
### Запуск сервера
```bash
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

## 2. Настройка базы данных и пользователя
### Проверка статуса
```bash
sudo systemctl status postgresql
```

### Подключение к PostgreSQL от суперпользователя
```bash
sudo -u postgres psql
```

### Создание базы данных
```sql
CREATE DATABASE lada_cms_pg 
    ENCODING 'UTF8' 
    LC_COLLATE='ru_RU.UTF-8' 
    LC_CTYPE='ru_RU.UTF-8'
    TEMPLATE=template0;
```
### Создание пользователя
```sql
CREATE USER lada_user WITH PASSWORD 'LadaCMS2025';
```

### Выдача прав
```sql
GRANT ALL PRIVILEGES ON DATABASE lada_cms_pg TO lada_user;
```

### Назначение владельца
```sql
ALTER DATABASE lada_cms_pg OWNER TO lada_user;
```

### Подключение к новой базе
```sql
\c lada_cms_pg
```

### Права на схему public
```sql
GRANT ALL ON SCHEMA public TO lada_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO lada_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO lada_user;

```

### Выход
```sql
\q
```

### Выход из пользователя postgres
```bash
exit
```

## 3. Подключение к базе данных

### Интерактивное подключение
```bash
psql -h localhost -U lada_user -d lada_cms_pg
```

### Пароль
```bash
LadaCMS2025
```
### Строка подключения
```bash
psql "postgresql://lada_user:LadaCMS2025@localhost:5432/lada_cms_pg"
```

## 4. Основные команды psql

### Список всех баз данных
```sql
\l
```

### Подключение к базе
```sql
\c lada_cms_pg
```

### Список всех таблиц
```sql
\dt
```

### Структура таблицы
```sql
\d users
```

### Список пользователей
```sql
\du
```

### Выход
```sql
\q
```

## 5. Просмотр данных

### Все пользователи
```sql
SELECT * FROM users;
```

### Первые 10 записей
```sql
SELECT * FROM users LIMIT 10;
```

### Количество записей
```sql
SELECT COUNT(*) FROM users;
```

### Структура таблицы
```sql
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'users';
```

## Бэкап и восстановление

### Создание дампа
```bash
pg_dump -h localhost -U lada_user -d lada_cms_pg > backup.sql
```

### Восстановление
```bash
psql -h localhost -U lada_user -d lada_cms_pg < backup.sql
```

### Только схема
```bash
pg_dump -h localhost -U lada_user -d lada_cms_pg --schema-only > schema.sql
```

### Только данные
```bash
pg_dump -h localhost -U lada_user -d lada_cms_pg --data-only > data.sql
```

## 7. Диагностика

### Проверка порта
```bash
sudo netstat -tlnp | grep 5432
```

### Просмотр логов
```bash
sudo tail -f /var/log/postgresql/postgresql-16-main.log
```

### Перезапуск
```bash
sudo systemctl restart postgresql
```

### Сброс пароля
```bash
sudo -u postgres psql -c "ALTER USER lada_user WITH PASSWORD 'LadaCMS2025';"
```

## 8. Данные для подключения

```yaml
Host: localhost
Port: 5432
Database: lada_cms_pg
Username: lada_user
Password: LadaCMS2025
```
==========================================================

## 9. Сравнение с MySQL

| MySQL          | PostgreSQL              |
|----------------|-------------------------|
| SHOW DATABASES | \l                      |
| SHOW TABLES    | \dt                     |
| DESCRIBE table | \d table                |
| USE database   | \c database             |
| AUTO_INCREMENT | SERIAL                  |
| bit(1)         | BOOLEAN                 |
| datetime       | TIMESTAMP               |
| ENUM           | VARCHAR или CREATE TYPE |
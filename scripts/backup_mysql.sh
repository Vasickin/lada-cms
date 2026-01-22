#!/bin/bash
# MySQL Backup Script for Lada CMS (Project-based)
# Скрипт резервного копирования MySQL для Lada CMS

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_DIR="$PROJECT_DIR/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="lada_cms"
DB_USER="lada_user"

echo "Starting backup for Lada CMS..."
echo "Начинаем резервное копирование Lada CMS..."

# Создаем директорию если нет
mkdir -p $BACKUP_DIR

# Бэкап базы данных
echo "Backing up database..."
echo "Создаем резервную копию базы данных..."
mysqldump -u $DB_USER -p"Lada_CMS_2025!Secure" $DB_NAME > $BACKUP_DIR/${DB_NAME}_${DATE}.sql

if [ $? -eq 0 ]; then
    # Сжимаем бэкап
    gzip $BACKUP_DIR/${DB_NAME}_${DATE}.sql
    
    # Удаляем бэкапы старше 7 дней
    find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
    
    echo "✅ Backup created: ${DB_NAME}_${DATE}.sql.gz"
    echo "✅ Резервная копия создана: ${DB_NAME}_${DATE}.sql.gz"
    echo "📍 Location: $BACKUP_DIR/"
    echo "📍 Расположение: $BACKUP_DIR/"
else
    echo "❌ Backup failed!"
    echo "❌ Ошибка создания резервной копии!"
    exit 1
fi

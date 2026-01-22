#!/bin/bash
# MySQL Restore Script for Lada CMS
# Скрипт восстановления MySQL для Lada CMS

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_DIR="$PROJECT_DIR/backups/mysql"
DB_NAME="lada_cms"
DB_USER="lada_user"

echo "Available backups:"
echo "Доступные резервные копии:"
ls -la $BACKUP_DIR/*.sql.gz 2>/dev/null | head -10

if [ $? -ne 0 ]; then
    echo "❌ No backups found in $BACKUP_DIR"
    echo "❌ Резервные копии не найдены в $BACKUP_DIR"
    exit 1
fi

echo ""
echo "Enter backup filename to restore (without path):"
echo "Введите имя файла для восстановления (без пути):"
read backup_file

FULL_PATH="$BACKUP_DIR/$backup_file"

if [ ! -f "$FULL_PATH" ]; then
    echo "❌ Backup file not found: $backup_file"
    echo "❌ Файл резервной копии не найден: $backup_file"
    exit 1
fi

echo "Restoring from: $backup_file"
echo "Восстанавливаем из: $backup_file"

# Распаковываем и восстанавливаем
gunzip -c $FULL_PATH | mysql -u $DB_USER -p"Lada_CMS_2025!Secure" $DB_NAME

if [ $? -eq 0 ]; then
    echo "✅ Database restored successfully!"
    echo "✅ База данных успешно восстановлена!"
else
    echo "❌ Database restoration failed!"
    echo "❌ Ошибка восстановления базы данных!"
    exit 1
fi

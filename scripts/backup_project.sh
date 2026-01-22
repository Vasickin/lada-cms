#!/bin/bash
# Project Backup Script for Lada CMS
# Скрипт резервного копирования проекта Lada CMS

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_DIR="$PROJECT_DIR/backups/project"
DATE=$(date +%Y%m%d_%H%M%S)

echo "Starting project backup..."
echo "Начинаем резервное копирование проекта..."

mkdir -p $BACKUP_DIR

# Бэкап проекта (исключаем target, backups и .git)
echo "Creating project archive..."
echo "Создаем архив проекта..."
tar -czf $BACKUP_DIR/lada_cms_project_${DATE}.tar.gz \
  --exclude=target \
  --exclude=backups \
  --exclude=.git \
  -C $PROJECT_DIR .

if [ $? -eq 0 ]; then
    # Удаляем старые бэкапы (старше 30 дней)
    find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
    
    echo "✅ Project backup created: lada_cms_project_${DATE}.tar.gz"
    echo "✅ Резервная копия проекта создана: lada_cms_project_${DATE}.tar.gz"
    echo "📍 Location: $BACKUP_DIR/"
    echo "📍 Расположение: $BACKUP_DIR/"
else
    echo "❌ Project backup failed!"
    echo "❌ Ошибка создания резервной копии проекта!"
    exit 1
fi

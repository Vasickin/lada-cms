# Команды системы бэкапов

## Бэкап базы данных
./scripts/backup_mysql.sh

## Бэкап проекта  
./scripts/backup_project.sh

## Восстановление базы данных
./scripts/restore_mysql.sh

## Просмотр бэкапов БД
ls -la backups/mysql/

## Просмотр бэкапов проекта
ls -la backups/project/

## Полный бэкап (БД + проект)
./scripts/backup_mysql.sh && ./scripts/backup_project.sh

package com.community.cms.domain.service.people;

import com.community.cms.domain.enums.PartnerType;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.repository.people.PartnerRepository;
import com.community.cms.infrastructure.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления партнерами организации "ЛАДА".
 *
 * <p>Предоставляет бизнес-логику для работы с партнерами,
 * включая управление участием в проектах.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Partner
 * @see PartnerRepository
 */
@Service
@Transactional
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final FileStorageService fileStorageService;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param partnerRepository репозиторий для работы с партнерами
     * @param fileStorageService сервис для работы с файлами
     */
    @Autowired
    public PartnerService(PartnerRepository partnerRepository,
                          FileStorageService fileStorageService) {
        this.partnerRepository = partnerRepository;
        this.fileStorageService = fileStorageService;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет партнера.
     *
     * @param partner партнер для сохранения
     * @return сохраненный партнер
     */
    public Partner save(Partner partner) {

// ======== ЛОГИРОВАНИЕ ========
        System.out.println("=== PartnerService.save() START ===");
        System.out.println("Partner name: " + partner.getName());
        System.out.println("Partner.logoFile: " + partner.getLogoFile());
        System.out.println("Partner.logoFile is null? " + (partner.getLogoFile() == null));
        System.out.println("Partner.logoFile isEmpty? " +
                (partner.getLogoFile() != null ? partner.getLogoFile().isEmpty() : "N/A"));


        validatePartner(partner);
        processLogoFile(partner);

        System.out.println("After processLogoFile - partner.logoUrl: " + partner.getLogoUrl());

        Partner saved = partnerRepository.save(partner);

        System.out.println("After repository.save - saved.logoUrl: " + saved.getLogoUrl());
        System.out.println("=== PartnerService.save() END ===\n");

        return partnerRepository.save(partner);
    }

    /**
     * Обновляет существующего партнера.
     *
     * @param partner партнер для обновления
     * @return обновленный партнер
     */
    public Partner update(Partner partner) {
        validatePartner(partner);
        processLogoFile(partner);
        return partnerRepository.save(partner);
    }

    /**
     * Находит партнера по ID.
     *
     * @param id идентификатор партнера
     * @return Optional с партнером, если найден
     */
    @Transactional(readOnly = true)
    public Optional<Partner> findById(Long id) {
        return partnerRepository.findById(id);
    }

    /**
     * Удаляет партнера по ID.
     *
     * @param id идентификатор партнера для удаления
     */
    public void deleteById(Long id) {
        // Удаляем файл логотипа если есть
        Optional<Partner> partnerOpt = partnerRepository.findById(id);
        if (partnerOpt.isPresent()) {
            Partner partner = partnerOpt.get();
            if (partner.hasLogo()) {
                deleteLogoFile(partner.getLogoUrl());
            }
        }

        partnerRepository.deleteById(id);
    }

    // ================== ОБРАБОТКА ЛОГОТИПА ==================

    /**
     * Обрабатывает загрузку файла логотипа.
     */
    private void processLogoFile(Partner partner) {
        MultipartFile logoFile = partner.getLogoFile();

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                // Валидируем файл
                fileStorageService.validateFile(logoFile);

                // Если есть старый логотип - удаляем
                if (partner.hasLogo()) {
                    deleteLogoFile(partner.getLogoUrl());
                }

                // Сохраняем новый файл
                String fileName = fileStorageService.storeFile(logoFile);

                // Сохраняем путь к файлу
                partner.setLogoUrl("/uploads/" + fileName);

            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при обработке логотипа: " + e.getMessage());
            }
        }
    }

    /**
     * Удаляет файл логотипа.
     */
    private void deleteLogoFile(String logoUrl) {
        if (logoUrl != null && logoUrl.startsWith("/uploads/")) {
            try {
                String fileName = logoUrl.substring("/uploads/".length());
                fileStorageService.deleteFile(fileName);
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем выполнение
                System.err.println("Ошибка при удалении файла логотипа: " + e.getMessage());
            }
        }
    }

    // ================== ПОИСК ПО АКТИВНОСТИ ==================

    /**
     * Находит всех активных партнеров.
     *
     * @return список активных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllActive() {
        return partnerRepository.findByActiveTrue();
    }

    /**
     * Находит всех активных партнеров, отсортированных по порядку сортировки.
     *
     * @return список активных партнеров (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllActiveOrderBySortOrder() {
        return partnerRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Находит всех активных партнеров, отсортированных по названию.
     *
     * @return список активных партнеров (по названию)
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllActiveOrderByName() {
        return partnerRepository.findByActiveTrueOrderByNameAsc();
    }

    /**
     * Находит всех неактивных партнеров.
     *
     * @return список неактивных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllInactive() {
        return partnerRepository.findByActiveFalse();
    }

    // ================== ПОИСК ПО НАЗВАНИЮ И ОПИСАНИЮ ==================

    /**
     * Находит партнеров по части названия (без учета регистра).
     *
     * @param name фрагмент названия для поиска
     * @return список найденных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findByNameContaining(String name) {
        return partnerRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Находит активных партнеров по части названия (без учета регистра).
     *
     * @param name фрагмент названия для поиска
     * @return список найденных активных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveByNameContaining(String name) {
        return partnerRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    /**
     * Комплексный поиск партнеров по названию или описанию (без учета регистра).
     *
     * @param searchTerm поисковый запрос
     * @return список найденных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> searchByNameOrDescription(String searchTerm) {
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        return partnerRepository.searchByNameOrDescription(searchPattern);
    }

    /**
     * Комплексный поиск активных партнеров по названию или описанию.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных активных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> searchActiveByNameOrDescription(String searchTerm) {
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        return partnerRepository.searchActiveByNameOrDescription(searchPattern);
    }

    // ================== ПОИСК ПО ТИПУ ПАРТНЕРА ==================

    /**
     * Находит партнеров по типу партнерства.
     *
     * @param type тип партнера
     * @return список партнеров указанного типа
     */
    @Transactional(readOnly = true)
    public List<Partner> findByType(PartnerType type) {
        return partnerRepository.findByType(type);
    }

    /**
     * Находит активных партнеров по типу партнерства.
     *
     * @param type тип партнера
     * @return список активных партнеров указанного типа
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveByType(PartnerType type) {
        return partnerRepository.findByTypeAndActiveTrue(type);
    }

    /**
     * Находит спонсоров.
     *
     * @return список спонсоров
     */
    @Transactional(readOnly = true)
    public List<Partner> findSponsors() {
        return partnerRepository.findSponsors();
    }

    /**
     * Находит активных спонсоров.
     *
     * @return список активных спонсоров
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveSponsors() {
        return partnerRepository.findActiveSponsors();
    }

    /**
     * Находит информационных партнеров.
     *
     * @return список информационных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findInformationPartners() {
        return partnerRepository.findInformationPartners();
    }

    /**
     * Находит активных информационных партнеров.
     *
     * @return список активных информационных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveInformationPartners() {
        return partnerRepository.findActiveInformationPartners();
    }

    /**
     * Находит технических партнеров.
     *
     * @return список технических партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findTechnicalPartners() {
        return partnerRepository.findTechnicalPartners();
    }

    /**
     * Находит активных технических партнеров.
     *
     * @return список активных технических партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveTechnicalPartners() {
        return partnerRepository.findActiveTechnicalPartners();
    }

    // ================== РАБОТА С ПРОЕКТАМИ ==================

    /**
     * Находит партнеров, участвующих в указанном проекте.
     *
     * @param project проект
     * @return список партнеров участвующих в проекте
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProject(Project project) {
        return partnerRepository.findByProject(project);
    }

    /**
     * Находит партнеров, участвующих в указанном проекте, отсортированных по порядку сортировки.
     *
     * @param project проект
     * @return список партнеров участвующих в проекте (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectOrderBySortOrder(Project project) {
        return partnerRepository.findByProjectOrderBySortOrder(project);
    }

    /**
     * Находит партнеров, участвующих в проекте по ID проекта.
     *
     * @param projectId ID проекта
     * @return список партнеров участвующих в проекте
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectId(Long projectId) {
        return partnerRepository.findByProjectId(projectId);
    }

    /**
     * Находит партнеров, НЕ участвующих в указанном проекте.
     *
     * @param project проект
     * @return список партнеров не участвующих в проекте
     */
    @Transactional(readOnly = true)
    public List<Partner> findNotInProject(Project project) {
        return partnerRepository.findNotInProject(project);
    }

    /**
     * Находит партнеров, не имеющих проектов.
     *
     * @return список партнеров без проектов
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutProjects() {
        return partnerRepository.findWithoutProjects();
    }

    // ================== СОРТИРОВКА ==================

    /**
     * Находит всех партнеров, отсортированных по названию (A-Z).
     *
     * @return список всех партнеров отсортированных по названию
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllOrderByName() {
        return partnerRepository.findAllByOrderByNameAsc();
    }

    /**
     * Находит всех партнеров, отсортированных по порядку сортировки.
     *
     * @return список всех партнеров отсортированных по sortOrder
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllOrderBySortOrder() {
        return partnerRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Находит всех партнеров, отсортированных по дате создания (новые сначала).
     *
     * @return список всех партнеров отсортированных по дате создания
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllOrderByCreatedDateDesc() {
        return partnerRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Находит всех партнеров, отсортированных по типу.
     *
     * @return список всех партнеров отсортированных по типу
     */
    @Transactional(readOnly = true)
    public List<Partner> findAllOrderByType() {
        return partnerRepository.findAllByOrderByTypeAsc();
    }

    // ================== СТАТИСТИКА И СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Подсчитывает количество активных партнеров.
     *
     * @return количество активных партнеров
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return partnerRepository.countByActiveTrue();
    }

    /**
     * Подсчитывает количество неактивных партнеров.
     *
     * @return количество неактивных партнеров
     */
    @Transactional(readOnly = true)
    public long countInactive() {
        return partnerRepository.countByActiveFalse();
    }

    /**
     * Подсчитывает количество партнеров указанного типа.
     *
     * @param type тип партнера
     * @return количество партнеров указанного типа
     */
    @Transactional(readOnly = true)
    public long countByType(PartnerType type) {
        return partnerRepository.countByType(type);
    }

    /**
     * Подсчитывает количество активных партнеров указанного типа.
     *
     * @param type тип партнера
     * @return количество активных партнеров указанного типа
     */
    @Transactional(readOnly = true)
    public long countActiveByType(PartnerType type) {
        return partnerRepository.countByTypeAndActiveTrue(type);
    }

    /**
     * Находит партнеров с логотипом.
     *
     * @return список партнеров с логотипом
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithLogo() {
        return partnerRepository.findByLogoUrlIsNotNull();
    }

    /**
     * Находит партнеров без логотипа.
     *
     * @return список партнеров без логотипа
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutLogo() {
        return partnerRepository.findByLogoUrlIsNull();
    }

    /**
     * Находит партнеров с описанием.
     *
     * @return список партнеров с описанием
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithDescription() {
        return partnerRepository.findWithDescription();
    }

    /**
     * Находит партнеров без описания.
     *
     * @return список партнеров без описания
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutDescription() {
        return partnerRepository.findWithoutDescription();
    }

    /**
     * Находит партнеров с веб-сайтом.
     *
     * @return список партнеров с веб-сайтом
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithWebsite() {
        return partnerRepository.findWithWebsite();
    }

    /**
     * Находит партнеров без веб-сайта.
     *
     * @return список партнеров без веб-сайта
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutWebsite() {
        return partnerRepository.findWithoutWebsite();
    }

    /**
     * Находит последних N добавленных партнеров.
     *
     * @param limit количество партнеров
     * @return список последних добавленных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findRecentPartners(int limit) {
        return partnerRepository.findRecentPartners(limit);
    }

    /**
     * Находит ключевых партнеров (с высокой позицией в сортировке).
     *
     * @param limit количество партнеров
     * @return список ключевых партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findKeyPartners(int limit) {
        return partnerRepository.findKeyPartners(limit);
    }

    // ================== РАБОТА С КОНТАКТАМИ ==================

    /**
     * Проверяет существование партнера с указанным email.
     *
     * @param email email для проверки
     * @return true если партнер с таким email существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return partnerRepository.existsByContactEmail(email);
    }

    /**
     * Находит партнера по email.
     *
     * @param email email для поиска
     * @return Optional с партнером, если найден
     */
    @Transactional(readOnly = true)
    public Optional<Partner> findByEmail(String email) {
        return partnerRepository.findByContactEmail(email);
    }

    /**
     * Находит активного партнера по email.
     *
     * @param email email для поиска
     * @return Optional с активным партнером, если найден
     */
    @Transactional(readOnly = true)
    public Optional<Partner> findActiveByEmail(String email) {
        return partnerRepository.findByContactEmailAndActiveTrue(email);
    }

    /**
     * Проверяет существование партнера с указанным сайтом.
     *
     * @param website сайт для проверки
     * @return true если партнер с таким сайтом существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByWebsite(String website) {
        return partnerRepository.existsByWebsite(website);
    }

    /**
     * Находит партнеров по контактному лицу.
     *
     * @param contactPerson имя контактного лица
     * @return список партнеров с указанным контактным лицом
     */
    @Transactional(readOnly = true)
    public List<Partner> findByContactPerson(String contactPerson) {
        return partnerRepository.findByContactPersonContainingIgnoreCase(contactPerson);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для партнера.
     *
     * @param partner партнер для валидации
     * @throws IllegalArgumentException если партнер невалиден
     */
    private void validatePartner(Partner partner) {
        if (partner == null) {
            throw new IllegalArgumentException("Партнер не может быть null");
        }

        if (partner.getName() == null || partner.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название партнера обязательно");
        }

        if (partner.getSortOrder() == null) {
            partner.setSortOrder(0);
        }
    }

    /**
     * Создает нового партнера.
     *
     * @param name название организации
     * @param type тип партнерства
     * @param description описание (опционально)
     * @param logoUrl URL логотипа (опционально)
     * @param website сайт (опционально)
     * @param contactEmail email для контактов (опционально)
     * @param contactPhone телефон (опционально)
     * @param contactPerson контактное лицо (опционально)
     * @param sortOrder порядок сортировки
     * @return созданный партнер
     */
    public Partner createPartner(String name, PartnerType type,
                                 String description, String logoUrl,
                                 String website, String contactEmail,
                                 String contactPhone, String contactPerson,
                                 Integer sortOrder) {
        Partner partner = new Partner(name, type);
        partner.setDescription(description);
        partner.setLogoUrl(logoUrl);
        partner.setWebsite(website);
        partner.setContactEmail(contactEmail);
        partner.setContactPhone(contactPhone);
        partner.setContactPerson(contactPerson);
        partner.setSortOrder(sortOrder != null ? sortOrder : 0);
        partner.setActive(true);

        return save(partner);
    }

    /**
     * Активирует партнера.
     *
     * @param partner партнер для активации
     * @return активированный партнер
     */
    public Partner activate(Partner partner) {
        partner.setActive(true);
        return partnerRepository.save(partner);
    }

    /**
     * Деактивирует партнера.
     *
     * @param partner партнер для деактивации
     * @return деактивированный партнер
     */
    public Partner deactivate(Partner partner) {
        partner.setActive(false);
        return partnerRepository.save(partner);
    }

    /**
     * Активирует партнера по ID.
     *
     * @param id ID партнера
     * @return активированный партнер
     * @throws IllegalArgumentException если партнер не найден
     */
    public Partner activateById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Партнер с ID " + id + " не найден"));
        return activate(partner);
    }

    /**
     * Деактивирует партнера по ID.
     *
     * @param id ID партнера
     * @return деактивированный партнер
     * @throws IllegalArgumentException если партнер не найден
     */
    public Partner deactivateById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Партнер с ID " + id + " не найден"));
        return deactivate(partner);
    }

    /**
     * Добавляет проект к партнеру.
     *
     * @param partner партнер
     * @param project проект для добавления
     * @return обновленный партнер
     */
    public Partner addProjectToPartner(Partner partner, Project project) {
        partner.addProject(project);
        return partnerRepository.save(partner);
    }

    /**
     * Удаляет проект у партнера.
     *
     * @param partner партнер
     * @param project проект для удаления
     * @return обновленный партнер
     */
    public Partner removeProjectFromPartner(Partner partner, Project project) {
        partner.removeProject(project);
        return partnerRepository.save(partner);
    }

    // ================== ПРОВЕРКИ И УТИЛИТЫ ==================

    /**
     * Проверяет имеет ли партнер логотип.
     *
     * @param partner партнер
     * @return true если у партнера есть логотип, иначе false
     */
    public boolean hasLogo(Partner partner) {
        return partner.hasLogo();
    }

    /**
     * Проверяет имеет ли партнер описание.
     *
     * @param partner партнер
     * @return true если у партнера есть описание, иначе false
     */
    public boolean hasDescription(Partner partner) {
        return partner.hasDescription();
    }

    /**
     * Проверяет имеет ли партнер сайт.
     *
     * @param partner партнер
     * @return true если у партнера есть сайт, иначе false
     */
    public boolean hasWebsite(Partner partner) {
        return partner.hasWebsite();
    }

    /**
     * Проверяет указано ли контактное лицо.
     *
     * @param partner партнер
     * @return true если у партнера указано контактное лицо, иначе false
     */
    public boolean hasContactPerson(Partner partner) {
        return partner.hasContactPerson();
    }

    /**
     * Получает количество проектов, в которых участвует партнер.
     *
     * @param partner партнер
     * @return количество проектов
     */
    public int getProjectsCount(Partner partner) {
        return partner.getProjectsCount();
    }

    /**
     * Получает отображаемое название типа партнера.
     *
     * @param partner партнер
     * @return отображаемое название типа
     */
    public String getTypeDisplayName(Partner partner) {
        return partner.getTypeDisplayName();
    }

    /**
     * Проверяет участвует ли партнер в указанном проекте.
     *
     * @param partner партнер
     * @param project проект для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Partner partner, Project project) {
        return partner.participatesInProject(project);
    }

    /**
     * Проверяет участвует ли партнер в проекте по ID.
     *
     * @param partner партнер
     * @param projectId ID проекта для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(Partner partner, Long projectId) {
        return partner.participatesInProject(projectId);
    }

    /**
     * Обновляет порядок сортировки партнеров.
     *
     * @param partners список партнеров с обновленными sortOrder
     * @return список обновленных партнеров
     */
    public List<Partner> updateSortOrder(List<Partner> partners) {
        return partnerRepository.saveAll(partners);
    }

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит всех активных партнеров с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница активных партнеров
     */
    @Transactional(readOnly = true)
    public Page<Partner> findAllActive(Pageable pageable) {
        return partnerRepository.findByActiveTrue(pageable);
    }

    /**
     * Находит всех партнеров с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница всех партнеров
     */
    @Transactional(readOnly = true)
    public Page<Partner> findAll(Pageable pageable) {
        return partnerRepository.findAll(pageable);
    }

    /**
     * Находит всех неактивных партнеров с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница неактивных партнеров
     */
    @Transactional(readOnly = true)
    public Page<Partner> findAllInactiveWithPagination(Pageable pageable) {
        // Нужно добавить метод в репозиторий: Page<Partner> findByActiveFalse(Pageable pageable);
        // Временно используем создание Page из List
        List<Partner> inactivePartners = findAllInactive(); // Используем существующий метод
        return createPageFromList(inactivePartners, pageable);
    }

    /**
     * Находит партнеров по типу с пагинацией.
     *
     * @param type тип партнера
     * @param pageable параметры пагинации
     * @return страница партнеров указанного типа
     */
    @Transactional(readOnly = true)
    public Page<Partner> findByTypeWithPagination(PartnerType type, Pageable pageable) {
        // Нужно добавить метод в репозиторий: Page<Partner> findByType(PartnerType type, Pageable pageable);
        // Временно используем создание Page из List
        List<Partner> partnersByType = findByType(type); // Используем существующий метод
        return createPageFromList(partnersByType, pageable);
    }

    /**
     * Поиск партнеров с пагинацией.
     *
     * @param searchTerm поисковый запрос
     * @param pageable параметры пагинации
     * @return страница с результатами поиска
     */
    @Transactional(readOnly = true)
    public Page<Partner> searchByNameOrDescriptionWithPagination(String searchTerm, Pageable pageable) {
        // Нужно добавить метод в репозиторий для поиска с пагинацией
        // Временно используем создание Page из List
        List<Partner> searchResults = searchByNameOrDescription(searchTerm);
        return createPageFromList(searchResults, pageable);
    }

    /**
     * Вспомогательный метод для создания Page из List.
     * Временное решение до добавления пагинации в репозиторий.
     */
    private Page<Partner> createPageFromList(List<Partner> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return Page.empty(pageable);
        }

        List<Partner> pageContent = list.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, list.size());
    }
}
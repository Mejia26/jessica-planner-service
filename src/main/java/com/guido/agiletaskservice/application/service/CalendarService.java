package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.CalendarDtos;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.CalendarCategoryEntity;
import com.guido.agiletaskservice.domain.entity.CalendarEventEntity;
import com.guido.agiletaskservice.domain.enums.CalendarEventStatus;
import com.guido.agiletaskservice.domain.repository.CalendarCategoryRepository;
import com.guido.agiletaskservice.domain.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final String DEFAULT_TIME_ZONE = "UTC";

    private final CalendarEventRepository calendarEventRepository;
    private final CalendarCategoryRepository calendarCategoryRepository;

    @Transactional
    public CalendarDtos.CalendarEventResponse createEvent(UUID ownerUserId, CalendarDtos.CreateCalendarEventRequest request) {
        CalendarEventEntity event = new CalendarEventEntity();
        applyEventValues(event, ownerUserId, request.title(), request.notes(), request.location(), request.startAt(), request.endAt(),
                request.allDay(), request.timeZone(), request.categoryKey(), request.color(), request.status(), request.reminderAt());
        CalendarEventEntity savedEvent = calendarEventRepository.save(event);
        log.info("Created personal calendar event: eventId={}, ownerUserId={}, startAt={}", savedEvent.getId(), ownerUserId, savedEvent.getStartAt());
        return ApiMapper.toCalendarEventResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public CalendarDtos.CalendarEventResponse getEvent(UUID ownerUserId, UUID eventId) {
        return ApiMapper.toCalendarEventResponse(getActiveEvent(ownerUserId, eventId));
    }

    @Transactional(readOnly = true)
    public Page<CalendarDtos.CalendarEventResponse> searchEvents(
            UUID ownerUserId,
            Instant from,
            Instant to,
            String categoryKey,
            CalendarEventStatus status,
            String text,
            Pageable pageable
    ) {
        String normalizedCategory = normalizeKey(categoryKey);
        String normalizedText = StringUtils.hasText(text) ? text.trim() : null;
        return calendarEventRepository.findAll(calendarEventSpec(ownerUserId, from, to, normalizedCategory, status, normalizedText), pageable)
                .map(ApiMapper::toCalendarEventResponse);
    }

    @Transactional(readOnly = true)
    public List<CalendarDtos.CalendarEventResponse> listEventsForDate(UUID ownerUserId, LocalDate date, String timeZone) {
        ZoneId zoneId = resolveZone(timeZone);
        Instant from = date.atStartOfDay(zoneId).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        return calendarEventRepository.findAll(calendarEventSpec(ownerUserId, from, to, null, null, null), Sort.by(Sort.Direction.ASC, "startAt"))
                .stream()
                .map(ApiMapper::toCalendarEventResponse)
                .toList();
    }

    @Transactional
    public CalendarDtos.CalendarEventResponse updateEvent(UUID ownerUserId, UUID eventId, CalendarDtos.UpdateCalendarEventRequest request) {
        CalendarEventEntity event = getActiveEvent(ownerUserId, eventId);
        applyEventValues(event, ownerUserId, request.title(), request.notes(), request.location(), request.startAt(), request.endAt(),
                request.allDay(), request.timeZone(), request.categoryKey(), request.color(), request.status(), request.reminderAt());
        CalendarEventEntity savedEvent = calendarEventRepository.save(event);
        log.info("Updated personal calendar event: eventId={}, ownerUserId={}", savedEvent.getId(), ownerUserId);
        return ApiMapper.toCalendarEventResponse(savedEvent);
    }

    @Transactional
    public void archiveEvent(UUID ownerUserId, UUID eventId) {
        CalendarEventEntity event = getActiveEvent(ownerUserId, eventId);
        event.softDelete(ownerUserId);
        calendarEventRepository.save(event);
        log.info("Archived personal calendar event: eventId={}, ownerUserId={}", eventId, ownerUserId);
    }

    @Transactional
    public CalendarDtos.CalendarCategoryResponse createCategory(UUID ownerUserId, CalendarDtos.CreateCalendarCategoryRequest request) {
        String key = normalizeKey(request.key());
        ensureCategoryKeyAvailable(ownerUserId, key, null);
        CalendarCategoryEntity category = new CalendarCategoryEntity();
        category.setOwnerUserId(ownerUserId);
        category.setKey(key);
        category.setName(request.name().trim());
        category.setDescription(request.description());
        category.setColor(request.color());
        category.setPosition(request.position() == null ? 0 : request.position());
        CalendarCategoryEntity savedCategory = calendarCategoryRepository.save(category);
        log.info("Created personal calendar category: categoryId={}, ownerUserId={}, key={}", savedCategory.getId(), ownerUserId, key);
        return ApiMapper.toCalendarCategoryResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CalendarDtos.CalendarCategoryResponse> listCategories(UUID ownerUserId) {
        return calendarCategoryRepository.findByOwnerUserIdAndDeletedAtIsNullOrderByPositionAscNameAsc(ownerUserId).stream()
                .map(ApiMapper::toCalendarCategoryResponse)
                .toList();
    }

    @Transactional
    public CalendarDtos.CalendarCategoryResponse updateCategory(UUID ownerUserId, UUID categoryId, CalendarDtos.UpdateCalendarCategoryRequest request) {
        CalendarCategoryEntity category = getActiveCategory(ownerUserId, categoryId);
        String key = normalizeKey(request.key());
        ensureCategoryKeyAvailable(ownerUserId, key, categoryId);
        category.setKey(key);
        category.setName(request.name().trim());
        category.setDescription(request.description());
        category.setColor(request.color());
        category.setPosition(request.position() == null ? 0 : request.position());
        CalendarCategoryEntity savedCategory = calendarCategoryRepository.save(category);
        log.info("Updated personal calendar category: categoryId={}, ownerUserId={}, key={}", categoryId, ownerUserId, key);
        return ApiMapper.toCalendarCategoryResponse(savedCategory);
    }

    @Transactional
    public void archiveCategory(UUID ownerUserId, UUID categoryId) {
        CalendarCategoryEntity category = getActiveCategory(ownerUserId, categoryId);
        category.softDelete(ownerUserId);
        calendarCategoryRepository.save(category);
        log.info("Archived personal calendar category: categoryId={}, ownerUserId={}", categoryId, ownerUserId);
    }

    private void applyEventValues(
            CalendarEventEntity event,
            UUID ownerUserId,
            String title,
            String notes,
            String location,
            Instant startAt,
            Instant endAt,
            Boolean allDay,
            String timeZone,
            String categoryKey,
            String color,
            CalendarEventStatus status,
            Instant reminderAt
    ) {
        validateEventWindow(startAt, endAt, reminderAt);
        String normalizedCategoryKey = normalizeKey(categoryKey);
        ensureCategoryExists(ownerUserId, normalizedCategoryKey);

        event.setOwnerUserId(ownerUserId);
        event.setTitle(title.trim());
        event.setNotes(notes);
        event.setLocation(location);
        event.setStartAt(startAt);
        event.setEndAt(endAt);
        event.setAllDay(Boolean.TRUE.equals(allDay));
        event.setTimeZone(StringUtils.hasText(timeZone) ? timeZone.trim() : DEFAULT_TIME_ZONE);
        event.setCategoryKey(normalizedCategoryKey);
        event.setColor(color);
        event.setStatus(status == null ? CalendarEventStatus.PLANNED : status);
        event.setReminderAt(reminderAt);
    }

    private CalendarEventEntity getActiveEvent(UUID ownerUserId, UUID eventId) {
        return calendarEventRepository.findById(eventId)
                .filter(event -> !event.isDeleted())
                .filter(event -> event.getOwnerUserId().equals(ownerUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event was not found."));
    }

    private Specification<CalendarEventEntity> calendarEventSpec(
            UUID ownerUserId,
            Instant from,
            Instant to,
            String categoryKey,
            CalendarEventStatus status,
            String text
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("ownerUserId"), ownerUserId));
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (from != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(root.get("endAt")),
                                criteriaBuilder.greaterThanOrEqualTo(root.get("endAt"), from)
                        ),
                        criteriaBuilder.and(
                                criteriaBuilder.isNull(root.get("endAt")),
                                criteriaBuilder.greaterThanOrEqualTo(root.get("startAt"), from)
                        )
                ));
            }

            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startAt"), to));
            }

            if (StringUtils.hasText(categoryKey)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("categoryKey")), categoryKey));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(text)) {
                String likeText = "%" + text.toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeText),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("notes"), "")), likeText),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("location"), "")), likeText)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private CalendarCategoryEntity getActiveCategory(UUID ownerUserId, UUID categoryId) {
        return calendarCategoryRepository.findById(categoryId)
                .filter(category -> !category.isDeleted())
                .filter(category -> category.getOwnerUserId().equals(ownerUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Calendar category was not found."));
    }

    private void ensureCategoryExists(UUID ownerUserId, String key) {
        if (!StringUtils.hasText(key) || calendarCategoryRepository.existsByOwnerUserIdAndKeyIgnoreCaseAndDeletedAtIsNull(ownerUserId, key)) {
            return;
        }
        CalendarCategoryEntity category = new CalendarCategoryEntity();
        category.setOwnerUserId(ownerUserId);
        category.setKey(key);
        category.setName(toDisplayName(key));
        category.setPosition(0);
        calendarCategoryRepository.save(category);
        log.info("Auto-created personal calendar category: ownerUserId={}, key={}", ownerUserId, key);
    }

    private void ensureCategoryKeyAvailable(UUID ownerUserId, String key, UUID currentCategoryId) {
        calendarCategoryRepository.findByOwnerUserIdAndKeyIgnoreCaseAndDeletedAtIsNull(ownerUserId, key)
                .filter(category -> currentCategoryId == null || !category.getId().equals(currentCategoryId))
                .ifPresent(category -> {
                    throw new BusinessRuleException("A calendar category with this key already exists for the user.");
                });
    }

    private void validateEventWindow(Instant startAt, Instant endAt, Instant reminderAt) {
        if (endAt != null && endAt.isBefore(startAt)) {
            throw new BusinessRuleException("Calendar event endAt cannot be before startAt.");
        }
        if (reminderAt != null && reminderAt.isAfter(startAt)) {
            throw new BusinessRuleException("Calendar event reminderAt cannot be after startAt.");
        }
    }

    private ZoneId resolveZone(String timeZone) {
        try {
            return StringUtils.hasText(timeZone) ? ZoneId.of(timeZone.trim()) : ZoneId.of(DEFAULT_TIME_ZONE);
        } catch (DateTimeException exception) {
            throw new BusinessRuleException("Invalid calendar time zone.");
        }
    }

    private String normalizeKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim()
                .replaceAll("[^a-zA-Z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase(Locale.ROOT);
    }

    private String toDisplayName(String key) {
        String value = key.replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT);
        return StringUtils.capitalize(value);
    }
}

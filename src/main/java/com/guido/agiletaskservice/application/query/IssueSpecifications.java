package com.guido.agiletaskservice.application.query;

import com.guido.agiletaskservice.domain.entity.IssueEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class IssueSpecifications {

    private IssueSpecifications() {
    }

    public static Specification<IssueEntity> byCriteria(IssueSearchCriteria criteria) {
        Specification<IssueEntity> specification = (root, query, cb) -> cb.isNull(root.get("deletedAt"));

        if (criteria.projectId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("project").get("id"), criteria.projectId()));
        }
        if (criteria.boardId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("board").get("id"), criteria.boardId()));
        }
        if (criteria.sprintId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("sprint").get("id"), criteria.sprintId()));
        }
        if (criteria.parentIssueId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("parentIssue").get("id"), criteria.parentIssueId()));
        }
        if (criteria.assigneeUserId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("assigneeUserId"), criteria.assigneeUserId()));
        }
        if (criteria.reporterUserId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("reporterUserId"), criteria.reporterUserId()));
        }
        if (criteria.type() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("type"), criteria.type()));
        }
        if (criteria.priority() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("priority"), criteria.priority()));
        }
        if (criteria.resolution() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("resolution"), criteria.resolution()));
        }
        if (criteria.classOfService() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("classOfService"), criteria.classOfService()));
        }
        if (StringUtils.hasText(criteria.status())) {
            specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("status")), criteria.status().toLowerCase()));
        }
        if (StringUtils.hasText(criteria.categoryKey())) {
            specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("categoryKey")), criteria.categoryKey().toLowerCase()));
        }
        if (StringUtils.hasText(criteria.componentKey())) {
            specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("componentKey")), criteria.componentKey().toLowerCase()));
        }
        if (StringUtils.hasText(criteria.labelKey())) {
            specification = specification.and((root, query, cb) -> {
                query.distinct(true);
                return cb.equal(cb.lower(root.join("labels")), criteria.labelKey().toLowerCase());
            });
        }
        if (Boolean.TRUE.equals(criteria.unassigned())) {
            specification = specification.and((root, query, cb) -> cb.isNull(root.get("assigneeUserId")));
        }
        if (Boolean.TRUE.equals(criteria.backlogOnly())) {
            specification = specification.and((root, query, cb) -> cb.isNull(root.get("sprint")));
        }
        if (StringUtils.hasText(criteria.text())) {
            specification = specification.and((root, query, cb) -> {
                    String value = "%" + criteria.text().toLowerCase() + "%";
                    return cb.or(
                            cb.like(cb.lower(root.get("title")), value),
                            cb.like(cb.lower(root.get("description")), value)
                    );
                });
        }
        if (criteria.dueFrom() != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), criteria.dueFrom()));
        }
        if (criteria.dueTo() != null) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), criteria.dueTo()));
        }

        return specification;
    }
}

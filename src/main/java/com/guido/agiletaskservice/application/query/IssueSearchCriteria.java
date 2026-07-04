package com.guido.agiletaskservice.application.query;

import com.guido.agiletaskservice.domain.enums.ClassOfService;
import com.guido.agiletaskservice.domain.enums.IssuePriority;
import com.guido.agiletaskservice.domain.enums.IssueResolution;
import com.guido.agiletaskservice.domain.enums.IssueType;

import java.time.LocalDate;
import java.util.UUID;

public record IssueSearchCriteria(
        UUID projectId,
        UUID boardId,
        UUID sprintId,
        UUID parentIssueId,
        UUID assigneeUserId,
        UUID reporterUserId,
        IssueType type,
        IssuePriority priority,
        IssueResolution resolution,
        ClassOfService classOfService,
        String status,
        String categoryKey,
        String componentKey,
        String labelKey,
        Boolean unassigned,
        Boolean backlogOnly,
        String text,
        LocalDate dueFrom,
        LocalDate dueTo
) {
}

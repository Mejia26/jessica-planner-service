package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.BoardDtos;
import com.guido.agiletaskservice.api.dto.IssueDtos;
import com.guido.agiletaskservice.api.dto.ProjectDtos;
import com.guido.agiletaskservice.api.dto.SprintDtos;
import com.guido.agiletaskservice.api.dto.WorkWeekDtos;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.domain.entity.BoardEntity;
import com.guido.agiletaskservice.domain.entity.IssueEntity;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.entity.SprintEntity;
import com.guido.agiletaskservice.domain.enums.BoardType;
import com.guido.agiletaskservice.domain.enums.ClassOfService;
import com.guido.agiletaskservice.domain.enums.IssuePriority;
import com.guido.agiletaskservice.domain.enums.IssueResolution;
import com.guido.agiletaskservice.domain.enums.IssueType;
import com.guido.agiletaskservice.domain.enums.SprintCompletionDestination;
import com.guido.agiletaskservice.domain.enums.SprintStatus;
import com.guido.agiletaskservice.domain.enums.StatusCategory;
import com.guido.agiletaskservice.domain.enums.WorkflowMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AgileBusinessRulesIntegrationTests {

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private ProjectService projectService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private WorkWeekService workWeekService;

    @Test
    void kanbanWipRejectsStandardWorkAndAllowsExpedite() {
        ProjectEntity project = createProject("KAN");
        BoardEntity board = createBoard(project, BoardType.KANBAN, WorkflowMode.OPEN, 1);

        issueService.create(issueRequest(project, board, "First task", "DOING", null, null, ClassOfService.STANDARD), USER_ID);

        assertThatThrownBy(() -> issueService.create(issueRequest(project, board, "Second task", "DOING", null, null, ClassOfService.STANDARD), USER_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("WIP limit");

        IssueEntity expedite = issueService.create(issueRequest(project, board, "Production outage", "DOING", null, null, ClassOfService.EXPEDITE), USER_ID);

        assertThat(expedite.getClassOfService()).isEqualTo(ClassOfService.EXPEDITE);
    }

    @Test
    void scrumStartFreezesBaselineAndCompletionMovesUnfinishedWorkToBacklog() {
        ProjectEntity project = createProject("SCR");
        BoardEntity board = createBoard(project, BoardType.SCRUM, WorkflowMode.OPEN, null);
        SprintEntity sprint = sprintService.create(new SprintDtos.CreateSprintRequest(project.getId(), "Sprint 1", "Baseline test", LocalDate.now(), LocalDate.now().plusDays(14)));
        IssueEntity planned = issueService.create(issueRequest(project, board, "Planned story", "TODO", sprint.getId(), null, ClassOfService.STANDARD), USER_ID);
        planned.setStoryPoints(5);

        SprintEntity active = sprintService.start(sprint.getId());

        assertThat(active.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        assertThat(active.getCommitmentBaselinePoints()).isEqualTo(5);
        assertThat(issueService.get(planned.getId()).getSprintScopeAdded()).isFalse();

        IssueEntity lateIssue = issueService.create(issueRequest(project, board, "Late scope", "TODO", active.getId(), null, ClassOfService.STANDARD), USER_ID);
        IssueEntity doneIssue = issueService.create(issueRequest(project, board, "Done scope", "DONE", active.getId(), IssueResolution.FIXED, ClassOfService.STANDARD), USER_ID);

        assertThat(issueService.get(lateIssue.getId()).getSprintScopeAdded()).isTrue();

        SprintEntity completed = sprintService.complete(active.getId(), new SprintDtos.CompleteSprintRequest(SprintCompletionDestination.BACKLOG, null));

        assertThat(completed.getStatus()).isEqualTo(SprintStatus.COMPLETED);
        assertThat(issueService.get(planned.getId()).getSprint()).isNull();
        assertThat(issueService.get(lateIssue.getId()).getSprint()).isNull();
        assertThat(issueService.get(doneIssue.getId()).getSprint().getId()).isEqualTo(active.getId());
    }

    @Test
    void doneStatusRequiresResolution() {
        ProjectEntity project = createProject("DON");
        BoardEntity board = createBoard(project, BoardType.SCRUM, WorkflowMode.OPEN, null);

        assertThatThrownBy(() -> issueService.create(issueRequest(project, board, "Done without resolution", "DONE", null, null, ClassOfService.STANDARD), USER_ID))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("resolution");
    }

    @Test
    void workWeekReturnsUiReadyWeeklyStructure() {
        LocalDate monday = LocalDate.of(2026, 7, 6);
        WorkWeekDtos.MeetingResponse meeting = workWeekService.toResponse(workWeekService.create(
                new WorkWeekDtos.CreateMeetingRequest(
                        monday,
                        "Daily stand-up",
                        "Review blockers.",
                        List.of(
                                new WorkWeekDtos.MeetingParticipantRequest("Harriet White", "Lead", null),
                                new WorkWeekDtos.MeetingParticipantRequest("External Camera Crew", "Vendor", "studio-crew")
                        )
                ),
                USER_ID
        ));

        WorkWeekDtos.WorkWeekResponse week = workWeekService.getWeek(monday.plusDays(2));

        assertThat(meeting.participants()).hasSize(2);
        assertThat(week.weekStart()).isEqualTo(monday);
        assertThat(week.weekEnd()).isEqualTo(monday.plusDays(6));
        assertThat(week.days()).hasSize(7);
        assertThat(week.days().getFirst().meetings()).hasSize(1);
        assertThat(week.days().getFirst().meetings().getFirst().participants())
                .extracting(WorkWeekDtos.MeetingParticipantResponse::displayName)
                .containsExactly("Harriet White", "External Camera Crew");
    }

    private ProjectEntity createProject(String key) {
        return projectService.create(new ProjectDtos.CreateProjectRequest(key + UUID.randomUUID().toString().substring(0, 4).toUpperCase(), key + " project", "Test project"), USER_ID);
    }

    private BoardEntity createBoard(ProjectEntity project, BoardType type, WorkflowMode workflowMode, Integer doingWipLimit) {
        return boardService.create(new BoardDtos.CreateBoardRequest(
                project.getId(),
                type + " board",
                type,
                workflowMode,
                List.of(
                        new BoardDtos.CreateBoardColumnRequest("To do", "TODO", StatusCategory.TO_DO, 0, null),
                        new BoardDtos.CreateBoardColumnRequest("Doing", "DOING", StatusCategory.IN_PROGRESS, 1, doingWipLimit),
                        new BoardDtos.CreateBoardColumnRequest("Done", "DONE", StatusCategory.DONE, 2, null)
                )
        ));
    }

    private IssueDtos.CreateIssueRequest issueRequest(ProjectEntity project, BoardEntity board, String title, String status, UUID sprintId, IssueResolution resolution, ClassOfService classOfService) {
        return new IssueDtos.CreateIssueRequest(
                project.getId(),
                board.getId(),
                sprintId,
                null,
                title,
                "Test issue",
                IssueType.STORY,
                IssuePriority.MEDIUM,
                status,
                resolution,
                classOfService,
                null,
                null,
                Set.of(),
                USER_ID,
                5,
                null,
                0
        );
    }
}

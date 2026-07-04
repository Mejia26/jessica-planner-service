package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.IssueDtos;
import com.guido.agiletaskservice.application.query.IssueSearchCriteria;
import com.guido.agiletaskservice.application.service.IssueService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import com.guido.agiletaskservice.common.web.UserContextResolver;
import com.guido.agiletaskservice.domain.enums.ClassOfService;
import com.guido.agiletaskservice.domain.enums.IssuePriority;
import com.guido.agiletaskservice.domain.enums.IssueResolution;
import com.guido.agiletaskservice.domain.enums.IssueType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/issues")
@Tag(name = "Issues", description = "Agile work item APIs for epics, stories, tasks, bugs, and subtasks.")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create issue", description = "Creates an agile work item. The reporter is resolved from X-User-Id.")
    @ApiResponse(responseCode = "201", description = "Issue created.")
    @ApiResponse(responseCode = "400", description = "Invalid request or business rule violation.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public IssueDtos.IssueResponse create(
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId,
            @Valid @RequestBody IssueDtos.CreateIssueRequest request
    ) {
        return issueService.createResponse(request, userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by id", description = "Returns an issue with planning and assignment metadata.")
    public IssueDtos.IssueResponse get(@PathVariable UUID id) {
        return issueService.getResponse(id);
    }

    @GetMapping
    @Operation(summary = "Search issues", description = "Returns paginated issues using filters commonly needed by a Jira-like frontend.")
    public Page<IssueDtos.IssueResponse> search(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID boardId,
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) UUID parentIssueId,
            @RequestParam(required = false) UUID assigneeUserId,
            @RequestParam(required = false) UUID reporterUserId,
            @RequestParam(required = false) IssueType type,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) IssueResolution resolution,
            @RequestParam(required = false) ClassOfService classOfService,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoryKey,
            @RequestParam(required = false) String componentKey,
            @RequestParam(required = false) String labelKey,
            @RequestParam(required = false) Boolean unassigned,
            @RequestParam(required = false) Boolean backlogOnly,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @PageableDefault(size = 50, sort = "position") Pageable pageable
    ) {
        IssueSearchCriteria criteria = new IssueSearchCriteria(
                projectId,
                boardId,
                sprintId,
                parentIssueId,
                assigneeUserId,
                reporterUserId,
                type,
                priority,
                resolution,
                classOfService,
                status,
                categoryKey,
                componentKey,
                labelKey,
                unassigned,
                backlogOnly,
                text,
                dueFrom,
                dueTo
        );
        return issueService.searchResponses(criteria, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update issue", description = "Updates issue details, assignment, priority, status, story points, and due date.")
    public IssueDtos.IssueResponse update(@PathVariable UUID id, @Valid @RequestBody IssueDtos.UpdateIssueRequest request) {
        return issueService.updateResponse(id, request);
    }

    @PatchMapping("/{id}/assignment")
    @Operation(summary = "Assign issue", description = "Assigns or unassigns an issue using a user id supplied by the user-management microservice.")
    public IssueDtos.IssueResponse assign(@PathVariable UUID id, @Valid @RequestBody IssueDtos.AssignIssueRequest request) {
        return issueService.assignResponse(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive issue", description = "Soft-deletes an issue so normal board and backlog views no longer show it while preserving history.")
    public void archive(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        issueService.archive(id, userId);
    }

    @PostMapping("/{id}/move")
    @Operation(summary = "Move issue", description = "Moves an issue between board columns, backlog, or sprint positions.")
    public IssueDtos.IssueResponse move(@PathVariable UUID id, @Valid @RequestBody IssueDtos.MoveIssueRequest request) {
        return issueService.moveResponse(id, request);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add issue comment", description = "Adds a comment to an issue. The author is resolved from X-User-Id.")
    public IssueDtos.CommentResponse addComment(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @Valid @RequestBody IssueDtos.CreateCommentRequest request
    ) {
        return com.guido.agiletaskservice.api.mapper.ApiMapper.toCommentResponse(issueService.addComment(id, request, userId));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List issue comments", description = "Returns comments ordered from oldest to newest.")
    public List<IssueDtos.CommentResponse> listComments(@PathVariable UUID id) {
        return issueService.listComments(id).stream()
                .map(com.guido.agiletaskservice.api.mapper.ApiMapper::toCommentResponse)
                .toList();
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive issue comment", description = "Soft-deletes an issue comment while preserving audit history.")
    public void archiveComment(
            @PathVariable UUID id,
            @PathVariable UUID commentId,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        issueService.archiveComment(id, commentId, userId);
    }

    @PostMapping(value = "/{id}/attachments", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload issue attachment", description = "Uploads an attachment through the configured ResourceStorageService implementation.")
    public IssueDtos.AttachmentResponse addAttachment(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER) UUID userId,
            @RequestParam("file") MultipartFile file
    ) {
        return com.guido.agiletaskservice.api.mapper.ApiMapper.toAttachmentResponse(issueService.addAttachment(id, file, userId));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "List issue attachments", description = "Returns attachment metadata ordered from newest to oldest.")
    public List<IssueDtos.AttachmentResponse> listAttachments(@PathVariable UUID id) {
        return issueService.listAttachments(id).stream()
                .map(com.guido.agiletaskservice.api.mapper.ApiMapper::toAttachmentResponse)
                .toList();
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive issue attachment", description = "Soft-deletes attachment metadata. The stored object is preserved for audit retention.")
    public void archiveAttachment(
            @PathVariable UUID id,
            @PathVariable UUID attachmentId,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        issueService.archiveAttachment(id, attachmentId, userId);
    }
}

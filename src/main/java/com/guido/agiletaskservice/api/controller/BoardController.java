package com.guido.agiletaskservice.api.controller;

import com.guido.agiletaskservice.api.dto.BoardDtos;
import com.guido.agiletaskservice.api.mapper.ApiMapper;
import com.guido.agiletaskservice.application.service.BoardService;
import com.guido.agiletaskservice.common.exception.ApiErrorResponse;
import com.guido.agiletaskservice.common.web.UserContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
@Tag(name = "Boards", description = "Scrum and Kanban board APIs.")
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create board", description = "Creates a Scrum or Kanban board with its initial workflow columns.")
    @ApiResponse(responseCode = "201", description = "Board created.")
    @ApiResponse(responseCode = "400", description = "Invalid request or duplicated status keys.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public BoardDtos.BoardResponse create(@Valid @RequestBody BoardDtos.CreateBoardRequest request) {
        var board = boardService.create(request);
        return ApiMapper.toBoardResponse(board, boardService.getColumns(board.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get board by id", description = "Returns a board with its ordered workflow columns.")
    @ApiResponse(responseCode = "404", description = "Board not found.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public BoardDtos.BoardResponse get(@PathVariable UUID id) {
        return ApiMapper.toBoardResponse(boardService.get(id), boardService.getColumns(id));
    }

    @GetMapping
    @Operation(summary = "List boards by project", description = "Returns boards for a project. This is useful for project dashboards and board switchers.")
    public List<BoardDtos.BoardResponse> listByProject(
            @RequestParam @Parameter(description = "Project id used to filter boards.", required = true) UUID projectId
    ) {
        return boardService.listByProject(projectId).stream()
                .map(board -> ApiMapper.toBoardResponse(board, boardService.getColumns(board.getId())))
                .toList();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update board", description = "Updates mutable board fields such as the display name.")
    public BoardDtos.BoardResponse update(@PathVariable UUID id, @Valid @RequestBody BoardDtos.UpdateBoardRequest request) {
        return ApiMapper.toBoardResponse(boardService.update(id, request), boardService.getColumns(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive board", description = "Soft-deletes a board while preserving its workflow and historical issues.")
    public void archive(
            @PathVariable UUID id,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId
    ) {
        boardService.archive(id, userId);
    }

    @PostMapping("/{boardId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create board column", description = "Adds a configurable workflow column to an existing Scrum or Kanban board.")
    public BoardDtos.BoardColumnResponse createColumn(
            @PathVariable UUID boardId,
            @Valid @RequestBody BoardDtos.CreateBoardColumnRequest request
    ) {
        return ApiMapper.toBoardColumnResponse(boardService.createColumn(boardId, request));
    }

    @GetMapping("/{boardId}/columns")
    @Operation(summary = "List board columns", description = "Returns active workflow columns ordered from left to right.")
    public List<BoardDtos.BoardColumnResponse> listColumns(@PathVariable UUID boardId) {
        return boardService.getColumns(boardId).stream()
                .map(ApiMapper::toBoardColumnResponse)
                .toList();
    }

    @PatchMapping("/{boardId}/columns/{columnId}")
    @Operation(summary = "Update board column", description = "Updates a workflow column and migrates existing issues if the status key changes.")
    public BoardDtos.BoardColumnResponse updateColumn(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @Valid @RequestBody BoardDtos.UpdateBoardColumnRequest request
    ) {
        return ApiMapper.toBoardColumnResponse(boardService.updateColumn(boardId, columnId, request));
    }

    @PostMapping("/{boardId}/columns/reorder")
    @Operation(summary = "Reorder board columns", description = "Reorders board workflow columns for Jira-like board customization.")
    public List<BoardDtos.BoardColumnResponse> reorderColumns(
            @PathVariable UUID boardId,
            @Valid @RequestBody BoardDtos.ReorderBoardColumnsRequest request
    ) {
        return boardService.reorderColumns(boardId, request).stream()
                .map(ApiMapper::toBoardColumnResponse)
                .toList();
    }

    @DeleteMapping("/{boardId}/columns/{columnId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Archive board column",
            description = "Soft-deletes a workflow column. If it has issues, send a replacement status key so issues are moved before archiving."
    )
    public void archiveColumn(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @RequestHeader(UserContextResolver.USER_ID_HEADER)
            @Parameter(description = "Authenticated user id supplied by the gateway.", required = true)
            UUID userId,
            @RequestBody(required = false) BoardDtos.ArchiveBoardColumnRequest request
    ) {
        boardService.archiveColumn(boardId, columnId, request, userId);
    }
}

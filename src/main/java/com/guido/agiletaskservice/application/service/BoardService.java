package com.guido.agiletaskservice.application.service;

import com.guido.agiletaskservice.api.dto.BoardDtos;
import com.guido.agiletaskservice.common.exception.BusinessRuleException;
import com.guido.agiletaskservice.common.exception.ResourceNotFoundException;
import com.guido.agiletaskservice.domain.entity.BoardColumnEntity;
import com.guido.agiletaskservice.domain.entity.BoardEntity;
import com.guido.agiletaskservice.domain.entity.ProjectEntity;
import com.guido.agiletaskservice.domain.enums.StatusCategory;
import com.guido.agiletaskservice.domain.enums.WorkflowMode;
import com.guido.agiletaskservice.domain.repository.BoardColumnRepository;
import com.guido.agiletaskservice.domain.repository.BoardRepository;
import com.guido.agiletaskservice.domain.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final IssueRepository issueRepository;
    private final ProjectService projectService;

    @Transactional
    public BoardEntity create(BoardDtos.CreateBoardRequest request) {
        ProjectEntity project = projectService.get(request.projectId());
        validateUniqueStatusKeys(request.columns());

        BoardEntity board = new BoardEntity();
        board.setProject(project);
        board.setName(request.name().trim());
        board.setType(request.type());
        board.setWorkflowMode(request.workflowMode() == null ? WorkflowMode.OPEN : request.workflowMode());
        BoardEntity savedBoard = boardRepository.save(board);

        request.columns().forEach(columnRequest -> boardColumnRepository.save(toColumn(savedBoard, columnRequest)));
        log.info("Created board: boardId={}, projectId={}, type={}", savedBoard.getId(), project.getId(), savedBoard.getType());
        return savedBoard;
    }

    @Transactional(readOnly = true)
    public BoardEntity get(UUID id) {
        return boardRepository.findById(id)
                .filter(board -> !board.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Board was not found."));
    }

    @Transactional(readOnly = true)
    public List<BoardEntity> listByProject(UUID projectId) {
        projectService.get(projectId);
        return boardRepository.findByProjectIdAndDeletedAtIsNullOrderByCreatedAtAsc(projectId);
    }

    @Transactional(readOnly = true)
    public List<BoardColumnEntity> getColumns(UUID boardId) {
        return boardColumnRepository.findByBoardIdAndDeletedAtIsNullOrderByPositionAsc(boardId);
    }

    @Transactional
    public void ensureStatusExists(UUID boardId, String status) {
        if (!boardColumnRepository.existsByBoardIdAndStatusKeyIgnoreCaseAndDeletedAtIsNull(boardId, status)) {
            throw new BusinessRuleException("Issue status must match an existing board column status key.");
        }
    }

    @Transactional(readOnly = true)
    public BoardColumnEntity getColumnByStatus(UUID boardId, String status) {
        return boardColumnRepository.findByBoardIdAndStatusKeyIgnoreCaseAndDeletedAtIsNull(boardId, normalizeStatusKey(status))
                .orElseThrow(() -> new BusinessRuleException("Issue status must match an existing board column status key."));
    }

    @Transactional(readOnly = true)
    public StatusCategory resolveStatusCategory(UUID boardId, String status) {
        return getColumnByStatus(boardId, status).getStatusCategory();
    }

    @Transactional
    public BoardEntity update(UUID id, BoardDtos.UpdateBoardRequest request) {
        BoardEntity board = get(id);
        board.setName(request.name().trim());
        BoardEntity savedBoard = boardRepository.save(board);
        log.info("Updated board: boardId={}", id);
        return savedBoard;
    }

    @Transactional
    public BoardColumnEntity createColumn(UUID boardId, BoardDtos.CreateBoardColumnRequest request) {
        BoardEntity board = get(boardId);
        String statusKey = normalizeStatusKey(request.statusKey());
        if (boardColumnRepository.existsByBoardIdAndStatusKeyIgnoreCaseAndDeletedAtIsNull(boardId, statusKey)) {
            throw new BusinessRuleException("Board column status key already exists.");
        }

        BoardColumnEntity column = toColumn(board, request);
        BoardColumnEntity savedColumn = boardColumnRepository.save(column);
        log.info("Created board column: boardId={}, columnId={}, statusKey={}", boardId, savedColumn.getId(), savedColumn.getStatusKey());
        return savedColumn;
    }

    @Transactional(readOnly = true)
    public BoardColumnEntity getColumn(UUID boardId, UUID columnId) {
        BoardColumnEntity column = boardColumnRepository.findById(columnId)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Board column was not found."));
        if (!column.getBoard().getId().equals(boardId)) {
            throw new ResourceNotFoundException("Board column was not found for the selected board.");
        }
        return column;
    }

    @Transactional
    public BoardColumnEntity updateColumn(UUID boardId, UUID columnId, BoardDtos.UpdateBoardColumnRequest request) {
        BoardColumnEntity column = getColumn(boardId, columnId);
        String previousStatusKey = column.getStatusKey();
        String nextStatusKey = normalizeStatusKey(request.statusKey());
        boolean statusKeyChanged = !previousStatusKey.equalsIgnoreCase(nextStatusKey);

        if (statusKeyChanged && boardColumnRepository.existsByBoardIdAndStatusKeyIgnoreCaseAndDeletedAtIsNull(boardId, nextStatusKey)) {
            throw new BusinessRuleException("Board column status key already exists.");
        }

        column.setName(request.name().trim());
        column.setStatusKey(nextStatusKey);
        column.setStatusCategory(resolveCategory(request.statusCategory(), nextStatusKey));
        column.setPosition(request.position());
        column.setWipLimit(request.wipLimit());
        BoardColumnEntity savedColumn = boardColumnRepository.save(column);

        if (statusKeyChanged) {
            issueRepository.findByBoardIdAndStatusIgnoreCaseAndDeletedAtIsNullOrderByPositionAsc(boardId, previousStatusKey)
                    .forEach(issue -> {
                        issue.setStatus(nextStatusKey);
                        issueRepository.save(issue);
                    });
            log.info("Migrated issue statuses after board column update: boardId={}, from={}, to={}", boardId, previousStatusKey, nextStatusKey);
        }

        log.info("Updated board column: boardId={}, columnId={}, statusKey={}", boardId, columnId, savedColumn.getStatusKey());
        return savedColumn;
    }

    @Transactional
    public List<BoardColumnEntity> reorderColumns(UUID boardId, BoardDtos.ReorderBoardColumnsRequest request) {
        get(boardId);
        Map<UUID, BoardColumnEntity> columnsById = getColumns(boardId).stream()
                .collect(Collectors.toMap(BoardColumnEntity::getId, Function.identity()));

        request.columns().forEach(item -> {
            BoardColumnEntity column = columnsById.get(item.id());
            if (column == null) {
                throw new ResourceNotFoundException("Board column was not found for the selected board.");
            }
            column.setPosition(item.position());
            boardColumnRepository.save(column);
        });

        log.info("Reordered board columns: boardId={}, count={}", boardId, request.columns().size());
        return getColumns(boardId);
    }

    @Transactional
    public void archive(UUID id, UUID userId) {
        BoardEntity board = get(id);
        board.softDelete(userId);
        boardRepository.save(board);
        log.info("Archived board: boardId={}, deletedByUserId={}", id, userId);
    }

    @Transactional
    public void archiveColumn(UUID boardId, UUID columnId, BoardDtos.ArchiveBoardColumnRequest request, UUID userId) {
        BoardColumnEntity column = getColumn(boardId, columnId);
        long activeColumnCount = boardColumnRepository.countByBoardIdAndDeletedAtIsNull(boardId);
        if (activeColumnCount <= 1) {
            throw new BusinessRuleException("A board must keep at least one active column.");
        }

        long issueCount = issueRepository.countByBoardIdAndStatusIgnoreCaseAndDeletedAtIsNull(boardId, column.getStatusKey());
        if (issueCount > 0) {
            if (request == null || request.replacementStatusKey() == null || request.replacementStatusKey().isBlank()) {
                throw new BusinessRuleException("Replacement status key is required when archiving a column that still has issues.");
            }
            String replacementStatusKey = normalizeStatusKey(request.replacementStatusKey());
            if (replacementStatusKey.equalsIgnoreCase(column.getStatusKey())) {
                throw new BusinessRuleException("Replacement status key must be different from the archived column status key.");
            }
            ensureStatusExists(boardId, replacementStatusKey);
            issueRepository.findByBoardIdAndStatusIgnoreCaseAndDeletedAtIsNullOrderByPositionAsc(boardId, column.getStatusKey())
                    .forEach(issue -> {
                        issue.setStatus(replacementStatusKey);
                        issueRepository.save(issue);
                    });
            log.info("Moved issues before archiving board column: boardId={}, from={}, to={}, count={}",
                    boardId, column.getStatusKey(), replacementStatusKey, issueCount);
        }

        column.softDelete(userId);
        boardColumnRepository.save(column);
        log.info("Archived board column: boardId={}, columnId={}, deletedByUserId={}", boardId, columnId, userId);
    }

    private BoardColumnEntity toColumn(BoardEntity board, BoardDtos.CreateBoardColumnRequest request) {
        BoardColumnEntity column = new BoardColumnEntity();
        column.setBoard(board);
        column.setName(request.name().trim());
        column.setStatusKey(normalizeStatusKey(request.statusKey()));
        column.setStatusCategory(resolveCategory(request.statusCategory(), column.getStatusKey()));
        column.setPosition(request.position());
        column.setWipLimit(request.wipLimit());
        return column;
    }

    private void validateUniqueStatusKeys(List<BoardDtos.CreateBoardColumnRequest> columns) {
        long distinctCount = columns.stream()
                .map(column -> normalizeStatusKey(column.statusKey()))
                .distinct()
                .count();
        if (distinctCount != columns.size()) {
            throw new BusinessRuleException("Board column status keys must be unique.");
        }
    }

    private String normalizeStatusKey(String statusKey) {
        return statusKey.trim()
                .replaceAll("[^a-zA-Z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase(Locale.ROOT);
    }

    private StatusCategory resolveCategory(StatusCategory requestedCategory, String statusKey) {
        if (requestedCategory != null) {
            return requestedCategory;
        }
        String normalized = normalizeStatusKey(statusKey);
        if (normalized.contains("DONE") || normalized.contains("CLOSED") || normalized.contains("RESOLVED")) {
            return StatusCategory.DONE;
        }
        if (normalized.contains("PROGRESS") || normalized.contains("REVIEW") || normalized.contains("QA") || normalized.contains("TEST")) {
            return StatusCategory.IN_PROGRESS;
        }
        return StatusCategory.TO_DO;
    }
}

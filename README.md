# Agile Task Service

Spring Boot microservice for managing agile work with Scrum and Kanban: projects, boards, board columns, sprints, issues, comments, and attachments.

## Stack

- Java 21
- Spring Boot 3.5.3
- PostgreSQL with Flyway migrations
- Optional Redis through `InMemoryKeyValueStoreService`
- OpenAPI/Swagger through Springdoc
- Storage abstraction through `ResourceStorageService`
- Google Cloud Storage implementation behind the storage abstraction

## Run Locally

```bash
docker compose up -d
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Important Headers

The gateway/user microservice is expected to authenticate users. This service only needs the caller identity:

```text
X-User-Id: <uuid>
```

## Configuration

Everything operational is controlled by configuration or environment variables:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `APP_CACHE_PROVIDER` with `NONE` or `REDIS`
- `APP_STORAGE_PROVIDER` with `LOCAL`, `AWS_S3`, or `GOOGLE_CLOUD_STORAGE`
- `APP_STORAGE_MAX_FILE_SIZE`
- `AWS_S3_*` and `GCP_STORAGE_*` values for cloud storage profiles

AWS and Google Cloud profile examples are included in:

- `src/main/resources/application-aws-s3.yml`
- `src/main/resources/application-google-cloud-storage.yml`

Google Cloud Storage is production-ready when `APP_STORAGE_PROVIDER=GOOGLE_CLOUD_STORAGE` and the following values are provided:

- `GCP_STORAGE_BUCKET_NAME`
- `GCP_PROJECT_ID`
- `GCP_STORAGE_KEY_PREFIX`
- `GCP_STORAGE_PUBLIC_BASE_URL`

Credentials are intentionally not hardcoded. Use Application Default Credentials, workload identity, or your deployment secret manager.

## API Shape

Main endpoints:

- `POST /api/v1/projects`
- `GET /api/v1/projects?text=&page=&size=&sort=`
- `PATCH /api/v1/projects/{id}`
- `DELETE /api/v1/projects/{id}` soft-deletes a project
- `POST /api/v1/projects/{projectId}/options`
- `GET /api/v1/projects/{projectId}/options?type=ISSUE_CATEGORY`
- `PATCH /api/v1/projects/{projectId}/options/{optionId}`
- `DELETE /api/v1/projects/{projectId}/options/{optionId}` soft-deletes a configurable option
- `POST /api/v1/boards`
- `GET /api/v1/boards?projectId=`
- `PATCH /api/v1/boards/{id}`
- `DELETE /api/v1/boards/{id}` soft-deletes a board
- `POST /api/v1/boards/{boardId}/columns`
- `GET /api/v1/boards/{boardId}/columns`
- `PATCH /api/v1/boards/{boardId}/columns/{columnId}`
- `POST /api/v1/boards/{boardId}/columns/reorder`
- `DELETE /api/v1/boards/{boardId}/columns/{columnId}` soft-deletes a column and can move issues to a replacement status
- `POST /api/v1/sprints`
- `POST /api/v1/sprints/{id}/start`
- `POST /api/v1/sprints/{id}/complete`
- `DELETE /api/v1/sprints/{id}` soft-deletes a non-active sprint
- `POST /api/v1/issues`
- `GET /api/v1/issues` with filters for project, board, sprint, assignee, reporter, type, priority, status, category, component, label, unassigned, backlog-only, text, and due date range
- `PATCH /api/v1/issues/{id}`
- `PATCH /api/v1/issues/{id}/assignment`
- `DELETE /api/v1/issues/{id}` soft-deletes an issue
- `POST /api/v1/issues/{id}/move`
- `POST /api/v1/issues/{id}/comments`
- `GET /api/v1/issues/{id}/comments`
- `DELETE /api/v1/issues/{id}/comments/{commentId}` soft-deletes a comment
- `POST /api/v1/issues/{id}/attachments`
- `GET /api/v1/issues/{id}/attachments`
- `DELETE /api/v1/issues/{id}/attachments/{attachmentId}` soft-deletes attachment metadata

## Flexible Configuration

The service keeps stable agile concepts as enums, such as board type, issue type, priority, and sprint lifecycle status. User-owned classification is configurable:

- Board columns define issue workflow statuses per board.
- Project options define issue categories, components, and labels.
- Issues store `categoryKey`, `componentKey`, and `labels` so the UI can filter and group work without recompiling the backend.

Deletes are soft deletes through `deletedAt` and `deletedByUserId`. Normal list/search endpoints return active records only.

## Design Notes

The application services depend on business ports instead of concrete infrastructure:

- `ResourceStorageService` hides local disk, AWS S3, and Google Cloud Storage.
- `InMemoryKeyValueStoreService` hides Redis and allows a no-op implementation when cache is disabled.

This keeps the microservice independent from infrastructure choices while avoiding generic abstractions where the domain is stable and explicit.

# ☁️ CloudVault Main Service

> Core business API for memories, photos, places, dashboard data, timeline, notifications, settings, profiles and accounts.

## ✨ Responsibilities
- 🧠 Create/manage memories
- 🔎 Search/filter memories
- 🖼️ Upload/serve/delete photos
- 📍 Places and map locations
- 🗓️ Timeline
- 📊 Dashboard/storage statistics
- 🔔 Notifications
- ⚙️ User settings
- 👤 Profile statistics
- 🗑️ Soft-delete accounts
- 🔐 JWT security
- 🧩 Internal user provisioning

## 🏗️ Architecture
```text
API Gateway :8080
       │
       ▼
Main Service :8082/api
   ├── PostgreSQL
   ├── Local photo storage
   └── Eureka :8761
```

## 🧰 Stack
| Technology | Version / Role |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.8 |
| Spring Data JPA | Persistence |
| PostgreSQL | Database |
| Spring Security | JWT API security |
| JJWT | 0.12.6 |
| Validation | Spring Validation |
| Eureka | Discovery |
| Springdoc | 3.1.0 |
| Storage | Local filesystem |

## 🌐 API Reference

### 🧠 Memories
| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/v1/memories` | Create |
| GET | `/v1/memories` | List/filter/paginate |
| GET | `/v1/memories/search?q=...` | Search |
| GET | `/v1/memories/recent` | Recent |
| GET | `/v1/memories/{id}` | Get |
| PUT | `/v1/memories/{id}` | Update |
| DELETE | `/v1/memories/{id}` | Soft delete |
| POST | `/v1/memories/{id}/photos` | Upload photo |
| GET | `/v1/memories/{memoryId}/photos/{photoId}` | Stream photo |
| DELETE | `/v1/memories/{memoryId}/photos/{photoId}` | Delete photo |
| PUT | `/v1/memories/{memoryId}/photos/{photoId}/cover` | Set cover |

### 📍 Discovery
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/v1/dashboard` | Dashboard |
| GET | `/v1/storage` | Storage usage |
| GET | `/v1/places` | Places |
| GET | `/v1/places/{id}` | Place details |
| GET | `/v1/places/{id}/memories` | Place memories |
| GET | `/v1/map/locations` | Map locations |
| GET | `/v1/timeline` | Year/month timeline |

### 👤 Profile / account
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/v1/profile` | Profile |
| PUT | `/v1/profile` | Update profile |
| GET | `/v1/profile/stats` | Statistics |
| DELETE | `/v1/account` | Soft delete |

### 🔔 Notifications
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/v1/notifications` | List |
| GET | `/v1/notifications/unread-count` | Count |
| PUT | `/v1/notifications/{id}/read` | Mark one read |
| PUT | `/v1/notifications/read-all` | Mark all read |
| DELETE | `/v1/notifications/{id}` | Delete |

### ⚙️ Settings
```text
GET/PUT /v1/settings/notifications
GET/PUT /v1/settings/privacy
GET/PUT /v1/settings/appearance
```

Theme values: `LIGHT`, `DARK`, `SYSTEM`.

## 🧩 Internal provisioning
```http
POST /api/internal/users
X-Internal-Service-Key: <secret>
```

## 🗄️ Data model
```text
UserAccount
  ├──< Memory
  │     ├──< MemoryPhoto
  │     └──> Place
  ├──< AppNotification
  └──1 UserSettings

Memory ──< memory_tag
```

| Table | Purpose |
|---|---|
| `app_user` | Main-service user identity |
| `memory` | Memory records |
| `memory_photo` | Photo metadata |
| `place` | User-scoped places |
| `memory_tag` | Memory tag collection |
| `app_notification` | Notifications |
| `user_settings` | Preferences |

## 🖼️ Storage
Default:
```text
./cloudvault-storage
```

Defaults:
- Max image: `10 MiB`
- Multipart file: `10MB`
- Request: `50MB`
- Free storage limit: `10 GiB`

The database stores photo metadata; binary files are stored on disk.

**Production:** use durable object storage or a shared filesystem for multi-instance deployments.

## 🔐 Security
```http
Authorization: Bearer <JWT>
```

Current rules include:
- Swagger/OpenAPI → public
- OPTIONS → public
- `/api/v1/**` → authenticated
- `/v1/**` → authenticated
- `/internal/**` → service-key checked by controller

## ⚙️ Environment
| Variable | Default |
|---|---|
| `SERVER_PORT` | `8082` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cloudvaults` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `1234` |
| `JPA_DDL_AUTO` | `update` |
| `JPA_SHOW_SQL` | `false` |
| `MAX_UPLOAD_SIZE` | `10MB` |
| `MAX_REQUEST_SIZE` | `50MB` |
| `INTERNAL_SERVICE_KEY` | development placeholder |
| `APP_FRONTEND_URL` | `http://localhost:3000` |
| `STORAGE_PATH` | `./cloudvault-storage` |
| `MAX_IMAGE_BYTES` | `10485760` |
| `FREE_STORAGE_LIMIT_BYTES` | `10737418240` |
| `JWT_SECRET` | development placeholder |
| `JWT_EXPIRATION` | `86400000` |
| `EUREKA_DEFAULT_ZONE` | `http://localhost:8761/eureka/` |

## 📁 Structure
```text
mainservice/
├── controller/
├── service/
├── entity/
├── repository/
├── storage/
├── security/
├── config/
├── dto/
├── exception/
├── src/main/resources/application.yaml
└── pom.xml
```

## ⚠️ Known implementation notes
- `ProfileController` currently returns an empty profile data object and has profile field assignments commented out.
- Photo binaries use local filesystem storage.
- Several relationships use scalar IDs instead of JPA relationships.
- `/internal/**` should also be network-restricted.
- Default credentials/secrets must be replaced.
- Production should use schema migrations rather than relying on `ddl-auto: update`.

## 🔐 Production Checklist
- [ ] Replace credentials and secrets
- [ ] Use DB migrations
- [ ] Use durable image storage
- [ ] Protect internal endpoints
- [ ] Complete profile implementation
- [ ] Add integration tests
- [ ] Add metrics and structured logs
- [ ] Restrict CORS
- [ ] Use HTTPS

## 📚 Full documentation
See **`CloudVault-Main-Service-Documentation.pdf`**.

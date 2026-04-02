# Loveratory - 實驗室受試者報名管理系統

## 1. 系統概述

Loveratory 是一套讓學術實驗室（心理學、認知科學等）管理受試者報名的系統。
實驗室可以建立帳號、開設專案與實驗、設定可報名時段，受試者無需登入即可報名。

### 技術架構

| 層級 | 技術 |
|------|------|
| 前端 | Angular 19+ (standalone components) |
| 後端 | Java 21 + Spring Boot 3.x |
| 資料庫 | PostgreSQL 16 |
| 認證 | JWT (實驗室人員) / 無認證 (受試者) |
| 郵件 | Gmail SMTP (spring-boot-starter-mail) |

---

## 2. 使用者角色

| 角色 | 說明 | 需要登入 |
|------|------|----------|
| **系統管理員 (SYSTEM_ADMIN)** | 審核實驗室成立申請、管理所有實驗室、系統設定 | 是 |
| **實驗室管理員 (LAB_ADMIN)** | 申請建立實驗室的人，審核通過後自動成為 admin，可管理成員權限、踢除成員 | 是 |
| **實驗室成員 (LAB_MEMBER)** | 可開專案、建實驗、管理自己主持的專案報名 | 是 |
| **受試者 (PARTICIPANT)** | 透過公開連結報名實驗時段 | **否** |

### 實驗室建立流程

1. 使用者註冊帳號
2. 提交「建立實驗室」申請（填寫名稱、說明等）
3. **SYSTEM_ADMIN 審核**
4. 審核通過 → 實驗室成立，申請人自動成為 LAB_ADMIN
5. 審核拒絕 → 通知申請人（附拒絕原因），申請人可修改後重新申請

### 權限矩陣

| 操作 | SYSTEM_ADMIN | LAB_ADMIN | LAB_MEMBER (主持人) | LAB_MEMBER (非主持人) | PARTICIPANT |
|------|:---:|:---:|:---:|:---:|:---:|
| 審核實驗室申請 | V | - | - | - | - |
| 管理實驗室成員 | V | V | - | - | - |
| 變更成員角色 | V | V | - | - | - |
| 移除成員 | V | V | - | - | - |
| 建立專案 | V | V | V | V | - |
| 編輯專案 | V | V | V | - | - |
| 管理專案主持人 | V | V | V | - | - |
| 建立/編輯實驗 | V | V | V | - | - |
| 管理時段 | V | V | V | - | - |
| 查看報名資料 | V | V | V | - | - |
| 報名時段 | - | - | - | - | V |
| 取消報名 | - | - | - | - | 依設定 |

> LAB_MEMBER 預設只能看到自己建立或被加入主持人名單的專案。
> LAB_ADMIN 和 SYSTEM_ADMIN 可以看到實驗室內所有專案。

---

## 3. 核心資料模型

### 3.1 使用者 (User)

```
User
├── id: UUID
├── email: String (unique)
├── password_hash: String
├── name: String
├── role: Enum [SYSTEM_ADMIN, USER]   -- 系統層級角色
├── created_at: Timestamp
└── updated_at: Timestamp
```

### 3.2 實驗室 (Lab)

```
Lab
├── id: UUID
├── name: String              -- 實驗室名稱，例如「認知心理學實驗室」
├── code: String (unique)     -- 短代碼，用於邀請連結，例如 "cog-psy-lab"
├── description: String
├── status: Enum [PENDING, APPROVED, REJECTED]
├── applied_by: UUID (FK -> User)
├── reviewed_by: UUID (FK -> User, nullable)
├── review_note: String (nullable)    -- 審核備註（拒絕原因等）
├── approved_at: Timestamp (nullable)
├── created_at: Timestamp
└── updated_at: Timestamp
```

### 3.3 實驗室邀請 (LabInvitation)

LAB_ADMIN 輸入 Email 邀請成員，系統寄出邀請信，受邀者點連結接受。

```
LabInvitation
├── id: UUID
├── lab_id: UUID (FK -> Lab)
├── email: String             -- 被邀請者的 Email
├── token: String (unique)    -- 邀請 token，用於接受連結
├── invited_by: UUID (FK -> User)
├── status: Enum [PENDING, ACCEPTED, EXPIRED]
├── expires_at: Timestamp     -- 邀請過期時間（預設 7 天）
├── accepted_at: Timestamp (nullable)
├── created_at: Timestamp
└── updated_at: Timestamp
```

**規則：**
- 被邀請者若已有帳號，點連結後直接加入實驗室
- 被邀請者若無帳號，點連結後導向註冊頁，註冊完自動加入
- 同一 Email 對同一實驗室不重複發送未過期的邀請

### 3.4 實驗室成員關聯 (LabMember)

一個使用者可以屬於多個實驗室，在不同實驗室可有不同角色。

```
LabMember
├── id: UUID
├── lab_id: UUID (FK -> Lab)
├── user_id: UUID (FK -> User)
├── role: Enum [LAB_ADMIN, LAB_MEMBER]
├── joined_at: Timestamp
└── status: Enum [ACTIVE, REMOVED]
```

### 3.5 專案 (Project)

```
Project
├── id: UUID
├── lab_id: UUID (FK -> Lab)
├── name: String              -- 例如「注意力與工作記憶研究」
├── description: String
├── created_by: UUID (FK -> User)
├── status: Enum [DRAFT, ACTIVE, ARCHIVED]
├── created_at: Timestamp
└── updated_at: Timestamp
```

### 3.6 專案主持人 (ProjectInvestigator)

每個專案至少一位主持人。建立者自動成為主持人。主持人可以新增/移除其它主持人，但不能低於一人。

```
ProjectInvestigator
├── id: UUID
├── project_id: UUID (FK -> Project)
├── user_id: UUID (FK -> User)       -- 必須是該實驗室的成員
├── added_by: UUID (FK -> User)
├── added_at: Timestamp
└── status: Enum [ACTIVE, REMOVED]
```

**約束：每個 project 至少保留一位 ACTIVE 的主持人。**

### 3.7 實驗 (Experiment)

```
Experiment
├── id: UUID
├── project_id: UUID (FK -> Project)
├── name: String              -- 例如「前測 - Stroop Task」
├── description: String       -- 給受試者看的說明
├── location: String          -- 實驗地點
├── duration_minutes: Integer -- 每次時段長度（分鐘），例如 20, 30, 60
├── max_participants_per_slot: Integer -- 每個時段預設可報名人數
├── slug: String (unique)     -- 公開報名連結用，例如 "stroop-2026-spring"
├── status: Enum [DRAFT, OPEN, CLOSED, ARCHIVED]
│
│   -- 報名設定
├── allow_duplicate_email: Boolean (default: false)  -- 同 Email 是否可重複報名
├── allow_participant_cancel: Boolean (default: true) -- 受試者是否可自行取消
│   ⚠️ status 變為 OPEN 後，allow_participant_cancel 不可再更改
│
│   -- 報名表單欄位設定
├── form_config: JSONB        -- 見 3.8
│
│   -- 通知設定
├── notification_config: JSONB -- 見 3.11
│
├── created_by: UUID (FK -> User)
├── created_at: Timestamp
└── updated_at: Timestamp
```

### 3.8 報名表單欄位設定 (FormConfig)

存在 Experiment 的 `form_config` JSONB 欄位中。
Email 永遠必填不可關閉，其餘欄位可設定「是否顯示」與「是否必填」。

```json
{
  "fields": [
    { "key": "email",       "label": "Email",  "visible": true, "required": true, "locked": true },
    { "key": "name",        "label": "姓名",   "visible": true, "required": true  },
    { "key": "phone",       "label": "手機",   "visible": true, "required": false },
    { "key": "student_id",  "label": "學號",   "visible": false, "required": false },
    { "key": "age",         "label": "年齡",   "visible": false, "required": false },
    { "key": "gender",      "label": "性別",   "visible": false, "required": false },
    { "key": "dominant_hand","label": "慣用手", "visible": false, "required": false },
    { "key": "notes",       "label": "備註",   "visible": true, "required": false }
  ]
}
```

**預設：** Email（必填、鎖定）、姓名（顯示、必填）、手機（顯示、選填）、備註（顯示、選填），其餘隱藏。

**可用欄位清單（固定，不可自訂新增）：**

| key | label | 類型 | 預設 |
|-----|-------|------|------|
| `email` | Email | email | 顯示 + 必填（鎖定） |
| `name` | 姓名 | text | 顯示 + 必填 |
| `phone` | 手機 | tel | 顯示 + 選填 |
| `student_id` | 學號 | text | 隱藏 |
| `age` | 年齡 | number | 隱藏 |
| `gender` | 性別 | select (男/女/其它/不願透露) | 隱藏 |
| `dominant_hand` | 慣用手 | select (左/右/雙手) | 隱藏 |
| `notes` | 備註 | textarea | 顯示 + 選填 |

### 3.9 時段 (TimeSlot)

```
TimeSlot
├── id: UUID
├── experiment_id: UUID (FK -> Experiment)
├── start_time: Timestamp     -- 時段開始時間
├── end_time: Timestamp       -- 時段結束時間（= start_time + duration_minutes）
├── capacity: Integer         -- 此時段可報名人數（繼承或覆寫實驗設定）
├── current_count: Integer    -- 目前已報名人數（冗餘欄位，用於快速查詢）
├── status: Enum [AVAILABLE, FULL, CANCELLED]
├── created_at: Timestamp
└── updated_at: Timestamp
```

### 3.10 報名 (Registration)

```
Registration
├── id: UUID
├── time_slot_id: UUID (FK -> TimeSlot)
├── participant_email: String     -- 永遠必填
├── participant_name: String (nullable)
├── participant_phone: String (nullable)
├── participant_student_id: String (nullable)
├── participant_age: Integer (nullable)
├── participant_gender: String (nullable)
├── participant_dominant_hand: String (nullable)
├── participant_notes: String (nullable)
├── cancel_token: String (unique) -- 用於免登入取消報名
├── status: Enum [CONFIRMED, CANCELLED, NO_SHOW]
├── registered_at: Timestamp
├── cancelled_at: Timestamp (nullable)
└── updated_at: Timestamp
```

**唯一約束：** 當 `allow_duplicate_email = false` 時，同一 experiment 下同一 email 只能有一筆 CONFIRMED 的報名。此邏輯在 application layer 檢查。

### 3.11 通知設定 (NotificationConfig)

存在 Experiment 的 `notification_config` JSONB 欄位中：

```json
{
  "enabled": true,
  "on_registration": true,
  "reminders": [1, 2]
}
```

**提醒選項（多選）：**
- 實驗前 **1 天**
- 實驗前 **2 天**
- 實驗前 **3 天**

### 3.12 通知紀錄 (Notification)

```
Notification
├── id: UUID
├── registration_id: UUID (FK -> Registration)
├── type: Enum [REGISTRATION_CONFIRMED, REMINDER]
├── channel: Enum [EMAIL]
├── status: Enum [PENDING, SENT, FAILED]
├── scheduled_at: Timestamp
├── sent_at: Timestamp (nullable)
├── retry_count: Integer (default: 0)
├── error_message: String (nullable)
└── created_at: Timestamp
```

---

## 4. ER Diagram

```
User (SYSTEM_ADMIN / USER)
 │
 ├──< LabMember >── Lab (PENDING / APPROVED / REJECTED)
 │                    │
 │                    ├──< LabInvitation
 │                    │
 │                    └──< Project
 │                          │
 │                          ├──< ProjectInvestigator >── User
 │                          │
 │                          └──< Experiment
 │                                ├── form_config (JSONB)
 │                                ├── notification_config (JSONB)
 │                                │
 │                                └──< TimeSlot
 │                                      │
 │                                      └──< Registration
 │                                            │
 │                                            └──< Notification
```

---

## 5. API 設計 (RESTful)

### 5.1 認證

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/auth/register` | 使用者註冊 |
| POST | `/api/auth/login` | 登入，取得 JWT |
| POST | `/api/auth/refresh` | 刷新 token |

### 5.2 系統管理 (SYSTEM_ADMIN)

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/admin/labs/pending` | 列出待審核的實驗室申請 |
| PUT | `/api/admin/labs/{labId}/approve` | 核准實驗室 |
| PUT | `/api/admin/labs/{labId}/reject` | 拒絕實驗室（附原因） |
| GET | `/api/admin/labs` | 列出所有實驗室 |

### 5.3 實驗室邀請

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/labs/{labId}/invitations` | 列出邀請紀錄 (LAB_ADMIN) |
| POST | `/api/labs/{labId}/invitations` | 輸入 Email 發送邀請 (LAB_ADMIN) |
| DELETE | `/api/labs/{labId}/invitations/{invitationId}` | 撤銷邀請 (LAB_ADMIN) |
| GET | `/api/invitations/{token}` | 查看邀請資訊（公開） |
| POST | `/api/invitations/{token}/accept` | 接受邀請加入實驗室 |

### 5.4 實驗室管理

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/labs` | 申請建立實驗室（進入 PENDING 狀態） |
| POST | `/api/labs/{labId}/reapply` | 被拒絕後重新申請（修改內容重新提交） |
| GET | `/api/labs` | 列出我所屬的實驗室 |
| GET | `/api/labs/{labId}` | 取得實驗室資訊 |
| PUT | `/api/labs/{labId}` | 更新實驗室資訊 (LAB_ADMIN) |

### 5.5 實驗室成員管理

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/labs/{labId}/members` | 列出成員 |
| PUT | `/api/labs/{labId}/members/{userId}/role` | 變更成員角色 (LAB_ADMIN) |
| DELETE | `/api/labs/{labId}/members/{userId}` | 移除成員 (LAB_ADMIN) |

### 5.6 專案管理

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/labs/{labId}/projects` | 建立專案（建立者自動成為主持人） |
| GET | `/api/labs/{labId}/projects` | 列出我主持的專案（LAB_ADMIN 看全部） |
| GET | `/api/projects/{projectId}` | 取得專案詳情 |
| PUT | `/api/projects/{projectId}` | 更新專案（限主持人） |
| DELETE | `/api/projects/{projectId}` | 封存專案（限主持人） |

### 5.7 專案主持人管理

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/projects/{projectId}/investigators` | 列出主持人 |
| POST | `/api/projects/{projectId}/investigators` | 新增主持人（限現有主持人） |
| DELETE | `/api/projects/{projectId}/investigators/{userId}` | 移除主持人（至少保留一位） |

### 5.8 實驗管理

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/projects/{projectId}/experiments` | 建立實驗 |
| GET | `/api/projects/{projectId}/experiments` | 列出實驗 |
| GET | `/api/experiments/{experimentId}` | 取得實驗詳情 |
| PUT | `/api/experiments/{experimentId}` | 更新實驗 |
| PUT | `/api/experiments/{experimentId}/form-config` | 更新報名表單設定 |
| PUT | `/api/experiments/{experimentId}/notification-config` | 更新通知設定 |

### 5.9 時段管理

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/experiments/{experimentId}/slots` | 建立時段（支援批次） |
| GET | `/api/experiments/{experimentId}/slots` | 列出時段（含報名狀況） |
| PUT | `/api/slots/{slotId}` | 更新時段 |
| DELETE | `/api/slots/{slotId}` | 取消時段 |

### 5.10 報名管理（管理後台查看）

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/experiments/{experimentId}/registrations` | 列出所有報名 |
| PUT | `/api/registrations/{registrationId}/status` | 更新報名狀態（如標記 NO_SHOW）|

### 5.11 受試者報名（公開，免登入）

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/public/experiments/{slug}` | 取得實驗資訊、表單設定、可報名時段 |
| POST | `/api/public/experiments/{slug}/register` | 報名（回傳 cancel_token） |
| GET | `/api/public/registrations/{cancelToken}` | 查看報名狀態 |
| DELETE | `/api/public/registrations/{cancelToken}` | 取消報名（需實驗允許取消） |

---

## 6. 前端頁面規劃

### 6.1 公開頁面（受試者）

| 頁面 | Route | 說明 |
|------|-------|------|
| 實驗報名頁 | `/e/{slug}` | 顯示實驗說明、可選時段、動態表單 |
| 報名成功頁 | `/e/{slug}/success` | 顯示報名資訊、取消連結（若允許取消） |
| 取消報名頁 | `/cancel/{cancelToken}` | 確認取消報名 |
| 接受邀請頁 | `/invite/{token}` | 顯示邀請資訊，登入/註冊後加入實驗室 |

### 6.2 管理後台（實驗室人員）

| 頁面 | Route | 說明 |
|------|-------|------|
| 登入/註冊 | `/auth/login`, `/auth/register` | |
| Dashboard | `/dashboard` | 所屬實驗室列表、待審核申請狀態 |
| 實驗室總覽 | `/lab/{labId}` | 專案列表、成員管理入口 |
| 成員管理 | `/lab/{labId}/members` | 成員列表、角色管理、移除 |
| 專案詳情 | `/project/{projectId}` | 實驗列表、主持人管理 |
| 實驗詳情 | `/experiment/{experimentId}` | 時段管理、報名列表 |
| 實驗設定 | `/experiment/{experimentId}/settings` | 表單欄位設定、通知設定、取消政策 |
| 時段設定 | `/experiment/{experimentId}/slots` | 新增/編輯時段（含批次設定） |

### 6.3 系統管理員

| 頁面 | Route | 說明 |
|------|-------|------|
| 審核列表 | `/admin/labs/pending` | 待審核的實驗室申請 |
| 全部實驗室 | `/admin/labs` | 所有實驗室列表 |

---

## 7. 時段設定 UX 流程

### 批次建立時段

實驗人員常常需要一次開出整週的時段，所以提供批次建立：

1. 選擇日期範圍（例如 4/6 ~ 4/10）
2. 選擇星期幾有時段（例如一、三、五）
3. 設定每天的時段起訖（例如 09:00 ~ 17:00）
4. 設定每次時段長度（例如 30 分鐘）
5. 設定時段間休息時間（例如 10 分鐘）
6. 設定每個時段人數上限（預設繼承實驗設定）
7. 預覽 → 確認建立

**範例產出：**
```
4/6 (一) 09:00-09:30, 09:40-10:10, 10:20-10:50 ...  各 5 人
4/8 (三) 09:00-09:30, 09:40-10:10, 10:20-10:50 ...  各 5 人
4/10 (五) 09:00-09:30, 09:40-10:10, 10:20-10:50 ... 各 5 人
```

也支援手動單獨新增個別時段。

---

## 8. 通知機制

### 8.1 通知類型

| 類型 | 觸發時機 | 收件者 |
|------|----------|--------|
| 報名成功確認 | 受試者完成報名後立即 | 受試者 (Email) |
| 實驗提醒 | 實驗前 N 天（依設定） | 受試者 (Email) |

### 8.2 實驗人員設定通知

在實驗設定頁面中：

- [ ] 啟用通知
- [ ] 報名成功時發送確認信
- 實驗提醒（可多選）：
  - [ ] 實驗前 1 天
  - [ ] 實驗前 2 天
  - [ ] 實驗前 3 天

### 8.3 技術實作

- 使用 Gmail SMTP (`smtp.gmail.com:587`) + Google App Password
- Spring Boot 設定：`spring-boot-starter-mail`
- 使用 Spring Scheduler (`@Scheduled`) 定期掃描需發送的提醒
  - 每小時掃描一次，查找 `scheduled_at <= now AND status = PENDING` 的通知
- 通知紀錄存入 `Notification` 表，方便追蹤發送狀態
- 失敗自動重試（最多 3 次）

### 8.4 Gmail SMTP 設定

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME}
    password: ${GMAIL_APP_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

---

## 9. 商業規則摘要

| 規則 | 說明 |
|------|------|
| 實驗室需審核 | 建立實驗室需 SYSTEM_ADMIN 審核通過 |
| 被拒可重新申請 | 實驗室申請被拒絕後，申請人可修改內容重新提交 |
| 邀請加入實驗室 | LAB_ADMIN 透過 Email 邀請，受邀者點連結加入（邀請 7 天過期） |
| 專案可見性 | LAB_MEMBER 只看到自己主持的專案；LAB_ADMIN 看到全部 |
| 主持人最少一人 | 專案至少保留一位主持人，不可全部移除 |
| Email 重複報名 | 預設同一實驗同一 Email 不可重複報名，可由實驗設定開啟 |
| 取消政策鎖定 | `allow_participant_cancel` 在實驗 status 變為 OPEN 後不可更改 |
| Email 必填鎖定 | 報名表單的 email 欄位永遠必填，不可關閉 |
| NO_SHOW 標記 | 實驗人員可在後台將報名標記為「未到」 |

---

## 10. 專案結構

```
loveratory/
├── backend/                          # Spring Boot 3.x
│   └── src/main/java/com/loveratory/
│       ├── config/                   # Security, CORS, Mail, Scheduler 設定
│       ├── auth/                     # 認證模組 (JWT)
│       ├── admin/                    # 系統管理模組 (審核)
│       ├── lab/                      # 實驗室模組
│       ├── project/                  # 專案模組 (含主持人管理)
│       ├── experiment/               # 實驗模組 (含表單設定)
│       ├── slot/                     # 時段模組
│       ├── registration/             # 報名模組
│       ├── notification/             # 通知模組
│       └── common/                   # 共用：例外處理、DTO、工具
├── web/                              # Angular 19+
│   └── src/app/
│       ├── core/                     # Guards, interceptors, auth service
│       ├── shared/                   # 共用 components, pipes, directives
│       ├── features/
│       │   ├── auth/                 # 登入/註冊
│       │   ├── admin/                # 系統管理（審核實驗室）
│       │   ├── dashboard/            # 總覽
│       │   ├── lab/                  # 實驗室管理
│       │   ├── project/              # 專案管理（含主持人）
│       │   ├── experiment/           # 實驗管理（含設定、時段、報名）
│       │   └── public/              # 受試者報名頁面
│       └── environments/
└── docs/                             # 文件
```

---

## 11. 安全性考量

### 受試者報名安全

- 報名不需登入，但 Email 永遠必填
- 每個報名產生唯一 `cancel_token`（UUID），用於查看/取消
- 重複報名檢查（可設定開關）
- 報名表單加上 rate limiting 防止濫用
- cancel_token 只透過 Email 通知，不在 URL 中暴露於第三方

### 實驗室人員認證

- JWT (Access Token: 15min) + Refresh Token (7 days)
- 密碼使用 BCrypt 加密
- Spring Security 角色權限檢查
- 專案層級權限由 ProjectInvestigator 表控制

---

## 12. 未來擴充方向（不在 MVP 內）

- SMS / LINE 通知
- 受試者帳號系統（追蹤參與歷史）
- 酬金/受試費管理
- 匯出報名資料（CSV/Excel）
- 多語系 (i18n)
- 受試者黑名單
- 實驗間的前後依賴（例如前測完才能報後測）

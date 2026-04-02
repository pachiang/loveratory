# Spring Boot 3 團隊開發規範

本 skill 定義團隊 Spring Boot 3 多模組專案的統一編碼風格與架構慣例。產出的所有 Java 程式碼都應遵守這些規則。


## 核心原則速覽

以下是最關鍵的規則摘要。各項的完整說明、範例與邊界情況請參閱對應的參考文件。

### 類別注入

所有 Spring 管理的類別一律使用 `@Slf4j` + `@RequiredArgsConstructor`，依賴以 `private final` 欄位宣告，不使用 `@Autowired`。

### 命名

全名不縮寫（`request` 非 `req`）。命名要能看出功能，不怕長。不把回傳型態放在命名中。

### 建構方式

靜態工廠方法取代 constructor。禁止 overload 建構子。少用 method overload。

### 設計哲學

- 除非有強烈擴展需求或能大幅減少冗餘程式碼，不引入設計模式。
- 以組合代替繼承。
- 重要邏輯元件用 DTO，業務邏輯寫在 DTO 中而非 Entity。
- 偏好 Stream 但不過度串接；偏好 Optional 取代 null 判斷。
- 除非型別宣告很長，否則不用 `var`。
- 時間統一用 `ZonedDateTime`。
- 善用 Lombok `@NonNull` 標註參數可空性。

### 速查清單

撰寫程式碼前確認：

- [ ] `@Slf4j` + `@RequiredArgsConstructor`？
- [ ] JPA 用 Specification？無 native SQL？Entity field 用 `@FieldNameConstants`？
- [ ] 命名完整有描述性？無縮寫？
- [ ] DTO 承載業務邏輯？靜態工廠方法？
- [ ] Controller 有 Swagger 註解？有 log 請求？
- [ ] Response 由 AOP 包裹？錯誤用 ErrorCode + BusinessException？
- [ ] URL 符合 kebab-case 規範？Request/Response 為 camelCase JSON？
- [ ] Job 有分散式鎖？時間用 ZonedDateTime？
- [ ] 複雜邏輯有註解？類別方法有 JavaDoc？
- [ ] Commit message 符合 Conventional Commits？


---

# 架構與分層規範


## 1. 多模組架構

專案採用 Gradle 或 Maven 多模組結構，依功能領域與技術職責劃分模組。

```
project-root/
├── build.gradle (or pom.xml)          # 根專案，管理共用 dependency 版本
├── settings.gradle
├── common/                            # 共用模組：DTO、工具、常數、例外
│   └── src/main/java/
│       └── com.example.common/
│           ├── dto/                   # 跨模組共用的 DTO
│           ├── exception/             # BusinessException, ErrorCode
│           ├── response/              # ApiResponse, ResponseBodyAdvice
│           ├── util/                  # 通用工具類別
│           └── constant/             # 全域常數
├── domain-order/                      # 領域模組：訂單
│   └── src/main/java/
│       └── com.example.order/
│           ├── controller/
│           ├── usecase/
│           ├── service/
│           ├── manager/
│           ├── repository/
│           ├── entity/
│           ├── dto/                   # 模組內部 DTO
│           ├── constant/
│           └── config/
├── domain-payment/                    # 領域模組：付款
├── domain-inventory/                  # 領域模組：庫存
├── infra/                             # 基礎設施模組：資料庫設定、Redis、MQ、安全性
│   └── src/main/java/
│       └── com.example.infra/
│           ├── config/
│           ├── security/
│           ├── lock/                  # 分散式鎖
│           └── messaging/
└── app/                               # 啟動模組：Spring Boot Application 入口
    └── src/main/java/
        └── com.example.app/
            └── Application.java
```

### 模組命名

- 以 `domain-` 前綴標記領域模組（`domain-order`、`domain-payment`）。
- `common` 放置所有模組共用的橫切關注（cross-cutting concerns）。
- `infra` 放置技術基礎設施，與業務邏輯無關。
- `app` 僅包含啟動類別與最終的 application 設定，不含業務邏輯。

---

## 2. 模組間依賴規則

```
app → domain-* → common
app → infra    → common
domain-* → common
domain-* → infra（僅限基礎設施介面，如分散式鎖、訊息發送）
```

**嚴格禁止：**
- 領域模組之間直接互相依賴。`domain-order` 不可以 import `domain-payment` 的類別。
- 如果兩個領域模組需要協作，透過以下方式解耦：
  1. **事件驅動**：透過 Spring Event 或 MQ 發送事件，由對方模組監聯。
  2. **共用 DTO 抽到 common**：需要共用的資料結構放到 `common` 模組。
  3. **在 app 模組組合**：如果確實需要跨領域流程，在 `app` 模組建立 orchestration 層。

**common 模組注意事項：**
- common 不可依賴任何 domain 模組。
- common 應保持輕量，只放真正跨模組共用的東西。
- 不要把只有一個模組用到的類別放進 common。

**infra 模組注意事項：**
- infra 提供技術能力的介面與實作（Redis、MQ、S3 等）。
- 領域模組透過介面引用 infra 的能力，不直接依賴具體實作類別。

---

## 3. 單一模組內的分層架構

```
com.example.order
├── controller/        # REST 端點，與 UseCase 幾乎一對一
├── usecase/           # 業務編排層，可組合 Service 與 Manager
├── service/           # 業務邏輯封裝（非 CRUD），可引用多個 Manager
├── manager/           # 與 Repository 一對一，封裝特定 Entity 的 CRUD
├── repository/        # Spring Data JPA Repository
├── entity/            # JPA Entity
├── dto/               # 資料傳輸物件（含業務邏輯方法）
├── constant/          # 依功能分檔的常數類別
├── util/              # 模組內的工具類別（通用的放 common）
├── config/            # 模組內的 Spring 設定
├── event/             # Spring Event / MQ 事件類別
└── job/               # 排程任務
```

---

## 4. 各層職責與引用規則

| 層級 | 職責 | 可引用 | 不可引用 |
|---|---|---|---|
| **Controller** | 接收 HTTP 請求、參數驗證、呼叫 UseCase、回傳 Response | UseCase | Service, Manager, Repository |
| **UseCase** | 業務流程編排，串接多個 Service 與 Manager | Service, Manager | Controller, 其他 UseCase |
| **Service** | CRUD 以外的業務邏輯封裝 | Manager（可多個） | Controller, UseCase, 其他 Service（盡可能避免） |
| **Manager** | 封裝特定 Entity 的 CRUD，與 Repository 一對一 | Repository（對應的那一個） | Controller, UseCase, Service, 其他 Manager |
| **Repository** | Spring Data JPA 介面 | — | — |

### 重要原則

- **Controller 與 UseCase 幾乎一對一**：一個 Controller 對應一個 UseCase。Controller 不直接呼叫 Service 或 Manager。
- **Manager 與 Repository 嚴格一對一**：`OrderManager` 只操作 `OrderRepository`，只負責 `OrderEntity` 的 CRUD 封裝。
- **Service 之間盡可能不互相引用**：如果 ServiceA 需要 ServiceB 的功能，考慮是否該提取到 Manager 或 UseCase 層。被引用的 Service 應保持無狀態。
- **UseCase 是唯一的「組合點」**：複雜流程的組合邏輯放 UseCase，不要散落在各 Service 中。
- 通用無狀態功能放 `util` package（模組內的放模組的 util，跨模組的放 common 的 util）。
- `constant` package 內依功能分檔存放不同常數類別，不要全部塞一個 `Constants.java`。

---

## 5. Package 組織

### Entity package

```java
// entity/OrderEntity.java
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "orders")
public class OrderEntity { ... }

// entity/OrderItemEntity.java
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "order_items")
public class OrderItemEntity { ... }
```

### DTO package

依照用途子分類：

```
dto/
├── request/
│   └── OrderCreateRequest.java
│   └── OrderSearchRequest.java
├── response/
│   └── OrderDetailResponse.java
│   └── OrderSummaryResponse.java
└── internal/
    └── OrderCalculationResult.java    # 內部邏輯傳遞用，不對外暴露
```

### Constant package

```
constant/
├── OrderStatusConstant.java       # 訂單狀態相關常數
├── PaymentTypeConstant.java       # 付款方式相關常數
└── CacheKeyConstant.java          # 快取 key 相關常數
```


---

# JPA 與資料存取規範


## 1. 核心原則

- **禁止 Native SQL**：除非經過團隊討論確認有極特殊的效能或功能需求，否則一律不使用 `@Query(nativeQuery = true)` 或 `EntityManager.createNativeQuery()`。
- **優先 Specification**：複雜查詢使用 `JpaSpecificationExecutor` 搭配 `Specification`，而非 JPQL 字串拼接。
- **Entity field 引用使用 Lombok FieldNameConstants**：引用欄位名稱時，絕對不使用字串字面值。

---

## 2. Entity 設計

### 基本結構

```java
/**
 * 訂單 Entity。
 * 對應資料表 orders，儲存訂單主檔資料。
 */
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
```

### Entity 設計原則

- Entity 只負責映射資料庫結構，**不要在 Entity 中放業務邏輯**。業務邏輯放 DTO。
- 所有 Entity 都加 `@FieldNameConstants`。
- 使用 `@Enumerated(EnumType.STRING)` 儲存列舉，不用 `ORDINAL`。
- 時間欄位一律使用 `ZonedDateTime`。
- 善用 JPA Auditing（`@CreatedDate`、`@LastModifiedDate`）自動管理時間戳。
- 關聯映射盡量使用 `LAZY` fetch（`@OneToMany` 預設就是 LAZY，`@ManyToOne` 要顯式設定）。

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id", nullable = false)
private OrderEntity order;
```

---

## 3. Repository 設計

Repository 繼承 `JpaRepository` 與 `JpaSpecificationExecutor`：

```java
/**
 * 訂單 Repository。
 */
public interface OrderRepository extends JpaRepository<OrderEntity, Long>,
        JpaSpecificationExecutor<OrderEntity> {

    /**
     * 根據訂單編號查詢訂單。
     */
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    /**
     * 檢查訂單編號是否已存在。
     */
    boolean existsByOrderNumber(String orderNumber);
}
```

- 簡單查詢可以使用 Spring Data 的方法名稱推導（method name query derivation）。
- 稍微複雜的查詢使用 `@Query` 搭配 JPQL（不是 native SQL）。
- 更複雜的動態查詢使用 Specification。
- Repository 只定義介面方法，所有封裝邏輯放在 Manager。

---

## 4. Specification 查詢

### Specification 類別組織

每個 Entity 對應一個 Specification 工具類別，集中管理該 Entity 的所有查詢條件：

```java
/**
 * 訂單查詢條件 Specification。
 * 所有 OrderEntity 的動態查詢條件集中在此。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecification {

    /**
     * 依客戶名稱模糊查詢。
     */
    public static Specification<OrderEntity> hasCustomerNameLike(String customerName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(OrderEntity.Fields.customerName),
                        "%" + customerName + "%");
    }

    /**
     * 依訂單狀態查詢。
     */
    public static Specification<OrderEntity> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(OrderEntity.Fields.status), status);
    }

    /**
     * 依建立時間區間查詢。
     */
    public static Specification<OrderEntity> createdBetween(ZonedDateTime startTime,
                                                             ZonedDateTime endTime) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(OrderEntity.Fields.createdAt),
                        startTime, endTime);
    }

    /**
     * 依多個狀態查詢（IN 條件）。
     */
    public static Specification<OrderEntity> hasStatusIn(List<OrderStatus> statuses) {
        return (root, query, criteriaBuilder) ->
                root.get(OrderEntity.Fields.status).in(statuses);
    }
}
```

### 組合 Specification

```java
// 在 Manager 或 UseCase 中組合條件
Specification<OrderEntity> specification = Specification.where(null);

if (StringUtils.hasText(searchRequest.getCustomerName())) {
    specification = specification.and(
            OrderSpecification.hasCustomerNameLike(searchRequest.getCustomerName()));
}

if (searchRequest.getStatus() != null) {
    specification = specification.and(
            OrderSpecification.hasStatus(searchRequest.getStatus()));
}

Page<OrderEntity> orderPage = orderRepository.findAll(specification, pageable);
```

### 注意事項

- 每個 Specification 方法只負責一個查詢條件，透過 `and()` / `or()` 組合。
- **所有 field 引用都必須使用 `Entity.Fields.xxx`**，禁止字串。
- Specification 類別使用 `@NoArgsConstructor(access = AccessLevel.PRIVATE)` 阻止實例化。

---

## 5. Manager 封裝

Manager 與 Repository 一對一，封裝 CRUD 操作，並提供語義清晰的方法：

```java
/**
 * 訂單資料存取管理器。
 * 封裝 OrderEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderManager {

    private final OrderRepository orderRepository;

    /**
     * 根據 ID 查詢訂單，找不到時拋出 BusinessException。
     */
    public OrderEntity findByIdOrThrow(@NonNull Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 根據訂單編號查詢訂單。
     */
    public Optional<OrderEntity> findByOrderNumber(@NonNull String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * 依查詢條件搜尋訂單（分頁）。
     */
    public Page<OrderEntity> findBySpecification(@NonNull Specification<OrderEntity> specification,
                                                  @NonNull Pageable pageable) {
        return orderRepository.findAll(specification, pageable);
    }

    /**
     * 儲存訂單。
     */
    public OrderEntity save(@NonNull OrderEntity orderEntity) {
        return orderRepository.save(orderEntity);
    }

    /**
     * 批次儲存訂單。
     */
    public List<OrderEntity> saveAll(@NonNull List<OrderEntity> orderEntities) {
        return orderRepository.saveAll(orderEntities);
    }
}
```

- Manager 不做業務邏輯判斷，只做資料存取的薄封裝。
- 常用的「查不到就拋例外」可以封裝為 `findByXxxOrThrow` 方法。
- Manager 方法參數善用 `@NonNull` 標註。

---

## 6. 分頁與排序

使用 Spring Data 的 `Pageable` 處理分頁與排序：

```java
// Controller 接收分頁參數
@GetMapping
public Page<OrderSummaryResponse> searchOrders(
        @Valid OrderSearchRequest searchRequest,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
    log.info("搜尋訂單，conditions: {}, page: {}", searchRequest, pageable);
    return orderUseCase.searchOrders(searchRequest, pageable);
}
```

- 前端傳遞 `page`、`size`、`sort` 參數，Spring 自動綁定到 `Pageable`。
- 使用 `@PageableDefault` 設定預設分頁與排序。
- 排序欄位名稱需對應 Entity 的 Java field 名稱。

---

## 7. 交易管理

- `@Transactional` 標註在 UseCase 或 Service 層的方法上，不要標在 Controller 或 Manager。
- 唯讀操作使用 `@Transactional(readOnly = true)` 以優化效能。
- 明確指定 rollback 條件：`@Transactional(rollbackFor = Exception.class)`。

```java
@Transactional(rollbackFor = Exception.class)
public OrderCreateResponse createOrder(@NonNull OrderCreateRequest request) {
    // 跨多個 Manager 的寫入操作
}

@Transactional(readOnly = true)
public OrderDetailResponse findOrderDetail(@NonNull Long orderId) {
    // 純查詢操作
}
```


---

# API 設計規範


## 1. URL 設計

### 命名規則

- **一律使用 kebab-case**（小寫、連字號分隔），不使用 camelCase 或 snake_case。
- URL 代表資源（名詞），不使用動詞。操作語義由 HTTP 方法表達。
- 資源名稱使用複數形式。

```
✓ /api/v1/order-items
✓ /api/v1/payment-records
✓ /api/v1/user-profiles/{userId}/addresses

✗ /api/v1/orderItems          （camelCase）
✗ /api/v1/order_items         （snake_case）
✗ /api/v1/getOrderItems       （動詞）
✗ /api/v1/order-item          （單數）
```

### URL 結構

```
/api/v{版本號}/{資源名稱}
/api/v{版本號}/{資源名稱}/{id}
/api/v{版本號}/{資源名稱}/{id}/{子資源}
```

```
GET     /api/v1/orders                     # 查詢訂單列表
GET     /api/v1/orders/{orderId}           # 查詢單一訂單
POST    /api/v1/orders                     # 建立訂單
PUT     /api/v1/orders/{orderId}           # 更新訂單（完整取代）
PATCH   /api/v1/orders/{orderId}           # 部分更新訂單
DELETE  /api/v1/orders/{orderId}           # 刪除訂單
GET     /api/v1/orders/{orderId}/items     # 查詢訂單的商品列表
```

### 非 CRUD 操作

對於無法用標準 CRUD 表達的操作，使用動詞作為子資源：

```
POST    /api/v1/orders/{orderId}/cancel         # 取消訂單
POST    /api/v1/orders/{orderId}/confirm        # 確認訂單
POST    /api/v1/reports/export                  # 匯出報表
```

---

## 2. HTTP 方法使用

| 方法 | 用途 | 是否冪等 | 有 Request Body |
|---|---|---|---|
| GET | 查詢資源 | 是 | 否 |
| POST | 建立資源 / 觸發操作 | 否 | 是 |
| PUT | 完整更新資源 | 是 | 是 |
| PATCH | 部分更新資源 | 否 | 是 |
| DELETE | 刪除資源 | 是 | 否（通常） |

- GET 請求不可有副作用（不可修改資料）。
- POST 用於建立新資源或觸發無法歸類為 CRUD 的操作。
- PUT 是完整取代，所有欄位都要傳。PATCH 只傳需要更新的欄位。

---

## 3. Request 格式

### JSON 欄位命名

Request Body 統一使用 **camelCase**：

```json
{
    "customerName": "王小明",
    "shippingAddress": "台北市信義區",
    "orderItems": [
        {
            "productId": 12345,
            "quantity": 2,
            "unitPrice": 599.00
        }
    ]
}
```

### Query Parameter 命名

Query parameter 也使用 **camelCase**：

```
GET /api/v1/orders?customerName=王小明&startDate=2025-01-01&pageSize=20
```

### Request 物件設計

```java
@Getter
@Setter
@Schema(description = "建立訂單請求")
public class OrderCreateRequest {

    @NotBlank(message = "客戶名稱不可為空")
    @Schema(description = "客戶名稱", example = "王小明")
    private String customerName;

    @NotBlank(message = "配送地址不可為空")
    @Schema(description = "配送地址", example = "台北市信義區信義路五段7號")
    private String shippingAddress;

    @NotEmpty(message = "訂單商品不可為空")
    @Valid
    @Schema(description = "訂單商品列表")
    private List<OrderItemCreateRequest> orderItems;
}
```

- Request 物件使用 `@Getter` + `@Setter`（因為 Jackson 反序列化需要）。
- 善用 Bean Validation 註解（`@NotBlank`、`@NotNull`、`@Size`、`@Min` 等）。
- 每個欄位都加 `@Schema` 描述。

### 日期時間格式

Request 中的時間欄位統一使用 ISO 8601 格式：

```json
{
    "startTime": "2025-01-01T00:00:00+08:00",
    "endTime": "2025-12-31T23:59:59+08:00"
}
```

---

## 4. Response 格式

### 統一包裹結構

所有 API 回應都由 `ApiResponse` 包裹：

```json
{
    "success": true,
    "code": "SUCCESS",
    "message": null,
    "data": {
        "orderId": 12345,
        "orderNumber": "ORD-20250101-001",
        "customerName": "王小明",
        "totalAmount": 1198.00,
        "status": "PENDING",
        "createdAt": "2025-01-01T10:30:00+08:00"
    }
}
```

### 錯誤回應

```json
{
    "success": false,
    "code": "ORDER_001",
    "message": "訂單不存在",
    "data": null
}
```

### 分頁回應

分頁查詢回傳 Spring Data 的 `Page` 物件，由 Jackson 自動序列化：

```json
{
    "success": true,
    "code": "SUCCESS",
    "message": null,
    "data": {
        "content": [ ... ],
        "totalElements": 100,
        "totalPages": 5,
        "number": 0,
        "size": 20,
        "first": true,
        "last": false
    }
}
```

### Response 物件欄位命名

Response Body 統一使用 **camelCase**，與 Request 一致：

```java
@Getter
@Builder
@Schema(description = "訂單明細回應")
public class OrderDetailResponse {

    @Schema(description = "訂單 ID")
    private final Long orderId;

    @Schema(description = "訂單編號")
    private final String orderNumber;

    @Schema(description = "客戶名稱")
    private final String customerName;

    @Schema(description = "訂單總金額")
    private final BigDecimal totalAmount;

    @Schema(description = "訂單狀態")
    private final OrderStatus status;

    @Schema(description = "建立時間")
    private final ZonedDateTime createdAt;

    /**
     * 從 Entity 建立 Response 的靜態工廠方法。
     */
    public static OrderDetailResponse fromEntity(@NonNull OrderEntity entity) {
        return OrderDetailResponse.builder()
                .orderId(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .customerName(entity.getCustomerName())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
```

- Response 物件使用 `@Getter` + `@Builder`，欄位宣告為 `private final`（不可變）。
- 提供 `fromEntity` 靜態工廠方法用於轉換。
- 每個欄位都加 `@Schema` 描述。

---

## 5. 統一 Response 包裹（AOP）

Controller 直接回傳業務 Response 即可，包裹動作透過 `ResponseBodyAdvice` 完成：

```java
/**
 * 統一 API Response 包裹。
 * 自動將 Controller 回傳值包裹為 ApiResponse 格式。
 */
@RestControllerAdvice(basePackages = "com.example")
public class ApiResponseWrappingAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ApiResponseWrappingAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().isAssignableFrom(ApiResponse.class)
                && !returnType.getParameterType().isAssignableFrom(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                   MethodParameter returnType,
                                   MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request,
                                   ServerHttpResponse response) {
        // 處理 String 回傳（StringHttpMessageConverter 不走 Jackson）
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(ApiResponse.ok(body));
            } catch (JsonProcessingException exception) {
                throw new RuntimeException("序列化 String Response 失敗", exception);
            }
        }

        if (body == null) {
            return ApiResponse.ok(null);
        }

        return ApiResponse.ok(body);
    }
}
```

```java
/**
 * 統一 API 回應包裹物件。
 */
@Getter
@Builder
@Schema(description = "統一 API 回應格式")
public class ApiResponse<T> {

    @Schema(description = "是否成功")
    private final boolean success;

    @Schema(description = "回應代碼")
    private final String code;

    @Schema(description = "回應訊息")
    private final String message;

    @Schema(description = "回應資料")
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
```

---

## 6. Controller 規範

### 完整範例

```java
/**
 * 訂單管理 API。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "訂單管理", description = "訂單相關 API")
public class OrderController {

    private final OrderUseCase orderUseCase;

    /**
     * 查詢訂單明細。
     *
     * @param orderId 訂單 ID
     * @return 訂單明細
     */
    @Operation(summary = "查詢訂單明細", description = "根據訂單 ID 查詢完整訂單明細")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查詢成功"),
            @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    @GetMapping("/{orderId}")
    public OrderDetailResponse findOrderDetail(
            @Parameter(description = "訂單 ID") @PathVariable Long orderId) {
        log.info("查詢訂單明細，orderId: {}", orderId);
        return orderUseCase.findOrderDetail(orderId);
    }

    /**
     * 建立訂單。
     *
     * @param request 建立訂單請求
     * @return 建立結果
     */
    @Operation(summary = "建立訂單", description = "建立新訂單")
    @PostMapping
    public OrderCreateResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        log.info("建立訂單，customerName: {}, itemCount: {}",
                 request.getCustomerName(), request.getOrderItems().size());
        return orderUseCase.createOrder(request);
    }

    /**
     * 搜尋訂單。
     *
     * @param searchRequest 搜尋條件
     * @param pageable 分頁參數
     * @return 訂單分頁列表
     */
    @Operation(summary = "搜尋訂單", description = "依條件搜尋訂單列表")
    @GetMapping
    public Page<OrderSummaryResponse> searchOrders(
            @Valid OrderSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("搜尋訂單，conditions: {}", searchRequest);
        return orderUseCase.searchOrders(searchRequest, pageable);
    }
}
```

### Controller 規則

- Controller 與 UseCase 一對一。Controller 只做：接收參數 → log → 呼叫 UseCase → 回傳。
- 每個方法都要有 log，但**敏感資訊不可 log**（密碼、token、身分證字號、信用卡號等）。
- 直接回傳業務 Response 物件，不需要手動包裹 `ApiResponse`。
- 使用 `@Valid` 觸發 Request 的 Bean Validation。

---

## 7. Swagger / OpenAPI 註解

### Controller 層級

```java
@Tag(name = "模組名稱", description = "模組描述")
```

### 方法層級

```java
@Operation(summary = "簡短摘要", description = "詳細描述")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "400", description = "請求參數錯誤"),
        @ApiResponse(responseCode = "404", description = "資源不存在")
})
```

### 參數層級

```java
@Parameter(description = "參數描述")
```

### DTO 層級

每個 Request / Response 類別都要加 `@Schema(description = "...")`。
每個欄位都要加 `@Schema(description = "...", example = "...")`。

---

## 8. 版本管理

API 版本透過 URL path 管理：

```
/api/v1/orders
/api/v2/orders    （有重大 breaking change 時才升版）
```

- 不使用 header 或 query parameter 進行版本控制。
- 同一時間最多維護兩個主要版本。
- 舊版本至少維護一個迭代週期後才可棄用。


---

# 編碼風格規範


## 1. 命名慣例

### 基本原則

- **全名不縮寫**：用 `request` 而非 `req`，`response` 而非 `res`，`transaction` 而非 `tx`，`configuration` 而非 `config`（package 名稱除外）。
- **命名足夠描述功能**：能從名稱看出用途，不用擔心過長。
- **不把回傳型態放在命名中**：不要用 `getOrderList`、`findOrderDTO`。

### 類別命名

| 層級 | 命名模式 | 範例 |
|---|---|---|
| Controller | `{Domain}Controller` | `OrderController` |
| UseCase | `{Domain}UseCase` | `OrderUseCase` |
| Service | `{Domain}{Feature}Service` | `OrderPricingService` |
| Manager | `{Entity}Manager` | `OrderManager` |
| Repository | `{Entity}Repository` | `OrderRepository` |
| Entity | `{Table}Entity` | `OrderEntity` |
| Specification | `{Entity}Specification` | `OrderSpecification` |
| Request DTO | `{Action}{Domain}Request` | `OrderCreateRequest` |
| Response DTO | `{Domain}{Detail}Response` | `OrderDetailResponse` |
| Exception | `{Domain}Exception` 或 `BusinessException` | `BusinessException` |
| Constant | `{Feature}Constant` | `OrderStatusConstant` |
| Job | `{Feature}Job` | `OrderExpiredCleanupJob` |

### 方法命名

```java
// 查詢 — 用 find
public OrderDetailResponse findOrderDetailByOrderId(Long orderId) { ... }
public List<OrderSummaryResponse> findOrdersByCustomerId(Long customerId) { ... }

// 建立 — 用 create
public OrderCreateResponse createOrder(OrderCreateRequest request) { ... }

// 更新 — 用 update
public void updateOrderStatus(Long orderId, OrderStatus newStatus) { ... }

// 刪除 — 用 delete 或 remove
public void deleteOrder(Long orderId) { ... }

// 判斷 — 用 is / has / can
public boolean isOrderCancellable(Long orderId) { ... }
public boolean hasPermission(Long userId, String action) { ... }

// 計算 — 用 calculate
public BigDecimal calculateFinalAmount(List<OrderItem> items) { ... }

// 驗證 — 用 validate
public void validateOrderCreatable(OrderCreateRequest request) { ... }
```

### 變數命名

```java
// 正確 — 語義清晰
OrderEntity orderEntity = orderManager.findByIdOrThrow(orderId);
List<OrderItemResponse> orderItemResponses = ...;
ZonedDateTime orderCreatedAt = orderEntity.getCreatedAt();

// 錯誤 — 縮寫或含糊
OrderEntity o = orderManager.findByIdOrThrow(orderId);
List<OrderItemResponse> list = ...;
ZonedDateTime dt = orderEntity.getCreatedAt();
```

---

## 2. 類別注入

所有 Spring 管理的類別一律使用：

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderPricingService {

    private final OrderManager orderManager;
    private final PromotionManager promotionManager;
}
```

- 不使用 `@Autowired`（field injection 或 setter injection）。
- 依賴以 `private final` 宣告，透過 `@RequiredArgsConstructor` 自動產生建構子。
- 所有類別都加 `@Slf4j`。

---

## 3. 建構方式

- **靜態工廠方法優先於 constructor**，語義更清晰。
- **禁止 overload 建構子**（多個參數不同的 constructor）。
- **少用 method overload**，容易造成呼叫端無法從參數判斷意圖。

```java
// 正確 — 靜態工廠方法，從名稱看出建構情境
public static OrderCreateRequest ofSingleItem(Long productId, Integer quantity) {
    return OrderCreateRequest.builder()
            .orderItems(List.of(OrderItemCreateRequest.of(productId, quantity)))
            .build();
}

public static OrderCreateRequest ofMultipleItems(List<OrderItemCreateRequest> items) {
    return OrderCreateRequest.builder()
            .orderItems(items)
            .build();
}

// 錯誤 — overload constructor，從參數列看不出差異
public OrderCreateRequest(Long productId, Integer quantity) { ... }
public OrderCreateRequest(List<OrderItemCreateRequest> items) { ... }
```

---

## 4. var 的使用

除非型別宣告非常冗長導致可讀性下降，否則不使用 `var`。

```java
// 正確 — 型別清晰
OrderDetailResponse orderDetail = orderUseCase.findOrderDetail(orderId);
List<OrderItemResponse> orderItems = orderDetail.getOrderItems();

// 可接受 — 型別極長時
var specificationResultPage = orderRepository.findAll(specification, pageable);
var orderItemCategoryDiscountMap = calculateCategoryDiscounts(orderItems, promotionRules);

// 錯誤 — 一般情境不用 var
var order = orderManager.findByIdOrThrow(orderId);
var items = order.getOrderItems();
```

---

## 5. Stream 的使用

偏好使用 Stream 處理集合操作，但不過度串接。人類難以理解時就改用傳統迴圈。

```java
// 正確 — 清晰的 Stream（2-3 層以內）
List<OrderItemResponse> itemResponses = orderEntity.getOrderItems().stream()
        .filter(item -> item.getQuantity() > 0)
        .map(OrderItemResponse::fromEntity)
        .toList();

// 正確 — 簡單的 grouping
Map<OrderStatus, List<OrderEntity>> ordersByStatus = orders.stream()
        .collect(Collectors.groupingBy(OrderEntity::getStatus));

// 錯誤 — 過度串接，難以閱讀
var result = orders.stream()
        .filter(o -> o.getStatus() == ACTIVE)
        .flatMap(o -> o.getItems().stream())
        .collect(Collectors.groupingBy(
                OrderItem::getCategoryId,
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        items -> items.stream()
                                .mapToInt(OrderItem::getQuantity)
                                .sum())));
// 上述情況拆成多步驟或使用 for 迴圈
```

**判斷標準**：如果 Stream 需要超過 5 秒才能理解它在做什麼，就拆開或改用迴圈。

---

## 6. Optional 的使用

用 `Optional` 取代 `!= null` 的判斷：

```java
// 正確
Optional<OrderEntity> orderOptional = orderManager.findByOrderNumber(orderNumber);
OrderEntity order = orderOptional
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

// 正確 — Optional 搭配 map
String customerEmail = orderManager.findByOrderNumber(orderNumber)
        .map(OrderEntity::getCustomerEmail)
        .orElse("unknown@example.com");

// 錯誤 — 手動 null 檢查
OrderEntity order = orderManager.findByOrderNumber(orderNumber).orElse(null);
if (order == null) {
    throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
}
```

**注意**：
- 不要用 `Optional` 作為方法參數（Lombok `@NonNull` 或 `@Nullable` 更適合）。
- 不要用 `Optional` 作為 Entity 欄位。
- `Optional` 適合用在方法回傳值，表達「可能沒有」的語義。

---

## 7. 時間處理

時間統一使用 `ZonedDateTime`，不使用 `LocalDateTime`、`Date` 或 `Timestamp`。

```java
// Entity
@Column(name = "created_at", nullable = false, updatable = false)
private ZonedDateTime createdAt;

// DTO
@Schema(description = "建立時間", example = "2025-01-01T10:30:00+08:00")
private ZonedDateTime createdAt;

// 取得當前時間
ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Taipei"));

// 時間比較
boolean isExpired = order.getExpiredAt().isBefore(ZonedDateTime.now());
```

---

## 8. DTO 設計

- 重要邏輯元件使用 DTO 承載。
- **業務邏輯寫在 DTO 中，不寫在 Entity 中。**
- DTO 可包含計算方法、格式化方法、驗證方法。

```java
@Getter
@Builder
@Schema(description = "訂單明細回應")
public class OrderDetailResponse {

    @Schema(description = "訂單 ID")
    private final Long orderId;

    @Schema(description = "商品列表")
    private final List<OrderItemResponse> orderItems;

    @Schema(description = "訂單總金額")
    private final BigDecimal totalAmount;

    /**
     * 計算折扣後金額。
     * 套用折扣率後取得最終金額，最低不低於 0。
     */
    public BigDecimal calculateDiscountedAmount(@NonNull BigDecimal discountRate) {
        return totalAmount.multiply(BigDecimal.ONE.subtract(discountRate))
                .max(BigDecimal.ZERO);
    }

    /**
     * 判斷是否為高價值訂單（金額超過 10,000）。
     */
    public boolean isHighValueOrder() {
        return totalAmount.compareTo(new BigDecimal("10000")) > 0;
    }

    /**
     * 從 Entity 建立 Response。
     */
    public static OrderDetailResponse fromEntity(@NonNull OrderEntity entity,
                                                  @NonNull List<OrderItemResponse> items) {
        return OrderDetailResponse.builder()
                .orderId(entity.getId())
                .orderItems(items)
                .totalAmount(entity.getTotalAmount())
                .build();
    }
}
```

---

## 9. 設計模式與繼承

- **除非有強烈擴展需求或能顯著減少冗餘程式碼，不引入設計模式。**
- **以組合代替繼承。**
- 簡單直覺的程式碼優於精巧的抽象。

```java
// 正確 — 組合
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderNotificationService {

    private final EmailSender emailSender;       // 組合
    private final SmsSender smsSender;            // 組合
    private final NotificationTemplateManager notificationTemplateManager;

    public void notifyOrderCreated(@NonNull OrderEntity order) {
        String emailContent = notificationTemplateManager
                .renderOrderCreatedEmail(order);
        emailSender.send(order.getCustomerEmail(), "訂單建立通知", emailContent);
    }
}

// 錯誤 — 不必要的繼承鏈或 Strategy Pattern
public abstract class AbstractNotificationService { ... }
public class EmailNotificationService extends AbstractNotificationService { ... }
public class SmsNotificationService extends AbstractNotificationService { ... }
// 除非真的有 5+ 種通知管道且未來會持續增加，否則不需要這樣設計
```

---

## 10. 註解規範

### JavaDoc

所有類別與 public 方法都要有 JavaDoc：

```java
/**
 * 訂單價格計算服務。
 * 負責根據促銷規則、會員等級計算訂單的最終金額。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderPricingService {

    /**
     * 計算訂單最終金額。
     * 
     * 先套用商品類別折扣，再依總金額判斷滿額折扣，兩者可疊加但不低於原價 50%。
     *
     * @param orderItems 訂單商品列表
     * @param promotionRules 適用的促銷規則
     * @return 折扣後的最終金額
     */
    public BigDecimal calculateFinalAmount(@NonNull List<OrderItem> orderItems,
                                           @NonNull List<PromotionRule> promotionRules) {
        // ...
    }
}
```

### 行內註解

- 複雜邏輯要有詳細註解，以人類能理解為目的。
- **註解中不要提到「修改」、「建議修改」、「待修改」、「TODO: 改成...」等字眼。** 註解描述的是程式碼當前的行為與設計意圖。
- 用「為什麼這樣做」而非「做了什麼」來撰寫註解（程式碼本身已經說明了做了什麼）。

```java
// 正確 — 解釋設計意圖
// 先計算各商品類別折扣的小計，因為類別折扣與滿額折扣的計算基礎不同
BigDecimal subtotal = calculateCategoryDiscountSubtotal(orderItems, promotionRules);

// 錯誤 — 重複程式碼的敘述
// 計算小計
BigDecimal subtotal = calculateCategoryDiscountSubtotal(orderItems, promotionRules);

// 錯誤 — 提到修改
// 這邊之後要改成用新的折扣引擎
BigDecimal subtotal = calculateCategoryDiscountSubtotal(orderItems, promotionRules);
```

---

## 11. Lombok 使用

### 常用註解

| 註解 | 用途 | 常見場景 |
|---|---|---|
| `@Slf4j` | 自動產生 log 欄位 | 所有 Spring 管理的類別 |
| `@RequiredArgsConstructor` | 依 final 欄位產生建構子 | 所有 Spring 管理的類別 |
| `@Getter` | 產生 getter | Entity, DTO |
| `@Setter` | 產生 setter | Entity, Request DTO |
| `@Builder` | 建造者模式 | Response DTO, 不可變物件 |
| `@FieldNameConstants` | 產生欄位名稱常數 | Entity |
| `@NonNull` | 參數 null 檢查 | 方法參數 |
| `@NoArgsConstructor(access = AccessLevel.PRIVATE)` | 私有無參建構子 | 工具類別, Specification |

### @NonNull 標註

善用 `@NonNull` 讓開發者一眼看出參數是否可空：

```java
public OrderDetailResponse findOrderDetail(@NonNull Long orderId) { ... }

public void updateOrderStatus(@NonNull Long orderId,
                               @NonNull OrderStatus newStatus) { ... }
```

如果參數允許 null，使用 `@Nullable`（`jakarta.annotation`）明確標示：

```java
public List<OrderEntity> findOrders(@NonNull OrderStatus status,
                                     @Nullable ZonedDateTime startTime,
                                     @Nullable ZonedDateTime endTime) { ... }
```

---

## 12. 其他風格偏好

### 常數定義

- 不要使用 magic number 或 magic string。
- 常數放在對應的 Constant 類別中，按功能分檔。

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderConstant {
    public static final int MAX_ORDER_ITEMS = 100;
    public static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");
}
```

### 條件判斷

複雜條件提取為有意義的變數或方法：

```java
// 正確
boolean isEligibleForFreeShipping = order.getTotalAmount()
        .compareTo(OrderConstant.FREE_SHIPPING_THRESHOLD) >= 0;
boolean isVipCustomer = customer.getMemberLevel() == MemberLevel.VIP;

if (isEligibleForFreeShipping || isVipCustomer) {
    applyFreeShipping(order);
}

// 錯誤
if (order.getTotalAmount().compareTo(new BigDecimal("1000")) >= 0
        || customer.getMemberLevel() == MemberLevel.VIP) {
    applyFreeShipping(order);
}
```

### 方法長度

單一方法不超過 50 行。超過時拆分為多個私有方法，每個方法有清晰的職責。

### Early Return

善用 early return 減少巢狀層級：

```java
// 正確 — early return
public void processOrder(@NonNull OrderEntity order) {
    if (order.getStatus() != OrderStatus.PENDING) {
        throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
    }

    if (order.getOrderItems().isEmpty()) {
        throw new BusinessException(ErrorCode.EMPTY_ORDER_ITEMS);
    }

    // 主要邏輯...
}

// 錯誤 — 深層巢狀
public void processOrder(OrderEntity order) {
    if (order.getStatus() == OrderStatus.PENDING) {
        if (!order.getOrderItems().isEmpty()) {
            // 主要邏輯...
        }
    }
}
```


---

# 錯誤處理與日誌規範


## 1. 自訂 ErrorCode

使用列舉定義所有錯誤代碼，按業務領域分群組：

```java
/**
 * 系統錯誤代碼。
 * 代碼格式：{領域}_{序號}，例如 ORDER_001。
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== 通用錯誤 =====
    SYSTEM_ERROR("SYSTEM_001", "系統錯誤", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PARAMETER("SYSTEM_002", "請求參數錯誤", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("SYSTEM_003", "資源不存在", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("SYSTEM_004", "存取被拒", HttpStatus.FORBIDDEN),

    // ===== 訂單相關 =====
    ORDER_NOT_FOUND("ORDER_001", "訂單不存在", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS("ORDER_002", "訂單狀態不允許此操作", HttpStatus.BAD_REQUEST),
    EMPTY_ORDER_ITEMS("ORDER_003", "訂單商品不可為空", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CANCELLED("ORDER_004", "訂單已取消", HttpStatus.CONFLICT),

    // ===== 庫存相關 =====
    INSUFFICIENT_STOCK("INVENTORY_001", "庫存不足", HttpStatus.BAD_REQUEST),

    // ===== 付款相關 =====
    PAYMENT_FAILED("PAYMENT_001", "付款失敗", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_TIMEOUT("PAYMENT_002", "付款逾時", HttpStatus.GATEWAY_TIMEOUT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
```

### ErrorCode 命名規則

- 代碼格式：`{領域}_{三位數序號}`，例如 `ORDER_001`。
- 每個領域的代碼獨立編號。
- message 是人類可讀的預設訊息，可在拋出例外時覆寫。

---

## 2. 自訂例外類別

```java
/**
 * 業務例外。
 * 所有可預期的業務錯誤都應使用此例外拋出。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 以 ErrorCode 的預設訊息建立例外。
     */
    public BusinessException(@NonNull ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 以自訂訊息建立例外。
     * 適用於需要附帶動態資訊的場景（如含具體的 ID、數值等）。
     */
    public BusinessException(@NonNull ErrorCode errorCode, @NonNull String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    /**
     * 以 ErrorCode 建立例外的靜態工廠方法。
     */
    public static BusinessException of(@NonNull ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    /**
     * 以自訂訊息建立例外的靜態工廠方法。
     */
    public static BusinessException of(@NonNull ErrorCode errorCode,
                                        @NonNull String detailMessage) {
        return new BusinessException(errorCode, detailMessage);
    }
}
```

### 使用方式

```java
// 使用預設訊息
throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);

// 使用自訂訊息（附帶具體資訊）
throw new BusinessException(ErrorCode.ORDER_NOT_FOUND,
        String.format("訂單 %d 不存在", orderId));

// 搭配 Optional
OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
```

---

## 3. ControllerAdvice 統一處理

```java
/**
 * 全域例外處理器。
 * 統一將各類例外轉換為 ApiResponse 格式回應。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 處理業務例外。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception) {
        log.warn("業務例外，errorCode: {}, message: {}",
                 exception.getErrorCode().getCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(
                        exception.getErrorCode().getCode(),
                        exception.getMessage()));
    }

    /**
     * 處理 Bean Validation 驗證失敗（@Valid 觸發）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("參數驗證失敗，errors: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_PARAMETER.getCode(),
                        errorMessage));
    }

    /**
     * 處理 Query Parameter 綁定失敗。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            BindException exception) {
        String errorMessage = exception.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("參數綁定失敗，errors: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_PARAMETER.getCode(),
                        errorMessage));
    }

    /**
     * 處理路徑參數型別轉換失敗。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        String errorMessage = String.format("參數 %s 的值 '%s' 型別不正確",
                exception.getName(), exception.getValue());
        log.warn("參數型別轉換失敗，{}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_PARAMETER.getCode(),
                        errorMessage));
    }

    /**
     * 處理存取被拒。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException exception) {
        log.warn("存取被拒，message: {}", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        ErrorCode.ACCESS_DENIED.getCode(),
                        ErrorCode.ACCESS_DENIED.getMessage()));
    }

    /**
     * 處理所有未預期的例外。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception exception) {
        log.error("未預期例外", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.SYSTEM_ERROR.getCode(),
                        ErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
```

### 處理順序

ControllerAdvice 的 `@ExceptionHandler` 按照例外類別的特殊性排列：越具體的例外放越前面，`Exception.class` 作為兜底放最後。

---

## 4. 日誌規範

### 日誌層級使用

| 層級 | 用途 | 範例 |
|---|---|---|
| `ERROR` | 系統無法自動恢復的錯誤 | 資料庫連線失敗、未預期例外 |
| `WARN` | 可預期的業務錯誤、需關注的情況 | BusinessException、重試成功 |
| `INFO` | 重要業務事件、API 請求 | 訂單建立、付款完成、Controller 請求 |
| `DEBUG` | 除錯資訊 | 查詢條件、中間計算結果 |

### Controller 請求日誌

```java
@PostMapping
public OrderCreateResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
    log.info("建立訂單，customerName: {}, itemCount: {}",
             request.getCustomerName(), request.getOrderItems().size());
    return orderUseCase.createOrder(request);
}
```

### 敏感資訊禁止 log

以下資訊絕對不可出現在 log 中：

- 密碼、token、API key
- 身分證字號、護照號碼
- 信用卡號、銀行帳號
- 個人地址、電話號碼（視業務需求判斷）
- 任何可直接識別個人身份的完整資訊

```java
// 正確 — 只 log 非敏感欄位
log.info("使用者登入，userId: {}", request.getUserId());

// 錯誤 — log 了密碼
log.info("使用者登入，userId: {}, password: {}", request.getUserId(), request.getPassword());
```

### 日誌格式

- 使用 SLF4J 的 `{}` 占位符，不要用字串拼接。
- log 訊息使用中文或英文皆可，但同一專案統一。
- 帶上足夠的 context 資訊（ID、狀態、數量等）。

```java
// 正確
log.info("訂單狀態更新，orderId: {}, from: {}, to: {}",
         orderId, oldStatus, newStatus);

// 錯誤 — 字串拼接
log.info("訂單狀態更新，orderId: " + orderId);

// 錯誤 — 資訊不足
log.info("訂單狀態更新");
```

---

## 5. Validation 錯誤處理

### Request 驗證

使用 Bean Validation 註解在 Request DTO 上進行驗證：

```java
@Getter
@Setter
@Schema(description = "建立訂單請求")
public class OrderCreateRequest {

    @NotBlank(message = "客戶名稱不可為空")
    @Size(max = 100, message = "客戶名稱不可超過 100 字")
    @Schema(description = "客戶名稱")
    private String customerName;

    @NotNull(message = "訂單金額不可為空")
    @DecimalMin(value = "0.01", message = "訂單金額必須大於 0")
    @Schema(description = "訂單金額")
    private BigDecimal amount;

    @NotEmpty(message = "訂單商品不可為空")
    @Size(max = 100, message = "單筆訂單商品數不可超過 100")
    @Valid
    @Schema(description = "訂單商品列表")
    private List<OrderItemCreateRequest> orderItems;
}
```

- Controller 方法參數加 `@Valid` 觸發驗證。
- 驗證失敗由 `GlobalExceptionAdvice` 統一處理，轉換為 `ApiResponse` 格式。
- 每個驗證註解都要帶 `message` 參數，提供人類可讀的錯誤訊息。


---

# Git 與協作規範


## 1. Commit Message 格式

採用 **Conventional Commits** 規範：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type（必填）

| Type | 用途 | 範例 |
|---|---|---|
| `feat` | 新功能 | `feat(order): 新增訂單取消功能` |
| `fix` | 修復 bug | `fix(payment): 修復付款金額計算錯誤` |
| `refactor` | 重構（不改變功能） | `refactor(order): 將訂單查詢改為 Specification` |
| `docs` | 文件 | `docs(readme): 更新 API 文件` |
| `test` | 測試 | `test(order): 新增訂單建立的單元測試` |
| `chore` | 維護性工作 | `chore(deps): 升級 Spring Boot 至 3.2.1` |
| `style` | 格式調整（不影響邏輯） | `style(order): 調整 import 排序` |
| `perf` | 效能優化 | `perf(query): 優化訂單列表查詢效能` |
| `ci` | CI/CD 設定 | `ci(pipeline): 新增 SonarQube 掃描步驟` |
| `build` | 建置系統 | `build(gradle): 新增 Lombok dependency` |

### Scope（選填但建議填寫）

Scope 對應模組或功能領域：

- 模組名稱：`order`、`payment`、`inventory`、`common`、`infra`
- 橫切功能：`auth`、`config`、`ci`、`deps`

### Subject（必填）

- 用中文或英文皆可，但同一專案統一。
- 不超過 72 個字元。
- 不以句號結尾。
- 使用祈使語氣（中文：「新增...」、「修復...」、「調整...」）。

### Body（選填）

解釋「為什麼」做這個變更，而非「做了什麼」（commit diff 已經說明了做了什麼）。
每行不超過 72 個字元。

### Footer（選填）

- 關聯的 issue：`Closes #123`、`Refs #456`
- Breaking change：`BREAKING CHANGE: 移除了 /api/v1/orders/legacy 端點`

### 完整範例

```
feat(order): 新增依客戶名稱搜尋訂單功能

新增 OrderSpecification.hasCustomerNameLike()，支援模糊搜尋。
搜尋結果支援分頁，預設依建立時間降冪排列。

Closes #234
```

```
fix(payment): 修復並行付款時的金額扣款競爭條件

使用分散式鎖確保同一筆訂單的付款操作互斥執行，
避免多次扣款的問題。

Closes #567
```

### 禁止事項

- 不要用 `update`、`modify`、`change` 這類模糊的 type。
- 不要在一個 commit 中混合不相關的變更。
- 不要用 `fix: 修了一些東西` 這種無意義的描述。

---

## 2. Merge Request (MR) 規範

### MR 標題

與 commit message 格式一致：`<type>(<scope>): <subject>`

如果 MR 包含多個 commit，標題取最主要的變更來命名。

### MR 描述模板

```markdown
## 變更說明

簡述這個 MR 做了什麼，以及為什麼需要這個變更。

## 變更類型

- [ ] 新功能 (feat)
- [ ] Bug 修復 (fix)
- [ ] 重構 (refactor)
- [ ] 文件更新 (docs)
- [ ] 其他：___

## 影響範圍

列出受影響的模組、API、資料表等。

## 測試

說明如何驗證這個變更，或附上測試結果。

## 相關 Issue

Closes #___

## 自我檢查

- [ ] 符合團隊編碼規範
- [ ] 已撰寫 / 更新相關測試
- [ ] 已更新 Swagger 註解
- [ ] 無敏感資訊被 log
- [ ] 無 native SQL
- [ ] 複雜邏輯已加註解
```

### MR 粒度

- 一個 MR 只做一件事。不要把新功能、bug 修復、重構混在同一個 MR。
- 單個 MR 的變更量建議不超過 500 行（不含測試和自動產生的程式碼）。
- 如果功能較大，拆成多個連續的 MR，每個 MR 獨立可工作。

### MR 流程

1. 從目標分支建立 feature branch。
2. 開發完成後推送並建立 MR。
3. 指定至少一位 reviewer。
4. Reviewer 完成 review 並 approve 後，由 MR 作者合併。
5. 合併後刪除 feature branch。

### Merge 策略

- 使用 **Squash and Merge**：將 feature branch 的多個 commit 壓縮為一個 commit 合併到目標分支。
- Squash 後的 commit message 使用 MR 的標題。
- 保持目標分支的 commit history 線性且乾淨。

---

## 3. 分支策略

### 分支命名

```
feature/{issue-id}-{brief-description}    # 新功能
fix/{issue-id}-{brief-description}        # Bug 修復
refactor/{brief-description}              # 重構
hotfix/{issue-id}-{brief-description}     # 緊急修復

範例：
feature/234-order-search-by-customer-name
fix/567-payment-race-condition
refactor/specification-migration
hotfix/789-critical-payment-bug
```

- 分支名稱使用 kebab-case。
- 帶上 issue ID 方便追溯。
- 描述要能看出這個分支在做什麼。

### 主要分支

| 分支 | 用途 | 保護規則 |
|---|---|---|
| `main` | 生產環境程式碼 | 禁止直接推送，只接受 MR 合併 |
| `develop` | 開發整合分支 | 禁止直接推送，只接受 MR 合併 |
| `release/*` | 版本發布準備 | 禁止直接推送 |

---

## 4. Code Review 原則

### Reviewer 檢查重點

1. **架構合規**：是否遵循分層架構？引用關係是否正確？
2. **命名**：是否全名、有描述性？是否有縮寫？
3. **JPA 規範**：是否使用 Specification？有無 native SQL？有無字串 field 引用？
4. **錯誤處理**：是否使用 ErrorCode + BusinessException？
5. **日誌**：Controller 是否有 log？有無敏感資訊？
6. **Swagger**：Controller 方法與 DTO 是否有完整的 Swagger 註解？
7. **測試**：是否有對應的測試？
8. **註解**：複雜邏輯是否有註解？註解是否有「修改」字眼？

### Review 禮儀

- Review 針對程式碼，不針對個人。
- 用「建議」而非「命令」的語氣。
- 如果有疑問，先提問而非直接否決。
- 對於好的設計，給予正面回饋。


---

# 基礎設施規範


## 1. 排程任務與分散式鎖

### 所有 Job 必須加分散式鎖

在多實例部署環境下，排程任務會在每個實例上同時觸發。必須使用分散式鎖確保同一時間只有一個實例執行。

```java
/**
 * 過期訂單清理排程。
 * 每日凌晨 2 點執行，清理超過 30 天未完成的訂單。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderExpiredCleanupJob {

    private final OrderCleanupService orderCleanupService;
    private final DistributedLockService distributedLockService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredOrders() {
        distributedLockService.executeWithLock(
                "job:order-expired-cleanup",
                Duration.ofMinutes(30),
                () -> {
                    log.info("開始清理過期訂單");
                    int cleanedCount = orderCleanupService.cleanupExpiredOrders();
                    log.info("過期訂單清理完成，清理數量: {}", cleanedCount);
                });
    }
}
```

### 分散式鎖服務介面

放在 `infra` 模組，領域模組透過介面引用：

```java
/**
 * 分散式鎖服務。
 * 提供跨實例的互斥鎖能力。
 */
public interface DistributedLockService {

    /**
     * 取得鎖並執行任務。若無法取得鎖則跳過。
     *
     * @param lockKey 鎖的唯一識別
     * @param leaseTime 鎖的最長持有時間
     * @param task 要執行的任務
     */
    void executeWithLock(@NonNull String lockKey,
                          @NonNull Duration leaseTime,
                          @NonNull Runnable task);

    /**
     * 取得鎖並執行有回傳值的任務。
     */
    <T> Optional<T> executeWithLock(@NonNull String lockKey,
                                     @NonNull Duration leaseTime,
                                     @NonNull Supplier<T> task);
}
```

### Job 規範

- Job class 放在模組的 `job/` package 下。
- 每個 Job 只做一件事。
- 使用 `@Scheduled` 定義排程。
- 記錄 Job 開始與結束的 log，包含處理數量等關鍵資訊。
- 鎖的 key 使用 `job:` 前綴 + 描述性名稱。

---

## 2. 快取

### 快取 Key 管理

統一在 Constant 中定義快取 key，避免散落各處的 magic string：

```java
/**
 * 快取 key 常數。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeyConstant {

    public static final String ORDER_DETAIL_PREFIX = "order:detail:";
    public static final String USER_PROFILE_PREFIX = "user:profile:";
    public static final Duration ORDER_DETAIL_TTL = Duration.ofMinutes(30);
    public static final Duration USER_PROFILE_TTL = Duration.ofHours(1);

    public static String orderDetailKey(Long orderId) {
        return ORDER_DETAIL_PREFIX + orderId;
    }

    public static String userProfileKey(Long userId) {
        return USER_PROFILE_PREFIX + userId;
    }
}
```

### 快取使用原則

- 快取 key 使用冒號分隔的階層結構：`{domain}:{entity}:{id}`。
- 明確設定 TTL，不使用永不過期的快取。
- 寫入操作後主動清除或更新相關快取。
- 快取穿透、雪崩等問題需要有對應的防護機制。

---

## 3. 設定管理

### application.yml 結構

```yaml
# application.yml — 共用設定
spring:
  application:
    name: order-service
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: Asia/Taipei

# application-dev.yml — 開發環境
# application-staging.yml — 測試環境
# application-prod.yml — 生產環境
```

### 自訂設定

使用 `@ConfigurationProperties` 搭配 record 或不可變類別：

```java
/**
 * 訂單相關設定。
 */
@ConfigurationProperties(prefix = "app.order")
public record OrderProperties(
        int maxItemsPerOrder,
        Duration expiredCleanupRetention,
        BigDecimal freeShippingThreshold
) {
}
```

```yaml
app:
  order:
    max-items-per-order: 100
    expired-cleanup-retention: 30d
    free-shipping-threshold: 1000
```

### 設定原則

- 不要在程式碼中硬編碼環境相關的值。
- 敏感設定（密碼、金鑰）使用環境變數或 secret management 工具，不寫在 yml 中。
- `spring.jpa.open-in-view` 一律設為 `false`。

---

## 4. 安全性

### API 認證

統一使用 Spring Security 處理認證與授權。Controller 不直接做權限判斷。

```java
/**
 * 安全性設定。
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

### 敏感資料處理

- 密碼使用 BCrypt 或 Argon2 雜湊，不可明文儲存。
- API 回應中不回傳密碼、token 等敏感資訊。
- log 中不記錄敏感資訊（參見 error-and-logging.md）。

---

## 5. 測試

### 測試分層

| 類型 | 範圍 | 工具 |
|---|---|---|
| 單元測試 | Service、Manager、DTO 邏輯 | JUnit 5、Mockito |
| 整合測試 | Repository、API 端到端 | @SpringBootTest、TestContainers |

### 單元測試範例

```java
@ExtendWith(MockitoExtension.class)
class OrderPricingServiceTest {

    @InjectMocks
    private OrderPricingService orderPricingService;

    @Mock
    private OrderManager orderManager;

    @Mock
    private PromotionManager promotionManager;

    @Test
    @DisplayName("計算最終金額：套用類別折扣後不低於原價 50%")
    void calculateFinalAmount_shouldNotBeLowerThanHalfOfOriginalPrice() {
        // given
        List<OrderItem> orderItems = List.of(
                OrderItem.of(1L, new BigDecimal("1000"), 1));
        List<PromotionRule> promotionRules = List.of(
                PromotionRule.ofCategoryDiscount(1L, new BigDecimal("0.8")));

        // when
        BigDecimal finalAmount = orderPricingService
                .calculateFinalAmount(orderItems, promotionRules);

        // then
        assertThat(finalAmount)
                .isGreaterThanOrEqualTo(new BigDecimal("500"));
    }
}
```

### 測試命名

- 測試類別：`{被測類別名稱}Test`。
- 測試方法：`{methodName}_{scenario}` 或用 `@DisplayName` 描述。
- `@DisplayName` 使用中文描述測試情境。

### 測試原則

- 每個 public 方法至少要有正向與反向測試。
- 測試方法遵循 Given-When-Then 結構。
- Mock 外部依賴，只測試當前類別的邏輯。
- 不要在測試中使用 `@Autowired` 注入 Spring context（單元測試用 `@ExtendWith(MockitoExtension.class)`）。
- 整合測試使用 TestContainers 管理資料庫等外部服務。

---

## 6. 資料庫遷移

### 使用 Flyway 或 Liquibase

- 所有資料庫結構變更必須透過 migration script 管理。
- 禁止手動修改資料庫結構。
- `spring.jpa.hibernate.ddl-auto` 在非開發環境一律設為 `validate`。

### Migration 檔案命名（Flyway）

```
V{版號}__{描述}.sql

範例：
V1.0.0__create_orders_table.sql
V1.0.1__add_customer_email_to_orders.sql
V1.1.0__create_payment_records_table.sql
```

### Migration 原則

- 每個 migration 只做一件事。
- migration 必須可重複執行（idempotent）或確保只執行一次。
- 資料遷移與結構遷移分開。
- 大型資料表的結構變更要考慮鎖表影響。

---

## 7. 健康檢查與監控

### Actuator 設定

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: when-authorized
```

- 開放 `/actuator/health` 作為負載均衡器的健康檢查端點。
- Prometheus metrics 端點供監控系統抓取。
- 敏感的 actuator 端點需要認證。

---

## 8. 非同步與事件

### Spring Event

模組間的非同步通訊優先使用 Spring Event：

```java
/**
 * 訂單建立事件。
 */
@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {

    private final Long orderId;
    private final String orderNumber;
    private final ZonedDateTime createdAt;

    public static OrderCreatedEvent of(@NonNull OrderEntity order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCreatedAt());
    }
}
```

```java
// 發送事件
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void afterOrderCreated(@NonNull OrderEntity order) {
        applicationEventPublisher.publishEvent(OrderCreatedEvent.of(order));
        log.info("已發送訂單建立事件，orderId: {}", order.getId());
    }
}
```

```java
// 監聯事件
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderNotificationEventListener {

    private final OrderNotificationService orderNotificationService;

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("收到訂單建立事件，orderId: {}", event.getOrderId());
        orderNotificationService.sendOrderCreatedNotification(event.getOrderId());
    }
}
```

### 事件設計原則

- 事件物件是不可變的（所有欄位 `final`）。
- 事件只攜帶 ID 與必要資訊，不攜帶完整 Entity。
- 事件監聽器中的重操作考慮使用 `@Async` 或 MQ。
- 跨模組的事件類別放在 `common` 模組。

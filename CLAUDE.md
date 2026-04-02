# Loveratory

## Backend 開發規範

後端採用 Spring Boot 3 多模組架構，所有 Java 程式碼必須遵守團隊開發規範。

完整規範請參閱：@docs/spring-boot-conventions.md

### 關鍵規則速查

- **注入**：`@Slf4j` + `@RequiredArgsConstructor` + `private final`，禁止 `@Autowired`
- **命名**：全名不縮寫，描述性命名，不把回傳型態放命名中
- **建構**：靜態工廠方法優先，禁止 overload 建構子
- **分層**：Controller → UseCase → Service → Manager → Repository（嚴格單向引用）
- **JPA**：用 Specification 不用 native SQL，field 引用用 `@FieldNameConstants`
- **Entity**：只映射資料庫，業務邏輯寫在 DTO
- **錯誤處理**：ErrorCode enum + BusinessException，由 ControllerAdvice 統一處理
- **Response**：由 `ResponseBodyAdvice` AOP 自動包裹 `ApiResponse`
- **API URL**：kebab-case、複數名詞、`/api/v1/` 前綴
- **時間**：統一用 `ZonedDateTime`
- **Commit**：Conventional Commits 格式（`feat(scope): subject`）
- **Swagger**：Controller 加 `@Tag`、`@Operation`，DTO 每個欄位加 `@Schema`
- **日誌**：Controller 必須 log 請求，禁止 log 敏感資訊
- **排程 Job**：必須加分散式鎖

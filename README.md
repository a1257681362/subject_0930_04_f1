# EVOPS 基础工作区

这是 springboot的骨架项目。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

启动前准备：

1. 执行 `mvn -q -DskipTests compile` 验证骨架构建。
2. 执行 `mvn spring-boot:run` 启动应用；Spring Boot 会自动执行 `src/main/resources/schema.sql`。
3. H2 数据文件默认写入工作区 `data/evops`。如需修改路径，编辑 `application.yml` 中的 `jdbc:h2:file:` 连接串。

根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。

# 城市地下管网巡检台账（com.evops.pipe）

已跑通闭环：**建档 → 派检 → 现场记录 → 隐患登记 → 复核**（抢修结算后续迭代）。

## 数据模型

| 表 | 说明 |
| --- | --- |
| `t_pipe_segment` | 管段档案：材质、口径、埋深、所属道路；`segment_no` 唯一 |
| `t_pipe_well` | 井室档案：坐标、盖板、风险等级；`well_no` 唯一；`confirm_status` 确认状态 |
| `t_pipe_well_location_hist` | 井室位置历史，**只增不删** |
| `t_inspect_order` / `t_inspect_order_route` | 巡检工单（人员、状态机）与路线明细 |
| `t_inspect_record` | 现场记录（发现问题、是否隐患） |
| `t_hazard` | 隐患登记与复核 |
| `t_network_permission` | 管网权限模式（单行）：NORMAL / TIGHTENED |

## 业务规则

- 管段编号、井室编号全局唯一，建档后不可变更。
- 井室坐标每次变化追加位置历史；历史位置不能被删除（无删除入口）。
- 井室确认（confirm）后，已确认事实不可篡改。
- 工单状态机：`DISPATCHED` → `FIELD_RECORDED` → `CLOSED`；隐患复核退回时工单回到 `DISPATCHED` 待重检。
- 隐患复核通过且工单无其他待复核隐患时，工单自动闭环；无隐患工单现场记录完成后可手动闭环。
- **权限收紧（TIGHTENED）**：管段档案停止推进（一切写操作拒绝）；井室档案仅保留已确认事实（写操作冻结，已确认数据保留可查、不删除）。

## API 一览（/api/pipe，本地演示已在 Shiro 放行）

```bash
# 建档
POST /api/pipe/segments        {"segmentNo":"SEG-001","material":"球墨铸铁","diameterMm":600,"buryDepthM":2.5,"roadName":"人民路"}
POST /api/pipe/wells           {"wellNo":"WELL-001","segmentId":1,"coordLng":120.123456,"coordLat":30.123456,"coverType":"重型铸铁井盖","riskLevel":"MEDIUM"}
# 派检
POST /api/pipe/inspect-orders  {"inspectorName":"张三","routeDesc":"人民路沿线","route":[{"wellId":1}]}
# 现场记录
POST /api/pipe/inspect-orders/1/records  {"wellId":1,"findingDesc":"井室积水，盖板锈蚀","hasHazard":true,"recordedBy":"张三"}
# 隐患登记
POST /api/pipe/hazards         {"orderId":1,"recordId":1,"hazardDesc":"盖板锈蚀存在坠落风险","hazardLevel":"MAJOR"}
# 复核（pass=false 退回重检）
POST /api/pipe/hazards/1/review  {"pass":true,"reviewComment":"属实","reviewedBy":"李四"}
# 井室确认 / 位置历史
POST /api/pipe/wells/1/confirm
GET  /api/pipe/wells/1/locations
# 权限收紧 / 恢复
POST /api/pipe/permission/tighten  {"reason":"上级保密要求"}
POST /api/pipe/permission/restore  {"reason":"保密期结束"}
```

## 验证

`mvn test` 中 `PipeClosedLoopTest` 覆盖：完整闭环、编号唯一、位置历史只增不删、已确认事实不可改、权限收紧/恢复。

注意：`application.yml` 中 `shiro.annotations.enabled=false` 是有意为之——Shiro 的注解代理会把 `@Transactional` 服务二次包装成 JDK 代理导致按类型注入失败；当前仅用 Shiro 过滤器链鉴权。

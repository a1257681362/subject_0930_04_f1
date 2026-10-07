# EVOPS 基础工作区

这是 springboot 的骨架项目。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

## 启动

1. 执行 `mvn -q -DskipTests compile` 验证骨架构建。
2. 执行 `mvn spring-boot:run` 启动应用；Spring Boot 会自动执行 `src/main/resources/schema.sql`（幂等，可重复执行）。
3. H2 数据文件默认写入工作区 `data/evops`。如需修改路径，编辑 `application.yml` 中的 `jdbc:h2:file:` 连接串。
4. 默认账号 `bootstrap/bootstrap`，除 `/api/health` 外接口均需登录；测试环境可用 `evops.security.enabled=false` 关闭鉴权。

根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。

## 城市地下管网巡检台账（pipe-1）

业务闭环：**建档 → 派检 → 现场记录 → 隐患登记 → 整改复核闭环**。抢修结算暂不实现，后续迭代再做。

### 核心规则

- **管段编号、井室编号唯一**：应用层 + 数据库唯一约束（`uk_pipe_segment_no`、`uk_manhole_no`）双重保证；编号一经建档不可修改。
- **历史位置不能被删除**：井室建档坐标自动留痕，坐标每次变化向 `t_manhole_location_history` 追加一条；该表只追加，系统不提供修改/删除接口。
- **隐患闭环**：`REGISTERED → RECTIFIED → REVIEWED（闭环）`；复核驳回为 `REJECTED`，须重新整改后再复核；闭环后不可再整改。
- **管网权限收紧**（`PUT /api/system/permission?restricted=true`）：
  - 管段档案**停止推进**：禁止新建、变更、确认；
  - 井室档案**保留已确认事实**：已确认井室的坐标/盖板/风险等级冻结，禁止变更、移位、新增确认；未确认草稿同样不能再确认；
  - 放开开关后档案可继续推进。

### 数据模型

| 表 | 说明 |
|---|---|
| `t_pipe_segment` | 管段档案：材质、口径(mm)、埋深(m)、所属道路、状态(DRAFT/CONFIRMED) |
| `t_manhole` | 井室档案：经纬度坐标、盖板、风险等级(LOW/MEDIUM/HIGH)、状态 |
| `t_manhole_location_history` | 井室历史位置（只追加） |
| `t_inspector` | 巡检人员（工号唯一） |
| `t_inspection_route` / `t_route_item` | 巡检路线及路线巡检点（管段/井室） |
| `t_inspection_order` | 巡检工单，关联人员与路线（ASSIGNED/IN_PROGRESS/COMPLETED） |
| `t_field_record` | 现场巡检记录，可标记问题 |
| `t_hidden_danger` | 隐患台账与整改复核记录 |
| `t_sys_config` | 系统配置（权限收紧开关） |

### 主要接口

- 管段：`POST /api/pipes`、`PUT /api/pipes/{id}`、`POST /api/pipes/{id}/confirm`、`GET /api/pipes`
- 井室：`POST /api/manholes`、`PUT /api/manholes/{id}`、`POST /api/manholes/{id}/confirm`、`GET /api/manholes/{id}/location-history`
- 人员：`POST /api/inspectors`
- 路线：`POST /api/routes`、`POST /api/routes/{id}/items?targetType=PIPE|MANHOLE&targetId=`、`GET /api/routes/{id}/items`
- 工单：`POST /api/orders/dispatch`、`POST /api/orders/{id}/start`、`POST /api/orders/{id}/finish`
- 现场记录：`POST /api/field-records`（仅进行中工单、对象须在路线上）
- 隐患：`POST /api/hazards/register`、`POST /api/hazards/from-field-record/{recordId}`、`POST /api/hazards/{id}/rectify`、`POST /api/hazards/{id}/review`
- 权限开关：`GET|PUT /api/system/permission?restricted=true|false`

### 测试

`mvn test`：5 个集成测试覆盖完整闭环、编号唯一、历史位置追加保留、权限收紧冻结规则及隐患状态机约束。

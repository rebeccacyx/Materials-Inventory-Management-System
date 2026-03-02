# Materials-Inventory-Management-System

## 一句话是什么

**物资库存管理系统（Materials Inventory Management System）**：支持物资维护、仓库库存、入出库流水、领用申请与审批、可追溯审计。

## 业务模块（按实现顺序）

### 模块 A：基础数据（最先做）

- Item（物资）
- Warehouse（仓库）
- User（用户/角色，先简化）

✅ 目标：支持新增 / 查询 / 修改 / 删除（CRUD）

### 模块 B：库存（核心）

- Stock（当前库存汇总：`warehouse × item` 一条）
- StockTxn（库存流水：每次 IN/OUT 一条，带时间、原因、操作人）

✅ 目标：

- 入库：更新 Stock + 写 StockTxn（同一事务）
- 出库：检查库存是否足够 → 更新 Stock + 写 StockTxn（同一事务）
- 查询：支持按仓库、按物资查询当前库存

### 模块 C：申请单（业务流程）

- Requisition（领用申请单）
- RequisitionItem（申请明细）

状态流转：

- `DRAFT → SUBMITTED → APPROVED/REJECTED → FULFILLED`

✅ 目标：

- 创建申请单（草稿）
- 提交申请单
- 审批通过/拒绝
- 通过后执行发放（FULFILL）：调用出库逻辑，扣库存并写流水

### 模块 D：权限与审计（工程化）

- JWT 登录（最简可用：用户名密码或 mock 用户）
- Role：`REQUESTER / APPROVER / ADMIN`
- 审计字段：`createdAt / updatedAt / createdBy`

✅ 目标：

- 只有 APPROVER 可以 approve/reject
- 只有 APPROVED 的单子才能 fulfill
- 所有库存变更都可追溯（StockTxn）

## 数据模型总图（开发时始终对齐）

- **Item**：物资定义（只定义一次）
- **Warehouse**：仓库定义（只定义一次）
- **Stock**：当前数量（状态表）
- **StockTxn**：历史记录（事件表）
- **Requisition**：申请单（状态机）
- **RequisitionItem**：申请明细

关键原则：

- “当前状态”放 `Stock`
- “发生过什么”放 `StockTxn`
- “业务流程约束”用 `Requisition` 状态机

## API 设计思路

### A. Items

- `POST /items`
- `GET /items`
- `GET /items/{id}`

### B. Warehouses

- `POST /warehouses`
- `GET /warehouses`

### C. Stock（核心）

- `POST /stock/in` 入库（`warehouseId, itemId, qty, note`）
- `POST /stock/out` 出库（同上）
- `GET /stock?warehouseId=`
- `GET /stock?itemId=`

### D. Requisition（流程）

- `POST /requisitions` 创建草稿
- `POST /requisitions/{id}/submit`
- `POST /requisitions/{id}/approve`
- `POST /requisitions/{id}/reject`
- `POST /requisitions/{id}/fulfill`（扣库存 + 写流水）

## 工程路径（按这个顺序推进）

### Phase 1：能跑 + CRUD

- Spring Boot 启动
- Item CRUD
- Warehouse CRUD

### Phase 2：库存闭环（项目核心价值）

- 设计 `Stock`（复合主键）+ `StockTxn`
- 编写 `StockService`（事务：更新库存 + 写流水）
- 暴露 `/stock/in`、`/stock/out`
- 加并发保护（事务 + 行锁/乐观锁）

### Phase 3：申请审批流程（体现业务理解）

- `Requisition` + 状态机校验
- `fulfill` 时调用 `StockService.out`（真正扣库存）

### Phase 4：工程化加分项（简历亮点）

- DTO + Validation + GlobalExceptionHandler
- Swagger/OpenAPI 文档
- 单元测试（service 层）
- Docker-compose（可选）
- 部署（Render/Fly.io/EC2 三选一）

## 每一步完成后的验收标准

- **Phase 1**：Swagger 中可创建 item/warehouse，H2 可见表与数据
- **Phase 2**：入库/出库后，Stock 变化正确且 StockTxn 有记录
- **Phase 3**：申请单状态不允许乱跳（例如未 approve 不能 fulfill）
- **Phase 4**：错误信息清晰、参数校验完整、接口文档可用

## 详细设计文档

- `docs/PROJECT_DESIGN_CN.md`

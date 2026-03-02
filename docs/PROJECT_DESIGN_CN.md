# 物资库存管理系统设计（执行版）

## 0. 项目一句话

**物资库存管理系统（Materials Inventory Management System）**：支持物资维护、仓库库存、入出库流水、领用申请与审批、可追溯审计。

---

## 1) 业务模块（按实现顺序）

### 模块 A：基础数据（最先做）

- Item（物资）
- Warehouse（仓库）
- User（用户/角色，先简化）

✅ 目标：能新增/查询/修改/删除（CRUD）

---

### 模块 B：库存（核心）

- Stock（当前库存汇总：`warehouse × item` 一条）
- StockTxn（库存流水：每次 IN/OUT 一条，带时间、原因、操作人）

✅ 目标：

- 入库：更新 Stock + 写 StockTxn（同一事务）
- 出库：检查库存够不够 → 更新 Stock + 写 StockTxn（事务）
- 查询：按仓库、按物资查当前库存

---

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

---

### 模块 D：权限与审计（工程化）

- JWT 登录（最简：用户名密码或 mock 用户）
- Role：`REQUESTER / APPROVER / ADMIN`
- 审计字段：`createdAt / updatedAt / createdBy`

✅ 目标：

- 只有 APPROVER 能 approve/reject
- 只有 APPROVED 的单子才能 fulfill
- 所有库存变更可追溯（StockTxn）

---

## 2) 数据模型（脑中长期保持）

- Item：物资定义（只定义一次）
- Warehouse：仓库定义（只定义一次）
- Stock：当前数量（状态表）
- StockTxn：历史记录（事件表）
- Requisition：申请单（状态机）
- RequisitionItem：申请明细

关键原则：

- “当前状态”放 Stock
- “发生过什么”放 StockTxn
- “业务流程”用 Requisition 状态机约束

---

## 3) API 设计思路

### A. Items

- `POST /items`
- `GET /items`
- `GET /items/{id}`

### B. Warehouses

- `POST /warehouses`
- `GET /warehouses`

### C. Stock（核心）

- `POST /stock/in` 入库（warehouseId, itemId, qty, note）
- `POST /stock/out` 出库（同上）
- `GET /stock?warehouseId=`
- `GET /stock?itemId=`

### D. Requisition（流程）

- `POST /requisitions` 创建草稿
- `POST /requisitions/{id}/submit`
- `POST /requisitions/{id}/approve`
- `POST /requisitions/{id}/reject`
- `POST /requisitions/{id}/fulfill`（扣库存 + 流水）

---

## 4) 工程路径（严格按顺序）

### Phase 1：能跑 + CRUD

- Spring Boot 跑起来
- Item CRUD
- Warehouse CRUD

### Phase 2：库存闭环（项目最值钱的部分）

- 设计 Stock（复合主键）+ StockTxn
- 写 StockService（事务：更新库存 + 写流水）
- 暴露 `/stock/in`、`/stock/out`
- 加并发保护（简单版：事务 + 行锁/乐观锁）

### Phase 3：申请审批流程（体现业务理解）

- Requisition + 状态机校验
- fulfill 时调用 StockService.out（真正扣库存）

### Phase 4：工程化加分项（简历亮点）

- DTO + Validation + GlobalExceptionHandler
- Swagger/OpenAPI 文档
- 单元测试（service 层测试）
- Docker-compose（可选）
- 部署（Render/Fly.io/EC2 任选一个）

---

## 5) 每一步完成后的验收标准

- **Phase 1**：Swagger 里能创建 item/warehouse，H2 里能看到表和数据
- **Phase 2**：入库出库后，Stock 正确变化且 StockTxn 有记录
- **Phase 3**：申请单状态不允许乱跳（例如没 approve 不能 fulfill）
- **Phase 4**：错误信息清晰、参数校验完整、接口文档可用

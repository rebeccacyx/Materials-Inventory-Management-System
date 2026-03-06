# Inventory Management System — Backend Design Document

## 1. System Overview（系统概述）

### 1.1 Background（项目背景）

在当前业务场景中，仓库物资的出入库管理缺乏统一的系统管理方式，存在以下问题：

- 库存数据分散，无法统一查询
- 出入库记录缺乏追溯能力
- 无法准确反映当前库存数量
- 业务流程缺乏标准化

因此需要开发一套库存管理系统，用于统一管理仓库物资库存及其出入库流程。

### 1.2 Goals（项目目标）

系统需要实现以下目标：

- 提供统一的库存管理能力
- 支持标准出入库业务流程
- 保证库存数据的一致性和准确性
- 支持库存变动追溯（Audit Trail）
- 为后续业务扩展（调拨、盘点等）提供基础架构

---

## 2. System Scope（系统范围）

### 2.1 In Scope（本期实现）

本期系统将支持以下功能：

- 物料管理（Item）
- 仓库管理（Warehouse）
- 出库单管理（Outbound Order）
- 入库单管理（Inbound Order）
- 库存余额查询（Stock）
- 库存流水查询（Stock Movement）

### 2.2 Out of Scope（暂不实现）

以下功能不在本期范围：

- 多级审批流程
- 货位（Bin / Location）管理
- 批次（Batch）管理
- ERP 系统集成
- 多组织权限隔离

---

## 3. High-Level Architecture（系统架构）

系统采用分层架构：

```text
Controller Layer
        ↓
Service Layer
        ↓
Repository Layer
        ↓
Database
```

### Controller Layer

处理 HTTP 请求，提供 REST API。

### Service Layer

实现核心业务逻辑，例如：

- 创建单据
- 过账
- 库存更新

### Repository Layer

负责数据库访问（JPA）。

### Database Layer

存储业务数据和库存数据。

---

## 4. Core Domain Model（核心领域模型）

库存系统核心由三类数据组成：

### 1️⃣ Business Documents（业务单据）

用户操作的业务对象，例如：

- InboundOrder
- OutboundOrder

用于描述业务行为。

### 2️⃣ Stock Balance（库存余额）

当前库存状态：

`Stock = warehouse + item → quantity`

用于快速查询库存数量。

### 3️⃣ Stock Movement（库存流水）

记录每一次库存变化：

- IN / OUT / ADJUST

用于：

- 审计
- 追溯
- 对账

---

## 5. Data Model Design（数据模型）

### 5.1 Item

| Field | Type | Description |
| --- | --- | --- |
| id | Long | 物料ID |
| sku | String | 物料编码 |
| name | String | 物料名称 |
| unit | String | 计量单位 |

### 5.2 Warehouse

| Field | Type | Description |
| --- | --- | --- |
| id | Long | 仓库ID |
| name | String | 仓库名称 |
| location | String | 仓库位置 |

### 5.3 Stock

| Field | Type |
| --- | --- |
| id | Long |
| warehouse_id | Long |
| item_id | Long |
| quantity | Long |
| updated_at | Timestamp |

约束：

- `UNIQUE(warehouse_id, item_id)`

表示同一个仓库同一个物料只有一条库存记录。

### 5.4 StockMovement

| Field | Type |
| --- | --- |
| id | Long |
| warehouse_id | Long |
| item_id | Long |
| type | ENUM |
| delta | Long |
| reason | String |
| created_at | Timestamp |

Movement Type：

- IN
- OUT
- ADJUST

### 5.5 OutboundOrder

| Field | Type |
| --- | --- |
| id | Long |
| warehouse_id | Long |
| status | ENUM |
| created_at | Timestamp |
| posted_at | Timestamp |

Status：

- DRAFT
- POSTED

### 5.6 OutboundOrderLine

| Field | Type |
| --- | --- |
| id | Long |
| outbound_order_id | Long |
| item_id | Long |
| quantity | Long |

---

## 6. Business Workflow（业务流程）

### 6.1 Outbound Order Flow（出库流程）

```text
Create Order
      ↓
Order Status = DRAFT
      ↓
POST /outbound-orders/{id}/post
      ↓
System checks stock
      ↓
Update stock
      ↓
Create stock movement
      ↓
Order Status = POSTED
```

### 6.2 Stock Update Rules（库存更新规则）

库存更新遵循以下规则：

- 只有 `POSTED` 单据才能更新库存
- 每一次库存变化必须记录 `stock_movement`
- 同一单据不能重复过账

---

## 7. API Design（接口设计）

### 7.1 Item

- `POST /items`
- `GET /items/{id}`

### 7.2 Warehouse

- `POST /warehouses`
- `GET /warehouses/{id}`

### 7.3 Outbound Order

- `POST /outbound-orders`
- `GET /outbound-orders/{id}`
- `POST /outbound-orders/{id}/post`

### 7.4 Stock Query

- `GET /stocks`
- `GET /stocks/{warehouseId}/{itemId}`

### 7.5 Movement Query

- `GET /stock-movements`

---

## 8. Transaction Design（事务设计）

出库过账操作必须在一个事务中完成：

1. begin transaction
2. check order status
3. check stock quantity
4. update stock
5. insert stock movement
6. update order status
7. commit

如果任一步失败：

- rollback

---

## 9. Concurrency Control（并发控制）

为避免库存超卖，系统采用以下策略：

- 事务保证数据一致性
- 更新库存时使用数据库锁或乐观锁
- 防止并发请求导致库存扣减错误

---

## 10. Error Handling（异常处理）

系统将返回统一错误格式：

- timestamp
- status
- error
- message
- path

常见错误包括：

- Warehouse not found
- Item not found
- Stock not sufficient
- Order already posted

---

## 11. Future Extensions（未来扩展）

系统未来可以扩展以下能力：

- 调拨单（Transfer Order）
- 盘点单（Stocktake）
- 批次管理（Batch）
- 货位管理（Bin Location）
- 审批流程（Approval Workflow）
- ERP 集成

---

## 最重要的一点

如果你以后真的在公司做系统，你脑子里一定要有这一句话：

> 业务单据驱动库存变化，库存流水记录变化原因，库存余额提供实时状态。

也就是：

`Order → Movement → Stock`

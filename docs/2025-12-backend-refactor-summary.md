# 后端阶段性改造汇总（DTO 校验 / 异常统一 / 事务 / 常量化）

本文档用于汇总本次在 `skyBackend` 已完成的后端代码改造内容、改造动机与落地效果，便于后续继续开发用户端/管理端时保持一致的规范与心智模型。

---

## 1. 改造目标

1) **校验统一**：请求入参以 DTO 为边界，通过 `javax.validation` 统一校验，Controller 不再夹杂大量手写 if 校验与 `Result.error("...")` 的分支。

2) **异常统一**：保证绝大多数异常都能被全局异常处理器捕获并返回统一的 `Result` 结构，减少前端“有时拿到 JSON、有时是默认错误页/堆栈”的不确定性。

3) **事务补齐**：对“主表 + 关联表”的写操作补齐事务，避免出现半成功数据。

4) **审计字段统一**：用 `@AutoFill + AutoFillAspect` 统一填充 `createTime/updateTime/createUser/updateUser`，减少重复代码和漏填风险。

5) **错误提示常量化**：将散落在 Service/Controller/Handler/DTO 里的硬编码字符串收敛到常量，降低维护成本。

6) **类型安全**：减少 raw type，提升编译期约束与可读性。

---

## 2. 已完成的改造点

### 2.1 参数校验统一（DTO + @Valid/@Validated）

**动机**：避免 Controller 中大量手写校验与返回分支；让校验规则与入参模型绑定；让参数错误走统一异常处理器输出。

**落地**：
- 为多个 DTO 补齐/强化 `javax.validation` 注解（`@NotNull/@NotBlank/@DecimalMin/...`）。
- 管理端相关 Controller 增加 `@Validated`，并对 body 入参统一使用 `@Valid`。
- 对 `status`/`id` 这类 query/path 参数，使用 `@Min/@Max/@NotNull` 统一校验，并用常量描述 message。

**代表文件**：
- `sky-pojo/src/main/java/com/sky/dto/dish/DishCreateDTO.java`
- `sky-pojo/src/main/java/com/sky/dto/dish/DishUpdateDTO.java`
- `sky-pojo/src/main/java/com/sky/dto/setmeal/SetmealCreateDTO.java`
- `sky-pojo/src/main/java/com/sky/dto/setmeal/SetmealUpdateDTO.java`
- `sky-pojo/src/main/java/com/sky/dto/employee/EmployeeLoginDTO.java`
- `sky-pojo/src/main/java/com/sky/dto/PasswordEditDTO.java`
- `sky-pojo/src/main/java/com/sky/dto/employee/EmployeeUpdateDTO.java`
- `sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java`
- `sky-server/src/main/java/com/sky/controller/admin/DishController.java`
- `sky-server/src/main/java/com/sky/controller/admin/SetmealController.java`

---

### 2.2 异常体系统一（全局异常兜底）

**动机**：避免 `IllegalArgumentException/RuntimeException` 导致响应不一致；让“参数错误/系统错误”都有稳定输出。

**落地**：
- `GlobalExceptionHandler` 增加对 `IllegalArgumentException` 的处理。
- 增加 `Exception` 兜底处理（返回 `MessageConstant.UNKNOWN_ERROR`）。
- 将校验失败默认 message 从硬编码 `"参数校验失败"` 收敛为常量 `MessageConstant.PARAM_VALID_FAILED`。

**代表文件**：
- `sky-server/src/main/java/com/sky/handler/GlobalExceptionHandler.java`
- `sky-common/src/main/java/com/sky/constant/MessageConstant.java`

---

### 2.3 事务边界补齐（套餐模块）

**动机**：`Setmeal` 属于典型的“主表 + 关联表”，写入/修改如果没有事务，极易出现主表成功但关联表失败（或反之）。

**落地**：
- `SetmealServiceImpl.save(...)` 增加 `@Transactional`
- `SetmealServiceImpl.changeSetmeal(...)` 增加 `@Transactional`

**代表文件**：
- `sky-server/src/main/java/com/sky/service/impl/SetmealServiceImpl.java`

---

### 2.4 AutoFill 使用统一（分类模块示例）

**动机**：减少 Service 中重复填充审计字段的样板代码，并统一入口，避免漏填/不一致。

**落地**：
- `CategoryMapper.insert/update` 增加 `@AutoFill(OperationType.INSERT/UPDATE)`。
- `CategoryServiceImpl` 移除手动设置 `createTime/updateTime/createUser/updateUser` 的逻辑，交由 `AutoFillAspect` 统一处理。

**代表文件**：
- `sky-server/src/main/java/com/sky/mapper/CategoryMapper.java`
- `sky-server/src/main/java/com/sky/service/impl/CategoryServiceImpl.java`
- `sky-server/src/main/java/com/sky/aspect/AutoFillAspect.java`（已存在，作为机制基础）

---

### 2.5 错误提示常量化（MessageConstant + ValidationMessageConstant）

**动机**：减少硬编码字符串；改动文案时不需要全局搜索替换；降低各模块之间的耦合与重复。

**落地策略**：
- **业务/服务端错误提示**：集中在 `sky-common` 的 `MessageConstant`（供 `sky-server` 使用）。
- **DTO 校验 message**：由于 `sky-pojo` 不依赖 `sky-common`，因此新增 `ValidationMessageConstant` 放在 `sky-pojo`，专用于 `javax.validation` 注解 message。

**落地内容**：
- 新增/补充了多条常量：参数校验失败、id 必填、status 取值范围、分类/菜品/套餐不存在等。
- 将 Service/Controller/Handler 里部分硬编码字符串改为引用常量。
- 将 DTO 中校验注解的 message 全部改为引用 `ValidationMessageConstant`。

**代表文件**：
- `sky-common/src/main/java/com/sky/constant/MessageConstant.java`
- `sky-pojo/src/main/java/com/sky/constant/ValidationMessageConstant.java`

---

### 2.6 类型安全与一致性改造

**动机**：减少 raw type，提升 IDE 提示与编译期约束，避免隐藏的类型转换问题。

**落地**：
- `Result.error` 改为泛型安全返回（避免 raw `Result`）。
- `PageResult.records` 改为 `List<?>`（保留兼容性，同时避免 raw `List`）。
- `RedisTemplate` 在 ShopController 中增加泛型声明 `RedisTemplate<String, Object>`。

**代表文件**：
- `sky-common/src/main/java/com/sky/result/Result.java`
- `sky-common/src/main/java/com/sky/result/PageResult.java`
- `sky-server/src/main/java/com/sky/controller/admin/ShopController.java`
- `sky-server/src/main/java/com/sky/controller/user/ShopController.java`

---

## 2.7 变更清单（对齐到代码）

本节按文件列出本次改造的“具体改了什么”，便于你后续追溯与保持一致的编码风格。

### 2.7.1 `sky-common`（服务端共用）

- `sky-common/src/main/java/com/sky/result/Result.java`
  - `Result.error(String msg)` 改为返回 `Result<T>`（消除 raw type）。
- `sky-common/src/main/java/com/sky/result/PageResult.java`
  - `records` 从 raw `List` 改为 `List<?>`。
- `sky-common/src/main/java/com/sky/constant/MessageConstant.java`
  - 新增常量（本次新增/补充部分）：
    - 通用校验：`PARAM_VALID_FAILED`、`ID_REQUIRED`、`STATUS_MUST_BE_0_OR_1`
    - 业务不存在/状态不允许：`CATEGORY_NOT_FOUND`、`DISH_NOT_FOUND_OR_UPDATE_FAILED`、`SETMEAL_NOT_FOUND`、`SETMEAL_NOT_FOUND_OR_UPDATE_FAILED`
    - 密码相关：`OLD_NEW_PASSWORD_REQUIRED`、`NEW_PASSWORD_SAME_AS_OLD`

### 2.7.2 `sky-pojo`（DTO 校验文案常量）

> 说明：`sky-pojo` 不依赖 `sky-common`，因此 DTO 校验 message 不引用 `MessageConstant`，统一使用 `ValidationMessageConstant`。

- `sky-pojo/src/main/java/com/sky/constant/ValidationMessageConstant.java`
  - 新增文件：DTO/VO 校验 message 常量集中存放。
- DTO 校验 message 常量化：
  - `sky-pojo/src/main/java/com/sky/dto/category/CategoryCreateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/category/CategoryUpdateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/dish/DishCreateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/dish/DishUpdateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/setmeal/SetmealCreateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/setmeal/SetmealUpdateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/employee/EmployeeCreateDTO.java`（`@Pattern` message 常量化）
  - `sky-pojo/src/main/java/com/sky/dto/employee/EmployeeLoginDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/employee/EmployeeUpdateDTO.java`
  - `sky-pojo/src/main/java/com/sky/dto/PasswordEditDTO.java`

### 2.7.3 `sky-server`（Controller/Service/Mapper/Handler）

- Controller：去掉手写 if 校验与 `Result.error("...")` 分支，改为统一走校验框架
  - `sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java`
    - 类上增加 `@Validated`
    - `login(...)`、`changePassword(...)`、`changeEmployee(...)` 增加 `@Valid`
    - `updateStatus(...)` 的 `status/id` 参数改为注解校验，并使用 `MessageConstant.ID_REQUIRED/STATUS_MUST_BE_0_OR_1`
  - `sky-server/src/main/java/com/sky/controller/admin/DishController.java`
    - 类上增加 `@Validated`
    - `saveDish(...)`、`changeDish(...)` 增加 `@Valid`
    - `updateDishSaleStatus(...)` 参数校验 message 改为 `MessageConstant.ID_REQUIRED/STATUS_MUST_BE_0_OR_1`
  - `sky-server/src/main/java/com/sky/controller/admin/SetmealController.java`
    - 类上增加 `@Validated`
    - `saveSetmeal(...)`、`changeSetmeal(...)` 增加 `@Valid`
    - `updateSetmealSaleStatus(...)` 参数校验 message 改为 `MessageConstant.ID_REQUIRED/STATUS_MUST_BE_0_OR_1`
- Handler：统一兜底输出为 `Result`
  - `sky-server/src/main/java/com/sky/handler/GlobalExceptionHandler.java`
    - 增加 `@ExceptionHandler(IllegalArgumentException.class)`
    - 增加 `@ExceptionHandler(Exception.class)` 兜底（`MessageConstant.UNKNOWN_ERROR`）
    - 校验失败默认文案统一为 `MessageConstant.PARAM_VALID_FAILED`
- Service：异常 message 常量化 + 套餐事务补齐
  - `sky-server/src/main/java/com/sky/service/impl/CategoryServiceImpl.java`
    - 分类不存在：`IllegalArgumentException(MessageConstant.CATEGORY_NOT_FOUND)`
    - 移除手动填充审计字段（依赖 AutoFill）
  - `sky-server/src/main/java/com/sky/service/impl/DishServiceImpl.java`
    - 更新失败/不存在：`IllegalArgumentException(MessageConstant.DISH_NOT_FOUND_OR_UPDATE_FAILED + ", id=" + dishId)`
  - `sky-server/src/main/java/com/sky/service/impl/SetmealServiceImpl.java`
    - `save(...)`、`changeSetmeal(...)` 增加 `@Transactional`
    - 更新失败/不存在：`MessageConstant.SETMEAL_NOT_FOUND_OR_UPDATE_FAILED`
    - 详情不存在：`MessageConstant.SETMEAL_NOT_FOUND`
  - `sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java`
    - 密码校验相关 message 改为 `MessageConstant.OLD_NEW_PASSWORD_REQUIRED` / `MessageConstant.NEW_PASSWORD_SAME_AS_OLD`
- Mapper：分类写操作统一启用 AutoFill
  - `sky-server/src/main/java/com/sky/mapper/CategoryMapper.java`
    - `insert(Category)` 增加 `@AutoFill(OperationType.INSERT)`
    - `update(Category)` 增加 `@AutoFill(OperationType.UPDATE)`
- RedisTemplate 泛型：减少 raw type
  - `sky-server/src/main/java/com/sky/controller/admin/ShopController.java`
- `sky-server/src/main/java/com/sky/controller/user/ShopController.java`

---

## 2.8 示例代码改造（前后对比）

本节给出几处“从什么改成什么”的典型示例（节选），用于快速对齐本次改造的风格与目标。

### 示例 1：`Result.error` 去掉 raw type（类型安全）

文件：`sky-common/src/main/java/com/sky/result/Result.java`

**改造前（raw Result）**
```java
public static <T> Result<T> error(String msg) {
    Result result = new Result();
    result.msg = msg;
    result.code = 0;
    return result;
}
```

**改造后（泛型安全）**
```java
public static <T> Result<T> error(String msg) {
    Result<T> result = new Result<>();
    result.msg = msg;
    result.code = 0;
    return result;
}
```

### 示例 2：全局异常兜底与校验失败常量化（异常统一）

文件：`sky-server/src/main/java/com/sky/handler/GlobalExceptionHandler.java`

**改造前（校验失败文案硬编码，缺少 IllegalArgumentException/Exception 兜底）**
```java
.orElse("参数校验失败");
```

**改造后（引用常量 + 增加兜底处理）**
```java
.orElse(MessageConstant.PARAM_VALID_FAILED);

@ExceptionHandler(IllegalArgumentException.class)
public Result<?> handleIllegalArg(IllegalArgumentException ex) {
    return Result.error(ex.getMessage());
}

@ExceptionHandler(Exception.class)
public Result<?> handleAny(Exception ex) {
    return Result.error(MessageConstant.UNKNOWN_ERROR);
}
```

### 示例 3：套餐写操作补齐事务（避免半成功）

文件：`sky-server/src/main/java/com/sky/service/impl/SetmealServiceImpl.java`

**改造前（无事务）**
```java
@Override
public void save(SetmealCreateDTO dto) { ... }

@Override
public void changeSetmeal(SetmealUpdateDTO dto) { ... }
```

**改造后（补齐事务边界）**
```java
@Override
@Transactional
public void save(SetmealCreateDTO dto) { ... }

@Override
@Transactional
public void changeSetmeal(SetmealUpdateDTO dto) { ... }
```

### 示例 4：业务错误提示从硬编码改为常量（降低维护成本）

文件：`sky-server/src/main/java/com/sky/service/impl/CategoryServiceImpl.java`

**改造前（硬编码 message）**
```java
if (category == null) {
    throw new IllegalArgumentException("分类不存在");
}
```

**改造后（引用 `MessageConstant`）**
```java
if (category == null) {
    throw new IllegalArgumentException(MessageConstant.CATEGORY_NOT_FOUND);
}
```

### 示例 5：DTO 校验 message 常量化（pojo 模块解耦）

说明：`sky-pojo` 不依赖 `sky-common`，所以 DTO 的校验 message 不引用 `MessageConstant`，而是统一引用 `ValidationMessageConstant`。

文件：`sky-pojo/src/main/java/com/sky/dto/dish/DishCreateDTO.java`

**改造前（校验 message 硬编码）**
```java
@NotBlank(message = "菜品名称不能为空")
private String name;
```

**改造后（校验 message 常量化）**
```java
@NotBlank(message = ValidationMessageConstant.DISH_NAME_REQUIRED)
private String name;
```

## 3. 结果验证与已知问题

### 3.1 构建验证

- 已验证：`mvn -DskipTests package` 可通过编译打包。

### 3.2 已知问题（测试环境依赖）

- 当前 `sky-server` 的测试在无 Redis 环境下会失败（尝试连接 `127.0.0.1:6379`），属于环境依赖问题而非本次改造引入。
- 后续建议：将依赖外部环境的测试通过 profile/禁用/容器化等方式隔离，保证默认 `mvn test` 可稳定运行。

---

## 4. 后续建议（如果继续迭代）

1) **业务错误码体系**：目前 `Result.code` 只有成功/失败（1/0）。若前端需要精细化处理（比如区分未登录/无权限/参数错误/业务冲突），建议引入稳定的业务 errorCode（枚举或常量）。

2) **异常类型语义化**：当前使用 `IllegalArgumentException` 表达部分业务错误，建议逐步收敛为继承 `BaseException` 的业务异常（“不存在”“状态不允许”等），便于统一治理与统计。

3) **校验全覆盖**：对订单、购物车、用户端登录等 DTO 做同样的校验与 message 常量化，避免两套风格并存。

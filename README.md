# sky takeout

## day1:

考虑远程服务器配 docker mysql8 作为数据库;
主机暂时当前后端调试

设置服务器,要保证服务器安全,最好:

关闭 3306 端口

22 端口只开放固定ip : 这个由于没有固定公网ip做不到

密匙登录,禁止root登录,禁止使用密码登录

怕炸机暂时不关root了

### 第一步:安全服务器配置

1. 生成公钥

```cmd
ssh-keygen -t ed25519
```

设置路径和passphrase

生成公钥和私钥

私钥自己保存,公钥给服务器

查看公钥:

```cmd
type $env:USERPROFILE\.ssh\id_ed25519.pub
```

1. 创建用户yuan并给权限

```
adduser yuan
```

密码/full name

1. 验证

```cmd
id yuan
getent passwd yuan
ls -ld /home/yuan
```

1. 给 yuan sudo 权限

```
usermod -aG sudo yuan
groups yuan
```

1. 把公钥放到服务器

```bash
# 先切到 yuan
sudo -u yuan mkdir -p /home/yuan/.ssh
sudo -u yuan chmod 700 /home/yuan/.ssh

# 把公钥追加进去
echo 'ssh-ed25519 AAAA... x@DESKTOP-...' | sudo -u yuan tee -a /home/yuan/.ssh/authorized_keys > /dev/null
// 这里可以自己编辑更方便

sudo -u yuan chmod 600 /home/yuan/.ssh/authorized_keys
sudo -u yuan ls -l /home/yuan/.ssh
```

验证

```bash
sudo -u yuan wc -l /home/yuan/.ssh/authorized_keys
sudo -u yuan head -n 1 /home/yuan/.ssh/authorized_keys
```

1. 修改 SSH 配置：禁用密码登录

```bash
sudo nano /etc/ssh/sshd_config.d/99-hardening.conf
//nano 操作写入 
PermitRootLogin yes
PubkeyAuthentication yes
PasswordAuthentication no
```

改完先检查语法：

```bash
sudo sshd -t
```

无输出 = 通过。
再看“最终生效值”（你贴过类似结果）：

```bash
sudo sshd -T | egrep 'permitrootlogin|pubkeyauthentication|passwordauthentication'
```

最终看到类似：

```
permitrootlogin yes
pubkeyauthentication yes
passwordauthentication no
```

重载服务生效

```
sudo systemctl reload ssh
```

### 第二步:数据库创建连接idea

#### A. 安装 Docker

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker yuan
# 这里建议重新登录一下（或 newgrp docker）
```

#### B. 配镜像加速（如果你所在网络经常超时）

```bash
sudo mkdir -p /etc/docker
sudo nano /etc/docker/daemon.json
```

填：

```bash
{
  "registry-mirrors": ["https://YOUR_MIRROR_HERE"]
}
```

重启：

```bash
sudo systemctl restart docker
```

验证：

```bash
docker info | sed -n '/Registry Mirrors/,+5p'
```

#### C. 准备 compose 目录和文件（注意这里是 bash，不是 java）

```bash
mkdir -p ~/mysql/{data,init}
cd ~/mysql
# 写 docker-compose.yml
```

#### D. 启动

```bash
docker compose up -d
docker ps
docker logs -n 50 mysql8
```

进入 MySQL：

```bash
docker exec -it mysql8 mysql -uroot -p
```

#### E.idea

主机 localhost 端口 3306

用户/ 密码

ssh:

隧道配置

输入主机/密钥

测试连接成功

**idea的数据库只是方便查表,不能作为后端跟数据库的连接隧道**

### 第三步:从导入 SQL 到前后端联通

本机执行

```bash
CREATE DATABASE IF NOT EXISTS `sky_take_out`;


-- [42000][1044] Access denied for user 'app'@'%' to database 'sky_take_out'
```

原因:业务账号 `app`（权限受限），没有 `CREATE DATABASE` 权限

1）先用 root 进入容器内 MySQL：

```bash
docker ps
docker exec -it mysql8 mysql -uroot -p
```

2）用 root 创建库：

```sql
CREATE DATABASE IF NOT EXISTS sky_take_out
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

3）给 app 授权:只授权这个库

```sql
GRANT ALL PRIVILEGES ON sky_take_out.* TO 'app'@'%';
FLUSH PRIVILEGES;
exit;
```

IDEA 直接 Run SQL Script

**小问题:(GPT至高神)**

找不到主类 : IDEA安装目录下的maven不能接受有空格(可能)

mvn -v 弹出 java usage: G盘下隐藏.mvn配置文件,删除

编译失败：Lombok/Getter/日志字段缺失 + JDK 版本问题 ,改为JDK17

**前端登录无响应:**

“登录中…”卡住

**后端关键错误日志：**

```
DruidDataSource : create connection SQLException, url: jdbc:mysql://localhost:3306/sky_take_out...
Communications link failure
```

**问题定位：**

A.

- application-dev.yml 没有设置自己数据库的账号密码

B.

- IDEA 的 Data Source 连接成功是因为 **IDEA 自己开了 SSH 隧道**
- Spring Boot 不会复用 IDEA 隧道
- 你后端写 `localhost:3306` 指向的是你 Windows 本机，不是云服务器的 MySQL

**设置系统级 SSH 本地端口转发**

ssh -N -L 127.0.0.1:3307:127.0.0.1:3306 yuan@<SERVER_IP>

- `-N`：不执行远程命令，只做转发
- `127.0.0.1:3307`：本机监听端口
- `127.0.0.1:3306`：远端服务器上的 MySQL 暴露在服务器本机（你的容器端口映射是 `127.0.0.1:3306->3306`，正好匹配）

窗口“卡住”正常，隧道要一直开着。

修改application-dev.yml

- host：`127.0.0.1`
- port：`3307`
- db：`sky_take_out`
- username
  password

修改完即可登录,联通前后端

**防止密码泄露:**

#### `application-dev.yml`（GitHub）

```
spring:
  config:
    import: "optional:classpath:application-dev-local.yml"

sky:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: 127.0.0.1
    port: 3307
    database: sky_take_out
    username: ${DB_USERNAME:}
    password: ${DB_PASSWORD:}
```

说明：

- `spring.config.import` 在顶层。
- `${DB_USERNAME:}` 这个 `:` 后面空着，表示没设置也能启动到加载 local 文件为止（更容错）。

#### 2）`application-dev-local.yml`（**不提交**，本地私密）

```java
sky:
  datasource:
    username: xxx
    password: xxx
```

#### 3）`.gitignore` 加一条（必须）

```java
**/application-*-local.yml
```

------

## 项目内容

为什么前端请求

http://localhost/api/employee/login

后端能收到

通过nginx反向代理,将前端发送的动态请求由nginx转发到后端服务器

![image-20251218133515901](./D:/Typora/assets/image-20251218133515901.png)

1. nginx 缓存,提高访问速度
2. 堵在均衡,所谓负载均衡,就是把大量的请求按照我们指定的方式均衡的分配给集群中的每台服务器

1. 保证后端服务安全

如何设置:

nginx.conf 文件

反向代理:

```
    # 反向代理,处理管理端发送的请求
    location /api/ {
		proxy_pass   http://localhost:8080/admin/;
    }
```

匹配到 /api/xxx 就转发到 http://localhost:8080/admin/xxx

负载均衡:

匹配到之后自动 设置服务器负载

**完善登录功能**

1.数据库密码加密

改为bcrypt加密

A.

SERER模块

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

application-dev.yml添加

```xml
security:
  password:
    bcrypt-cost: 12
```

sky-config下面:

新建类:

PasswordConfiguration.java

```java
package com.sky.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder(SecurityPasswordProperties props) {
        return new BCryptPasswordEncoder(props.getBcryptCost());
    }
}
```

加一个属性类

```java
package com.sky.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.password")
public class SecurityPasswordProperties {
    private int bcryptCost = 12;
    public int getBcryptCost() { return bcryptCost; }
    public void setBcryptCost(int bcryptCost) { this.bcryptCost = bcryptCost; }
}
```

修改login函数

```java
import org.springframework.security.crypto.password.PasswordEncoder; // 哈希校验
@Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对

        // password：前端传来的明文
        // employee.getPassword()：数据库里存的 bcrypt 哈希串（例如 $2b$12$...）
        if (!passwordEncoder.matches(password, employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
```

创建临时类 修改数据库密码测试

```java
package com.sky;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class temp {
    public static void main(String[] args) {
        PasswordEncoder pe = new BCryptPasswordEncoder(12);
        System.out.println(pe.encode("123456"));
    }
}
$2a$12$9/Wa/2.RM6YIZWr8bM9xjudaoarqQ7c.Ygd2W757t7V.jzjBRWj9O
```

删除这个类

测试成功

![image-20251218194922256](./D:/Typora/assets/image-20251218194922256.png)

------

### 新增员工

操作数据库员工表 employee

**前端“新增员工”接口提交的字段，只是 Employee 实体的一部分**，而且字段集合/含义跟数据库实体并不完全一致，所以建议**单独设计一个 DTO 来接收请求**，不要直接用实体类 `Employee` 去接。

#### 实体类（Entity）

- 对应数据库表结构（字段通常和表一一对应）
- 用于持久化（Mapper/ORM 插入、查询、更新）
- 字段包含：业务字段 + 审计字段（创建时间/更新人/状态等）+ 可能还有内部字段

#### DTO（Data Transfer Object）

- 对应一次“接口交互”的数据结构（请求/响应）
- 只包含接口需要的字段，**不多不少**
- 可以做校验（必填、格式、长度）更清晰
- **不直接对应数据库表**，更接近“前端要什么/后端收什么”

一句话：
**Entity 面向数据库，DTO 面向接口。**

json数据接收加注解@RequestBody

post 方法请求需要加@Post注解

------

mapper里注解数据库没法自动补全:

#### 把 DataSource 指到“转发端口”的远程库

在 **数据源和驱动程序** 这个窗口里：

1. 左侧选中你这个 `appdb@localhost`，**复制一份**（右键 Duplicate 或点上方复制图标，避免影响原来的本机库）。
2. 新的 DataSource 改：
    - 主机：`127.0.0.1`（或 `localhost` 都行）
    - 端口：`3307`（你 SSH 转发的本地端口）
    - 数据库：选 `sky_take_out`（如果下拉没有，先测试连接再刷新）
    - 用户/密码：远程 MySQL 的账号（你 app 用户那个）
3. 点左下角 **测试连接**，能通过就 OK。

> 结论：**IDEA 连接哪个库，取决于这个 DataSource 的 host/port**。你转发到 3307，那就填 3307。

再按照下面修改:

[IDEA sql自动补全/sql自动提示/sql列名提示_idea提示sql语句-CSDN博客](https://blog.csdn.net/qq2523208472/article/details/89366264)

------

**新增员工接口实现流程:**

`EmployeeController.java` 实现对前端请求接收,打印日志,调用业务层,返回结果

请求接收:@PostMapping

> 前端发 HTTP 请求（POST），Body 里是 JSON
>
> Controller 方法参数写 `@RequestBody EmployeeDTO employeeDTO`
>
> Spring MVC 找到合适的 **HttpMessageConverter**
>
> 通常用 **Jackson**（`MappingJackson2HttpMessageConverter`）把 JSON → EmployeeDTO
>
> 字段映射规则：JSON 的 key 对应 DTO 的属性名（通过 setter/getter 或字段）
>
> 生成对象EmployeeDTO employeeDTO后传给你的 controller 方法

打印日志:log.info("新增员工:{}",employeeDTO);

调用业务层:employeeService.save(employeeDTO);

返回结果:Result

employeeService实现由EmployeeService.java接口新增方法

```java
    /**
     * 新增员工接口方法
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);
```

其实现类EmployeeServiceImpl新增方法

```java
    /**
     * 新增员工
     * 逻辑改为初始默认密码加强制首次改密码
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        // 对象属性拷贝
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        // 补全剩余属性
        // 状态
        employee.setStatus(StatusConstant.ENABLE);
        // 设置密码
        PasswordEncoder pe = new BCryptPasswordEncoder(12);
        employee.setPassword(pe.encode(PasswordConstant.DEFAULT_PASSWORD));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // TODO 改为当前登录用户id
        employee.setCreateUser(10L);
        employee.setUpdateUser(10L);

        employeeMapper.insert(employee);
    }

}
```

复制DTO字段并补充字段,交给employeeMapper持久化

EmployeeMapper接口新增方法

```java
    @Insert("insert into employee " +
            "(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);
```

通过映射,MyBatis 在运行时给接口生成了代理对象（动态代理）代理对象拦截方法调用后去执行你注解里的 SQL

------

考虑到id字段不应该放在DTO里,

将DTO拆分为EmployeeCreateDTO和EmployeeUpdateDTO

创建类EmployeeCreateDTO(不含id)

```java
@Data
public class EmployeeCreateDTO implements Serializable {
    private String username;
    private String name;
    private String phone;
    private String sex;
    private String idNumber;
}
```

修改：EmployeeUpdateDTO（含 id）

```java
@Data
public class EmployeeUpdateDTO implements Serializable {
    private Long id;
    private String name;
    private String phone;
    private String sex;
    private String idNumber;
    // username 是否允许改，看业务决定
}
```

将方法里的 EmployeeDTO 都改为 EmployeeCreateDTO

------

增加逻辑 :新用户第一次登陆强制改密码

- **数据库新增 `pwd_changed` 字段**（为什么：必须把“是否已完成首次改密”持久化下来，否则后端没法跨请求强制约束）

  ```sql
  ALTER TABLE employee
  ADD COLUMN pwd_changed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已修改初始密码(0否1是)'
  AFTER password;
  ```

- **`Employee` 实体补齐 `pwdChanged` 字段**（为什么：拦截器/Service 要读写该状态；不补实体就会出现“查得到列但 Java 拿不到/写不进去”）

  ```java
  // sky-pojo: com.sky.entity.Employee
  private Integer pwdChanged;
  ```

- **Mapper 增加按 id 查询改密状态的方法**（为什么：拦截器每次请求只需要这一列，最小查询成本、最清晰）

  ```java
  // sky-server: com.sky.mapper.EmployeeMapper
  @Select("select pwd_changed from employee where id = #{id}")
  Integer getPwdChangedById(Long id);
  ```

- **Mapper 增加“改密成功后更新密码 + 置位 pwd_changed=1”的更新方法**（为什么：两件事必须一起落库，避免只改密码不置位导致永远 403）

  ```java
  // sky-server: com.sky.mapper.EmployeeMapper
  @Update("update employee " +
          "set password = #{password}, pwd_changed = 1, update_time = #{updateTime}, update_user = #{updateUser} " +
          "where id = #{id}")
  void updatePasswordAndMarkChanged(@Param("id") Long id,
                                    @Param("password") String password,
                                    @Param("updateTime") LocalDateTime updateTime,
                                    @Param("updateUser") Long updateUser);
  ```

- **拦截器强制改密：未改密返回 403 + 统一 JSON**（为什么：强制规则必须后端兜底；返回 JSON 让前端/Knife4j 明确知道是“必须改密”而不是泛泛的权限不足）

  ```java
  // sky-server: com.sky.interceptor.JwtTokenAdminInterceptor
  @Autowired private JwtProperties jwtProperties;
  @Autowired private EmployeeMapper employeeMapper;
  @Autowired private ObjectMapper objectMapper; // 复用，别每次 new
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      if (!(handler instanceof HandlerMethod)) return true;
  
      String token = request.getHeader(jwtProperties.getAdminTokenName());
      if (token == null || token.isBlank()) {
          response.setStatus(401);
          return false;
      }
  
      try {
          Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
          Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
          BaseContext.setCurrentId(empId);
  
          String uri = request.getRequestURI();
          if ("/admin/employee/login".equals(uri)
                  || "/admin/employee/logout".equals(uri)
                  || "/admin/employee/password".equals(uri)) {
              return true; // 为什么：否则改密接口也会被拦，形成死锁
          }
  
          Integer pwdChanged = employeeMapper.getPwdChangedById(empId);
          if (pwdChanged == null || pwdChanged == 0) {
              response.setStatus(403);
              response.setContentType("application/json;charset=UTF-8");
              Result<Object> r = Result.error(MessageConstant.PASSWORD_NEED_CHANGE);
              response.getWriter().write(objectMapper.writeValueAsString(r));
              return false;
          }
          return true;
      } catch (Exception e) {
          response.setStatus(401);
          return false;
      }
  }
  
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
      BaseContext.removeCurrentId(); // 为什么：线程池复用，不清理会串号/越权
  }
  ```

- **改密接口：校验旧密码 + 写新 bcrypt + 置位 `pwd_changed=1`**（为什么：只允许本人改自己的密码；必须校验旧密码；必须用 bcrypt 的 matches/encode；成功后解除后续接口限制）

  ```java
  // sky-server: com.sky.controller.admin.EmployeeController
  @PostMapping("/editPassword")
  @ApiOperation("修改密码")
  public Result<String> changePassword(@RequestBody PasswordEditDTO dto) {
      employeeService.changePassword(dto);
      return Result.success();
  }
  ```

  ```java
  // sky-server: com.sky.service.EmployeeService
  void changePassword(PasswordEditDTO dto);
  ```

  ```java
  // sky-server: com.sky.service.impl.EmployeeServiceImpl
  @Autowired private EmployeeMapper employeeMapper;
  @Autowired private PasswordEncoder passwordEncoder;
  
  @Override
  public void changePassword(PasswordEditDTO dto) {
      Long empId = BaseContext.getCurrentId(); // 为什么：从 token 上下文拿当前登录人，防止越权
      Employee employee = employeeMapper.getById(empId); // 你需要已有或补一个 getById
  
      if (employee == null) {
          throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
      }
  
      // 为什么：bcrypt 每次 hash 不一样，必须用 matches 校验明文 vs hash
      if (!passwordEncoder.matches(dto.getOldPassword(), employee.getPassword())) {
          throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
      }
  
      String newHash = passwordEncoder.encode(dto.getNewPassword());
      employeeMapper.updatePasswordAndMarkChanged(empId, newHash, LocalDateTime.now(), empId);
  }
  ```

- **补齐缺失常量 `PASSWORD_NEED_CHANGE`**（为什么：拦截器返回统一可识别的业务提示，避免前端只看到“权限不足”）

  ```java
  // sky-common: com.sky.constant.MessageConstant
  public static final String PASSWORD_NEED_CHANGE = "首次登录请先修改密码";
  ```

如果你当前 `EmployeeMapper` 里没有 `getById`，你就再补一条（否则上面 Service 编译不过）：

```java
@Select("select * from employee where id = #{id}")
Employee getById(Long id);
```

------

**完善 : 重复user**

GlobalExceptionHandler里添加

```java
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public Result exceptionHandler(org.springframework.dao.DuplicateKeyException ex) {
        return Result.error(MessageConstant.ALREADY_EXISTS);
    }
```

具体原因：

**1）异常在 Spring 里经常被“翻译/包装”了**

在 Spring 生态里，底层 JDBC/MyBatis 抛出的 `SQLException`，通常会被 Spring 的异常转换器包装成统一的 `DataAccessException` 体系。

重复键这种最常见的情况，会变成：

- `org.springframework.dao.DuplicateKeyException`

所以你只写：

```
@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
```

很多时候根本进不到这个 handler（因为异常类型已经变了）。

而抓：

```
@ExceptionHandler(DuplicateKeyException.class)
```

命中率更高、语义更明确：就是“唯一约束冲突/重复键”。

**2）不再依赖 message 字符串解析**

你之前靠：

```
message.contains("Duplicate entry")
message.split(" ")[2]
```

这依赖数据库/驱动返回的英文错误文本格式，极不稳定。

抓 `DuplicateKeyException` 就不需要解析 message：异常类型本身就说明了问题。

**3）避免把数据库细节暴露给前端**

你之前会返回 `ex.getMessage()`，可能包含表名、索引名、SQL 片段等。
工程上应该返回业务文案：

```
return Result.error(MessageConstant.ALREADY_EXISTS);
```

一句话：**用 `DuplicateKeyException` 是为了“更稳地捕获重复键、避免脆弱的字符串解析、避免泄露数据库内部信息”。**

------

完善:创始人id,修改人id

因此要根据token反向解析id

解析出登录员工id后,如何传递给Service的save方法?

ThreadLocal并不是一个Thread,而是Threadi的局部变量.
ThreadLocal为每个线程提供单独一份存储空间,具有线程隔离的效果,只有在线程内才能获取到对应的值,线程外则不能访问.
ThreadLocala常用方法:
public void set(T value)
设置当前线程的线程局部变量的值public T get()
返回当前线程所对应的线程局部变量的值public void remove()
移除当前线程的线程局部变量

`BaseContext` 是用 `ThreadLocal` 保存**当前请求线程的上下文**，最常存的是“当前登录用户 id”。通常在 JWT 拦截器/过滤器里解析 token 后 `setCurrentId`，业务层用 `getCurrentId` 给 `createUser/updateUser` 等审计字段赋值。请求结束必须 `removeCurrentId`，否则线程复用会导致串号/越权。

**为什么用 ThreadLocal**

因为 Web 服务器线程池会用同一个线程处理同一次请求的多个调用链路。ThreadLocal 相当于“这个线程专属的变量”，免得你把 `currentUserId` 层层传参。

------

**修改密码接口实现:**

典型的错误:

```
 @Override
    public void changePassword(PasswordEditDTO passwordEditDTO){

        
        // 校验原密码
        Employee employee = employeeMapper.getById(passwordEditDTO.getEmpId());
        if (!passwordEncoder.matches(employee.getPassword(), passwordEditDTO.getOldPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }
        // mapper设置新密码
        employeeMapper.updatePasswordAndMarkChanged(employee.getId(),
                passwordEditDTO.getNewPassword(),
                LocalDateTime.now(),
                BaseContext.getCurrentId());

    }
这段代码里有 3 个关键问题：
用 passwordEditDTO.getEmpId() 查人 = 越权漏洞
前端可以把 empId 改成任意值，你就会去查并改别人的密码。
正确做法：只能用 BaseContext.getCurrentId() 作为目标用户 id。
matches 参数顺序写反了（这是会导致永远校验失败的 bug）
PasswordEncoder.matches(rawPassword, encodedPassword)
rawPassword：明文（用户输入 oldPassword）
encodedPassword：数据库里的 hash（employee.getPassword）
写成：
matches(employee.getPassword(), passwordEditDTO.getOldPassword())
完全反了。
更新时写入的是新密码明文（严重安全问题）
你传给 updatePasswordAndMarkChanged 的是：
passwordEditDTO.getNewPassword()
这是明文。必须先 passwordEncoder.encode(newPassword) 再存。
应该怎么改
Long empId = BaseContext.getCurrentId();
Employee employee = employeeMapper.getById(empId);
校验：passwordEncoder.matches(passwordEditDTO.getOldPassword(), employee.getPassword())
入库：String newHash = passwordEncoder.encode(passwordEditDTO.getNewPassword());
更新：updatePasswordAndMarkChanged(empId, newHash, now, empId)
另外还有两个工程点别漏：
先判空：employee == null 直接抛 AccountNotFoundException（防 NPE）
新密码最好做基本校验（长度/是否等于旧密码），至少 Service 层校验一次
```

------

### **员工分页开发接口**

**定接口契约**

- URL、方法（GET/POST/PUT/DELETE）、入参（body/query/path）、出参结构（Result）、错误码/错误消息约定。

**定义入参/出参对象**

- **DTO**：前端传什么你就建什么（必要字段、校验注解）。
- **VO**：你返回给前端的数据结构（不要直接把 entity 全量返回）。
- 需要分页就再定义 PageQueryDTO / **PageResult**。

**Controller**

- 只做：参数接收 + 校验 + 调 service + 返回 Result。
- 不做：业务判断、加密、拼 SQL。

**Service 接口 + 实现**

- 放业务：权限/当前登录用户、参数合法性、密码加密、状态机、事务边界。
- 如果涉及多表/多步骤更新，这里加 `@Transactional`。

**Mapper / Repository**

- 只做数据访问：CRUD、查询、更新。
- MyBatis：写 `@Select/@Update` 或 XML；参数用 `@Param`。
- 注意：写清楚主键生成策略（DB 自增/雪花等），并决定是否回填 id。

**Entity（持久化模型）**

- 对应表字段，别混进前端临时字段。

**异常与返回**

- Service 抛业务异常（例如 AccountNotFound/PasswordError）。
- 统一在 `GlobalExceptionHandler` 转成 `Result.error(...)`，别在 Controller 里到处 try/catch。

**联调验证**

- Postman/Swagger 先测通，再接前端；最后补单测/日志。

#### 推荐命名规则

- **Controller**：按接口动作命名（对外）
    - `login`
    - `logout`
    - `changePassword`
    - `create`
    - `page`
- **Service**：按业务动作命名（对内）
    - `login`
    - `changePassword`
    - `createEmployee`
    - `updateEmployee`
- **Mapper**：按 SQL 行为命名（数据层）
    - `insert`
    - `update`
    - `getById` / `selectById`
    - `getPwdChangedById`
    - `updatePasswordAndMarkChanged`

结论：**Controller/Service 同名可以；Mapper 尽量用 insert/update/select 这类“数据库语义”的名字。**

步骤:

Controller

看请求是get还是put GetMapping

EmployeePageQueryDTO dto看接口文档是不是json不是就不加@RequestBody

```java
@GetMapping("/page")   
@ApiOperation("查询员工信息")
public Result<PageResult> page (EmployeePageQueryDTO dto){  // 从url里读取参数, 不用
    if(dto.getName()==null) log.info("查询员工");
    else log.info("查询员工{}",dto.getName());
    return Result.success(employeeService.pageQuery(dto));
}
```

Service : 用PageHelper插件

```java
  @Override
    public PageResult pageQuery(EmployeePageQueryDTO dto) {
        // 开始分页查询
        PageHelper.startPage(dto.getPage(),dto.getPageSize()); // 插件
        Page<Employee> page = employeeMapper.pageQuery(dto);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);
    }
```

因为返回类型是List records

PageHelper.startPage(dto.getPage(),dto.getPageSize());设置分页

Page page = employeeMapper.pageQuery(dto);调用持久层返回page对象包含查出来的数据条数等信息,用List 包装

调用PageResult构造函数

Mapper : 执行xml里的sql

```java
Page<Employee> pageQuery(EmployeePageQueryDTO dto);
```

xml:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!--
  这个 mapper 文件用于给 EmployeeMapper 接口提供 SQL 映射。
  MyBatis 默认用 “Mapper接口全限定名 + '.' + 方法名” 去定位 SQL：
  例如调用 employeeMapper.pageQuery(dto)，会去找 statementId = com.sky.mapper.EmployeeMapper.pageQuery
-->
<mapper namespace="com.sky.mapper.EmployeeMapper">

    <!--
      id：SQL 语句的标识，通常与 Mapper 接口的方法名一致（这里对应 EmployeeMapper.pageQuery）
      resultType：查询结果“每一行”映射成的 Java 类型（不是 List 的类型，而是 List 里元素的类型）
      说明：select 返回多行时，MyBatis 会自动组装成 List<resultType>
    -->
    <select id="pageQuery" resultType="com.sky.entity.Employee">

        <!-- 基础查询：从 employee 表查询 -->
        select * from employee

        <!--
          where 动态拼接：
          - <where> 会自动处理前导 AND/OR（把第一个 and/or 去掉）
          - 并且在有条件时自动加上 WHERE；没有任何条件时不输出 WHERE
        -->
        <where>
            <!--
              test：OGNL 表达式，用于判断是否拼接该条件
              这里表示：只有 name 非空且非空串时，才拼接 like 条件
            -->
            <if test="name != null and name != ''">
                <!--
                  模糊匹配：name LIKE '%xxx%'
                  用 concat('%', #{name}, '%') 拼接通配符，避免手写 '%${name}%' 造成 SQL 注入风险
                  #{name}：预编译参数（PreparedStatement），安全
                -->
                and name like concat('%', #{name}, '%')
            </if>
        </where>

        <!-- 排序：按 id 升序 -->
        order by id
    </select>

</mapper>
```

1.![image-20251222170312104](./D:/Typora/assets/image-20251222170312104.png)

### 苍穹外卖时间字段格式化踩坑日志（从“返回密码不对”开始）

#### 起因：发现员工分页接口返回了密码字段，直觉不对

- 员工分页接口返回 JSON 里带了 `password`（以及其它表字段），不符合安全与接口契约原则。
- 判断原因：Mapper 用了 `select *` + `resultType=Employee`，实体字段全被序列化返回。

#### 决策：引入 VO，只返回前端需要的字段

- 新建 `EmployeePageVO`（放在 `com.sky.vo`），只保留页面需要字段：`id/name/username/phone/status/createTime/updateTime`。
- 目标：分页查询返回 `List<EmployeePageVO>`，从源头杜绝密码等敏感字段输出。

#### 第一轮尝试：给 VO 的时间字段加 @JsonFormat，但返回依然是 14 位时间串

- VO 里把 `createTime/updateTime` 改为 `LocalDateTime`，并加：
    - `@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")`
- 结果：时间仍显示成 `20251222152445`，说明 VO 的序列化格式化没有生效。

#### 根因定位：VO 写对了，但 MyBatis 根本没用 VO

- 查看 `EmployeeMapper.xml` 发现：
    - `resultType` 仍然是 `com.sky.entity.Employee`
- 结论：分页结果行仍映射到实体类，VO 上的 `@JsonFormat` 当然不会生效；同时实体还会继续把密码带出来。

#### 第二轮修改：把 resultType 改成 VO，结果项目启动直接报错

- 报错核心信息：
    - `Could not resolve type alias 'com.sky.entity.EmployeePageVO'`
    - `ClassNotFoundException: com.sky.entity.EmployeePageVO`
- 根因：XML 里把 VO 包名写错了（误写到 `com.sky.entity`），实际在 `com.sky.vo.EmployeeVO`。

#### 最终修复：resultType 指向正确类 + SQL 明确列并加别名

- XML 修正：
    - `resultType="com.sky.vo.EmployeeVO"`
- SQL 进一步规范：
    - 明确列选择，彻底去掉 `select *`
    - `create_time as createTime`
    - `update_time as updateTime`
- 结果：
    - 分页接口只返回 VO 字段，密码不再出现在响应中
    - `@JsonFormat` 对 `LocalDateTime` 生效，时间返回为 `yyyy-MM-dd HH:mm:ss` 标准格式

#### 经验总结（可复用检查清单）

- VO 的 `@JsonFormat` 是否生效，首先看：Mapper 的 `resultType` 是否真的映射到该 VO。
- `resultType` 报 `ClassNotFoundException`，优先核对：类包名是否写错。
- 只要是对外接口：尽量避免 `select *`，用 VO/DTO + 明确列选择控制输出字段，敏感字段默认不出现在响应里。
- 下划线字段到驼峰字段：最稳的是 SQL 别名，不依赖全局 `mapUnderscoreToCamelCase` 配置。

**方式二 : 在VebMvcConfiguration中扩展Spring MVC的消息转换器,统一对日期类型进行格式化处理**

```java
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 需要为消总转换器设置一个对象转换器,对象转换器可以将Java对象序列化为Json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将自已的消息转换器加入容器中
        converters.add(0,converter);
    }
```

不需要手动调用 `WebMvcConfiguration`。它加了 `@Configuration`，启动时被 Spring 扫描成 Bean。

因为继承了 `WebMvcConfigurationSupport`，Spring MVC 初始化时会**自动回调**重写的方法：

- `addInterceptors`：把 `JwtTokenAdminInterceptor` 注册进请求链；之后每次命中 `/admin/**` 的请求都会自动执行拦截器。
- `extendMessageConverters`：把自定义的 `MappingJackson2HttpMessageConverter + JacksonObjectMapper` 放到转换器列表最前；之后 Controller 返回对象时会按它的规则序列化成 JSON。

------

### 启用禁用员工接口

##### 1）`/admin/employee/status/{status}` 是什么？

这是 **REST 风格的路径**，`{status}` 叫 **路径变量（Path Variable）**，表示“这个位置放一个值”。

例子：

- `/admin/employee/status/0`：把 status=0
- `/admin/employee/status/1`：把 status=1

后端通常这样接：

```java
@PostMapping("/admin/employee/status/{status}")
public Result<?> updateStatus(@PathVariable Integer status, @RequestParam Long id) { ... }
```

含义：

- `status` 从路径里拿（`/status/1` 的 1）
- `id` 从查询参数里拿（`?id=123`）

含义：

- `status` 从路径里拿（`/status/1` 的 1）
- `id` 从查询参数里拿（`?id=123`）

------

##### 2）HTTP 方法：GET / POST / PUT / DELETE（你该怎么用）

你先记一条核心规则：**方法表达“意图”**，参数表达“数据”。

##### GET：查询（不改数据）

- 典型：分页查询、详情查询
- 数据放哪：**Query 参数**（`?page=1&pageSize=10&name=tom`）
- 特点：可缓存、可书签、一般不带 body

例：

```
GET /admin/employee/page?page=1&pageSize=10&name=张
```

##### POST：新增 / 提交 / 复杂操作（通常会改数据）

- 典型：新增员工、登录、提交表单
- 数据放哪：通常放 **请求体 body（JSON）**
- 也可以混用 query/path，但主数据建议放 body

例：

```
POST /admin/employee
Content-Type: application/json
{
  "name": "...",
  "username": "...",
  ...
}
```

##### PUT：整体更新（语义上“替换/更新资源”）

- 典型：更新员工信息
- 数据放哪：body(JSON)

例：

```
PUT /admin/employee
{ "id": 1, "name": "xx", ... }
```

##### DELETE：删除

- 典型：删除某个 id
- 数据放哪：path 或 query

例：

```
DELETE /admin/employee?id=1
```

或

```
DELETE /admin/employee/1
```

> 实战里很多项目也会用 POST 做更新/删除（历史包袱/前端方便），但语义上还是上面更规范。

------

##### 3）请求里“参数”到底有哪些位置？（你必须搞清）

你可以把一次请求拆成 4 个数据来源：

##### A. Path Variable：路径变量

- 位置：URL 路径里
- 例：`/status/1` 的 `1`
- 后端：`@PathVariable`

适合：资源的关键标识、状态切换等短参数。

##### B. Query Parameter：查询参数

- 位置：`?a=1&b=2`
- 后端：`@RequestParam`

适合：分页、过滤、排序、搜索条件。

##### C. Request Body：请求体

- 位置：HTTP body（常用 JSON）
- 后端：`@RequestBody`

适合：新增/更新时的一堆字段（结构化数据）。

##### D. Header：请求头

- 位置：Header（最常见：`Authorization`）
- 后端：`@RequestHeader` 或拦截器里读

适合：token、内容类型、语言、客户端信息等。

------

##### 4）结合你项目的典型例子（苍穹外卖）

##### 登录

- `POST /admin/employee/login`
- body：用户名密码（JSON）
- 返回：token

##### 分页查询

- `GET /admin/employee/page?page=1&pageSize=10&name=xxx`
- query：page/pageSize/name

##### 启用/禁用账号（你问的这个）

- 常见写法 1（你项目像这种）：
  `POST /admin/employee/status/{status}?id=xxx`
    - path：status
    - query：id
- 更规范写法 2：
  `PUT /admin/employee/{id}/status`
  body：`{ "status": 1 }`

------

##### 5）你现在最该记住的“最小规则”

- **查**：GET + query
- **增**：POST + body
- **改**：PUT/PATCH + body
- **删**：DELETE + path/query
- `{xxx}`：路径变量 `@PathVariable`
- `?a=1`：查询参数 `@RequestParam`
- JSON：请求体 `@RequestBody`
- token：请求头/拦截器

如果你把你项目里 `EmployeeController` 的 3 个接口签名贴出来（分页、改状态、改密），我可以按真实代码把每个参数从哪里来、为什么这么设计，逐行给你讲一遍。

### **感觉谁都能互相修改账号权限不太对劲,尝试增加权限系统**

权限定级

```java
package com.sky.role;
public enum RoleLevel {
    STAFF(1),
    MANAGER(5),
    SUPER(9);
    private final int level;
    RoleLevel(int level) {
        this.level = level;
    }
    public int level() {
        return level;
    }
    public static RoleLevel from(Object v) {
        if (v == null) return STAFF;
        String s = String.valueOf(v);
        // 允许 token 存 "STAFF"/"MANAGER"/"SUPER" 或数字
        try {
            int x = Integer.parseInt(s);
            if (x >= SUPER.level) return SUPER;
            if (x >= MANAGER.level) return MANAGER;
            return STAFF;
        } catch (NumberFormatException e) {
            return RoleLevel.valueOf(s);
        }
    }
}
```

进程绑定

```java
package com.sky.context;

import com.sky.role.RoleLevel;

public class BaseContext {
    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();
    private static final ThreadLocal<RoleLevel> currentRole = new ThreadLocal<>();

    public static void setCurrentId(Long id) { currentId.set(id); }
    public static Long getCurrentId() { return currentId.get(); }

    public static void setCurrentRole(RoleLevel role) { currentRole.set(role); }
    public static RoleLevel getCurrentRole() { return currentRole.get(); }

    public static void removeCurrentId() { currentId.remove(); }
    public static void removeCurrentRole() { currentRole.remove(); }
}
alter table employee add column role int not null default 1;
update employee set role = 9 where username = 'admin';  -- SUPER
-- 你指定哪个账号做中级：
update employee set role = 5 where id = 12;            -- MANAGER 示例
entity-employee
private Integer role; // 1=STAFF, 5=MANAGER, 9=SUPER
```

------

### **修改员工信息接口 : 前端不传id很不合逻辑**

使用vscode-Gemini修改前端,不小心把我后端也改了一部分,无伤大雅

**接口实现:**

```java
@PutMapping
@ApiOperation("修改员工信息")
public Result<String> changeEmployee(@RequestBody EmployeeUpdateDTO dto){
    if (dto.getId() == null) {
        throw new IllegalArgumentException("id is required");
    }
    log.info("修改员工信息：id={}, username={}", dto.getId(), dto.getUsername());
    employeeService.changeEmployee(dto);
    return Result.success();
}
@Override
public void changeEmployee(EmployeeUpdateDTO dto) {
    Long currentId = BaseContext.getCurrentId();
    Employee emp = Employee.builder()
            .id(dto.getId())
            .name(dto.getName())
            .username(dto.getUsername())
            .phone(dto.getPhone())
            .sex(dto.getSex())
            .idNumber(dto.getIdNumber())
            .updateUser(currentId)
            .build();

    employeeMapper.update(emp);
}
void update(Employee emp);
<update id="update" parameterType="com.sky.entity.Employee">
    update employee
    <set>
        <if test="name != null and name != ''">name = #{name},</if>
        <if test="username != null and username != ''">username = #{username},</if>
        <if test="phone != null and phone != ''">phone = #{phone},</if>
        <if test="sex != null and sex != ''">sex = #{sex},</if>
        <if test="idNumber != null and idNumber != ''">id_number = #{idNumber},</if>
        <if test="updateUser != null">update_user = #{updateUser},</if>
        update_time = now()
    </set>
    where id = #{id}
</update>
```

**修改文件路径：**
addEmployee.vue

**主要改动说明：**

1. **初始化表单数据时确保写入 `id`**：
   在 `init` 方法中，当从后端获取员工详情后，如果返回的数据中缺少 `id`，则回退使用路由参数中的 `id`（即列表页传过来的 `id`）。
2. **提交表单时强制补全 `id`**：
   在 `submitForm` 方法的编辑逻辑分支中，在构造请求参数 `params` 后，再次检查是否存在 `id`。如果不存在，则强制从路由参数中获取并赋值，确保发给后端的 PUT 请求包含 `id` 字段。

**关键代码片段：**

**1. 打开编辑弹窗/初始化数据 (`init` 方法)**

```typescript
  private async init() {
    const id = this.$route.query.id
    queryEmployeeById(id).then((res: any) => {
      if (res.data.code === 1) {
        const data = res.data.data
        this.ruleForm = {
          id: data.id || id, // 修复：优先使用返回的id，如果为空则使用路由参数中的id
          name: data.name,
          phone: data.phone,
          sex: data.sex === '0' ? '女' : '男',
          idNumber: data.idNumber,
          username: data.username
        }
      } else {
        this.$message.error(res.data.msg)
      }
    })
  }
```

**2. 提交请求 (`submitForm` 方法)**

```typescript
        } else {
          const params: any = {
            ...this.ruleForm,
            sex: this.ruleForm.sex === '女' ? '0' : '1'
          }
          // 修复：提交前再次检查，确保 id 字段存在，防止被解构丢失
          if (!params.id) {
            params.id = this.$route.query.id
          }
          editEmployee(params)
            .then((res: any) => {
              if (res.data.code === 1) {
                this.$message.success('员工信息修改成功！')
                this.$router.push({ path: '/employee' })
              } else {
                } else {
          const params: any = {
            ...this.ruleForm,
            sex: this.ruleForm.sex === '女' ? '0' : '1'
          }
          // 修复：提交前再次检查，确保 id 字段存在，防止被解构丢失
          if (!params.id) {
            params.id = this.$route.query.id
          }
          editEmployee(params)
            .then((res: any) => {
              if (res.data.code === 1) {
                this.$message.success('员工信息修改成功！')
                this.$router.push({ path: '/employee' })
              } else {
                this.$message.error(res.data.msg)
              }
            })
```

**Gemini后端部分 (Java)**

**EmployeeController.java**

新增了 `getById` 接口，用于前端编辑页面回显数据：

```java
    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息：{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
```

**EmployeeService.java**

接口定义中增加了方法声明：

```java
    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);
```

**EmployeeServiceImpl.java**

实现了查询逻辑，并对密码进行了脱敏处理：

```java
    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("****"); // 密码脱敏
        return employee;
    }
```

**文件：skyBackend/sky-server/src/main/java/com/sky/controller/admin/ShopController.java**

这是一个全新创建的文件，用于解决 `/admin/shop/status` 404 报错：

```java
package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取店铺营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
```
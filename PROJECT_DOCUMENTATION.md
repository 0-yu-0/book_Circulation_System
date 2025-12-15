# 图书流通管理系统 - 项目文档

## 项目概述

图书流通管理系统是一个完整的图书馆管理解决方案，采用前后端分离架构，提供图书管理、读者管理、借还书操作、统计分析等核心功能。

**项目特点**：
- 现代化的前后端分离架构
- 响应式设计，支持移动端访问
- 完整的借还书业务流程
- 丰富的统计分析功能
- 用户友好的界面设计

## 技术栈

### 后端技术栈
- **语言**: Java 17
- **框架**: 内置HTTP服务器
- **数据库**: MySQL
- **JSON处理**: Jackson 2.15.2
- **构建工具**: Maven
- **数据库驱动**: MySQL Connector/J 9.1.0

### 前端技术栈
- **框架**: Vue 3.3.0
- **路由**: Vue Router 4.2.0
- **状态管理**: Pinia 2.0.0
- **UI组件库**: Element Plus 2.3.0
- **HTTP客户端**: Axios 1.4.0
- **图表库**: ECharts 6.0.0
- **构建工具**: Vite 5.0.0
- **样式预处理器**: Sass

## 系统架构

### 目录结构
```
book_Circulation_System/
├── src/                    # 后端Java源码
│   ├── Server.java         # 主服务器类
│   ├── db/                 # 数据库操作
│   ├── entity/             # 实体类
│   └── service/            # 业务服务层
├── library-frontend/       # 前端项目
│   ├── src/
│   │   ├── api/            # API接口封装
│   │   ├── components/     # 公共组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # 状态管理
│   │   ├── views/          # 页面组件
│   │   └── utils/          # 工具函数
│   └── package.json
├── resources/              # 资源文件
│   ├── database.sql        # 数据库脚本
│   └── 数据流图.png        # 系统流程图
└── pom.xml                # Maven配置
```

### 数据流架构
- 前端通过RESTful API与后端通信
- 后端使用JDBC直接操作MySQL数据库
- 采用Bearer Token进行身份认证
- 支持CORS跨域请求

## 功能模块

### 1. 用户认证模块
- **登录/登出**: 基于JWT Token的身份验证
- **权限控制**: 路由守卫保护需要认证的页面
- **会话管理**: Token自动续期和存储

### 2. 图书管理模块
- **图书列表**: 支持分页、搜索、筛选（按作者、分类）
- **图书详情**: 查看图书详细信息
- **图书增删改**: 完整的CRUD操作
- **库存管理**: 调整图书库存数量
- **ISBN唯一性检查**: 防止重复ISBN

### 3. 读者管理模块
- **读者列表**: 读者信息管理
- **读者详情**: 查看读者借阅历史
- **读者状态管理**: 启用/禁用读者账户
- **借书限制**: 不同类型读者的借书上限管理

### 4. 借书管理模块
- **借书流程**: 三步式借书操作
  - 选择读者
  - 选择图书
  - 确认借书
- **批量借书**: 支持一次借阅多本图书
- **借书限制检查**: 检查读者状态和借书上限
- **库存检查**: 确保图书可借状态

### 5. 还书管理模块
- **还书流程**: 输入读者信息，选择归还图书
- **逾期计算**: 自动计算逾期天数和罚款
- **收据生成**: 归还确认和收据打印
- **状态更新**: 更新图书库存和借阅状态

### 6. 统计分析模块
- **数据概览**: 图书总数、读者数、借阅统计
- **热门图书**: 按借阅次数排行
- **借阅分布**: 在借图书与在馆图书比例
- **逾期记录**: 逾期借阅记录查询
- **数据导出**: 支持CSV格式导出

## 数据库设计

### 核心数据表

#### 1. bookInformation (图书信息表)
```sql
CREATE TABLE bookInformation (
    bookId VARCHAR(20) PRIMARY KEY,
    isbn VARCHAR(13) NOT NULL,
    bookName VARCHAR(255) NOT NULL,
    bookAuthor VARCHAR(100) NOT NULL,
    bookPublisher VARCHAR(100),
    bookPubDate DATE,
    bookCategory VARCHAR(50),
    bookLocation VARCHAR(50) NOT NULL,
    bookTotalCopies INT,
    bookAvailableCopies INT,
    bookPrice INT NOT NULL,
    borrowCount BIGINT NOT NULL
);
```

#### 2. readerInformation (读者信息表)
```sql
CREATE TABLE readerInformation (
    readerId VARCHAR(20) PRIMARY KEY,
    readerName VARCHAR(50) NOT NULL,
    readerCardType VARCHAR(20) NOT NULL,
    readerCardNumber VARCHAR(30) NOT NULL UNIQUE,
    readerPhoneNumber VARCHAR(20),
    registerDate DATE NOT NULL,
    readerStatus INT,
    totalBorrowNumber INT NOT NULL,
    nowBorrowNumber INT NOT NULL
);
```

#### 3. borrowTable (借阅登记表)
```sql
CREATE TABLE borrowTable (
    borrowId VARCHAR(20) PRIMARY KEY,
    bookId VARCHAR(20),
    readerId VARCHAR(20),
    borrowDate DATE NOT NULL,
    dueDate DATE NOT NULL,
    borrowStates INT NOT NULL,
    FOREIGN KEY (bookId) REFERENCES bookInformation(bookId),
    FOREIGN KEY (readerId) REFERENCES readerInformation(readerId)
);
```

#### 4. returnTable (归还登记表)
```sql
CREATE TABLE returnTable (
    returnId VARCHAR(20) PRIMARY KEY,
    borrowId VARCHAR(20) NOT NULL,
    returnDate DATE NOT NULL,
    overDays INT NOT NULL,
    fine DOUBLE NOT NULL,
    FOREIGN KEY (borrowId) REFERENCES borrowTable(borrowId)
);
```

### 索引设计
- 图书表：ISBN唯一索引、书名、作者、分类索引
- 读者表：证件号唯一索引、姓名索引
- 借阅表：图书ID、读者ID、借阅状态、应还日期索引
- 归还表：借阅ID、归还日期索引

## API接口文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

### 图书管理接口
- `GET /api/books` - 获取图书列表（支持分页、搜索、筛选）
- `GET /api/books/{id}` - 获取图书详情
- `POST /api/books` - 创建新图书
- `PUT /api/books/{id}` - 更新图书信息
- `DELETE /api/books/{id}` - 删除图书
- `PATCH /api/books/{id}/stock` - 调整库存

### 读者管理接口
- `GET /api/readers` - 获取读者列表
- `GET /api/readers/{id}` - 获取读者详情
- `POST /api/readers` - 创建新读者
- `PUT /api/readers/{id}` - 更新读者信息
- `DELETE /api/readers/{id}` - 删除读者
- `GET /api/readers/byCard/{cardNumber}` - 根据证件号查询读者

### 借阅管理接口
- `POST /api/borrow` - 借书操作
- `GET /api/borrow` - 查询借阅记录
- `GET /api/borrow/overdue` - 查询逾期记录
- `POST /api/return` - 还书操作

### 统计接口
- `GET /api/statistics` - 获取统计数据
- `GET /api/statistics/popular-books` - 获取热门图书

## 部署说明

### 后端部署
1. **环境要求**:
   - Java 17+
   - MySQL 5.7+
   - Maven 3.6+

2. **数据库配置**:
   ```sql
   # 创建数据库
   CREATE DATABASE LibraryDB;
   USE LibraryDB;
   
   # 执行resources/database.sql初始化数据库
   ```

3. **启动服务**:
   ```bash
   # 编译项目
   mvn clean compile
   
   # 运行服务
   mvn exec:java
   ```

### 前端部署
1. **环境要求**:
   - Node.js 16+
   - npm 或 yarn

2. **构建和运行**:
   ```bash
   # 进入前端目录
   cd library-frontend
   
   # 安装依赖
   npm install
   
   # 开发模式运行
   npm run dev
   
   # 生产构建
   npm run build
   ```

3. **生产部署**:
   - 使用Nginx托管构建后的静态文件
   - 配置反向代理到后端API

## 开发指南

### 后端开发
1. **实体类开发**:
   - 所有实体类位于`src/entity/`目录
   - 遵循Java Bean规范，提供getter/setter方法

2. **服务类开发**:
   - 业务逻辑位于`src/service/`目录
   - 使用PreparedStatement防止SQL注入
   - 事务处理确保数据一致性

3. **API开发**:
   - 遵循RESTful设计原则
   - 统一错误处理机制
   - 支持CORS跨域请求

### 前端开发
1. **组件开发**:
   - 使用Vue 3 Composition API
   - 遵循Element Plus组件规范
   - 响应式设计支持移动端

2. **状态管理**:
   - 使用Pinia进行状态管理
   - 模块化store设计
   - 持久化存储用户会话

3. **API调用**:
   - 统一封装在`src/api/`目录
   - 自动处理Token认证
   - 统一错误处理

## 测试数据

系统包含完整的测试数据：
- 40本测试图书，涵盖计算机、文学、历史、科幻等分类
- 15个测试读者，包含学生、教师等不同类型
- 20条借阅记录和10条归还记录

## 安全考虑

1. **认证安全**:
   - JWT Token认证机制
   - Token过期时间管理
   - 敏感操作需要重新认证

2. **数据安全**:
   - SQL注入防护
   - 输入参数验证
   - 敏感数据脱敏显示

3. **业务安全**:
   - 借书数量限制检查
   - 库存并发控制
   - 读者状态验证

## 性能优化

1. **数据库优化**:
   - 合理的索引设计
   - 分页查询避免全表扫描
   - 连接池管理

2. **前端优化**:
   - 组件懒加载
   - 图片和资源压缩
   - 缓存策略优化

3. **API优化**:
   - 响应数据压缩
   - 请求合并
   - 缓存热点数据

## 扩展性考虑

1. **模块化设计**: 各功能模块独立，便于扩展
2. **配置化**: 关键参数可配置，便于调整
3. **插件化**: 支持功能插件扩展
4. **微服务化**: 支持向微服务架构迁移

## 维护和支持

### 日志管理
- 后端服务记录操作日志
- 前端错误日志收集
- 数据库操作审计

### 监控告警
- 系统性能监控
- 异常情况告警
- 用户行为分析

---

**版本信息**: v1.0  
**最后更新**: 2025-12-15  
**维护团队**: 图书流通系统开发组
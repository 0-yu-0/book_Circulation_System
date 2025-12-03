# 图书流通管理系统前后端规格说明文档

## 1. 前端现状概述

### 1.1 技术栈
- Vue 3 (Composition API)
- Vue Router 4
- Pinia 状态管理
- Element Plus UI 组件库
- Axios HTTP 客户端
- Vite 构建工具
- Mock.js (用于模拟数据)
- ECharts (数据可视化)

### 1.2 项目结构
```
library-frontend/
├── src/
│   ├── api/           # API 接口封装
│   ├── components/    # 公共组件
│   ├── router/        # 路由配置
│   ├── stores/        # 状态管理
│   ├── styles/        # 全局样式
│   ├── utils/         # 工具函数
│   ├── views/         # 页面组件
│   ├── mock/          # 模拟数据和服务
│   ├── App.vue        # 根组件
│   └── main.js        # 入口文件
├── public/            # 静态资源
├── package.json       # 项目依赖配置
└── vite.config.js     # 构建配置
```

### 1.3 项目依赖和配置说明

#### 1.3.1 项目依赖
项目使用 npm 进行依赖管理，主要依赖包括：

```json
{
  "dependencies": {
    "axios": "^1.4.0",
    "echarts": "^6.0.0",
    "element-plus": "^2.3.0",
    "mockjs": "^1.1.0",
    "pinia": "^2.0.0",
    "vue": "^3.3.0",
    "vue-router": "^4.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.2.0",
    "vite": "^5.0.0"
  }
}
```

#### 1.3.2 项目配置
- 开发服务器：使用 Vite，运行命令 `npm run dev`，默认端口 3000
- 构建命令：`npm run build`
- 预览命令：`npm run preview`

#### 1.3.3 项目启动步骤
1. 进入项目目录：`cd library-frontend`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问应用：打开浏览器访问 `http://localhost:3000`

### 1.4 已实现功能模块

#### 1.4.1 登录模块 (Login.vue)
- 用户登录界面
- Token 认证机制（JWT）
- 路由守卫保护

#### 1.4.2 仪表盘 (Dashboard.vue)
- 系统关键指标展示（总图书、总读者、在借、今日借书）
- 热门图书柱状图展示
- 借阅状态分布饼图展示

#### 1.4.3 图书管理 (BookList.vue)
- 图书列表展示（分页、搜索）
- 图书增删改查操作
- CSV 导入导出功能
- 图书库存调整

#### 1.4.4 读者管理 (ReaderList.vue)
- 读者列表展示（分页、搜索）
- 读者增删改查操作
- CSV 导入导出功能

#### 1.4.5 借书模块 (BorrowPage.vue)
- 三步式借书流程
  1. 选择读者
  2. 选择图书
  3. 确认借阅
- 借书篮功能
- 借阅凭证打印

#### 1.4.6 还书模块 (ReturnPage.vue)
- 读者在借记录查询
- 图书归还操作
- 逾期罚款计算

#### 1.4.7 统计模块 (Statistics.vue)
- 热门图书统计（柱状图+表格）
- 逾期未还统计（饼图+表格）

#### 1.4.8 图书详情页面 (BookDetail.vue)
- 图书详细信息展示
- 图书借阅记录展示

#### 1.4.9 读者详情页面 (ReaderDetail.vue)
- 读者详细信息展示
- 读者借阅历史展示

#### 1.4.10 借阅历史查询 (BorrowHistory.vue)
- 借阅记录查询功能
- 支持按读者ID和图书名称筛选

#### 1.4.11 逾期记录管理 (OverdueRecords.vue)
- 逾期记录查询功能
- 支持按读者姓名和图书名称筛选

#### 1.4.12 系统设置页面 (Settings.vue)
- 系统基本参数设置
- 系统信息展示

## 2. 前端待实现功能

### 2.1 功能完善
1. **权限控制**
   - 不同角色用户界面权限控制
   - 操作按钮权限控制

2. **用户体验优化**
   - 加载状态提示 (已完成)
   - 错误处理优化 (已完成)
   - 表单验证增强 (已完成)

3. **界面改进**
   - 主题切换功能
   - 更多数据可视化图表

4. **功能增强**
   - 权限管理系统
   - 日志管理系统
   - 数据备份与恢复

### 2.2 技术优化
1. **性能优化**
   - 组件懒加载
   - 数据缓存机制
   - 图片懒加载

2. **代码质量**
   - 单元测试覆盖
   - 代码规范统一
   - 类型检查增强

## 3. 前端界面美化方案

为了提升系统的用户体验和视觉效果，我们建议实施以下前端界面美化方案：

### 3.1 整体视觉风格升级

#### 3.1.1 色彩体系优化
- 主色调：采用深蓝色 (#1890ff) 作为主要品牌色，体现专业性和可靠性
- 辅助色：
  - 成功色：绿色 (#52c41a)
  - 警告色：橙色 (#faad14)
  - 错误色：红色 (#ff4d4f)
  - 中性色：灰色系用于背景和文字排版
- 背景色：使用浅灰白色 (#f5f7fa) 替代纯白色，减少视觉疲劳

#### 3.1.2 字体排版优化
- 主字体：采用系统默认字体族，保证在各平台上的良好显示效果
- 字号层级：
  - 标题：18-24px
  - 子标题：16px
  - 正文：14px
  - 辅助文字：12px
- 字重区分：合理运用粗细对比突出重点信息

#### 3.1.3 间距与布局
- 统一内边距和外边距标准：8px的倍数体系 (8px, 16px, 24px, 32px)
- 卡片圆角：统一使用 8px 圆角，提升现代感
- 阴影效果：适度使用阴影区分层级关系

### 3.2 组件美化方案

#### 3.2.1 导航菜单优化 (Layout.vue)
- 侧边栏：
  - 添加图标增强识别度
  - 实现折叠/展开动画效果
  - 优化高亮状态显示
- 顶部栏：
  - 增加面包屑导航
  - 用户头像替换文本退出按钮
  - 添加通知中心入口

#### 3.2.2 登录页面美化 (Login.vue)
- 背景设计：使用渐变色彩或图书相关的简约插画作为背景
- 卡片样式：增加毛玻璃效果和更柔和的阴影
- 输入框：使用更大圆角和更舒适的间距
- 按钮设计：采用渐变色按钮并添加悬停动效

#### 3.2.3 仪表盘美化 (Dashboard.vue)
- KPI卡片：
  - 添加图标与数据关联
  - 使用不同颜色区分不同类型指标
  - 增加数据趋势可视化（小面积图表）
- 热门图书：
  - 优化图书卡片展示，包含封面占位图
  - 添加悬停效果显示更多信息
  - 使用网格布局提高可读性

#### 3.2.4 表格组件优化 (DataTable.vue)
- 表头：
  - 固定表头便于浏览大量数据
  - 添加排序功能指示器
- 行样式：
  - 斑马纹提高可读性
  - 悬停高亮当前行
  - 选中状态优化
- 分页器：
  - 美化分页按钮样式
  - 显示总条数和当前页信息
- 加载状态：
  - 添加加载动画提示
  - 数据加载时禁用交互元素

#### 3.2.5 表单组件优化 (BookForm.vue, ReaderForm.vue)
- 表单项：
  - 统一表单字段高度和圆角
  - 优化标签对齐方式
  - 增强错误状态提示
- 按钮组：
  - 使用主次分明的按钮层级
  - 添加合适的间距避免误触

### 3.3 交互体验提升

#### 3.3.1 动效设计
- 页面切换：使用淡入淡出或滑动转场效果
- 按钮反馈：点击时添加波纹或缩放效果
- 加载状态：使用定制化的加载动画替代简单旋转图标

#### 3.3.2 响应式适配
- 移动端适配：针对小屏幕优化布局和交互
- 触控优化：增大触摸目标区域，优化手势操作
- 自适应表格：在小屏幕上可横向滚动或转换为卡片视图

#### 3.3.3 可访问性改进
- 对比度：确保文字与背景有足够的对比度
- 键盘导航：支持完整的键盘操作流程
- 屏幕阅读器：为视障用户提供友好的语义标记

### 3.4 实施步骤

#### 3.4.1 第一阶段：基础样式重构
1. 建立全局样式变量体系
2. 重构基础组件样式（按钮、输入框、卡片等）
3. 优化布局和间距系统

#### 3.4.2 第二阶段：页面美化
1. 登录页面重新设计
2. 仪表盘视觉升级
3. 各功能页面布局优化

#### 3.4.3 第三阶段：动效和细节优化
1. 添加交互动效
2. 优化过渡效果
3. 微调细节和用户体验

### 3.5 技术实现建议

#### 3.5.1 样式方案
- 使用 SCSS 预处理器管理样式变量和混合宏
- 建立统一的命名规范（BEM 方法论）
- 利用 CSS 变量实现主题切换功能

#### 3.5.2 动效实现
- 使用 CSS3 transitions 和 animations 实现基础动效
- 复杂动效可引入 animate.css 或自定义 JavaScript 动画
- 注意性能优化，避免造成卡顿

#### 3.5.3 响应式实现
- 使用媒体查询适配不同屏幕尺寸
- 弹性布局采用 Flexbox 和 Grid 结合的方式
- 图片资源使用响应式图片技术

## 4. 后端需求规格

### 4.1 技术栈建议
- Java 
- MySQL 数据库
- RESTful API 设计
- JWT 身份验证
- Maven 项目管理

### 4.2 数据库设计

#### 4.2.1 图书信息表 (book_information)
| 字段名 | 类型 | 描述 |
|--------|------|------|
| book_id | String | 图书ID（主键） |
| isbn | String | ISBN国际标准书号 |
| book_name | String | 书名 |
| book_author | String | 作者 |
| book_publisher | String | 出版社 |
| book_pub_date | Date | 出版日期 |
| book_category | String | 图书分类 |
| book_price | Integer | 图书价格 |
| book_location | String | 图书位置 |
| book_total_copies | Long | 图书总数 |
| book_available_copies | Long | 图书在馆数 |

#### 4.2.2 读者信息表 (reader_information)
| 字段名 | 类型 | 描述 |
|--------|------|------|
| reader_id | String | 读者ID（主键） |
| reader_name | String | 读者姓名 |
| reader_card_type | String | 证件类型 |
| reader_card_number | String | 证件号 |
| reader_phone_number | String | 电话号码 |
| register_date | Date | 注册日期 |
| reader_status | Integer | 读者状态（0-正常，1-挂失，2-注销） |
| total_borrow_number | Integer | 最大借书数 |
| now_borrow_number | Integer | 当前借书数 |

#### 4.2.3 借阅记录表 (borrow_table)
| 字段名 | 类型 | 描述 |
|--------|------|------|
| borrow_id | Long | 借阅ID（主键） |
| book_id | String | 图书ID |
| reader_id | String | 读者ID |
| borrow_date | Date | 借阅日期 |
| due_date | Date | 应还日期 |
| borrow_states | Integer | 借阅状态（0-在借，1-归还，2-逾期） |
| borrow_count | Long | 借阅次数 |

#### 4.2.4 归还记录表 (return_table)
| 字段名 | 类型 | 描述 |
|--------|------|------|
| return_id | Long | 归还ID（主键） |
| borrow_id | Long | 借阅ID |
| return_date | Date | 归还日期 |
| over_days | Integer | 超期天数 |
| fine | Double | 罚款金额 |

### 4.3 API 接口设计

#### 4.3.1 认证接口
1. **用户登录**
   - URL: POST `/api/auth/login`
   - 请求参数: `{ username: string, password: string }`
   - 响应: `{ code: 0, message: 'ok', data: { token: 'xxx', user: { id, name, role } } }`

2. **用户登出**
   - URL: POST `/api/auth/logout`
   - 响应: `{ code: 0, message: 'ok' }`

#### 4.3.2 图书管理接口
1. **获取图书列表**
   - URL: GET `/api/books`
   - 查询参数: `page, size, search, author, category`
   - 响应: `{ code: 0, data: { items: [...], total: 123 } }`

2. **获取图书详情**
   - URL: GET `/api/books/{id}`
   - 响应: `{ code: 0, data: {...} }`

3. **创建图书**
   - URL: POST `/api/books`
   - 请求体: 图书信息对象
   - 响应: `{ code: 0, data: {...} }`

4. **更新图书**
   - URL: PUT `/api/books/{id}`
   - 请求体: 图书信息对象
   - 响应: `{ code: 0, data: {...} }`

5. **删除图书**
   - URL: DELETE `/api/books/{id}`
   - 响应: `{ code: 0, message: 'ok' }`

6. **调整图书库存**
   - URL: PATCH `/api/books/{id}/stock`
   - 请求体: `{ adjustment: number }`
   - 响应: `{ code: 0, data: {...} }`

#### 4.3.3 读者管理接口
1. **获取读者列表**
   - URL: GET `/api/readers`
   - 查询参数: `page, size, search`
   - 响应: `{ code: 0, data: { items: [...], total: 123 } }`

2. **获取读者详情**
   - URL: GET `/api/readers/{id}`
   - 响应: `{ code: 0, data: {...} }`

3. **根据证件号获取读者**
   - URL: GET `/api/readers/byCard/{cardNumber}`
   - 响应: `{ code: 0, data: {...} }`

4. **创建读者**
   - URL: POST `/api/readers`
   - 请求体: 读者信息对象
   - 响应: `{ code: 0, data: {...} }`

5. **更新读者**
   - URL: PUT `/api/readers/{id}`
   - 请求体: 读者信息对象
   - 响应: `{ code: 0, data: {...} }`

6. **删除读者**
   - URL: DELETE `/api/readers/{id}`
   - 响应: `{ code: 0, message: 'ok' }`

#### 4.3.4 借阅管理接口
1. **创建借阅记录**
   - URL: POST `/api/borrow`
   - 请求体: `{ readerId, books: [{bookId, count}], borrowDate, dueDate }`
   - 响应: `{ code: 0, data: { borrowId, receipt: {...} } }`

2. **归还图书**
   - URL: POST `/api/return`
   - 请求体: `{ borrowIds: [...], returnDate }`
   - 响应: `{ code: 0, data: { returned: [...], fines: [...] } }`

3. **获取借阅记录**
   - URL: GET `/api/borrow`
   - 查询参数: `readerId, status`
   - 响应: `{ code: 0, data: { items: [...] } }`

#### 4.3.5 统计接口
1. **获取系统概览**
   - URL: GET `/api/statistics/overview`
   - 响应: `{ code: 0, data: { totalBooks, totalReaders, borrowedCount, todayBorrows, todayReturns, overdueCount } }`

2. **获取热门图书**
   - URL: GET `/api/statistics/popular-books`
   - 查询参数: `top` (可选，默认10)
   - 响应: `{ code: 0, data: [...] }`

3. **获取逾期图书**
   - URL: GET `/api/statistics/overdue-books`
   - 查询参数: `page, size` (可选)
   - 响应: `{ code: 0, data: { items: [...], total: 123 } }`

4. **获取空闲图书**
   - URL: GET `/api/statistics/vacant-books`
   - 查询参数: `page, size` (可选)
   - 响应: `{ code: 0, data: { items: [...], total: 123 } }`

5. **获取读者借阅详情**
   - URL: GET `/api/statistics/borrow-details/{readerId}`
   - 响应: `{ code: 0, data: { items: [...] } }`

### 4.4 后端需实现的关键业务逻辑

#### 4.4.1 借书业务逻辑
1. 验证读者身份和状态
2. 检查读者是否达到借书上限
3. 验证图书库存是否充足
4. 创建借阅记录
5. 更新图书在馆数
6. 更新读者当前借书数
7. 生成借阅凭证

#### 4.4.2 还书业务逻辑
1. 验证借阅记录有效性
2. 计算是否逾期及罚款金额
3. 更新借阅状态为已还
4. 更新图书在馆数
5. 更新读者当前借书数
6. 生成归还记录

#### 4.4.3 数据一致性保障
1. 使用数据库事务确保操作原子性
2. 并发控制防止超借情况
3. 异常处理和回滚机制

### 4.5 安全要求
1. **身份认证**
   - JWT Token 认证机制
   - Token 过期和刷新机制

2. **权限控制**
   - 基于角色的访问控制(RBAC)
   - 接口级权限验证

3. **数据安全**
   - 敏感信息加密存储
   - SQL注入防护
   - XSS攻击防护

### 4.6 性能要求
1. **响应时间**
   - 简单查询操作：< 2秒
   - 复杂统计操作：< 5秒
   - 核心交易操作：< 3秒

2. **并发支持**
   - 支持至少10-20名管理员同时操作
   - 借还高峰期支持每分钟50次以上请求

3. **可扩展性**
   - 支持水平或垂直扩展
   - 数据库读写分离支持

## 5. 部署要求

### 5.1 环境要求
- Java 8+ 运行环境
- MySQL 5.7+ 或 PostgreSQL 10+
- Nginx (用于前端静态资源托管)
- Redis (可选，用于缓存)

### 5.2 部署架构建议
```
客户端 -> Nginx -> 前端静态资源
              -> 后端API服务 -> 数据库
                            -> 缓存(可选)
```

### 5.3 监控和日志
- 应用日志记录
- 性能监控
- 错误追踪
- 数据库慢查询日志

## 6. 开发计划建议

### 6.1 第一阶段 - 核心功能
1. 后端基础框架搭建
2. 数据库设计与初始化
3. 认证授权模块
4. 图书管理模块
5. 读者管理模块

### 6.2 第二阶段 - 业务流程
1. 借阅管理模块
2. 归还管理模块
3. 统计模块
4. 前后端联调

### 6.3 第三阶段 - 优化完善
1. 性能优化
2. 安全加固
3. 日志监控
4. 测试覆盖
5. 文档完善
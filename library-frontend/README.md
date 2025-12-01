# Library Circulation System 前端详尽设计文档

本文档为图书借还系统（Library Circulation System）前端设计说明，面向前端开发与维护人员。文档在之前总体方案基础上，进一步细化到模块级别（登录、仪表盘、图书管理、读者管理、借书、还书、统计等），并给出页面布局、需要展示的信息、用到的组件、API 调用示例、字段与表格列设计、校验规则、Pinia 状态结构、路由表与交互流程等。

说明：文档参照 `resources/library.md`、ER 图、数据流图 与 后端接口约定；若后端接口发生变更，请同步更新此处说明。

目录：
- 0. 总体约定
- 1. 路由与页面映射
- 2. 组件规范与通用行为
- 3. 各功能模块详细设计（逐页）
  - 3.1 登录模块（Login.vue）
  - 3.2 仪表盘（Dashboard.vue）
  - 3.3 图书管理（BookList.vue）
  - 3.4 读者管理（ReaderList.vue）
  - 3.5 借书（BorrowPage.vue）
  - 3.6 还书（ReturnPage.vue）
  - 3.7 统计查询（Statistics.vue）
- 4. 核心业务流程（借书/还书/查询）
- 5. API 及数据格式（示例）
- 6. Pinia 状态设计
- 7. 表单校验与 UX 细节
- 8. 布局与响应式策略（Layout.vue）
- 9. 安全与权限设计
- 10. 性能与缓存优化建议
- 11. 测试与部署建议
- 12. 附录：字段与表格列模板

---

0. 总体约定
- UI 库：Element Plus
- 路由：Vue Router
- 状态：Pinia
- HTTP：Axios（统一封装在 `src/api/request.js`）
- 时间格式：YYYY-MM-DD HH:mm:ss
- API 返回约定：{ code: number, message: string, data: any }

1. 路由与页面映射
建议路由表（`src/router/index.js`）：
- /login -> Login.vue
- / -> Dashboard.vue (需登录)
- /books -> BookList.vue
- /books/:id -> BookDetail.vue (可选)
- /readers -> ReaderList.vue
- /borrow -> BorrowPage.vue
- /return -> ReturnPage.vue
- /statistics -> Statistics.vue
- /settings -> Settings.vue (可选)

路由守卫：全局守卫验证 `stores/auth.isLoggedIn`，未登录重定向到 `/login`。

2. 组件规范与通用行为
- 命名：组件以 PascalCase 命名，例如 `BookCard.vue`、`DataTable.vue`。
- 事件/Props 约定：
  - DataTable：props { columns, data, total, page, pageSize, loading } events { 'page-change', 'sort-change', 'selection-change', 'action' }
  - SearchForm：props { schema, value } events { 'search' }
- 通用对话框：`ConfirmDialog`（用于删除/重大操作），`FormDialog`（用于新增/编辑）。
- 通用通知：使用 Element Plus 的 `ElMessage` / `ElNotification`，统一在 API 捕获错误后调用。

3. 各功能模块详细设计

3.1 登录模块（Login.vue）
- 目标：用户凭用户名/密码登录并获得 token
- 页面布局：表单居中，宽度 380px，包含 logo、用户名输入、密码输入、登录按钮、忘记密码链接。
- 展示字段：用户名、密码、记住我（checkbox，可选）
- 组件：ElForm、ElFormItem、ElInput、ElCheckbox、ElButton、ElLink
- 表单字段与校验规则：
  - username: required, min 3, max 50
  - password: required, min 6
- API：POST `/api/auth/login` 请求体 { username, password }
- 成功：保存 token 与用户信息到 `stores/auth`（localStorage + Pinia），跳转到 `/`。
- 失败：展示 `ElMessage.error(message)`；登录按钮在请求期间显示 loading 并禁用。

3.2 仪表盘（Dashboard.vue）
- 目标：展示关键统计与近期活动
- 页面布局：顶部为欢迎与 KPI 卡片（网格3~4列，使用 `ElRow`/`ElCol`），下方分两列：左侧热门图书/图表，右侧最近借阅/逾期列表。
- 展示项（每项为卡片）：
  - 总图书数量（totalBooks）
  - 总读者数量（totalReaders）
  - 当前在借图书数量（borrowedCount）
  - 今日借书次数（todayBorrows）
  - 今日还书次数（todayReturns）
  - 逾期未还图书数量（overdueCount）
- 组件：ElCard、ElRow、ElCol、BookCard（热门图书）、ECharts（借阅趋势折线图）、DataTable（最近借阅）
- API：GET `/api/statistics/overview`、GET `/api/statistics/popular-books?top=10`、GET `/api/borrow/recent?limit=10`
- 交互：点击热门图书跳转到 `/books` 并预填搜索关键字

3.3 图书管理（BookList.vue）
- 目标：图书 CRUD、搜索、分页、排序
- 页面布局：顶栏 SearchForm + 操作按钮（新增图书），下方为 DataTable
- 展示字段（表格列）：
  - checkbox（多选）
  - cover (缩略图)
  - title
  - author
  - publisher
  - publishDate (YYYY-MM-DD)
  - category
  - location
  - totalCopies
  - availableCopies
  - actions (查看/编辑/删除/库存调整)
- 组件：SearchForm、DataTable、ElButton、ElDialog（编辑/新增）、ElUpload（导入封面或批量导入CSV，可选）
- API：
  - GET `/api/books?page=&size=&search=&author=&category=` -> {code,data:{items,total}} 
  - POST `/api/books` -> {code,message,data}
  - PUT `/api/books/:id`
  - DELETE `/api/books/:id`
  - PATCH `/api/books/:id/stock` (调整在馆数)
- 表单字段与校验（新增/编辑）：title(required), author(required), isbn(format), publisher, publishDate(date), category, location, totalCopies(number >= 0)
- 操作 UX：删除需 ConfirmDialog；编辑在弹窗中进行；新增成功后列表刷新并提示。

3.4 读者管理（ReaderList.vue）
- 目标：读者信息管理
- 页面布局：顶部 SearchForm + 新增按钮，下方 DataTable
- 表格列：checkbox, name, idType, idNumber, phone, registerDate, status (active/forbidden), borrowLimit, currentBorrowCount, actions(edit/delete)
- 组件：SearchForm、DataTable、ElDialog（表单）、ReaderCard（详情弹窗）
- API：GET/POST/PUT/DELETE `/api/readers` 等
- 表单校验：name required, idNumber required + regex, phone regex, borrowLimit integer >=0
- 特殊操作：冻结/恢复用户会通过 PATCH `/api/readers/:id/status`，并在 UI 中即时反映状态

3.5 借书（BorrowPage.vue）
- 目标：办理借书手续并生成借阅凭证
- 流程与页面布局：三步式（Step1 选择读者 -> Step2 选择图书 -> Step3 确认并提交）
  - 左侧或顶部显示步骤进度条（ElSteps）
  - 中央为当前步骤表单/列表
  - 右侧为借书篮（selectedBooks）概要，实时展示待借图书
- Step1：选择/验证读者
  - 输入卡号/证件号，调用 GET `/api/readers/byCard/:cardNumber`，展示读者信息（姓名、状态、当前借书数、借书上限）
  - 若读者不存在或被禁用，显示错误并阻止下一步
- Step2：搜索并选择图书
  - 使用 SearchForm 搜索，DataTable 列出结果，操作列提供“加入借书篮”按钮（若 availableCopies>0）
  - 当加入时更新 `stores/borrow.selectedBooks`，检查重复与数量
- Step3：确认并提交
  - 显示读者与图书信息摘要（借出日期默认今日，应还日期 = 今日 + N 天，N 可配置），显示总册数
  - 提交 POST `/api/borrow` 请求体 { readerId, books: [{bookId, copyCount}], borrowDate, dueDate }
  - 成功后返回 borrowId 与凭证信息，展示成功页面/弹窗并提供打印按钮
- 组件：SearchForm、DataTable、ElSteps、ElForm、ElButton、PrintComponent（凭证打印，可用 window.print 或打印库）
- 校验：读者借书是否已达上限、图书 availability re-check（提交前再次查询后端确保库存）

3.6 还书（ReturnPage.vue）
- 目标：处理归还，计算逾期与罚款
- 流程与页面布局：输入读者 -> 列出在借记录 -> 选择需要归还的条目 -> 确认归还
- 页面字段：借阅记录表格（借阅编号、书名、借出日期、应还日期、逾期天数、应罚款、操作复选框）
- API：
  - GET `/api/borrow?readerId=...&status=borrowed` 获取在借记录
  - POST `/api/return` 请求体 { borrowIds: [..], returnDate }，后端计算逾期与罚金并返回结果
- 提交后：更新 Book 在馆数、Borrow 记录状态到 returned、显示归还收据（包含罚款金额）
- 组件：SearchForm、DataTable、ElForm、ConfirmDialog、ElButton

3.7 统计查询（Statistics.vue）
- 目标：按维度查看统计数据与导出
- 页面布局：顶部为时间筛选器（日期范围），下方为 Tab（热门图书 / 逾期清单 / 在馆图书 / 读者借阅详情）
- 热门图书：ECharts 柱状图或饼图 + DataTable 明细
- 逾期清单/在馆图书：DataTable（支持导出 CSV）
- 读者借阅详情：输入读者ID -> GET `/api/statistics/borrow-details/:readerId` -> 展示图表 + 列表
- 组件：ElTabs、ECharts、DataTable、ElDatePicker、ElButton

4. 核心业务流程

4.1 借书流程（前端关注点）
- 用户（管理员）登录并进入借书页面
- 校验读者身份与状态（GET `/api/readers/byCard/:card`）
- 选书并加入借书篮（本地 Pinia 存储）
- 提交借书请求：前端发送 POST `/api/borrow`，并在请求前对选中的每本书再次请求库存确认（防并发）
- 成功：展示凭证并刷新 Dashboard/BookList/ReaderList 的相关数据（可通过事件或 store 触发刷新）

4.2 还书流程（前端关注点）
- 输入读者标识，获取在借记录
- 选择归还项并提交 POST `/api/return`，随后展示逾期罚款信息与收据
- 成功：刷新相关视图与数据

4.3 图书/读者查询流程
- 在 BookList/ReaderList 中使用 SearchForm 发起带分页的查询请求，DataTable 支持排序和多列过滤，分页控件触发后端分页查询

5. API 及数据格式（示例）
- 登录：
  - 请求：POST /api/auth/login
    - body: { username: string, password: string }
  - 成功响应：{ code: 0, message: 'ok', data: { token: 'xxx', user: { id, name, role } } }
- 获取书籍列表：
  - GET /api/books?page=1&size=20&search=java
  - 响应：{ code: 0, data: { items: [ { id, title, author, publisher, publishDate, category, location, totalCopies, availableCopies, isbn } ], total: 123 } }
- 借书：
  - POST /api/borrow
    - body: { readerId: number, books: [{ bookId: number, count: number }], borrowDate: 'YYYY-MM-DD', dueDate: 'YYYY-MM-DD' }
  - 响应：{ code:0, data: { borrowId: 1001, receipt: { borrowId, borrowDate, dueDate, items: [...] } } }
- 还书：
  - POST /api/return
    - body: { borrowIds: [1001,1002], returnDate: 'YYYY-MM-DD' }
  - 响应：{ code:0, data: { returned:[...], fines: [{ borrowId, amount }] } }

5.1 错误响应约定
- 错误示例：{ code: 1001, message: '读者已达到最大借书数' }
- 前端策略：根据 code 显示适当提示；对于需要用户干预的错误，弹出对话框并提供下一步建议。

6. Pinia 状态设计（建议）
- stores/auth.js
  - state: { user: null, token: null, isLoggedIn: false }
  - actions: login(credentials), logout(), loadFromStorage()
- stores/borrow.js
  - state: { selectedReader: null, selectedBooks: [], step: 1 }
  - getters: selectedCount, totalBooks
  - actions: addBook(book), removeBook(bookId), clearSelection(), setReader(reader)
- stores/ui.js
  - state: { sidebarCollapsed: false, theme: 'light' }

7. 表单校验与 UX 细节
- 关键校验规则：ISBN 格式（10 或 13 位数字/含连字符）、证件号正则、手机号正则、日期合法性
- 提示语：所有失败提示使用简单明确的中文消息，并在表单字段下方或弹窗显示
- Loading/防抖：搜索框使用 300ms 防抖；提交按钮在请求中禁用并显示 loading
- 幂等与冲突处理：对于可能出现并发改动的操作（库存、借阅），在提交前再次拉取最新数据或依赖后端事务并处理 409 冲突

8. 布局与响应式策略（Layout.vue）
- 结构：
  - Header（顶部）：logo、系统名、全局搜索（可选）、用户下拉（个人资料、登出）
  - Sidebar（左侧）：功能导航（可折叠）
  - Main（右侧主区）：面包屑 + 路由渲染区
  - Footer（底部，任选）：版权、版本信息
- 响应式：小于 768px 时侧边栏自动隐藏，主菜单通过汉堡按钮展示，表格在窄屏下采用横向滚动或卡片式呈现

9. 安全与权限设计
- 登录采用 JWT，token 存 localStorage 并在 Axios 拦截器中加入 Authorization header
- 路由守卫校验登录并可根据 `user.role` 控制菜单/操作按钮可见性
- 敏感数据显示脱敏（读者证件号中间用星号隐藏），在详情页可按权限显示完整信息
- 前端仅做展示与初步校验，所有关键权限必须由后端校验

10. 性能与缓存优化建议
- 使用 Vite 的动态导入分割路由页面（按需加载）
- DataTable 使用服务端分页与服务器端排序
- 静态资源启用长缓存策略（带 hash 的静态文件）
- 对热门统计、图表类数据做短时缓存（如 1-5 分钟）以减少压力

11. 测试与部署建议
- 单元测试：对 Pinia store、API wrapper、工具函数编写 Jest 单元测试
- 集成/E2E：使用 Playwright 或 Cypress 测试关键业务流程（借书/还书/登录）
- CI/CD：建议 GitHub Actions 执行 lint -> test -> build -> deploy（到 staging/production）
- 生产用 Nginx 做静态资源托管并配置 HTTPS

12. 附录：字段与表格列模板（示例）
- Book 列模板（DataTable columns）
  - [{ prop: 'cover', label:'封面', width:80, render: slot }, { prop:'title', label:'书名' }, { prop:'author', label:'作者' }, { prop:'publisher', label:'出版社' }, { prop:'publishDate', label:'出版日期' }, { prop:'category', label:'分类' }, { prop:'location', label:'位置' }, { prop:'availableCopies', label:'在馆数' }, { prop:'actions', label:'操作', width:200 }]
- Reader 列模板
  - [{ prop:'name', label:'姓名' }, { prop:'idType', label:'证件类型' }, { prop:'idNumber', label:'证件号' }, { prop:'phone', label:'电话' }, { prop:'registerDate', label:'注册日期' }, { prop:'status', label:'状态' }, { prop:'borrowLimit', label:'可借书数' }, { prop:'actions', label:'操作' }]

结束语

此文档在模块级别提供了详细的前端实现方案：页面布局、每页需要展示的信息、具体用到的组件、API 调用示例、表格与表单字段、校验、Pinia 状态结构、以及安全性与性能优化建议。接下来建议的可执行任务：
- 在 `library-frontend/src` 根据此文档生成页面与组件骨架（路由、stores、api 封装、页面 layout、基础样式）
- 优先实现 Login、BookList、BorrowPage、ReturnPage 的完整交互（这四个为核心流程）

如果你希望我继续，我可以：
- 自动在 `library-frontend/src` 下生成页面与组件骨架文件（含基础模板、路由注册、stores、api 模块），或
- 仅生成某个模块的示例实现（例如完整的 `BorrowPage.vue` + `stores/borrow.js` + `api/borrow.js`），请告知优先项。

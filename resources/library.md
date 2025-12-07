

# 第1讲图书借还书数据库系统之需求分析
## 1. 需求分析
### 1.1 需求分析任务
调查应用领域，对各种应用的信息要求和操作要求进行详细分析，形成需求分析说明书。
### 1.2 任务描述
本项目旨在开发一个现代化、高效、可靠的图书借还数据库系统，以取代或升级传统的手工或半自动化管理方式。
系统需要覆盖图书馆日常运营的核心业务，即对"图书"、"读者"和"借阅关系"三大核心实体进行信息化管理。
系统需要提供一个稳定、易用的操作界面，确保数据的一致性和完整性，并能高效地处理日常的借还书业务。
#### 1.2.1 系统目标
（1）**实用性原则**：快速响应用户查询需求，提供更全面、便捷的信息服务，提升读者满意度。
（2）**可靠性原则**：系统稳定可用，并且保证读者信息不被泄露。
（3）**友好性原则**：系统操作上要求简单、方便、快捷，便于用户使用。
（4）**可扩展性原则**：系统设计应具备良好的可扩展性，便于未来增加新功能（如在线预约、罚款管理、多终端支持等）。
#### 1.2.2 系统的功能需求
（1）要求能够对图书资料进行管理，如登记新书，删除不存在的书目，对已经变更的图书信息进行修改，还可以根据多种条件从数据库中查询书目的详细信息。
（2）要求能对新读者信息进行登记，对已经变更的读者信息进行修改，对不再借阅的读者信息进行删除。还可以查询读者的详细信息，以及读者借阅过的书目和正在借阅的书目。
（3）提供借阅登记表和返还登记表来管理借阅，并且提供查询借阅次数最高的前10个书目。
#### 1.2.3 系统的性能需求
性能需求定义了系统运行时应达到的质量标准。
（1）**响应时间**：
- 对于简单的查询操作（如按ID查询图书/读者），系统响应时间应在2秒以内。
- 对于复杂的组合查询或统计操作（如热门图书排行），系统响应时间应在5秒以内。
- 借书、还书等核心交易操作，响应时间应在3秒以内完成。
（2）**并发性能**：
- 系统应能支持至少10-20名图书管理员同时进行操作而不出现明显的性能下降或数据冲突。
- 在借还高峰期，系统应能稳定处理每分钟50次以上的借还操作请求。
（3）**数据容量与可扩展性**：
- 数据库设计应能支持存储至少10万册图书信息和1万名读者信息。
- 历史借阅记录应能长期保存，至少支持百万级别的记录存储。
- 系统架构应具备水平或垂直扩展的能力，以应对未来数据量和用户量的增长。
（4）**可靠性与可用性**：
- 系统应保证99.9%的正常运行时间，核心业务中断时间全年不超过8.76小时。
- 数据库需要定期备份，支持在发生灾难（如硬件故障、数据损坏）后1小时内恢复核心服务。
- 关键操作（如借书、还书）应具备事务性，确保数据的一致性，避免出现"书借了但没记录"或"还了但状态未更新"的情况。
（5）**安全性**：
- **用户认证**：管理员必须通过用户名和密码登录系统才能进行操作。
- **权限控制**：不同角色的用户（如超级管理员、普通管理员）应具有不同的操作权限。
- **数据安全**：敏感数据（如读者证件号）在存储时应考虑加密或脱敏处理，防止数据泄露。
（6）**易用性**：
- 系统界面应简洁、直观，符合管理员操作习惯。
- 关键操作应有明确的提示和确认机制，防止误操作。
- 提供必要的帮助文档或操作指引。
### 1.3 需求分析
#### 1.3.1 数据字典
##### （1）数据项的描述
| 数据项 | 含义说明 | 类型 | 长度 | 约束 |
|-------|---------|------|------|------|
| bookId | 记录图书的id号 | String | 20 | 唯一，主键 |
| isbn | 国际标准书号 | String | 13 | 非空 |
| bookName | 书名 | String | 255 | 非空 |
| bookAuthor | 作者 | String | 100 | 非空 |
| bookPublisher | 出版社 | String | 100 | 可为空 |
| bookPubDate | 出版日期 | 日期 | - | 格式YYYY-MM-DD，可为空 |
| bookCategory | 图书分类 | String | 50 | 可为空（"文学"） |
| bookPrice | 图书价格 | int | - | 非空 |
| bookLocation | 图书位置 | String | 50 | 非空 |
| bookTotalCopies | 图书总数 | long long | - | 大于等于1 |
| bookAvailableCopies | 图书在馆数 | long long | - | 大于等于1，小于等于总数 |
| readerId | 读者id | String | 20 | 唯一，主键 |
| readerName | 读者姓名 | String | 50 | 非空 |
| readerCardType | 读者证件类型 | String | 20 | 非空 |
| readerCardNumber | 读者证件号 | String | 30 | 唯一，非空 |
| readerPhoneNumber | 读者电话 | String | 20 | 可为空 |
| registerDate | 注册日期 | 日期 | - | 非空 |
| readerStatus | 读者账户状态 | int | - | 0，1，2（正常、挂失、注销） |
| totalBorrowNumber | 最大借书数目 | int | - | 非空 |
| nowBorrowNumber | 当前借书数目 | int | - | 非空 |
| borrowId | 借阅id | long long | - | 主键 |
| borrowDate | 借阅日期 | 日期 | - | 非空 |
| dueDate | 应还日期 | 日期 | - | 非空 |
| returnDate | 归还日期 | 日期 | - | 非空 |
| borrowStates | 借阅状态 | int | - | 0，1，2（在借，归还，逾期） |
| borrowCount | 借阅次数 | long long | - | 非空 |
| returnId | 归还id | long long | - | 非空 |
| overDays | 超期天数 | int | - | 非空 |
| fine | 罚款金额 | int | - | 非空 |
##### （2）数据结构的描述
| 数据结构 | 含义说明 | 组成 |
|---------|---------|------|
| bookInformation | 图书信息 | bookId、bookName、bookAuthor、bookPublisher、bookPubDate、bookCategory、bookLocation、bookTotalCopies、bookAvailableCopies、borrowCount |
| readerInformation | 读者信息 | readerId、readerName、readerCardType、readerCardNumber、readerPhoneNumber、registerDate、readerStatus、totalBorrowNumber、nowBorrowNumber |
| borrowTable | 借阅登记表 | borrowId、bookId、readerId、borrowDate、dueDate、borrowStates |
| returnTable | 归还登记表 | returnId、borrowId、returnDate、overDays、fine |
##### （3）数据流的描述
| 数据流编号 | 数据流名称 | 简述 | 数据流来源 | 数据流去向 | 数据流组成 | 数据流量 | 高峰流量 |
|-----------|-----------|------|-----------|-----------|-----------|---------|---------|
| F1 | 图书查询结果 | 供用户查询图书信息，选择借阅图书 | 图书管理模块 | 读者/工作人员 | 图书编号,图书标题,作者,出版社,出版日期,馆藏位置,借阅状态 | 200/天 | 1000/天 |
| F2 | 借阅凭证 | 借书凭证 | 借阅管理模块 | 读者 | 读者姓名,图书标题,借阅日期,应还日期,借阅编号 | 150/天 | 800/天 |
| F3 | 借阅登记表 | 登记借书读者的信息及图书信息 | 借阅管理模块 | 读者,系统数据库 | 读者编号,读者姓名,图书编号,图书标题,借阅日期,应还日期 | 150/天 | 800/天 |
| F4 | 归还登记表 | 读者还书时登记的归还信息 | 归还管理模块 | 系统数据库 | 借阅编号,图书编号,实际归还日期,超期天数,罚款金额 | 150/天 | 800/天 |
| F5 | 读者信息表 | 供工作人员查询和修改读者信息 | 读者管理模块 | 工作人员 | 读者编号,读者姓名,身份证号,联系方式,读者类型,借阅数量 | 50/天 | 200/天 |
| F6 | 热门图书统计表 | 借阅次数最高的前10个书目 | 统计查询模块 | 管理人员 | 图书编号,图书标题,作者,借阅次数 | 10/天 | 50/天 |
| F7 | 新书登记表 | 新书入库登记信息 | 图书管理模块 | 系统数据库 | ISBN号,图书标题,作者,出版社,出版日期,价格,分类编号 | 20/天 | 100/天 |
##### （4）处理逻辑的描述
| 处理逻辑编号 | 处理逻辑名称 | 简述 | 输入的数据流 | 处理描述 | 输出的数据流 | 处理频率 |
|-------------|-------------|------|-------------|---------|-------------|---------|
| P1 | 更新图书状态 | 有借阅或归还操作时，及时更新图书状态 | 借阅登记表/归还登记表 | 对图书借阅状态进行更新操作 | 更新的图书信息 | 300次/天 |
| P2 | 办理借阅手续 | 读者选择图书后办理借阅手续 | 图书查询结果 | 验证读者资格，登记借阅信息 | 借阅凭证、借阅登记表 | 150次/天 |
| P3 | 办理归还手续 | 读者归还图书时办理归还手续 | 借阅凭证 | 检查超期情况，计算罚款，登记归还信息 | 归还登记表 | 150次/天 |
| P4 | 查询热门图书 | 统计借阅次数最高的前10个书目 | 借阅记录 | 从借阅记录中统计图书借阅次数并排序 | 热门图书统计表 | 10次/天 |
| P5 | 管理读者信息 | 登记新读者，修改或删除读者信息 | 读者信息表 | 对读者信息进行增删改操作 | 更新的读者信息 | 50次/天 |
| P6 | 管理图书信息 | 登记新书，修改或删除图书信息 | 新书登记表 | 对图书信息进行增删改操作 | 更新的图书信息 | 20次/天 |
##### （5）数据存储的描述
| 数据存储编号 | 数据存储名称 | 简述 | 数据存储组成 | 关键字 | 相关联的处理 |
|-------------|-------------|------|-------------|-------|-------------|
| S1 | 图书信息数据 | 记录所有图书的基本信息，包括图书状态 | 图书编号,ISBN号,图书标题,作者,出版社,出版日期,价格,馆藏位置 | 图书编号 | P1,P2,P3,P6 |
| S2 | 读者信息数据 | 记录所有读者的基本信息 | 读者编号,读者姓名,身份证号,联系方式,注册日期,最大借阅数量,当前借阅数量 | 读者编号 | P2,P3,P5 |
| S3 | 借阅记录数据 | 记录所有借阅信息 | 借阅编号,读者编号,图书编号,借阅日期,应还日期,借阅状态 | 借阅编号 | P2,P3,P4 |
| S4 | 归还记录数据 | 记录所有归还信息 | 归还编号,借阅编号,实际归还日期,超期天数,罚款金额 | 归还编号 | P3,P4 |
#### 1.3.2 数据流图
**图1.1 图书借还系统数据流图**
---

# 第2讲图书借还书数据库系统之数据库设计
## 1. 概念结构设计
将需求分析得到的用户需求抽象为信息结构即概念模型的过程就是概念结构设计。
根据需求分析形成的数据字典和数据流图，抽象得到的实体有：
- **图书**（图书id、isbn、书名、作者、出版社、出版日期、图书分类、图书位置、图书总数、图书在馆数、借阅次数）
- **读者**（读者id，姓名，证件类型，证件号，电话号码，注册日期，状态，借书数目上限，可借书数目）
- **借阅记录**（借阅id，借阅日期，应还日期，借阅状态）
- **归还记录**（归还id，归还日期，逾期天数，罚款）
实体之间的联系如下：
- 一种图书可以被多个读者借阅，一个读者可以借阅多种图书
- 一位读者可以有多条借阅记录（借过很多次书），但每一条借阅记录只能对应一位读者。
- 一本图书（指一个书目，如《三体》）可以有多条借阅记录（被很多人借过），但每一条借阅记录只能对应一本图书。
- 一条归还记录只能对应一条借阅记录，一条借阅记录对应一条归还记录或未归还。
图书借还数据库系统E-R图如图2.1所示。
**图2.1 图书借还数据库系统E-R图**
## 2. 逻辑结构设计
逻辑设计的任务就是把概念设计阶段设计的E-R图转换为与选用DBMS产品所支持的数据模型相符合的逻辑结构。
图书借还数据库系统的关系模式如下：
- **图书**（图书id、isbn、书名、作者、出版社、出版日期、图书分类、图书位置、图书总数、图书在馆数、借阅次数）
- **读者**（读者id，姓名，证件类型，证件号，电话号码，注册日期，账户状态，借书数目上限，可借书数目）
- **借阅登记表**（借阅id，书id，读者id，借阅日期，应还日期，借阅状态）
- **归还登记表**（归还id，借阅id，归还日期，逾期天数，罚款）
## 3. 物理结构设计
为一个给定的逻辑数据模型选取一个最适合应用环境的物理结构的过程就是数据库的物理设计。
数据库在物理设备上的存储结构与存取方法称为物理结构。
图书借还数据库系统数据库的数据文件、日志文件存放到指定的硬盘上，该硬盘最好不安装操作系统、DBMS等软件，数据库备份文件存放到移动硬盘。
根据处理需求，建立相关索引，主键索引略，如表3.1所示：
**表3.1 索引列表**
| 关系模式 | 索引属性列 | 索引类型 |
|---------|-----------|---------|
| 读者 | readerCardBunber（身份证号） | 唯一索引 |
| 读者 | readerName（姓名） | 普通索引 |
| 图书 | bookCategory（图书分类） | 普通索引 |
| 图书 | bookAuthor（作者） | 普通索引 |
| 图书 | bookName（书名） | 普通索引 |
| 图书 | Isbn（国际标准书号） | 唯一索引 |
| 借阅登记表 | readerId（读者id） | 普通索引（外键索引） |
| 借阅登记表 | bookId（书id） | 普通索引（外键索引） |
| 借阅登记表 | borrowStates（借阅状态） | 普通索引 |
| 归还登记表 | borrowId | 普通索引（外键索引） |
---
# 第3讲图书借还书数据库系统之数据库实施
## 3.1 Ms SQL Server 8.0 概述
### 3.1.1 安装
MySQL提供了多种安装方式，其中MySQL Installer for Windows提供了一个集成的向导，简化了在Windows系统上的安装和配置过程。它可以管理以下核心组件：
- **MySQL Server**: 数据库的核心引擎。
- **MySQL Workbench**: 官方的可视化数据库设计、管理和开发工具。
- **MySQL Shell**: 一个高级的客户端和代码编辑器，支持SQL、JavaScript和Python。
- **MySQL Router**: 一个轻量级中间件，为应用程序提供透明的连接路由和负载均衡。
- **MySQL Connectors**: 包括ODBC、JDBC、.NET等驱动程序，用于支持各种编程语言连接MySQL。
- **示例与文档**: 提供示例数据库和离线帮助文档。
**MySQL 8.0 安装的环境要求：**
- **操作系统**: 支持Windows 10/11、Windows Server 2016/2019/2022及主流Linux发行版（如Ubuntu、Red Hat Enterprise Linux、CentOS等）。
- **依赖软件**:
  - .NET Framework 4.5.2或更高版本（运行MySQL Installer所必需）。
  - Visual C++ Redistributable（通常需要2015、2017、2019或2022版本，MySQL Server运行所依赖）。
- **网络协议**: 主要支持TCP/IP协议进行网络连接，同时在本地也支持共享内存和命名管道协议。
- **硬件要求（配置建议）**:
  - 处理器: 双核2.0 GHz或更高
  - 内存: 4 GB RAM
  - 硬盘: 100 GB可用空间（建议使用SSD固态硬盘）
### 3.1.2 配置
安装完成后，除了使用命令行工具，推荐使用图形化管理工具Navicat进行便捷的配置和管理操作。
#### Navicat for MySQL 简介
Navicat是一套功能强大的数据库管理工具，专为简化MySQL的管理和开发而设计。它提供了直观的图形用户界面，支持连接本地或远程的MySQL服务器，进行数据可视化操作、管理用户权限、执行备份恢复等任务。
#### 连接配置
启动Navicat，点击"连接"按钮，填写连接信息：
- 连接名：自定义名称（如"My_Local_MySQL"）
- 主机名/IP地址：localhost（本地）或服务器IP（远程）
- 端口：默认3306
- 用户名/密码：MySQL的有效凭据
支持SSH隧道、HTTP隧道等安全连接方式。
#### 服务器参数配置
通过Navicat的"服务器监控"功能，可以直观查看服务器的运行状态，如连接数、查询缓存命中率、内存使用情况等关键性能指标。
#### 数据库与表管理
- 创建数据库/表：通过右键菜单或向导界面，无需编写SQL语句即可完成创建。
- 设计表结构：直观地添加、修改、删除字段，设置主键、外键、索引和约束。
- 数据操作：提供类似Excel的网格视图，方便地进行数据的增、删、改、查。
### 3.1.3 管理
#### 服务器连接配置
- 在Navicat中通过"连接"功能注册和管理多个MySQL服务器实例
- 支持本地和远程服务器连接配置
- 可保存连接参数，实现快速连接访问
#### 服务器参数配置
- 通过修改MySQL配置文件my.ini（Windows）或my.cnf（Linux）调整服务器参数
- 使用Navicat的"服务器监控"功能查看和调整运行参数
- 支持字符集、内存分配、连接数等关键参数配置
#### 数据库服务管理
可以使用多种方式管理MySQL数据库服务：
- Navicat服务管理界面：图形化启动、停止、重启MySQL服务
- Windows服务管理器：通过系统服务控制MySQL服务状态
- 命令行工具：使用net start mysql、net stop mysql等命令
- MySQL Notifier：系统托盘工具快速管理服务状态
#### 备份和还原
使用Navicat提供完整的备份还原解决方案：
- 完整备份：备份整个数据库结构和数据
- 部分备份：选择性备份特定表或数据
- 定时备份：设置自动备份计划任务
- 多种格式支持：支持SQL脚本、CSV、Excel等格式
- 一键还原：通过备份文件快速恢复数据
#### 数据库迁移和升级
- 数据迁移工具：使用Navicat的"数据传输"和"数据同步"功能
- 版本升级支持：提供从旧版本MySQL到8.0的平滑迁移方案
- 结构同步：对比和同步不同版本间的数据库结构差异
- 数据验证：确保迁移过程中数据的完整性和一致性
#### 自动化管理
通过自动化工具简化日常管理任务：
- 事件调度器：创建定时执行的SQL任务
- 批处理作业：将多个管理任务组合成工作流
- 任务计划：设置定期执行的维护任务
- 通知机制：任务执行结果自动通知
实现自动化管理的步骤：
1. 定义定期任务：识别可自动化的重复性管理操作
2. 定义定期任务：识别可自动化的重复性管理操作
3. 配置定时计划：设置任务的执行时间和频率
4. 创建维护脚本：编写SQL脚本或使用Navicat的作业设计器
5. 监控执行结果：通过日志和通知机制跟踪任务执行状态
## 3.2 数据库创建
### 3.2.1 创建数据库
定义LibraryDB数据库：
```sql
CREATE DATABASE LibraryDB 
ON (NAME = library_dat,
FILENAME = 'D:\data\library_data.mdf', 
SIZE = 100MB)
LOG ON (NAME = library_log,
FILENAME = 'D:\data\library_log.ldf', 
SIZE = 10MB);
```
### 3.2.2 定义基本表
在LibraryDB数据库上，根据关系模式，定义基本表。
#### （1）图书信息表：存储图书的基本信息和库存状态：
**表3.1 bookInformation（图书信息表）**
| 属性列名 | 属性说明 | 数据类型 | 码 | 备注 |
|---------|---------|---------|---|------|
| bookId | 记录图书的id | VARCHAR(20) | 主码 | |
| bookName | 书名 | VARCHAR(255) | | not null |
| bookAuthor | 作者 | VARCHAR(100) | | Not null |
| bookPublisher | 出版社 | VARCHAR(100) | | |
| bookPubDate | 出版日期 | Date | | 格式YYYY-MM-DD |
| bookCategory | 图书分类 | VARCHAR(50) | | |
| bookLocation | 图书位置 | VARCHAR(50) | | Not null |
| bookTotalCopies | 图书总数 | BIGINT | | CHECK(bookTotalCopies >= 1) |
| bookAvailableCopies | 图书在馆数 | BIGINT | | CHECK(bookAvailableCopies >= 1 AND bookAvailableCopies <= bookTotalCopies) |
| isbn | 国际标准书号 | VARCGAR(13) | | Not null |
| bookPrice | 图书价格 | int | | not null |
| borrowCount | 借阅次数 | BIGINT | | Not null |
#### （2）读者信息表：存储读者的基本信息和借阅权限。
**表3.2 readerInformation（读者表）**
| 属性列名 | 数据类型 | 码 | 备注 |
|---------|---------|---|------|
| readerId | Varchar(20) | 主码 | Id |
| readerName | Varchar(50) | | Not null |
| readerCardType | Varchar(20) | | Not null |
| readerCardNumber | Varchar(30) | | Not null, UNIQUE |
| readerPhoneNumber | Varchar(20) | | |
| registerDate | date | | Not null |
| readerStatus | int | | 0,1,2(正常、挂失、注销) |
| totalBoorowNumber | int | | Not null |
| nowBorrowNumber | Int | | Not null |
#### （3）借阅登记表：记录图书借阅的详细信息。
**表3.3 borrowTable（借阅登记表）**
| 属性列名 | 属性说明 | 数据类型 | 码 | 外码 | 备注 |
|---------|---------|---------|---|------|------|
| borrowId | 借阅Id | BIGINT | 主码 | | |
| bookId | 记录图书的id | Varchar(20) | | 外码 | |
| readerId | 读者Id | Varchar(20) | | 外码 | |
| borrowDate | 借阅日期 | date | | | Not null |
| dueDate | 应还日期 | Date | | | Not null |
| borrowStates | 借阅状态 | int | | | Not null, 0,1,2(正常、挂失、注销) |
#### （4）归还登记表：记录图书归还和罚款信息。
**表3.4 returnTable（归还登记表）**
| 属性列名 | 属性说明 | 数据类型 | 码 | 外码 | 备注 |
|---------|---------|---------|---|------|------|
| returnId | 归还Id | BIGINT | 主码 | | |
| borrowId | 借阅Id | BIGINT | | 外码 | Not null |
| returnDate | 归还日期 | Date | | | Not null |
| overDays | 超期天数 | int | | | Not null |
| fine | 罚款金额 | int | | | Not null |
### 3.2.3 定义视图
#### （1）当前在馆图书（书名、作者、分类）
```sql
Create View VacantBooks(BookName, Author, Location) as
Select bookName, bookAuthor, bookLocation 
From bookInformation 
Where bookAvailableCopies > 0;
```
#### （2）逾期未还清单
```sql
Create View OverdueList(BookName, ReaderName, PhoneNumber, DueDate) as
Select bi.bookName, ri.readerName, ri.readerPhoneNumber, bt.dueDate 
From borrowTable bt
Join bookInformation bi On bt.bookId = bi.bookId
Join readerInformation ri On bt.readerId = ri.readerId 
Left Join returnTable rt On bt.borrowId = rt.borrowId
Where rt.borrowId Is Null And bt.dueDate < Current_Date();
```
#### （3）热门图书排行
```sql
Create View PopularBooksRanking(BookName, Author, BorrowTimes) as
Select bi.bookName, bi.bookAuthor, Count(bt.borrowId) As BorrowTimes 
From borrowTable bt
Join bookInformation bi On bt.bookId = bi.bookId 
Group By bi.bookId
Order By BorrowTimes Desc 
Limit 10;
```
#### （4）读者借阅详情视图
```sql
CREATE VIEW ReaderBorrowDetails AS
SELECT r.readerId, r.readerName, b.bookName, br.borrowDate, br.dueDate, br.borrowStates
FROM readerInformation r
JOIN BorrowTable br ON r.readerId = br.readerId
JOIN BookInformation b ON br.bookId = b.bookId
WHERE br.borrowStates = 0; -- 只显示在借状态
```
### 3.2.4 定义索引
#### 1、图书信息表索引
```sql
（1） CREATE UNIQUE INDEX uk_isbn ON bookInformation (isbn);
（2） CREATE INDEX idx_bookName ON bookInformation (bookName);
（3） CREATE INDEX idx_bookAuthor ON bookInformation (bookAuthor); 
（4） CREATE INDEX idx_bookCategory ON bookInformation (bookCategory);
（5） CREATE INDEX idx_availableCopies ON bookInformation (bookAvailableCopies);
```
#### 2、读者信息表索引
```sql
（1） CREATE UNIQUE INDEX uk_cardNumber ON readerInformation (readerCardNumber);
（2） CREATE INDEX idx_readerName ON readerInformation (readerName);
（3） CREATE INDEX idx_readerStatus ON readerInformation (readerStatus);
```
#### 3、借阅登记表索引
```sql
（1） CREATE INDEX fk_bookId ON borrowTable (bookId);
（2） CREATE INDEX fk_readerId ON borrowTable (readerId);
（3） CREATE INDEX idx_borrowStates ON borrowTable (borrowStates); 
（4） CREATE INDEX idx_dueDate ON borrowTable (dueDate);
（5） CREATE INDEX idx_reader_borrow ON borrowTable (readerId, borrowDate);
（6） CREATE INDEX idx_book_stats ON borrowTable (bookId, borrowDate);
```
#### 4、归还登记表索引
```sql
（1） CREATE INDEX fk_borrowId ON returnTable (borrowId);
（2） CREATE INDEX idx_returnDate ON returnTable (returnDate);
```
## 3.3 数据加载
按照设计的数据库结构，使用Excel组织图书信息、读者信息、借阅登记表、归还登记表数据。
使用MySQL Server的导入数据向导将图书信息、读者信息、借阅登记表、归还登记表加载到LibraryDB数据库中。

--------------------------------

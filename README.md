# 直聘通 · 在线招聘求职平台（仿 BOSS 直聘）

基于 **Spring Cloud Alibaba 微服务 + Vue3 + Element Plus** 的在线招聘求职平台，课程实验项目（题目：BOSS直聘）。

## 一、功能清单

| 功能 | 说明 |
| --- | --- |
| 登录注册 | 求职者 / 招聘者双身份注册，登录后 Token 存 Redis，网关统一校验 |
| 首页展示 | 搜索、热门搜索词、平台特色、热门职位推荐、智能匹配入口 |
| 岗位发布 | 招聘者发布/下线职位，查看自己发布的岗位列表 |
| 职位浏览 | 关键字/城市/学历筛选 + 分页，职位详情页 |
| 简历投递 | 求职者在线维护简历并投递，招聘者查看收到的简历并设置「已查看/邀面试/不合适」 |
| 在线对话面试 | 求职者点击「立即沟通」与招聘官建立会话，双方可收发消息（前端 3 秒轮询） |
| 智能匹配助手 | ① 基于简历技能/城市/薪资/学历对全部在招岗位打分排序；② 自然语言关键词检索并生成推荐话术 |

## 二、技术栈

- **后端**：Spring Boot 2.6.13、Spring Cloud 2021.0.5、Spring Cloud Alibaba 2021.0.5.0
  - Nacos（服务注册发现）、Spring Cloud Gateway（统一入口 + Token 鉴权）、OpenFeign + LoadBalancer（服务间调用）
  - MyBatis-Plus 3.5.2、MySQL 8、Redis（Token 存储）
- **前端**：Vue 3 + Vite 6 + Element Plus + vue-router + axios
- **数据库**：MySQL（4 个独立库：recruit_user / recruit_job / recruit_resume / recruit_im）

## 三、模块与端口

| 模块 | 端口 | 职责 |
| --- | --- | --- |
| recruit-gateway | **18090** | API 网关：路由 + Token 鉴权 + 透传 X-UserId/X-Role |
| service-user | 18101 | 注册、登录、Token、用户信息 |
| service-job | 18102 | 岗位发布、检索、详情、下线 |
| service-resume | 18103 | 简历维护、简历投递、投递状态 |
| service-im | 18104 | 会话、消息、未读数 |
| service-match | 18105 | 智能匹配（Feign 调用岗位/简历服务） |
| recruit-web | **5180** | 前端（Vite，`/api` 代理到网关 18090） |

## 四、启动步骤

### 1. 基础设施（MySQL / Redis / Nacos）

已有环境可直接复用（默认连接：MySQL `127.0.0.1:13307`、Redis `6379`、Nacos `8848`）；
也可用本目录的 compose 单独起一套（端口 13308 / 6380 / 8858，需设置环境变量覆盖默认值）：

```bash
docker compose up -d
```

### 2. 初始化数据库

```bash
mysql -uroot -p123456 --default-character-set=utf8mb4 < sql/init.sql
# 或容器方式：
# Get-Content -Raw sql/init.sql | docker exec -i <mysql容器> mysql -uroot -p123456 --default-character-set=utf8mb4
```

### 3. 启动后端（任选其一）

```bash
# 方式一：命令行
mvn clean package -DskipTests
java -Xms128m -Xmx256m -jar service-user/target/service-user-1.0.0.jar
# 依次启动 service-job / service-resume / service-im / service-match / recruit-gateway

# 方式二：IDEA 依次运行各模块主类
```

> 若 MySQL/Redis/Nacos 地址不同，通过环境变量覆盖：
> `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_USER` / `MYSQL_PASSWORD` / `REDIS_HOST` / `REDIS_PORT` / `NACOS_ADDR`
>
> AI 求职助手需要额外设置 `DASHSCOPE_API_KEY`（阿里百炼 API Key），未设置时该功能自动降级为规则回复：
> ```bash
> # Windows PowerShell
> $env:DASHSCOPE_API_KEY="sk-xxxxxxxx"
> # Linux / macOS
> export DASHSCOPE_API_KEY=sk-xxxxxxxx
> ```

### 4. 启动前端

```bash
cd recruit-web
npm install
npm run dev
```

访问 **http://localhost:5180**

## 五、演示账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 求职者 | `demo` | 123456 | 已有一份简历（Java 方向），可直接体验匹配/投递/沟通 |
| 招聘者 | `hr001` | 123456 | 已发布 10 个岗位，可发布岗位、查看投递、回复消息 |

> 也可以自行注册新账号（选择身份）。

## 六、接口概览（经网关 18090）

| 服务 | 主要接口 |
| --- | --- |
| 用户 | `POST /user/register`、`POST /user/login`、`POST /user/logout`、`GET /user/info`、`GET /user/validate` |
| 岗位 | `GET /job/list`、`GET /job/hot`、`GET /job/{id}`、`POST /job/publish`、`GET /job/my`、`POST /job/offline/{id}` |
| 简历 | `GET /resume`、`POST /resume/save` |
| 投递 | `POST /delivery/send`、`GET /delivery/my`、`GET /delivery/received`、`POST /delivery/status` |
| 沟通 | `POST /im/start`、`GET /im/conversations`、`GET /im/messages`、`POST /im/send`、`POST /im/read` |
| 匹配 | `GET /match/jobs`、`GET /match/assistant?q=` |
| AI 助手 | `POST /match/ai/chat`（多轮咨询）、`POST /match/ai/upload`（附件解析）、`POST /match/ai/rewrite`（附件改写） |

鉴权方式：登录后把 `token` 放入请求头 `Authorization`；网关校验通过后向下游注入 `X-UserId`、`X-Role`。
公开接口：登录注册、岗位列表/热门/详情、智能助手。

### AI 助手附件能力

- 支持格式：`txt / md / markdown / csv / json / log / xml / yml / pdf / docx`，单文件 ≤ 10MB
- `upload`：上传后解析为纯文本返回（超长自动截断到 12000 字，PDF 用 PDFBox、Word 用 POI 解析）
- `rewrite`：把附件正文 + 修改要求交给大模型（qwen-plus）改写，返回「修改稿 + 修改说明」，前端可在线编辑、复制、下载为 `.md`
- 事实约束：原文没有的量化数据一律输出【待补充】，不编造数字；大模型不可用时原样返回正文并提示，不会报错
- 不支持的格式（图片、旧版 `.doc`）会返回明确提示

## 七、目录结构

```
在线招聘/
├── pom.xml                  # 父 POM（统一版本管理）
├── sql/init.sql             # 数据库初始化（4 库 6 表 + 演示数据）
├── docker-compose.yml       # 可选：独立基础设施
├── recruit-common/          # 公共模块：Result/异常/分页插件/Feign 客户端/DTO
├── recruit-gateway/         # 网关（鉴权过滤器）
├── service-user/            # 用户服务
├── service-job/             # 岗位服务
├── service-resume/          # 简历与投递服务
├── service-im/              # 在线沟通服务
├── service-match/           # 智能匹配服务
└── recruit-web/             # 前端（Vue3 + Element Plus）
```

---
version: "1.0"
status: active
updated: "2026-07-10"
name: Recruit Smart Unified Design System
scope: desktop-light-v1
source-of-truth: true
description: "Recruit Smart 的唯一视觉与交互事实来源。适用于招聘后台桌面端，并为候选人端保留共享基础。"
---

# Recruit Smart 统一设计规范

## 1. 文档地位

本文件是 Recruit Smart 前端视觉、组件、交互和验收的唯一事实来源。

规范中的措辞含义：

- **必须**：不可偏离，偏离即视为实现缺陷。
- **应该**：默认遵守；只有明确业务原因时才可偏离，并记录原因。
- **可以**：允许使用，但不是默认方案。

组件库默认样式、页面局部偏好或临时原型不得覆盖本规范。新增颜色、字号、间距、圆角、阴影、布局模式或 AI 交互模式前，必须先更新本文件。

## 2. 设计定位

### 2.1 Design Read

Recruit Smart 是面向 HR、管理员和面试官的高密度招聘工作台，同时为候选人端提供共享视觉基础。产品应呈现克制、可信、快速、可审计的企业软件体验。

设计方向：

- LinkedIn Recruiter 提供招聘信息深度与搜索能力参考。
- Linear 提供紧凑布局、清晰层级和键盘效率参考。
- Apple 提供尺寸精度、状态反馈和留白节奏参考。
- ChatGPT Enterprise 提供有上下文、有来源、需人工确认的 AI 交互参考。

这些产品仅作为设计原则来源，不复制其品牌、图形或界面。

### 2.2 产品原则

1. **工作优先**：每个页面必须支持明确的招聘任务。
2. **紧凑但不拥挤**：密度来自对齐、分组和稳定结构，不来自缩小字体。
3. **单一产品强调色**：Recruit Blue 是唯一产品强调色，语义色仅表示状态与风险。
4. **上下文优先**：详情与 AI 应靠近当前对象，避免失去列表位置。
5. **人工决策权**：AI 可以分析、建议和草拟，不能自动决定录用或改变最终业务状态。
6. **完整状态**：任何可操作组件必须覆盖默认、Hover、Focus、Active、Disabled、Loading、Success 和 Error 中适用的状态。

## 3. 范围与角色体验

### 3.1 一期范围

- 一期必须完成桌面浅色体验。
- `1280px` 是主流程最低完整可用宽度。
- `1440-1600px` 是最佳体验宽度。
- 一期不提供完整暗色主题。
- 一期不要求后台复杂表格在手机上保持完整功能。

### 3.2 共享基础双体验

所有角色共享颜色、字体、基础组件、状态、无障碍和内容规则。

- **HR / 管理员**：紧凑桌面工作台，强调搜索、批量操作、表格和流程状态。
- **面试官**：桌面优先，强调任务聚焦、候选人上下文和结构化评分。
- **候选人**：未来采用更简单、更宽松的任务流；本期只保留 Token 与组件兼容性，不作为移动端验收范围。

## 4. Token 治理

### 4.1 三层结构

所有实现必须使用三层 Token：

1. **Primitive Token**：原始颜色与尺度。
2. **Semantic Token**：表达用途，如 `text-primary`、`surface-selected`。
3. **Component Token**：表达组件角色，如 `button-primary-bg`、`table-row-height`。

业务页面禁止直接使用未登记的颜色、字号、间距、圆角和阴影值。组件需要新值时，必须先补充 Token。

### 4.2 Primitive Color

| Token | 值 | 用途 |
|---|---:|---|
| `blue-500` | `#3183D8` | 品牌、图表、大面积强调 |
| `blue-600` | `#2874C4` | 可访问行动蓝、链接、主按钮 |
| `blue-700` | `#2164AB` | Hover、Active、深色信息文字 |
| `blue-050` | `#EAF4FD` | 选中、信息浅底 |
| `gray-950` | `#15171C` | 主文字 |
| `gray-700` | `#454B57` | 次文字 |
| `gray-600` | `#68707D` | 弱文字 |
| `gray-400` | `#9AA1AD` | 禁用与装饰 |
| `gray-300` | `#CDD4DF` | 强边框 |
| `gray-200` | `#E3E7EE` | 常规边框 |
| `gray-100` | `#F0F2F6` | 静默表面 |
| `gray-050` | `#F8F9FB` | 次级表面 |
| `canvas` | `#F5F7FA` | 页面背景 |
| `white` | `#FFFFFF` | 工作表面 |
| `success-700` | `#126C32` | 成功前景 |
| `success-050` | `#EAF7EE` | 成功浅底 |
| `warning-800` | `#8A4B00` | 警告前景 |
| `warning-050` | `#FFF5DF` | 警告浅底 |
| `danger-700` | `#B42318` | 危险前景 |
| `danger-050` | `#FDECEC` | 危险浅底 |

### 4.3 Semantic Color

| Token | 映射 | 规则 |
|---|---|---|
| `brand-primary` | `blue-500` | 不承载普通尺寸白色文字 |
| `action-primary` | `blue-600` | 主按钮、可点击链接 |
| `action-primary-hover` | `blue-700` | Hover 与 Active |
| `focus-ring` | `blue-600` | 2px 外描边并保留 Offset |
| `surface-canvas` | `canvas` | 页面背景 |
| `surface-primary` | `white` | 工作区、表格、表单 |
| `surface-subtle` | `gray-050` | 工具栏、次级区域 |
| `surface-muted` | `gray-100` | 禁用与静默区域 |
| `surface-selected` | `blue-050` | 选中行与选中导航 |
| `text-primary` | `gray-950` | 标题、正文、选中项文字 |
| `text-secondary` | `gray-700` | 说明与元信息 |
| `text-tertiary` | `gray-600` | 辅助信息，仍满足 AA |
| `text-disabled` | `gray-400` | 仅限禁用与装饰，不承载必要信息 |
| `border-default` | `gray-200` | 常规分隔 |
| `border-strong` | `gray-300` | 控件边框与强调分隔 |
| `status-info` | `blue-700` | 信息状态，与 Recruit Blue 同一色相 |

必须满足：

- 普通文字与背景对比度不低于 `4.5:1`。
- 大文字和非文本控件边界不低于 `3:1`。
- 状态必须同时使用文字或图标，不得只依赖颜色。
- `gray-400` 不得用于时间、备注、说明或 Placeholder 等必要内容。

### 4.4 Typography

字体栈：

```css
font-family:
  Inter,
  "PingFang SC",
  "Microsoft YaHei",
  "Segoe UI",
  system-ui,
  -apple-system,
  sans-serif;
```

| 层级 | 字号 / 行高 | 字重 | 用途 |
|---|---|---:|---|
| `page-title` | `24px / 1.30` | 600 | 页面标题 |
| `section-title` | `18px / 1.35` | 600 | 页面区块 |
| `component-title` | `16px / 1.40` | 600 | 卡片、面板、弹层标题 |
| `body` | `14px / 1.50` | 400 | 默认正文与控件 |
| `body-strong` | `14px / 1.45` | 600 | 重要名称与表格关键列 |
| `caption` | `12px / 1.45` | 400 | 辅助文字 |
| `label` | `12px / 1.35` | 600 | 表头、标签 |
| `metric` | `28px / 1.15` | 600 | 指标 |

规则：

- 可见界面文字不得小于 `12px`。
- 不使用负字距。
- 中文正文不得使用等宽字体。
- 金额、比例、排名和时间列应该使用 `font-variant-numeric: tabular-nums`。
- 字重只使用 `400/500/600/700`，禁止 `650` 等游离值。

### 4.5 Spacing

基础网格为 `4px`。

| Token | 值 |
|---|---:|
| `space-1` | `4px` |
| `space-2` | `8px` |
| `space-3` | `12px` |
| `space-4` | `16px` |
| `space-6` | `24px` |
| `space-8` | `32px` |
| `space-12` | `48px` |

禁止新增 `6px`、`10px`、`14px`、`18px` 等间距值。只有 1px 边框和图标内部几何不受此限制。

### 4.6 Radius

| Token | 值 | 用途 |
|---|---:|---|
| `radius-xs` | `4px` | Checkbox、小型标记 |
| `radius-sm` | `8px` | Button、Input、Card |
| `radius-md` | `12px` | Drawer、Modal、大面板 |
| `radius-pill` | `9999px` | Status、Filter、Segmented Control |

禁止使用 `9px`、`10px`、`11px`、`16px`、`18px` 等未登记圆角。头像可以使用圆形。

### 4.7 Elevation

层级优先顺序：

1. 表面颜色
2. 细边框
3. 间距
4. 阴影

阴影只用于 Dropdown、Popover、Command Palette、Modal 和浮动 AI Composer：

```css
box-shadow: 0 8px 30px rgb(21 23 28 / 10%);
```

页面区块、普通表格和普通卡片不得使用装饰性阴影。

### 4.8 Motion

| Token | 时长 | 用途 |
|---|---:|---|
| `motion-fast` | `120ms` | Hover、Focus |
| `motion-standard` | `180ms` | 控件状态 |
| `motion-panel` | `240ms` | Drawer、Panel |

统一缓动：

```css
cubic-bezier(0.2, 0.8, 0.2, 1)
```

- 只动画 `transform` 与 `opacity`。
- 动效必须表达反馈、层级或状态变化。
- 禁止弹跳、视差和装饰性循环动画。
- 必须支持 `prefers-reduced-motion`。

## 5. 布局与密度

### 5.1 App Shell

| 区域 | 尺寸 |
|---|---:|
| Topbar | `56px` |
| Sidebar | `232px` |
| Collapsed Sidebar | `64px` |
| AI Panel | `400px` |
| Detail Panel | `440px` |
| Content Max Width | `1600px` |

- `>=1440px`：可以持续显示右侧详情或 AI 面板。
- `1280-1439px`：主流程必须可用；右侧面板优先覆盖或按任务临时打开。
- `<1280px`：不作为一期完整桌面验收范围，但不得出现不可恢复的布局破坏。
- 页面边距在 `1280-1439px` 使用 `24px`，在 `>=1440px` 使用 `32px`。

### 5.2 Density

| Token | 值 |
|---|---:|
| `control-compact` | `32px` |
| `control-default` | `36px` |
| `control-large` | `40px` |
| `table-row-compact` | `40px` |
| `table-row-default` | `48px` |
| `table-row-comfortable` | `56px` |

- 工具栏与高频筛选使用紧凑尺寸。
- 创建、编辑和确认表单使用默认尺寸。
- 关键单一步骤允许大号尺寸。
- 未来触控场景必须提供至少 `44px` 命中区域，但不要求桌面视觉控件统一增高到 `44px`。

### 5.3 Card 边界

页面结构默认不是卡片。

Card 仅用于：

- 可独立选择或重复的对象。
- 必须与周围内容形成明确边界的交互单元。
- Pipeline 候选人、独立指标、AI 审批、面试评分等。

页面标题区、普通分区、整张表格和大段表单不得为了“看起来完整”而额外套卡片。

## 6. Icon 与图像

- 图标统一使用 Lucide。
- 默认尺寸 `18px`，小型控件 `16px`，重点图标 `20px`。
- 统一 `stroke-width: 1.75`。
- 同一页面不得混用字符图标、Emoji 和其他图标家族。
- Icon-only Button 必须提供 Tooltip 与可访问名称。
- 图像圆角必须使用 `8px` 或 `12px`，不得混用方角和任意圆角。

## 7. 核心组件契约

### 7.1 Button

尺寸：

- Compact：`32px`
- Default：`36px`
- Large：`40px`

类型：

- **Primary**：`action-primary` 背景，白色文字，Hover 使用 `action-primary-hover`。
- **Secondary**：白色背景，`border-strong`，主文字。
- **Tertiary**：透明背景，Hover 使用 `surface-muted`。
- **Danger**：危险前景或危险填充，只用于真实破坏性操作。

规则：

- 圆角统一 `8px`。
- 文本不得换行。
- Loading 时保持原宽度并阻止重复提交。
- Focus Ring 必须始终可见。
- Disabled 必须同时降低交互感、改变颜色并移除指针事件，不能只降低透明度。
- `scale(0.98)` 可以用于 Active，必须在 Reduced Motion 下关闭。

### 7.2 IconButton

- 使用 `32px` 或 `36px` 正方形。
- 图标居中，尺寸 `18px`。
- 必须有 Tooltip、`aria-label` 和 Focus Ring。
- 危险图标不得仅依赖红色表达。

### 7.3 Input / Search / Select

必须覆盖：

- Default
- Hover
- Focus
- Filled
- Disabled
- Readonly
- Error
- Loading

规则：

- 圆角 `8px`。
- 默认高度 `36px`，紧凑筛选器 `32px`。
- Placeholder 使用 `text-tertiary`，不得使用不满足普通文字对比度的禁用色。
- Error 必须说明如何修复，不能只显示红框。
- Search 应支持一键清空；全局搜索和候选人搜索可以提供最近记录。
- 同一表单区域中的控件宽度与对齐方式必须一致。

### 7.4 Form

- 创建与编辑表单使用顶部标签。
- 紧凑筛选器可以使用行内或固定左侧标签。
- 同一区域不得混排顶部与左侧标签。
- 必填、说明、错误和单位必须有固定位置。
- 提交期间必须阻止重复请求。
- 离开含未保存内容的页面前必须提示。

### 7.5 Data Table

职位、候选人、面试、Offer 和员工列表默认使用 Table。

必须支持：

- Sticky Header
- 排序状态
- 行选择
- 批量操作
- 加载、空、错误和部分数据状态
- 键盘行导航
- 右侧详情预览

规则：

- 表头使用 `12px / 600`。
- 默认行高 `48px`，紧凑行高 `40px`。
- 文本左对齐，数值与日期按列右对齐。
- 比较性数字使用 Tabular Numerals。
- 不得把每个值都包装成 Badge。
- Hover、Selected 和 Keyboard Focus 必须可区分。

### 7.6 Status

- 使用文字加颜色；必要时增加图标。
- Status 使用 Pill，但保持紧凑。
- Success、Warning、Danger 仅表示真实业务状态。
- Warning 与 Danger 不得用作普通装饰强调。
- 等待反馈、阻塞、SLA 超时和审批风险必须区分。

### 7.7 Panel / Drawer / Modal

任务分工：

- **Right Panel**：预览、对象上下文、活动和 AI。
- **Drawer**：不离开当前页面的中等复杂编辑。
- **Modal**：短确认和少量字段。
- **Full Page**：复杂编辑、多步骤 Offer、入职与系统配置。

规则：

- 禁止 Modal 内再打开 Modal。
- Primary Action 位于操作区右侧，Secondary Action 位于左侧。
- Overlay 使用黑色半透明层，不使用玻璃拟态。
- 打开后 Focus 进入容器，关闭后返回触发元素。

### 7.8 Feedback

- 字段错误：字段旁就地展示。
- 页面阻断错误：Banner。
- 非阻断成功：Toast。
- 长加载：与最终结构一致的 Skeleton。
- 空状态：图标、说明与下一步操作。
- 长时间 AI 操作：显示“读取来源、比较要求、生成内容、等待确认”等真实阶段。

### 7.9 Timeline

- 候选人活动使用垂直 Timeline。
- 受控业务流程使用水平 Stepper。
- 每个事件包含 Actor、Action、Timestamp、Source 与 Related Object。
- AI 参与的事件必须标记来源并可审计。

## 8. 页面模式

### 8.1 Dashboard

- 只展示需要决策的指标与任务。
- 优先使用宽面板和列表，不使用装饰性指标卡墙。
- 必须突出待反馈、SLA 风险和下一步行动。

### 8.2 Candidate Explorer

- 顶部为搜索与结构化筛选。
- 主体为结果 Table。
- 右侧预览打开后不得丢失列表位置。
- AI 摘要作为上下文辅助，不取代表格与详情。

### 8.3 Candidate Detail

- 主区展示资料、简历、经历、投递与面试。
- 右侧展示活动、下一步行动和 AI。
- 推进流程、安排面试、发送消息、拒绝和创建 Offer 等主要动作保持稳定位置。

### 8.4 Pipeline

- Table 是精确操作模式。
- Board 是团队协同模式。
- 卡片只显示推进所需的信息，不复制完整候选人资料。

### 8.5 Interview Workspace

- 同屏展示候选人 Brief、结构化 Scorecard 和 AI 问题建议。
- AI 可以建议问题和总结反馈，但必须保留每位面试官原始意见。

## 9. 交互与反馈模型

### 9.1 确认等级

- **低风险**：直接执行，应该提供 Undo。
- **中风险**：确认框展示对象、动作和影响摘要。
- **高风险**：明确对象、不可逆后果、影响范围，并要求二次验证。

高风险包括：

- 删除关键记录
- 拒绝候选人
- 发送或撤销 Offer
- 取消面试
- 将入职数据转换为员工档案
- 由 AI 建议触发的关键业务状态变更

### 9.2 键盘

支持：

- `Ctrl/Cmd + K`：命令菜单
- `/`：聚焦当前页面搜索
- `J/K`：移动列表选择
- `Enter`：打开记录
- `Esc`：关闭 Panel、Drawer 或 Modal
- `A`：在已选中记录时打开上下文 AI

关键操作不得只通过快捷键提供。

## 10. AI 交互

### 10.1 入口

允许四类入口：

1. Global Ask AI
2. Contextual Right Panel
3. Inline AI Action
4. Full AI Workspace

默认入口是上下文右侧面板。

### 10.2 必须展示

- 当前候选人、职位、面试或 Offer 上下文。
- 数据来源与可点击证据。
- 事实、推断、建议和不确定性之间的区别。
- 读取、分析、生成、停止、失败和重试状态。
- 任何关键操作的人工审批步骤。

### 10.3 人工审批

AI 不得直接：

- 改变候选人阶段
- 发送候选人消息
- 安排或取消面试
- 创建、发送或撤销 Offer
- 拒绝候选人
- 转换员工档案

审批 UI 必须展示 Proposed Action、Affected Record、Editable Content 和 Audit Note。

## 11. 内容语言

产品语言应专业、直接、冷静。

推荐：

- `生成候选人摘要`
- `需要 3 位面试官补充反馈`
- `建议进入终面，查看依据`
- `发送前请确认候选人和职位信息`

禁止：

- `AI 认为这是完美候选人`
- `一键智能淘汰`
- `神奇匹配`
- `100% 适合`

错误信息必须告诉用户发生了什么以及如何修复，不展示专业代码或含糊描述。

## 12. 无障碍

一期必须满足 WCAG 2.2 AA：

- 普通文字对比度不低于 `4.5:1`。
- 大文字和非文本控件不低于 `3:1`。
- Focus Ring 始终可见。
- Icon-only Button 具有可访问名称。
- 表格排序状态可被辅助技术读取。
- AI 流式内容不得抢占焦点。
- 表单错误与字段建立程序化关联。
- 状态不得只依赖颜色。
- 动效尊重 Reduced Motion。

## 13. 禁止事项

- 禁止装饰性渐变、发光边框、彩色玻璃拟态和 AI 紫色光晕。
- 禁止所有页面区块自动套 Card。
- 禁止使用 `10px` 可见文字。
- 禁止未登记圆角、间距、颜色和阴影。
- 禁止字符图标、Emoji 与 Lucide 混用。
- 禁止用纯黑分割线。
- 禁止把 AI 分数描述为绝对事实。
- 禁止 AI 未经确认直接修改招聘状态。
- 禁止在日志或界面错误中暴露敏感候选人信息。

## 14. 原型契约

`prototype.html` 必须保持以下入口稳定：

- `?screen=dashboard`
- `?screen=candidates`
- `?screen=candidate`
- `?screen=pipeline`
- `?screen=interview`

原型必须：

- 使用本文件 Token。
- 在 `1600×1000` 完整展示。
- 在 `1280×800` 保持主流程可操作。
- 保留当前导航、角色和业务流程含义。
- 使用 Lucide 图标、统一尺寸和统一描边。
- 不包含游离 Token、`10px` 正文或 `font-weight: 650`。

## 15. Definition of Done

页面完成必须同时满足：

- 对应明确招聘任务。
- 使用登记 Token，不存在局部视觉体系。
- Loading、Empty、Error、Permission、Success 状态完整。
- 键盘、Focus 和 Esc 行为正确。
- AI 输出展示上下文、来源和审批。
- 关键业务操作按风险等级确认。
- `1280px` 主流程可用，`1600px` 页面完整。
- 无不必要 Card、颜色、阴影和动画。
- 所有关键文字、按钮与状态通过 WCAG 2.2 AA 对比度检查。
- 页面在真实企业数据密度下仍可快速扫描。

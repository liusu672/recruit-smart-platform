---
version: beta
name: Recruit Smart Unified Design System
description: "A professional recruiting workspace combining LinkedIn Recruiter's information depth, Linear's restrained productivity layout, Apple's precision and tactile polish, and ChatGPT Enterprise's contextual AI interaction. The product uses a light enterprise canvas, compact operational surfaces, a single Recruit Blue accent, and AI that stays grounded in recruiting data and requires explicit human approval for business decisions."

design-direction:
  professional-depth: "LinkedIn Recruiter"
  layout-discipline: "Linear"
  interaction-polish: "Apple"
  ai-model: "ChatGPT Enterprise"

colors:
  primary: "#3183D8"
  primary-hover: "#2874C4"
  primary-active: "#2164AB"
  primary-focus: "#70ACE8"
  primary-soft: "#EAF4FD"
  on-primary: "#FFFFFF"

  canvas: "#F5F7FA"
  surface: "#FFFFFF"
  surface-subtle: "#F8F9FB"
  surface-muted: "#F0F2F6"
  surface-selected: "#F0F2FF"
  surface-inverse: "#15171C"

  ink: "#15171C"
  ink-muted: "#454B57"
  ink-subtle: "#717784"
  ink-tertiary: "#9AA1AD"
  ink-inverse: "#FFFFFF"

  hairline: "#E3E7EE"
  hairline-strong: "#CDD4DF"
  overlay: "#15171C"

  success: "#16833A"
  success-soft: "#EAF7EE"
  warning: "#A86500"
  warning-soft: "#FFF5DF"
  danger: "#C2413A"
  danger-soft: "#FDECEC"
  info: "#0A66C2"
  info-soft: "#EAF3FC"

typography:
  font-family: "Inter, SF Pro Text, system-ui, -apple-system, Segoe UI, sans-serif"
  display-family: "Inter, SF Pro Display, system-ui, -apple-system, Segoe UI, sans-serif"
  mono-family: "JetBrains Mono, SFMono-Regular, Consolas, monospace"

  page-title:
    fontSize: 28px
    fontWeight: 600
    lineHeight: 1.20
    letterSpacing: -0.4px
  section-title:
    fontSize: 20px
    fontWeight: 600
    lineHeight: 1.30
    letterSpacing: -0.2px
  card-title:
    fontSize: 16px
    fontWeight: 600
    lineHeight: 1.35
  body:
    fontSize: 14px
    fontWeight: 400
    lineHeight: 1.50
  body-strong:
    fontSize: 14px
    fontWeight: 600
    lineHeight: 1.45
  body-large:
    fontSize: 16px
    fontWeight: 400
    lineHeight: 1.50
  caption:
    fontSize: 12px
    fontWeight: 400
    lineHeight: 1.40
  label:
    fontSize: 12px
    fontWeight: 600
    lineHeight: 1.30
  button:
    fontSize: 14px
    fontWeight: 500
    lineHeight: 1.20
  metric:
    fontSize: 30px
    fontWeight: 600
    lineHeight: 1.10
    letterSpacing: -0.6px

spacing:
  unit: 4px
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px
  page: 32px

rounded:
  xs: 4px
  sm: 6px
  md: 8px
  lg: 12px
  xl: 16px
  pill: 9999px
  full: 9999px

layout:
  topbar-height: 56px
  sidebar-width: 232px
  sidebar-collapsed-width: 64px
  ai-panel-width: 400px
  detail-panel-width: 440px
  content-max-width: 1600px
  table-row-height: 48px
  compact-row-height: 40px

motion:
  fast: 120ms
  standard: 180ms
  panel: 240ms
  easing: "cubic-bezier(0.2, 0.8, 0.2, 1)"
  press-scale: 0.98
---

# Recruit Smart Design System

## 1. Design Thesis

Recruit Smart is an operational recruiting product, not a generic admin dashboard and not a decorative AI demo.

The design system combines four influences with clear responsibilities:

- **LinkedIn Recruiter provides professional depth**: powerful search, candidate intelligence, relationship context, pipeline ownership, messaging, saved views, and high information density.
- **Linear provides layout discipline**: compact navigation, clear hierarchy, fast keyboard workflows, restrained surfaces, hairline borders, and minimal visual noise.
- **Apple provides precision**: typography rhythm, consistent spacing, tactile press states, refined empty states, careful icon sizing, and polished transitions.
- **ChatGPT Enterprise provides the AI interaction model**: contextual conversation, grounded evidence, tool execution states, citations, auditability, and explicit human approval.

The result should feel calm under complexity. A recruiter may manage hundreds of candidates, multiple roles, interviews, offers, and AI recommendations without losing orientation.

## 2. Product Principles

### 2.1 Work First

Every screen must support a concrete recruiting action: search, compare, shortlist, schedule, evaluate, communicate, approve, onboard, or audit.

### 2.2 Dense, Not Crowded

Use compact rows and panels, but preserve strong alignment, whitespace between groups, and predictable reading order. Density comes from structure, not smaller text.

### 2.3 One Accent

`Recruit Blue` `{colors.primary}` is the only product accent. Use semantic colors only for status, risk, success, and validation.

### 2.4 AI Is Contextual

AI appears beside the candidate, job, interview, offer, or employee record it is helping with. Avoid isolated chatbot pages unless the user explicitly opens the AI workspace.

### 2.5 Human Decision Authority

AI can summarize, compare, draft, recommend, and flag risks. It cannot automatically reject candidates, issue offers, or change final workflow state.

## 3. Information Architecture

Primary navigation:

1. Dashboard
2. Jobs
3. Candidates
4. Pipeline
5. Interviews
6. Offers
7. Onboarding
8. Employees
9. AI Workspace
10. Reports
11. Administration

Role-based visibility:

- **HR** sees the full recruiting workflow.
- **Interviewers** see assigned interviews, candidate context, and feedback tasks.
- **Candidates** use a simplified portal for profiles, resumes, applications, interviews, offers, and onboarding.
- **Administrators** manage accounts, roles, system data, integrations, and audit logs.

## 4. App Shell

### 4.1 Desktop Layout

Use a three-region productivity shell:

- Left: persistent sidebar, `232px`.
- Top: global command bar, `56px`.
- Center: working canvas.
- Optional right: candidate detail, activity, or AI panel, `400-440px`.

The center canvas must remain usable when the right panel is open. Avoid modal dialogs for information that users need to compare against the current list.

### 4.2 Sidebar

Use a cool-gray surface with a single selected state. Keep icons at `18px`, labels at `14px`, and row height at `40px`.

Group navigation into:

- Recruiting
- Talent operations
- Intelligence
- System

Do not use colorful icons or separate cards for each navigation item.

### 4.3 Global Command Bar

The command bar contains:

- Workspace switcher
- Global search
- Command menu trigger
- AI quick action
- Pending tasks
- Notifications
- User menu

Use a white surface with a bottom hairline and optional `backdrop-filter: saturate(160%) blur(16px)` when sticky.

## 5. Visual Language

### 5.1 Color

The default product is light:

- Page background: `{colors.canvas}`
- Working surfaces: `{colors.surface}`
- Secondary panels: `{colors.surface-subtle}`
- Selected rows: `{colors.surface-selected}`
- Primary text: `{colors.ink}`
- Secondary text: `{colors.ink-muted}`

Dark surfaces are reserved for command palettes, focused AI presentations, or high-contrast presentation modes. They are not the default app shell.

### 5.2 Typography

Use `Inter` as the primary implementation font with SF Pro as a platform fallback.

- Page titles: `28px / 600`
- Section titles: `20px / 600`
- Default UI: `14px / 400`
- Table headers: `12px / 600`
- Candidate and job names: `14px / 600`
- Metrics: `30px / 600`

Use negative tracking only on page titles and large metrics. Do not use oversized hero typography inside operational pages.

### 5.3 Spacing

Use a `4px` base grid.

- Compact control gaps: `8px`
- Form and toolbar gaps: `12px`
- Card padding: `16px`
- Major panel padding: `24px`
- Page padding: `32px`

Apple-like whitespace belongs around important decisions, not between every data row.

### 5.4 Radius

- Inputs and buttons: `8px`
- Cards and panels: `12px`
- Large previews and AI panels: `16px`
- Pills: status, filters, segmented controls, and primary Apple-style actions only

Avoid excessive rounded containers. A page section is not automatically a card.

### 5.5 Elevation

Hierarchy order:

1. Surface color
2. Hairline border
3. Spacing
4. Shadow

Use shadows only for floating layers:

- Command palette
- Popover
- Dropdown
- Modal
- Sticky AI composer

Default shadow:

```css
box-shadow: 0 8px 30px rgba(21, 23, 28, 0.10);
```

## 6. Core Components

### 6.1 Buttons

**Primary button**

- Recruit Blue fill
- White label
- `8px` radius for compact app actions
- `9999px` radius only for prominent Apple-style page actions
- Press state: `transform: scale(0.98)`

**Secondary button**

- White surface
- Strong hairline border
- Graphite label

**Tertiary button**

- Transparent surface
- Muted label
- Visible background only on hover or keyboard focus

Destructive actions must use danger color and confirmation when data or workflow state changes.

### 6.2 Search and Filters

Recruiter search is a primary surface, not a small utility field.

Support:

- Free-text semantic search
- Structured filters
- Boolean logic
- Saved searches
- Recent searches
- Filter count
- Clear-all action
- AI-assisted query rewriting

Display active filters as compact removable chips below the search field. Keep the result count and sort control visible.

### 6.3 Data Tables

Tables are the default representation for jobs, candidates, interviews, offers, and employees.

Required behavior:

- Sticky header
- Sortable columns
- Row selection
- Bulk actions
- Saved column preferences
- Empty, loading, partial, and error states
- Keyboard row navigation
- Optional right-side detail panel

Use `48px` default rows and `40px` compact rows. Do not place every value inside a badge.

### 6.4 Candidate Card

Candidate cards are used in pipeline boards and compact shortlists.

Show:

- Avatar or initials
- Name and current title
- Location
- Match score with explanation access
- Pipeline stage
- Owner
- Last activity
- Next action
- Risk or waiting-state indicator

Keep resumes, interview notes, and personal data behind deliberate navigation rather than exposing everything in one card.

### 6.5 Status

Use text plus color. Never rely on color alone.

Recruiting states:

- Draft
- Published
- Sourced
- Applied
- Screening
- Interview
- Waiting feedback
- Offer
- Hired
- Rejected
- Withdrawn
- Onboarding

Status pills are compact and quiet. Warning and danger colors are reserved for SLA breaches, blocked workflows, validation errors, and risk.

### 6.6 Timeline

Use a vertical timeline for candidate activity and a horizontal stepper for controlled business flows.

Timeline events should include:

- Actor
- Action
- Timestamp
- Source
- Related object
- Optional AI involvement

AI-generated actions must be visibly labeled and auditable.

## 7. AI Interaction System

### 7.1 AI Entry Points

Provide four entry patterns:

1. Global `Ask AI` command
2. Contextual right-side AI panel
3. Inline AI action beside a field or record
4. Full AI Workspace for cross-record analysis

The contextual panel is the default pattern.

### 7.2 AI Panel Structure

The AI panel contains:

1. Context header
2. Active sources
3. Conversation
4. Generated artifact or recommendation
5. Approval controls
6. Composer

Example context header:

```text
AI Assistant
Context: Senior Java Engineer · Candidate: Zhang Wei
Sources: Resume, job requirements, interview feedback
```

### 7.3 Message Types

Support:

- User message
- AI response
- Source/evidence block
- Tool execution state
- Generated draft
- Recommendation
- Warning
- Approval request
- Error and retry

AI responses should use progressive disclosure. Start with a concise conclusion, then show evidence, risks, and detailed reasoning on demand.

### 7.4 Grounding and Sources

Every consequential AI recommendation must show its source basis:

- Resume section
- Job requirement
- Interview feedback
- Historical application
- Offer status
- Employee record

Use clickable source chips. Do not display fake citations or imply certainty when source data is incomplete.

### 7.5 Approval Gates

Require explicit human confirmation before:

- Changing candidate stage
- Sending candidate communication
- Scheduling or cancelling interviews
- Creating or sending an Offer
- Rejecting a candidate
- Converting onboarding data into an employee record

Confirmation UI must show the proposed action, affected record, editable content, and audit note.

### 7.6 AI Composer

The composer supports:

- Natural-language prompt
- Slash commands
- Context attachments
- Suggested prompts
- Stop generation
- Regenerate
- Copy
- Insert into workflow
- Create task

Keep the composer sticky within the AI panel. Avoid decorative gradients, glowing borders, and novelty animation.

## 8. Screen Templates

### 8.1 Dashboard

Show only decision-relevant metrics:

- Open roles
- Qualified pipeline
- Interviews requiring feedback
- Offer acceptance rate
- SLA risks
- AI-recommended next actions

Use wide operational panels rather than a grid of decorative metric cards.

### 8.2 Candidate Explorer

Layout:

- Search and filter builder
- Result table
- Saved views
- Right-side candidate preview
- Optional AI summary

The preview should open without losing list position.

### 8.3 Candidate Detail

Use a stable two-column layout:

- Main: profile, resume, experience, applications, interviews
- Right: activity, next actions, AI assistant

Keep primary actions sticky: move stage, schedule interview, message, reject, create Offer.

### 8.4 Pipeline

Provide both table and board views. The table is the precision mode; the board is the coordination mode.

Cards must remain compact. Do not reproduce the full candidate profile inside each column.

### 8.5 Interview Workspace

Show:

- Candidate brief
- Interview plan
- Assigned interviewers
- Structured scorecard
- Notes
- AI-generated question suggestions
- Feedback completion status

AI may summarize feedback but must preserve individual interviewer comments.

### 8.6 Offer and Onboarding

Use a step-based workflow with clear ownership and blocking conditions. Show document status, approvals, candidate response, required tasks, and audit history.

## 9. Interaction Details

### 9.1 Keyboard

Support:

- `Cmd/Ctrl + K`: command menu
- `/`: focus current-page search
- `J/K`: move through rows
- `Enter`: open selected record
- `Esc`: close panel or clear selection
- `A`: open contextual AI panel when a record is selected

Never hide a critical action behind a shortcut only.

### 9.2 Motion

- Hover and focus: `120ms`
- Standard transitions: `180ms`
- Panels and drawers: `240ms`
- No bounce
- No parallax
- No decorative loading animation

Respect `prefers-reduced-motion`.

### 9.3 Loading

Use skeletons that match the final layout. For AI generation, show clear state text:

- Reading sources
- Comparing requirements
- Drafting response
- Waiting for approval

Do not use a generic spinner for long AI operations without state explanation.

## 10. Accessibility

- Meet WCAG 2.1 AA contrast.
- Minimum interactive target: `40px`; prefer `44px` on touch devices.
- Every icon-only button requires a tooltip and accessible label.
- Focus rings must always remain visible.
- Tables require keyboard navigation and announced sort state.
- AI streaming content must not steal focus.
- Status must use text, icon, and color where appropriate.
- Form errors must explain how to fix the problem.

## 11. Content Voice

The product voice is professional, direct, and calm.

Prefer:

- `生成候选人摘要`
- `需要 3 位面试官补充反馈`
- `建议进入终面，查看依据`
- `发送前请确认候选人和职位信息`

Avoid:

- `AI 认为这是完美候选人`
- `一键智能淘汰`
- `神奇匹配`
- `100% 适合`

AI language should distinguish facts, inference, recommendation, and uncertainty.

## 12. Do's and Don'ts

### Do

- Use recruiter-grade search, filters, context, ownership, and workflow state.
- Use Linear-style compact layouts and keyboard-first interactions.
- Use Apple-style spacing, typography, press feedback, and carefully limited elevation.
- Use ChatGPT Enterprise-style grounded AI conversations and approval gates.
- Keep the default interface light, calm, and data-focused.
- Let users compare records without losing their place.

### Don't

- Don't build a generic card-based admin dashboard.
- Don't wrap every section in a floating card.
- Don't use gradients, glow, glass effects, or shadows as decoration.
- Don't use AI as an autonomous hiring authority.
- Don't hide source evidence behind opaque match scores.
- Don't reduce professional recruiting workflows to a chatbot.
- Don't copy LinkedIn, Linear, Apple, or ChatGPT branding directly.

## 13. Responsive Strategy

| Breakpoint | Layout |
|---|---|
| `≥ 1440px` | Sidebar + main canvas + persistent right panel |
| `1024-1439px` | Sidebar + main canvas; right panel overlays or resizes |
| `768-1023px` | Collapsed sidebar; detail and AI panels become drawers |
| `< 768px` | Single-column task flows; desktop tables become focused lists |

Mobile prioritizes review and response, not full recruiter administration.

## 14. Implementation Order

1. App shell, tokens, typography, icons, focus states
2. Buttons, inputs, filters, badges, tables, panels
3. Jobs and candidate explorer
4. Candidate detail and recruiting timeline
5. Pipeline and interview workspace
6. Offer, onboarding, and employee records
7. Contextual AI panel and approval interactions
8. Reports, administration, responsive refinement
9. Motion, accessibility audit, visual regression testing

## 15. Definition of Done

A screen is complete when:

- It maps to a documented recruiting task.
- Loading, empty, error, permission, and success states exist.
- Keyboard and focus behavior work.
- AI output shows context and source evidence.
- Consequential AI actions require human approval.
- Layout works with and without the right panel.
- No unnecessary cards, colors, shadows, or decorative motion remain.
- The screen feels fast at real enterprise data density.

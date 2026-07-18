# Frontend Repository Guidelines

## Scope And Sources Of Truth

This file applies to `recruit-smart-frontend/` and inherits the repository-level `AGENTS.md` rules.

- Read `design.md` before changing any page, component, interaction, color, spacing, radius, typography, shadow, or AI surface.
- Read `前端框架.md` before changing architecture, dependencies, routing, state management, authentication, or API conventions.
- Treat current source files as implementation truth. Do not describe installed but unused libraries as completed features.
- AI is advisory only. Final candidate screening, rejection, Offer, interview, onboarding, and employee-record state changes require explicit human confirmation and backend enforcement.

## Current Project Structure

The frontend is a Vue 3 + TypeScript + Vite desktop SPA.

- `src/api/`: Axios client, login response adaptation, recruitment/onboarding/employee API adaptation, and AI SSE client.
- `src/app/`: application-level plugin configuration such as Vue Query.
- `src/components/<domain>/`: reusable domain components. Authentication and each completed recruitment domain follow the same domain folders.
- `src/composables/`: page state, validation, and interaction logic.
- `src/config/`: static page copy, menus, and option definitions.
- `src/router/`: route records, role metadata, and global guards.
- `src/stores/`: Pinia client state. It currently owns only the login session.
- `src/styles/`: global tokens, Element Plus overrides, and page-specific SCSS.
- `src/types/`: shared API, authentication, login, recruitment-domain, and AI contracts.
- `src/views/`: route-level page composition.
- `src/__tests__/`: Vitest tests using the jsdom environment.

Keep route pages thin. Put reusable markup in components, interaction logic in composables, HTTP adaptation in `api/`, and shared contracts in `types/`. Do not introduce a `features/` layer until multiple complete business modules justify the additional boundary.

## Development Commands

Run commands from `recruit-smart-frontend/`:

```bash
npm install
npm run mock-api
npm run dev
npm run type-check
npm run lint
npm test -- --run
npm run build
npm run preview
```

Environment requirements:

- Node.js 24 or newer.
- npm 11 or newer.
- Development server: `http://localhost:5173`.
- `/api` is proxied to `http://localhost:9000`; the local Mock API accepts the same gateway prefix.
- `npm run mock-api` is an explicit frontend-only fallback for local development. Never run it on the same port as the real backend or present its in-memory data as production data.

Before handing off code, run at least:

```bash
npm run type-check
npm run lint
npm test -- --run
npm run build
```

`npm run lint` includes `--fix` and may update files. Review the resulting diff.

## Vue And TypeScript Conventions

- Use Vue Composition API and `<script setup lang="ts">`.
- Keep TypeScript strict. Do not use `any`, `@ts-ignore`, or broad type assertions to bypass contracts.
- Use PascalCase for Vue component files, `useXxx` for composables, and camelCase for TypeScript modules and values.
- Prefer typed props, emits, `defineModel`, route metadata, API request objects, and response adapters.
- Use 2-space indentation and let ESLint/Prettier control formatting.
- Add concise Chinese comments only for business constraints or non-obvious behavior. Do not narrate obvious code.
- Preserve `prefers-reduced-motion` behavior for animations.

## Routing And Authorization

The current roles are `ADMIN`, `HR`, `INTERVIEWER`, and `CANDIDATE`.

- Public routes must declare `meta.public`.
- Protected routes must declare `meta.roles` when access is role-specific.
- Keep sidebar visibility aligned with route-role metadata.
- Unknown or missing roles must fail closed. Never default a user to HR or another privileged role.
- Frontend route and button checks improve UX only. Backend services must enforce real authorization and data ownership.
- Preserve the original target in the `redirect` query when sending unauthenticated users to login.

Current public routes are `/login` and `/register`. Candidate registration submits to the backend `/auth/register` endpoint and establishes the returned candidate session. Candidate verification-code login is UI-only while the backend supports username/password login.

## API And State Boundaries

- Use the shared Axios instance from `src/api/http.ts`; do not create page-local Axios clients.
- Send REST requests through `/api`; do not bypass the gateway to call business services directly.
- Let the API layer unwrap backend `Result<T>` and convert business failures to `ApiError`.
- Inject and clear JWT state through `useSessionStore`; page components must not read or write storage keys directly.
- Store only pure client or cross-page UI state in Pinia.
- Use TanStack Vue Query for server data, caching, loading, errors, mutations, and invalidation.
- Do not copy server lists or pagination results into Pinia.
- Confirm backend pagination fields before adapting them to the frontend `PagedData<T>` type. The job module is confirmed as `{ total, records }`; candidate, pipeline, interview, and Offer aggregation shapes remain frontend contracts pending backend implementation.
- The backend currently has only the Offer entity and Mapper. Keep Offer list/detail/create/update/send/revoke routes marked provisional, and never add an HR action that accepts or rejects an Offer on behalf of a candidate.
- The backend currently has only Onboarding/EmployeeProfile entities and Mappers. Treat their aggregation and action routes as provisional. Completing onboarding and creating an employee profile is one explicit human-confirmed business action.
- Keep interviewer comments and AI summaries in separate fields. AI output must never overwrite the original scorecard, comment, or suggestion.

For AI streaming, use `startAiStream` and typed `AiStreamEvent` values. Support cancellation and visible error handling. Do not treat free-form stream text as an executable business command.

## Design And Styling Rules

- Recruit Blue `#3183D8` is the single product accent.
- Use variables from `src/styles/tokens.scss`; add new visual values to `design.md` and the token layer before using them in components.
- Element Plus provides behavior, not the final visual language. Override its defaults through the shared token and SCSS layers.
- Use Lucide Vue Next for icons already covered by the library. Do not hand-draw replacement SVG icons.
- The current product layout is desktop-first with a `1280px` minimum complete width.
- Use `100dvh` for viewport-height authentication layouts to avoid browser-toolbar overflow.
- Keep authentication visuals and animations inside `login.scss` and `register.scss` unless they become genuinely shared.
- Avoid generic dashboard cards, decorative gradients, excessive glass effects, and new visual systems that conflict with `design.md`.
- Do not commit generated screenshots, prototype archives, or reference images with ordinary feature changes unless explicitly requested.

## Testing Guidelines

Existing tests cover session persistence, authentication-role normalization, and completed recruitment/onboarding/employee adapters and state rules. Add focused tests when changing:

- login response adaptation or role normalization;
- session persistence, restoration, or logout;
- route guards and role access;
- `Result<T>` response handling;
- form validation and duplicate-submit protection;
- AI SSE parsing, cancellation, or approval behavior.

Name unit tests `*.spec.ts`. Keep tests deterministic and use `npm test -- --run` for one-shot verification.

## Current Implementation Limits

Do not accidentally present the following as completed production functionality:

- verification-code authentication;
- HR 聚合页仍有部分临时契约；候选人端公开职位与 `/me` 接口、面试官本人任务接口已经按后端 Controller 接入；
- administrator role-permission CRUD, dictionaries, audit logs, and system health APIs; user account management is implemented;
- Vue Query adoption outside the completed job, candidate, pipeline, interview, Offer, onboarding, and employee modules.

When implementing one of these, update `前端框架.md`, add the relevant tests, and remove the corresponding limitation only after the behavior is verified.

## Security And Git Hygiene

- Never commit real tokens, passwords, gateway credentials, AI keys, or candidate personal data.
- Do not log resumes, interview feedback, employee records, JWTs, or registration form values.
- Keep `node_modules/`, `dist/`, `coverage/`, `.vite/`, and `test-results/` out of Git.
- Preserve unrelated untracked design documents and assets. Stage only files required by the current task.

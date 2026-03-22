@claude_dev.md

## Claude behavior

- **Первое действие при любой задаче** — `git checkout -b feature/<name>`. Никаких Edit/Write до создания ветки.
- **Последнее действие при завершении задачи** — коммит в ветку с осмысленным описанием (`type(scope): description`). Не заканчивать задачу без коммита.
- The main branch is **`master`** (not `main`). Always commit to and create PRs against `master`.
- **Git operations are batched to the END of a task** — do NOT commit during development.
- **Never use `is2xxSuccessful`** in test assertions — always use precise status matchers: `isOk`, `isCreated`, `isNoContent`, `isNotFound`, `isBadRequest`, `isUnauthorized`.

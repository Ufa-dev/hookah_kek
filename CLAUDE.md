@claude_dev.md

## Claude behavior

- The main branch is **`master`** (not `main`). Always commit to and create PRs against `master`.
- **Git operations are batched to the END of a task** — do NOT commit during development.
- **Never use `is2xxSuccessful`** in test assertions — always use precise status matchers: `isOk`, `isCreated`, `isNoContent`, `isNotFound`, `isBadRequest`, `isUnauthorized`.

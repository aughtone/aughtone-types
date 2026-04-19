# ViewModel & UDF Governance (viewmodel-udf-governance.md)
---
name: viewmodel-udf-governance
---

## 1. State Principles
- **Immutability**: State MUST be immutable. Use .copy().
- **Static Structure**: Use a single data class. NO sealed classes for state models.

## 2. Reducer & Effect Categorization
- .noEffect(): Pure state transition.
- .withEffect { ... }: Side effect independent of new state.
- .withFullEffect { newState, dispatch -> ... }: Side effect requiring new state or subsequent actions.

## 3. Command & Sync Strategy
- Suspend Commands: Isolation from state, no internal reduxStore.state access.
- Trigger Strategy: Commands called from within effect environment.

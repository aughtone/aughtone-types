# ADR Guidance

ADRs live in the Architecture Decision Records section of
`docs/knowledge/` (project-docs) and use the project-docs
ADR template (`assets/templates/adr.md` in that skill): title, date,
status, Context, Decision, Consequences. Keep each section tight — an ADR
can be a few sentences per section. The value is recording *that* a
decision was made and *why*.

Title articles `ADR-<slug>` (or follow the project's existing convention
— consistency within a repo beats either default); the sync names the
files by article ID. Create the section lazily, when the first ADR is
needed.

## When to offer an ADR — the triple test

All three must be true:

1. **Hard to reverse** — the cost of changing your mind later is meaningful
2. **Surprising without context** — a future reader will look at the code
   and wonder "why on earth did they do it this way?"
3. **The result of a real trade-off** — there were genuine alternatives and
   you picked one for specific reasons

If a decision is easy to reverse, skip it — you'll just reverse it. If
it's not surprising, nobody will wonder why. If there was no real
alternative, there's nothing to record beyond "we did the obvious thing."

### What qualifies

- **Architectural shape.** "We're using a monorepo." "The write model is
  event-sourced, the read model is projected into Postgres."
- **Integration patterns between contexts.** "Ordering and Billing
  communicate via domain events, not synchronous HTTP."
- **Technology choices that carry lock-in.** Database, message bus, auth
  provider, deployment target — the ones that would take a quarter to swap.
- **Boundary and scope decisions.** "Customer data is owned by the Customer
  context; others reference it by ID only." Explicit no-s are as valuable
  as yes-s.
- **Deliberate deviations from the obvious path.** "Manual SQL instead of
  an ORM because X." These stop the next engineer from "fixing" something
  deliberate.
- **Constraints not visible in the code.** "No AWS due to compliance."
  "Sub-200ms responses per the partner API contract."
- **Rejected alternatives when the rejection is non-obvious.** Record why
  GraphQL lost to REST, or someone re-proposes it in six months.

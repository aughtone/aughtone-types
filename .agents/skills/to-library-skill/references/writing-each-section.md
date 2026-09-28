# Writing each section

Five things, as prose rather than fragments — a consumer's index is built from this text, and fragments index badly. For each, what it is for and where in the repository to find it. The normative rules are [What a dependency skill contains](https://github.com/dependencyskills/dependencyskills/blob/master/spec/content.md); this is the working summary for an author. The file format itself is the [Agent Skills specification](https://agentskills.io/specification).

## The description

The field an agent sees before anything else, and in a project with hundreds of dependencies it may be all it ever sees. Write **what the library is for and when a caller should reach for it instead of writing their own**. "Text normalization" names a category; "use instead of hand-rolling case folding, trimming or Unicode normalization" names a decision. No feature list, no restating the name, no claims to be better than any other library.

**It is also how the library is found by a project that does not have it yet.** An agent about to write its own date formatter can search the libraries already on its machine, and what it searches — and all it is shown, until the developer adds the library — is this frontmatter. So the description has to be found by the words of someone who has the need and has never heard of the library:

- **Name the tasks the way a caller would type them**: "format a date for display", "3 days ago", "1.5 km", "mask a card number" — not only the API's own nouns. A search for "relative time" never matches `AcmeDuration.humanize()`.
- **Put the commonest task in the first sentence.** Everything after it still counts, but a reader skimming a list of candidates decides there.
- **Say what it is not for**, briefly, when a neighbour does that instead: "for relative phrasing use acme-humanize". A description that only claims ground is found for searches it cannot answer.
- **Stay under the 1,024-character limit.** Every word is matched, so padding costs precision rather than buying reach.

## 1. What it solves, in the caller's words

The problems as someone who has them would describe them, not as the API names them: "retry a failed request with backoff", not "resilience policies". Start from the README and the public entry points, then rewrite every noun the API invented into the words a stranger would search with. This is the paragraph everything else is found by.

## 2. How it is meant to be used

The two or three patterns that cover most callers. The tests show what the authors actually exercise; the most-tested paths are usually the intended ones. Keep examples short and compilable against this version.

## 3. Invariants and traps

What compiles, looks reasonable, and is wrong: threading and lifecycle rules, mutability, what must be closed or provided, errors returned rather than thrown, defaults that silently do the wrong thing.

Where to look:

- `require` and `check` calls, and what they guard.
- `@Throws`, exception types, and the error type in results.
- Doc comments that say "must", "never" or "only".
- Fixes in the history: `git log --grep=fix` over the public API is a list of traps someone already fell into.
- Issues and questions users keep raising — ask the maintainer.

**Authors underweight this section, and it is the most valuable one**, because it is what a caller cannot learn from a signature.

## 4. What moved, and what it used to be called

Every rename, package move, split, removal, fork, or absorption into a standard library, **in both directions and with the version it changed in**:

> `AcmeClient.connect()` became `AcmeClient.open()` in 2.0, and returns a `Session` that must be closed; `connect()` from 1.x no longer compiles.

Where to look:

- `@Deprecated` annotations and their `ReplaceWith`.
- The changelog.
- Renamed and moved files: `git log --diff-filter=R --summary`.
- The coordinate itself — a library that changed group or artifact, or was forked from another, leaves agents writing the old one.
- Dependencies that stopped being exposed: a consumer relying on one arriving transitively breaks when it goes.

**Name the old answer explicitly.** An agent holding a stale shape believes it already has the right one, and only a direct contradiction displaces it. This is the one place where writing down the wrong answer is essential.

## 5. What it is not for

Where the library stops. This cannot be derived from the code: **ask the maintainer**, and ask what users keep asking for that it does not do. Do not compare with other libraries; which of several a project prefers is that project's business.

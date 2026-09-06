# Stability Levels

Four words, one meaning each. Use them literally — the point is that a
reader who sees the word does not have to ask what it implies here.

## The ladder

**spike** — throwaway. Written to answer one question, expected to be
deleted once the question is answered. Lives in `poc/`. Promises nothing:
not correctness, not completeness, not that it still runs. Nobody should
depend on a spike, and holding one to the project's standards defeats its
only purpose.

**proof of concept** — a spike that worked and is being kept as a reference
implementation while the real thing gets written. Still in `poc/`, still
not wired into anything, still not maintained. **The only difference from a
spike is intent**: someone means to come back to it. Same directory, same
suspended standards, same fence against being copied into production — so
nothing depends on picking the right word, and the two are interchangeable
when asking for one. Its findings should already be
written up as a RAD, because the code will be deleted and the finding
should not go with it.

**experimental** — shipped and usable, and users may reasonably rely on it
working. The interface may change without a deprecation cycle, and the whole
thing may be withdrawn. Use is opt-in and informed: the word appears where
someone will read it before adopting, not buried in a changelog. Bug reports
are welcome; compatibility promises are not offered.

**supported** — the default, and the only level that does not need saying.
Changes follow the project's versioning, breaking changes are announced, and
there is a migration path. Everything without a stability marker is this,
which is why marking the others matters.

## Moving between them

Up the ladder is a decision someone makes, not something that happens by
drift. A proof of concept becomes experimental when it is deliberately
shipped; experimental becomes supported when the interface has stopped
moving and someone is willing to promise it will not.

Down is rarer and needs saying out loud — demoting something people are
using is a breaking change even when no code changed.

The failure mode is silence: code that shipped as an experiment, was never
promoted, and is now load-bearing for people who never saw the word.
Anything experimental should carry a note on what would make it supported,
so the question can actually be settled rather than deferred forever.

## Where the word goes

For a skill, all three, because agents and humans read different parts:

```yaml
metadata:
  stability: experimental
```

a banner as the first thing in the body:

```markdown
> **Experimental.** The command surface and the file format may change.
```

and a word in the `description`, since that is often all an agent sees
before deciding to act.

For a library or plugin, the README's opening paragraph and the release
notes. For a proof of concept, the `poc/<name>/README.md` header.

Say what may change, not just that something might. "The interface may
change" is a warning; "the ledger format and the flags may change, the
records themselves are plain markdown and will survive" is useful.

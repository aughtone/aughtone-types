# Filing Conventions

Structure is not prescribed - it is whatever the knowledge base shows,
arranged by humans in YouTrack, mirrored down by the sync. What follows
are conventions and starting points, not a required tree.

## Repo layout around the mirror

```
docs/
├── README.md       Git-native front door - explains this system (never synced)
├── knowledge/      THE MIRROR - two-way sync domain, whole project KB
│   └── .yt-sync/       sync state + merge bases (commit; never hand-edit)
├── stories/        GENERATED issue snapshot (story-reconcile) - never synced
├── outbox/         Outbound artifacts (third-party bug reports, letters) -
│                   written FOR someone else, not project knowledge
└── _archive/       Retired files - kept out of the mirror on purpose
```

Pure agent instructions stay at the repo root (`AGENTS.md`,
`WIRING.md`). Indexes and doc-system notes the agent maintains live at
the `docs/` root. None of that syncs.

**Everything under `docs/knowledge/` is published.** On a synced project
that is a public wiki page or a KB article, on the next sync, without
anyone approving it. Nothing else under `docs/` is - that distinction is
what the directory is for.

An image a document needs lives **beside that document**, and is embedded by filename (`![Flow](checkout-flow.png)`). Both bindings carry it; how differs enough to matter, so read the binding before replacing one.

**An image must paint its own opaque background.** It will be read on a theme its author did not choose - both targets have a dark mode, and a diagram drawn on an assumed white page renders its dark text on a dark ground, so the labels vanish with nothing left to suggest they were ever there. This covers SVG and any PNG with an alpha channel. Do not reach for a `prefers-color-scheme` media query instead: an embed renders in an `<img>`, which cannot see the host page, so the query follows the reader's operating system while the page follows a theme they set separately - that replaces a predictable failure with an unpredictable one.

**Sections belong to the taxonomy; sub-groups are free.** Subdivide a
section you are already filing into when the section has grown enough to
need it - which is uncommon, and never on the first document of a kind. A
sub-group inherits its parent's meaning, its
audience and its publish status, so it asserts nothing new. A new
*top-level* directory does: it claims the project has a kind of knowledge
it did not have before, and publishes it as a side effect. That is a
person's call, not an agent's.

An agent that cannot place something has nearly always found a *kind* of
an existing section rather than a new one. Work down this list and stop at
the first that fits:

1. **The section it belongs to.** Nearly everything lands here.
2. **A sub-group inside one.** A kind of research is still research; a
   kind of guide is still a guide. Name it for what it is, give it a
   README, and expect most projects never to need one.
3. **`documents/`**, as `DOC-000N-<slug>.md`. Genuinely informational
   material, and the honest answer when nothing above fits. Say in the
   summary what it is and which sections you rejected, so a person can
   promote it later.
4. **Never a new top-level section.** That is a claim about the project,
   and it belongs to a person.

`documents/` is the last stop, not the easy one. Reaching for it before
working through 1 and 2 is how a section becomes a drawer nobody can find
anything in - and a document filed there that belonged in `decisions/` is
lost to everyone looking for a decision.

**Numbering is per record type, not per directory.** A study nested under
research continues the RAD sequence - `research/studies/RAD-0042-….md`,
never a fresh `RAD-0001`. The identifier is the identity; the directory is
only organisation. Wiki page names are flat and globally unique, so a
restarted sequence collides.

`docs/design/` is a **companion tree to `docs/knowledge/`**, not a section
inside it: design records plus the images that make them worth reading.
It stays git-native because it is mostly not prose - mockups, exports, a
directory per iteration - and the KB holds what someone looks up, not the
working pile it came out of.

    docs/knowledge/   synced knowledge, text
    docs/design/      design records + samples, git-native
    docs/stories/     generated issue snapshot
    docs/            machinery: indexes, doc-system notes

So the KB's **Design & Accessibility** section holds design *direction* -
principles, accessibility standards, system-level guidance a reader wants
alongside the other knowledge. Anything with a picture in it belongs in
`docs/design/`, and the two cross-reference.

## Suggested starting sections

Sections are just top-level articles; create the ones the project needs
and let the owners rearrange freely.

**Titles are spelled out; directories are not.** A section has two names.
Its *title* - "Architecture Decision Records", "Quality Assurance" - is the
H1 of the section's `README.md`, and that is what the KB shows and what the
sync reads. Its *directory* is a plain lowercase word someone can type and
recognise without knowing the jargon. Accessibility (AX) belongs with
Design.

**Never put spaces or title case in a path.** If no H1 is present the sync
falls back to the directory stem, de-hyphenated and title-cased, so
`cms-server/` still yields "CMS Server" - but write the H1 anyway.

| Directory | Section title | What belongs there |
|---|---|---|
| `decisions/` | Architecture Decision Records | One hard-to-reverse choice each; append-only history |
| `requirements/` | Product Requirements | PRD narratives + Stories tables of tracker IDs (never AC) |
| `specifications/` | Specifications | How a thing IS - architecture, component specs; update in place |
| `research/` | Research | Investigations - question, trail, findings. Postmortems and worked case studies are kinds of investigation and belong here, as files or as a sub-group if there are enough to warrant one |
| `reference/` | Reference | External facts: vendors, prospects, regulations, domain material - and the **Domain Glossary** (the project's canonical terms; `AGENTS.md` at the repo root points at it so agents find it without a path) |
| `guides/` | Developer Guides | How-to - onboarding, environment, CI |
| `testing/` | Quality Assurance | Durable test plans and protocols (QA *runs* are issues) |
| `compliance/` | Mandates & Compliance | Legal/regulatory rules the work must satisfy |
| `support/` | Support | Support knowledge, runbooks, customer-facing material |
| `documents/` | Documents | Informational pages that explain the project - what it is, why it exists, how the pieces relate. Numbered `DOC-0001-…`, and maintained in place rather than dated and closed. Also the honest home for a KB document that fits no other section |

These are starting points, not a required tree. Match what a project
already has rather than renaming its directories to fit this table.

**There is no design section.** Design splits three ways, and none of them
is knowledge:

- `DESIGN.md` at the repo root - the design *system*: tokens (colour,
  typography, spacing, components) plus the rationale and the do's and
  don'ts, in the [DESIGN.md](https://github.com/google-labs-code/design.md)
  format. It sits beside `AGENTS.md` because it is agent-facing wiring, and
  root files are the ones reliably read.
- `docs/design/` - design *records*: a decision about a particular screen
  or flow, with the mockups attached. Git-native and never synced - working
  material for a screen, not something a reader looks up.
- Accessibility - the legal floor (WCAG level, AODA, EN 301 549) belongs in
  `compliance/` with the other rules the work must satisfy; the practical
  rules (contrast, focus order, target sizes) belong in DESIGN.md's do's
  and don'ts, next to the tokens they constrain.

A project running a real accessibility programme - audits, VPATs,
conformance statements - has a body of documents and earns a section. It
earns it by having them.

Every section directory's `README.md` is the section article's body:
one or two sentences on what lives there and who reads it, so both KB
readers and filing agents get the same guidance. Write one whenever you
create a section **or a sub-group** - a sub-group is a page too, and one
without a README publishes as a bare list of links with nothing saying
what the group is for.

**Subsystems (monorepos):** split WITHIN a section by subsystem
subdirectory, one per Subsystem field value, lazily - only where a system
actually has documents. Same two-name rule: the directory is a lowercase
slug, the field value is the child article's H1 ("CMS Server" →
`cms-server/README.md`, titled "CMS Server"). The KB then reads "Architecture Decision
Records → CMS Server" and the board field uses the same vocabulary.

## Organizing by audience instead

The sections above are organized by **document type**, which suits a
project whose readers are all engineers. A project serving several
audiences can organize the top level by **who reads it** instead, with the
type-based sections nesting inside:

| Tier | Section name | Holds | Written for |
|---|---|---|---|
| PD | Product Development | ADRs, RADs, PRDs, specs, engineering standards, reference registers with full technical detail - schemas, types, costs, ingestion rules | engineers |
| PM | Product Management | capability catalogues, coverage and cost views, roadmap and sequencing - "what we can build, what it costs, in what order" | product |
| BD | Business Development | differentiators, benefit-led overviews, positioning, pitch framing | go-to-market |

Note this **re-parents** rather than renames: Architecture Decision Records
does not map *to* Product Development, it moves *inside* it. When adopting
this shape over existing sections, map to what is already there and move
it - do not create near-duplicates beside it.

### The audience-replication convention

When a piece of knowledge matters to more than one audience - a register, a
capability, a standard - author **separate, audience-tailored versions**,
one per relevant tier. Not one document with sections for each reader:
nobody reads past their own part, and the version that matters to them ends
up buried in a document written for someone else.

- **PD** carries the full technical detail.
- **PM** carries the capability, cost and sequencing view.
- **BD** carries the differentiator and the pitch.
- **Every version cross-links the others.** That is what keeps them from
  silently diverging, and it is how a reader who needs more depth finds it.

Each tier may legitimately hold material the others do not - competitive
positioning was never in the PD version and does not belong there. Where
they state the same fact, **PD owns it**: correct it there first, then
carry the correction outward.

Not everything needs replicating. A thing that changes nothing for anyone
outside engineering has one version, in PD, and that is the normal case.

## Distinctions that matter

- **spec vs adr**: a spec describes how a thing IS; an ADR records why a
  choice was made. Specs update in place; ADRs are append-only.
- **research vs reference**: research is your investigation (trail and
  conclusion); reference is someone else's facts kept close.
- **vendors vs prospects** (both under Reference): a vendor you use or
  integrate today; a prospect you may approach - the dossier is
  knowledge, the act of reaching out is an issue. Prospects graduate to
  vendors when they become active.
- **Quality Assurance vs story QA**: per-story Gherkin lives in the
  story's `## QA` section; the KB section holds what spans stories.
- **Mandate vs procedure**: the rule ("PIPEDA requires consent") and the
  project's response ("how we check for it") are separate documents that
  cite each other. Both live in the KB; they just sit in different
  sections with different audiences.

## No status frontmatter

Docs carry at most `title`/`date` frontmatter. `id:`, `status:`,
`type:`, `parent:` blocks are retired - that state lives in the
tracker, and syncing it is what caused drift in the legacy system.

## Adopting over a legacy docs tree

1. Decide the section layout in YouTrack (create the top-level articles,
   or accept the suggestions above).
2. Move legacy knowledge files into `docs/knowledge/` under the matching
   section directories, then run the first sync with `--force` - each
   file is adopted and pushed up as a new article. (Alternatively:
   paste content into YouTrack by hand and let a clean first sync pull
   everything down.)
3. Work-tracking legacy docs (`ac/`, `gap/`, statused QA runs,
   handoffs) are NOT knowledge - hand those to the story-reconcile
   skill; that migration has its own approval gate.
4. Strip any `<!-- GENERATED -->` banners left over from the retired
   one-way publisher; the sync then pushes the clean copies up.

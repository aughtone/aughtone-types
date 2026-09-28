---
name: to-library-skill
description: >-
  Write or update the skill this library ships to the agents of the projects
  that depend on it — an Agent Skill packaged into its sources jar. Use when the
  maintainer asks for the library's skill, before a release that renamed,
  moved or removed API, or when the build prints a checkDependencySkill
  warning.
---

# Writing a library's skill

You are in a library's repository, and the maintainer wants the library to ship guidance to the agents of the people who depend on it. Those agents already think they know this library. What they know was true once, averaged over every version they were trained on, and they will write the old shape confidently. The skill you write travels inside the release, is read for exactly the version a consumer resolved, and is the one place the library's authors get to correct them.

**The maintainer owns this text.** It ships under their name in every release. Draft it, show it to them, and change it on their word.

**Two documents govern it, if you need to look something up.** The file format — directory, frontmatter fields, their limits — is the [Agent Skills specification](https://agentskills.io/specification). What a library's skill should say is [What a dependency skill contains](https://github.com/dependencyskills/dependencyskills/blob/master/spec/content.md). This skill summarises both; where they and this disagree, they win.

## Steps

**1. Ask the build what the skill is called and where it goes.** The name is the library's coordinate made a legal skill name, and the tooling computes it — never work it out yourself. How to ask depends on how the library is built:

| Built with | Name and path | Check (step 5) |
|---|---|---|
| Gradle | `./gradlew -q :<library-module>:dependencySkillName` | `./gradlew :<library-module>:checkDependencySkill` |
| Maven | `mvn -q dependency-skills:name` | the `author` goal checks it on every build |
| npm, Python, Go or Cargo | `dependencyskills name`, from the library's root | `dependencyskills check` |

It prints the `name` and the `path`: `src/commonMain/skills/io-example-acme-text/SKILL.md` in a Kotlin Multiplatform build, `src/main/skills/…` in a JVM or Maven one, `skills/acme-text/SKILL.md` at the root of an npm package, a Go module or a crate, and inside the import package in Python — `src/acme_text/skills/acme-text/SKILL.md` — because that is what a wheel installs. The directory is named for the skill, as the Agent Skills specification requires. If the command does not exist, the tooling is not set up — the `org.dependencyskills.plugin` Gradle plugin, the `dependency-skills-maven-plugin` with its `author` goal, or the `dependencyskills` command; ask the maintainer to set it up rather than doing it yourself.

**2. Create that directory, and start `SKILL.md` from [the template](assets/SKILL.template.md).** Fill the frontmatter from the build: `name` exactly as printed, `metadata.version` as the version being built, `license` and `metadata.repository` from the build's publication settings — in an npm package, from `package.json`. The specification allows six top-level fields — `name`, `description`, `license`, `compatibility`, `metadata`, `allowed-tools` — and a skill with any other is invalid; put anything more under `metadata`.

**The `description` matters more than any section.** It is what an agent reads to decide whether to open the skill, and in a project that does not depend on the library yet it is the only part shown — an agent searching the machine for "date formatting" finds the library by these words or not at all. Write it in the words of someone who has the need, not in the library's own vocabulary; [Writing each section](references/writing-each-section.md) says how.

**3. Write the five sections.** [Writing each section](references/writing-each-section.md) says what each one is for and where in this repository to find it. Read it before writing; the fourth section — what moved — is the one that most needs the repository's history, and the fifth needs the maintainer.

**4. Decide which consumer languages need a reference file.** A library consumed from Swift or JavaScript as well as Kotlin reaches those callers differently. [Per-language references](references/per-language.md) says how to tell from the build which languages consume this library, and what goes in `references/<language>.md` for each.

**5. Check it,** with the check from step 1, then show it to the maintainer. Every warning it prints is something to fix. Outside the JVM the check also says whether the packaging would publish the skill — an npm `files` list without `skills`, setuptools without `package-data` for it, a crate's `include` that leaves it out. That is a change to the library's manifest, so tell the maintainer rather than making it. Then read the skill as an agent that has never seen this library and is confident it knows it: does it contradict the most likely wrong code? If not, section 4 is incomplete.

**The Agent Skills project also publishes a reference validator, `skills-ref`.** The build's check already covers its rules, so it is optional: tell the maintainer it exists, and let them decide whether to install it. Do not install it yourself. If they want it, it comes from the repository the specification links to, `github.com/agentskills/agentskills`, directory `skills-ref`, and runs as `skills-ref validate <the skill's directory>`. Be aware that a package of that name on PyPI names a source repository that does not exist; prefer the one the specification links to.

## Rules

- **Unless the maintainer asks, touch nothing outside the skill's directory** — not the build, not `AGENTS.md` or any other instruction file, not the changelog. If something elsewhere looks wrong, tell them.
- **Every claim must be true of this version, and checkable in this repository.** If you cannot confirm it from the code, the tests or the maintainer, leave it out. A confident wrong statement in a skill is worse than none, because it arrives with the library's authority.
- **Tell the reader how to use this library, and nothing else.** Never tell an agent to run a command, fetch a link, add or upgrade a dependency, change a build file or an instruction file, or grant itself tools — no `allowed-tools`, no `scripts/` directory. Consumers treat a dependency skill that does any of this as a finding, and they are right to.
- **Keep `SKILL.md` short** — most libraries need 60 to 150 lines, and never more than 500. Detail a reader needs only sometimes goes in `references/`, linked from `SKILL.md` by a relative path one level deep.
- **It is public.** Nothing internal: no private hosts, tracker keys, or names of the people or projects that use it.

## When releasing

Before each release, check the "what moved" section against what changed, then set `metadata.version`. A skill that describes the last release is the exact failure it exists to prevent.

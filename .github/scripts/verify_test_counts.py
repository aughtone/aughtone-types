#!/usr/bin/env python3
"""Fail when a test task ran fewer tests than its source sets declare.

Gradle can report a test task as successful while executing only part of the suite: a browser
run once executed 481 of 646 tests and passed. This compares, for each task named on the command
line, the tests recorded in its JUnit XML results against the `@Test` functions in the source sets
that task compiles, and exits non-zero on any shortfall or on a task with no results at all.

Usage: verify_test_counts.py <test task>...   (run from the repository root, after the tests)
       verify_test_counts.py --log <test task>=<log file>...

A Kotlin/Native test binary run by hand writes no JUnit XML. For one of those, pass `--log` with the
output of `test.kexe --ktest_logger=SIMPLE`; its `Passed:` and `Failed:` lines are counted instead.
"""
import glob
import re
import sys
import xml.etree.ElementTree as ElementTree

MODULE = "types"

# The test source sets each test task compiles, besides commonTest. A task missing here is
# refused, so a new target cannot be added to the gate without saying what it should run.
EXTRA_SOURCE_SETS = {
    "jvmTest": ["jvmTest"],
    "jsBrowserTest": [],
    "wasmJsBrowserTest": [],
    "iosSimulatorArm64Test": ["appleTest"],
    "iosX64Test": ["appleTest"],
    "macosArm64Test": ["appleTest"],
    "linuxX64Test": [],
    "linuxArm64Test": [],
    "mingwX64Test": ["mingwTest"],
}

TEST_ANNOTATION = re.compile(r"^\s*@Test\b", re.MULTILINE)


def declared(source_set):
    total = 0
    for path in glob.glob(f"{MODULE}/src/{source_set}/**/*.kt", recursive=True):
        with open(path, encoding="utf-8") as source:
            total += len(TEST_ANNOTATION.findall(source.read()))
    return total


def executed(task):
    files = glob.glob(f"{MODULE}/build/test-results/{task}/*.xml")
    return len(files), sum(int(ElementTree.parse(f).getroot().get("tests", 0)) for f in files)


def executed_from_log(path):
    with open(path, encoding="utf-8") as log:
        lines = log.read().splitlines()
    return 1, sum(1 for line in lines if line.startswith(("Passed:", "Failed:")))


def main(args):
    logs = {}
    tasks = []
    use_logs = bool(args) and args[0] == "--log"
    for arg in args[1:] if use_logs else args:
        if use_logs:
            task, _, path = arg.partition("=")
            logs[task] = path
        tasks.append(arg.partition("=")[0] if use_logs else arg)
    if not tasks:
        print("usage: verify_test_counts.py <test task>... | --log <test task>=<log file>...", file=sys.stderr)
        return 2
    common = declared("commonTest")
    failed = False
    for task in tasks:
        if task not in EXTRA_SOURCE_SETS:
            print(f"{task}: unknown test task; add it to EXTRA_SOURCE_SETS", file=sys.stderr)
            failed = True
            continue
        expected = common + sum(declared(s) for s in EXTRA_SOURCE_SETS[task])
        files, ran = executed_from_log(logs[task]) if task in logs else executed(task)
        status = "ok" if files and ran >= expected else "SHORT"
        print(f"{task}: ran {ran} of {expected} declared tests ({files} result files) {status}")
        failed = failed or status != "ok"
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))

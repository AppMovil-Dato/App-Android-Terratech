#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STRINGS = re.compile(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'')
QUALIFIED_NAME = re.compile(
    r"\b(?:com|androidx|kotlinx|java|javax|android|kotlin|org)(?:\s*\.\s*[A-Za-z_]\w*)+"
)


def violations(path):
    source = path.read_text()
    issues = []
    if re.search(r"^import .*\.\*$", source, re.MULTILINE):
        issues.append("Replace wildcard imports with explicit imports")
    code = re.sub(r"^(?:package|import) [^\n]*", "", source, flags=re.MULTILINE)
    code = STRINGS.sub(lambda match: " " * len(match[0]), code)
    if QUALIFIED_NAME.search(code):
        issues.append(
            "Import qualified references instead of spelling out their package"
        )
    if "presentation/ui" in path.as_posix() and re.search(
        r"\b[pm]:\s*\w+State\b", code
    ):
        issues.append("Use descriptive state parameter names")
    return issues


def main():
    files = sorted((ROOT / "app/src").rglob("*.kt"))
    failures = [(path, issue) for path in files for issue in violations(path)]
    for path, issue in failures:
        print(f"{path.relative_to(ROOT)}: {issue}")
    if failures:
        raise SystemExit(1)
    print(f"Kotlin style checked: {len(files)} files")


if __name__ == "__main__":
    main()

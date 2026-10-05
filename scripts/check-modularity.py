#!/usr/bin/env python3
"""Reject bundled named types and bundled top-level Compose components."""
import argparse
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
KOTLIN_TYPE = re.compile(
    r"^\s*(?:(?:public|private|internal|data|sealed|enum|value|abstract|open|inline)\s+)*(?:class|interface|object)\s+(\w+)",
    re.MULTILINE,
)
COMPOSABLE = re.compile(
    r"^@Composable\s*\n(?:internal |private |public )?fun\s+([\w.]+)", re.MULTILINE
)
CS_TYPE = re.compile(
    r"^\s*(?:public|internal)\s+(?:(?:static|sealed|abstract|partial|readonly)\s+)*(?:class|record|interface|enum|struct)\s+(\w+)",
    re.MULTILINE,
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--backend", type=pathlib.Path)
    args = parser.parse_args()
    problems = []
    kotlin_count = 0
    for file in sorted((ROOT / "app/src").rglob("*.kt")):
        source = file.read_text()
        names = KOTLIN_TYPE.findall(source)
        components = COMPOSABLE.findall(source)
        if len(names) > 1:
            problems.append(
                f"{file.relative_to(ROOT)}: types bundled: {', '.join(names)}"
            )
        if names and file.stem != names[0]:
            problems.append(f"{file.relative_to(ROOT)}: filename must match {names[0]}")
        if len(components) > 1:
            problems.append(
                f"{file.relative_to(ROOT)}: Compose components bundled: {', '.join(components)}"
            )
        kotlin_count += 1
    csharp_count = 0
    if args.backend:
        for folder in ("NovaTech.TerraTech.Platform", "TerraTech.IntegrationTests"):
            for file in sorted((args.backend / folder).rglob("*.cs")):
                if any(part in ("bin", "obj", "Migrations") for part in file.parts):
                    continue
                names = CS_TYPE.findall(file.read_text(encoding="utf-8-sig"))
                if len(names) > 1:
                    problems.append(
                        f"{file.relative_to(args.backend)}: types bundled: {', '.join(names)}"
                    )
                csharp_count += 1
    if problems:
        print("\n".join(problems), file=sys.stderr)
        return 1
    print(
        f"Modularity checked: {kotlin_count} Kotlin files, {csharp_count} C# files; no bundled types/components."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

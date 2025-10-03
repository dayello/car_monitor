#!/usr/bin/env python3
"""
Strip non-code binary headers from Java source files.

This script removes any bytes before the first `package` declaration in
`.java` files. It is tailored to clean files that contain the abnormal
`%TSD-Header-###%` binary signature, but works for any leading garbage.

By default it operates on module source roots commonly used in this repo:
`commons/src/main/java`, `protocol/src/main/java`, `server/src/main/java`.

Two cleaning strategies are supported:
- Current file trim (default): read the working copy, keep content from the
  first `package` line to the end.
- Git HEAD trim (`--use-git-head`): read the file content from `HEAD` and keep
  content from the first `package` line. Use when the working copy is severely
  corrupted.

Usage examples:
  - Dry-run over defaults:
      python scripts/clean_java_headers.py --dry-run
  - Clean protocol only using HEAD content and create backups:
      python scripts/clean_java_headers.py --roots protocol/src/main/java \
        --use-git-head --backup
"""

import argparse
import os
import re
import subprocess
from typing import List, Optional, Tuple


HEADER_SIGNATURE_DEFAULT = b"%TSD-Header-###%"
PACKAGE_RE = re.compile(rb"^package\s", re.MULTILINE)


def find_java_files(roots: List[str]) -> List[str]:
    files: List[str] = []
    for root in roots:
        if not os.path.isdir(root):
            continue
        for dirpath, _, filenames in os.walk(root):
            for fn in filenames:
                if fn.endswith(".java"):
                    files.append(os.path.join(dirpath, fn))
    return files


def read_binary(path: str) -> bytes:
    with open(path, "rb") as f:
        return f.read()


def write_binary(path: str, data: bytes) -> None:
    with open(path, "wb") as f:
        f.write(data)


def get_head_content(path: str) -> Optional[bytes]:
    # The repo root is expected to be the current working directory.
    rel_path = path
    if rel_path.startswith("./"):
        rel_path = rel_path[2:]
    try:
        out = subprocess.check_output(["git", "show", f"HEAD:./{rel_path}"])  # bytes
        return out
    except subprocess.CalledProcessError:
        return None


def trim_to_package(data: bytes) -> Optional[bytes]:
    match = PACKAGE_RE.search(data)
    if not match:
        return None
    start = match.start()
    return data[start:]


def needs_clean(data: bytes, signature: bytes) -> bool:
    # Clean if signature exists or there is any non-whitespace before package
    if signature and signature in data:
        return True
    m = PACKAGE_RE.search(data)
    if not m:
        # No package declaration found — likely not a Java source or corrupted.
        return False
    head = data[: m.start()]
    # If the head is not empty after stripping common whitespace, it likely contains garbage
    return head.strip() != b""


def process_file(path: str, use_head: bool, backup: bool, signature: bytes, dry_run: bool, verbose: bool) -> Tuple[bool, str]:
    original = None
    source: Optional[bytes] = None
    try:
        original = read_binary(path)
    except Exception as e:
        return False, f"SKIP {path}: read error: {e}"

    if not needs_clean(original, signature):
        return False, f"OK   {path}: no leading header detected"

    if use_head:
        head_data = get_head_content(path)
        if head_data is None:
            source = original
            if verbose:
                print(f"WARN {path}: HEAD content not found; using working copy")
        else:
            source = head_data
    else:
        source = original

    trimmed = trim_to_package(source)
    if trimmed is None:
        return False, f"SKIP {path}: no package declaration found"

    if dry_run:
        removed = len(source) - len(trimmed)
        return True, f"DRY  {path}: would remove {removed} leading bytes"

    try:
        if backup:
            bak_path = path + ".bak"
            if not os.path.exists(bak_path):
                write_binary(bak_path, original)
        write_binary(path, trimmed)
        removed = len(source) - len(trimmed)
        return True, f"FIX  {path}: removed {removed} leading bytes"
    except Exception as e:
        return False, f"FAIL {path}: write error: {e}"


def main() -> int:
    parser = argparse.ArgumentParser(description="Strip non-code headers from Java sources.")
    parser.add_argument(
        "--roots",
        nargs="+",
        default=[
            "commons/src/main/java",
            "protocol/src/main/java",
            "server/src/main/java",
        ],
        help="Source roots to scan for .java files",
    )
    parser.add_argument("--use-git-head", action="store_true", help="Read content from git HEAD for trimming")
    parser.add_argument("--backup", action="store_true", help="Create .bak backups before writing")
    parser.add_argument("--dry-run", action="store_true", help="Do not write changes, only report")
    parser.add_argument("--verbose", action="store_true", help="Verbose logging")
    parser.add_argument(
        "--signature",
        default=HEADER_SIGNATURE_DEFAULT.decode("ascii"),
        help="Header signature to consider as abnormal (default: %TSD-Header-###%)",
    )

    args = parser.parse_args()
    signature = args.signature.encode("utf-8", "ignore") if args.signature else b""

    roots = [r for r in args.roots if os.path.isdir(r)]
    if not roots:
        print("No valid roots provided; nothing to do.")
        return 0

    files = find_java_files(roots)
    if args.verbose:
        print(f"Scanning {len(files)} .java files under: {', '.join(roots)}")

    changed = 0
    skipped = 0
    for path in files:
        ok, msg = process_file(path, args.use_git_head, args.backup, signature, args.dry_run, args.verbose)
        if msg:
            print(msg)
        if ok:
            changed += 1
        else:
            skipped += 1

    if args.dry_run:
        print(f"Dry-run complete. Files needing cleanup: {changed}. Skipped/clean: {skipped}.")
    else:
        print(f"Cleanup complete. Files cleaned: {changed}. Skipped/OK: {skipped}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

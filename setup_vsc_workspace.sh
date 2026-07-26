#!/usr/bin/env bash
#
# setup_vsc_workspace.sh - Generate the RbmkSimulator multi-project development
#  workspace that can be opened in VS code
#
#     <workspace-root>/
#         code.code-workspace      <- generated multi-root workspace + build tasks
#         .vscode/                  (nothing required here; tasks live in the workspace file)
#         utils/                    <- cloned
#         jmplot/                   <- cloned
#         PhxNetMod/                <- cloned
#         RbmkSimulator/            <- this repo (contains this script)
#             lib/AbsoluteLayout.jar   <- downloaded here and used from here
#
# This script was 100 % vibe coded using Github Copilot with Claude Opus 4.8
#
set -euo pipefail

# ---------------------------------------------------------------------------
# Locations
# ---------------------------------------------------------------------------
SIM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"   # RbmkSimulator repo
ROOT="$(cd "$SIM_DIR/.." && pwd)"                         # workspace root (parent)

ABS_VERSION="RELEASE290"
ABS_URL="https://repo1.maven.org/maven2/org/netbeans/external/AbsoluteLayout/${ABS_VERSION}/AbsoluteLayout-${ABS_VERSION}.jar"

echo "Workspace root : $ROOT"
echo "Simulator repo : $SIM_DIR"
echo

# ---------------------------------------------------------------------------
# 1. Clone the sibling dependency repositories (kept independent)
# ---------------------------------------------------------------------------
clone_repo() {
	name="$1"; url="$2"
	if [ -d "$ROOT/$name/.git" ]; then
		echo "  [skip ] $name already present"
	else
		echo "  [clone] $name"
		git clone "$url" "$ROOT/$name"
	fi
}

echo "==> Dependency repositories"
clone_repo utils     "https://github.com/hartrusion/utils.git"
clone_repo jmplot    "https://github.com/hartrusion/jmplot.git"
clone_repo PhxNetMod "https://github.com/hartrusion/PhxNetMod.git"
echo

# ---------------------------------------------------------------------------
# 2. AbsoluteLayout -> RbmkSimulator/lib/AbsoluteLayout.jar
# ---------------------------------------------------------------------------
echo "==> AbsoluteLayout (Swing layout helper)"
mkdir -p "$SIM_DIR/lib"
if [ -f "$SIM_DIR/lib/AbsoluteLayout.jar" ]; then
	echo "  [skip ] lib/AbsoluteLayout.jar already present"
else
	echo "  [fetch] lib/AbsoluteLayout.jar ($ABS_VERSION from Maven Central)"
	if command -v curl >/dev/null 2>&1; then
		curl -fSL "$ABS_URL" -o "$SIM_DIR/lib/AbsoluteLayout.jar"
	elif command -v wget >/dev/null 2>&1; then
		wget -qO "$SIM_DIR/lib/AbsoluteLayout.jar" "$ABS_URL"
	else
		echo "ERROR: neither curl nor wget is available to download AbsoluteLayout." >&2
		exit 1
	fi
fi
echo

# ---------------------------------------------------------------------------
# 3. Generate the VS Code workspace + build tasks
# ---------------------------------------------------------------------------
echo "==> Generating VS Code configuration"

echo "  [write] code.code-workspace"
cat > "$ROOT/code.code-workspace" <<'WORKSPACE_EOF'
{
	"folders": [
		{ "path": "utils", "name": "utils" },
		{ "path": "jmplot", "name": "jmplot" },
		{ "path": "PhxNetMod", "name": "PhxNetMod" },
		{ "path": "RbmkSimulator", "name": "RbmkSimulator" }
	],
	"settings": {
		"java.dependency.syncWithFolderExplorer": true
	},
	"extensions": {
		"recommendations": [ "vscjava.vscode-java-pack" ]
	},
	"tasks": {
		"version": "2.0.0",
		"tasks": [
			{
				"label": "javac: utils",
				"type": "shell",
				"command": "bash",
				"args": [ "-lc", "set -euo pipefail; mkdir -p build/classes; javac -encoding UTF-8 -source 21 -target 21 -d build/classes $(find src -name '*.java' -print)" ],
				"windows": {
					"command": "powershell",
					"args": [ "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "New-Item -ItemType Directory -Force build/classes | Out-Null; $s = (Get-ChildItem -Recurse -Filter *.java src).FullName; javac -encoding UTF-8 -source 21 -target 21 -d build/classes $s" ]
				},
				"options": { "cwd": "${workspaceFolder:utils}" },
				"problemMatcher": [ "$javac" ],
				"group": "build"
			},
			{
				"label": "javac: jmplot",
				"type": "shell",
				"command": "bash",
				"args": [ "-lc", "set -euo pipefail; mkdir -p build/classes; javac -encoding UTF-8 -source 21 -target 21 -d build/classes $(find src -name '*.java' -print)" ],
				"windows": {
					"command": "powershell",
					"args": [ "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "New-Item -ItemType Directory -Force build/classes | Out-Null; $s = (Get-ChildItem -Recurse -Filter *.java src).FullName; javac -encoding UTF-8 -source 21 -target 21 -d build/classes $s" ]
				},
				"options": { "cwd": "${workspaceFolder:jmplot}" },
				"problemMatcher": [ "$javac" ],
				"group": "build"
			},
			{
				"label": "javac: PhxNetMod",
				"type": "shell",
				"command": "bash",
				"args": [ "-lc", "set -euo pipefail; mkdir -p build/classes; CP=../utils/build/classes; for j in lib/*.jar; do [ -e \"$j\" ] && CP=\"$CP:$j\"; done; javac -encoding UTF-8 -source 21 -target 21 -d build/classes -cp \"$CP\" $(find src -name '*.java' -print)" ],
				"windows": {
					"command": "powershell",
					"args": [ "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "New-Item -ItemType Directory -Force build/classes | Out-Null; $cp = @('../utils/build/classes'); Get-ChildItem lib/*.jar -ErrorAction SilentlyContinue | ForEach-Object { $cp += $_.FullName }; $s = (Get-ChildItem -Recurse -Filter *.java src).FullName; javac -encoding UTF-8 -source 21 -target 21 -d build/classes -cp ($cp -join ';') $s" ]
				},
				"options": { "cwd": "${workspaceFolder:PhxNetMod}" },
				"problemMatcher": [ "$javac" ],
				"group": "build"
			},
			{
				"label": "javac: RbmkSimulator",
				"type": "shell",
				"command": "bash",
				"args": [ "-lc", "set -euo pipefail; mkdir -p build/classes; CP=../utils/build/classes:../jmplot/build/classes:../PhxNetMod/build/classes; for j in lib/*.jar; do [ -e \"$j\" ] && CP=\"$CP:$j\"; done; javac -encoding UTF-8 -source 17 -target 17 -d build/classes -cp \"$CP\" $(find src -name '*.java' -print)" ],
				"windows": {
					"command": "powershell",
					"args": [ "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", "New-Item -ItemType Directory -Force build/classes | Out-Null; $cp = @('../utils/build/classes','../jmplot/build/classes','../PhxNetMod/build/classes'); Get-ChildItem lib/*.jar -ErrorAction SilentlyContinue | ForEach-Object { $cp += $_.FullName }; $s = (Get-ChildItem -Recurse -Filter *.java src).FullName; javac -encoding UTF-8 -source 17 -target 17 -d build/classes -cp ($cp -join ';') $s" ]
				},
				"options": { "cwd": "${workspaceFolder:RbmkSimulator}" },
				"problemMatcher": [ "$javac" ],
				"group": { "kind": "build", "isDefault": true }
			},
			{
				"label": "javac: all",
				"dependsOn": [ "javac: utils", "javac: jmplot", "javac: PhxNetMod", "javac: RbmkSimulator" ],
				"dependsOrder": "sequence",
				"problemMatcher": [],
				"group": "build"
			}
		]
	}
}
WORKSPACE_EOF

# Per-project Java settings so the Red Hat Java extension resolves the
# cross-project references (needed for IntelliSense and debugging).
write_settings() {
	target_dir="$1"; content="$2"
	mkdir -p "$target_dir/.vscode"
	printf '%s\n' "$content" > "$target_dir/.vscode/settings.json"
	echo "  [write] ${target_dir##*/}/.vscode/settings.json"
}

LEAF_SETTINGS='{
	"java.project.sourcePaths": [ "src", "test" ],
	"java.project.outputPath": "build/classes"
}'

PHX_SETTINGS='{
	"java.project.sourcePaths": [ "src", "test" ],
	"java.project.outputPath": "build/classes",
	"java.project.referencedProjects": [ "../utils" ]
}'

RBMK_SETTINGS='{
	"java.project.sourcePaths": [ "src", "test" ],
	"java.project.outputPath": "build/classes",
	"java.project.referencedProjects": [ "../utils", "../jmplot", "../PhxNetMod" ],
	"java.project.referencedLibraries": [ "lib/AbsoluteLayout.jar" ],
	"java.import.exclusions": [ "**/.github" ],
	"maven.excludedFolders": [ "**/.github" ]
}'

write_settings "$ROOT/utils"     "$LEAF_SETTINGS"
write_settings "$ROOT/jmplot"    "$LEAF_SETTINGS"
write_settings "$ROOT/PhxNetMod" "$PHX_SETTINGS"
write_settings "$SIM_DIR"        "$RBMK_SETTINGS"
echo

# ---------------------------------------------------------------------------
# 4. Toolchain hint
# ---------------------------------------------------------------------------
echo "==> Toolchain"
if command -v javac >/dev/null 2>&1; then
	javac -version
else
	echo "  WARNING: 'javac' not found on PATH. Install a JDK 21 to build." >&2
fi

echo
echo "Done. Next steps:"
echo "  1. Open '$ROOT/code.code-workspace' in VS Code (File > Open Workspace from File...)."
echo "  2. Accept the recommended Java extension when prompted."
echo "  3. Build everything with the task 'javac: all' (Ctrl+Shift+B)."

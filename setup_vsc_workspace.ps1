<#
    setup_vsc_workspace.ps1 - Generate the RbmkSimulator multi-project development
    workspace that can be opened in VS code

        <workspace-root>/
            code.code-workspace      <- generated multi-root workspace + build tasks
            utils/                    <- cloned
            jmplot/                   <- cloned
            PhxNetMod/                <- cloned
            RbmkSimulator/            <- this repo (contains this script)
                lib/AbsoluteLayout.jar   <- downloaded here and used from here

    Nothing is committed to the source-only repositories: the checked-out
    dependencies, the AbsoluteLayout jar and the generated VS Code config are all
    produced locally by this script.

    This script was 100 % vibe coded using Github Copilot with Claude Opus 4.8
#>
$ErrorActionPreference = 'Stop'

# ---------------------------------------------------------------------------
# Locations
# ---------------------------------------------------------------------------
$SimDir = $PSScriptRoot                                   # RbmkSimulator repo
$Root   = (Resolve-Path (Join-Path $SimDir '..')).Path    # workspace root (parent)

$AbsVersion = 'RELEASE290'
$AbsUrl = "https://repo1.maven.org/maven2/org/netbeans/external/AbsoluteLayout/$AbsVersion/AbsoluteLayout-$AbsVersion.jar"

Write-Host "Workspace root : $Root"
Write-Host "Simulator repo : $SimDir"
Write-Host ''

# ---------------------------------------------------------------------------
# 1. Clone the sibling dependency repositories (kept independent)
# ---------------------------------------------------------------------------
function Clone-Repo([string]$name, [string]$url) {
    $dest = Join-Path $Root $name
    if (Test-Path (Join-Path $dest '.git')) {
        Write-Host "  [skip ] $name already present"
    } else {
        Write-Host "  [clone] $name"
        git clone $url $dest
    }
}

Write-Host '==> Dependency repositories'
Clone-Repo 'utils'     'https://github.com/hartrusion/utils.git'
Clone-Repo 'jmplot'    'https://github.com/hartrusion/jmplot.git'
Clone-Repo 'PhxNetMod' 'https://github.com/hartrusion/PhxNetMod.git'
Write-Host ''

# ---------------------------------------------------------------------------
# 2. AbsoluteLayout -> RbmkSimulator/lib/AbsoluteLayout.jar
# ---------------------------------------------------------------------------
Write-Host '==> AbsoluteLayout (Swing layout helper)'
$LibDir = Join-Path $SimDir 'lib'
New-Item -ItemType Directory -Force -Path $LibDir | Out-Null
$AbsDest = Join-Path $LibDir 'AbsoluteLayout.jar'
if (Test-Path $AbsDest) {
    Write-Host '  [skip ] lib/AbsoluteLayout.jar already present'
} else {
    Write-Host "  [fetch] lib/AbsoluteLayout.jar ($AbsVersion from Maven Central)"
    Invoke-WebRequest -Uri $AbsUrl -OutFile $AbsDest
}
Write-Host ''

# ---------------------------------------------------------------------------
# 3. Generate the VS Code workspace + build tasks
# ---------------------------------------------------------------------------
Write-Host '==> Generating VS Code configuration'

$Workspace = @'
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
'@
Set-Content -Path (Join-Path $Root 'code.code-workspace') -Value $Workspace -Encoding UTF8
Write-Host '  [write] code.code-workspace'

# Per-project Java settings so the Red Hat Java extension resolves the
# cross-project references (needed for IntelliSense and debugging).
function Write-Settings([string]$dir, [string]$content) {
    $vs = Join-Path $dir '.vscode'
    New-Item -ItemType Directory -Force -Path $vs | Out-Null
    Set-Content -Path (Join-Path $vs 'settings.json') -Value $content -Encoding UTF8
    Write-Host ("  [write] {0}/.vscode/settings.json" -f (Split-Path $dir -Leaf))
}

$LeafSettings = @'
{
	"java.project.sourcePaths": [ "src", "test" ],
	"java.project.outputPath": "build/classes"
}
'@

$PhxSettings = @'
{
	"java.project.sourcePaths": [ "src", "test" ],
	"java.project.outputPath": "build/classes",
	"java.project.referencedProjects": [ "../utils" ]
}
'@

$RbmkSettings = @'
{
	"java.project.sourcePaths": [ "src", "test" ],
	"java.project.outputPath": "build/classes",
	"java.project.referencedProjects": [ "../utils", "../jmplot", "../PhxNetMod" ],
	"java.project.referencedLibraries": [ "lib/AbsoluteLayout.jar" ],
	"java.import.exclusions": [ "**/.github" ],
	"maven.excludedFolders": [ "**/.github" ]
}
'@

Write-Settings (Join-Path $Root 'utils')     $LeafSettings
Write-Settings (Join-Path $Root 'jmplot')    $LeafSettings
Write-Settings (Join-Path $Root 'PhxNetMod') $PhxSettings
Write-Settings $SimDir                        $RbmkSettings
Write-Host ''

# ---------------------------------------------------------------------------
# 4. Toolchain hint
# ---------------------------------------------------------------------------
Write-Host '==> Toolchain'
if (Get-Command javac -ErrorAction SilentlyContinue) {
    javac -version
} else {
    Write-Warning "'javac' not found on PATH. Install a JDK 21 to build."
}

Write-Host ''
Write-Host 'Done. Next steps:'
Write-Host "  1. Open '$Root\code.code-workspace' in VS Code (File > Open Workspace from File...)."
Write-Host '  2. Accept the recommended Java extension when prompted.'
Write-Host "  3. Build everything with the task 'javac: all' (Ctrl+Shift+B)."

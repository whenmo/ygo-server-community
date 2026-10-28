param(
	[switch]$HardReboot,
	[int]$Port = 8080
)

$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path -LiteralPath $PSScriptRoot).Path
$mavenWrapper = Join-Path $projectRoot 'mvnw.cmd'
Set-Location -LiteralPath $projectRoot

if (-not (Test-Path -LiteralPath $mavenWrapper -PathType Leaf)) {
	throw "Maven Wrapper not found: $mavenWrapper"
}

$javaVersion = (& java --version | Out-String)
if ($javaVersion -notmatch '(?:version "|java\s+)25(?:\.|\s)') {
	throw "Java 25 is required. Detected Java version output:`n$javaVersion"
}

Write-Host "Project root: $projectRoot" -ForegroundColor DarkGray
Write-Host "Using Java 25" -ForegroundColor DarkGray

if ($HardReboot) {
	Write-Host "Hard reboot mode enabled" -ForegroundColor Yellow

	# 停止目前佔用應用程式連接埠的程序。
	$portProcesses = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
		Select-Object -ExpandProperty OwningProcess -Unique

	if ($portProcesses) {
		foreach ($processId in $portProcesses) {
			try {
				$process = Get-Process -Id $processId -ErrorAction Stop
				Stop-Process -Id $processId -Force -ErrorAction Stop
				Write-Host ("Stopped PID {0} ({1}) on port {2}" -f $processId, $process.ProcessName, $Port) -ForegroundColor DarkYellow
			}
			catch {
				Write-Warning ("Failed to stop PID {0}: {1}" -f $processId, $_.Exception.Message)
			}
		}
	}
	else {
		Write-Host ("No process is listening on port {0}" -f $Port) -ForegroundColor DarkGray
	}

	# 額外停止與目前專案路徑相關的 Java 程序。
	$escapedPath = [regex]::Escape($PSScriptRoot)
	$javaProcesses = Get-CimInstance Win32_Process -Filter "Name = 'java.exe'" -ErrorAction SilentlyContinue |
		Where-Object { $_.CommandLine -match $escapedPath }

	foreach ($javaProcess in $javaProcesses) {
		try {
			Stop-Process -Id $javaProcess.ProcessId -Force -ErrorAction Stop
			Write-Host ("Stopped project Java PID {0}" -f $javaProcess.ProcessId) -ForegroundColor DarkYellow
		}
		catch {
			Write-Warning ("Failed to stop project Java PID {0}: {1}" -f $javaProcess.ProcessId, $_.Exception.Message)
		}
	}

	$targetDirectory = Join-Path $projectRoot 'target'
	if (Test-Path -LiteralPath $targetDirectory) {
		Remove-Item -LiteralPath $targetDirectory -Recurse -Force
		Write-Host "Removed target directory" -ForegroundColor DarkYellow
	}

	Write-Host "Running Maven clean..." -ForegroundColor Cyan
	& $mavenWrapper clean
	if ($LASTEXITCODE -ne 0) {
		throw "Maven clean failed. Hard reboot aborted."
	}
}

Write-Host "Starting Spring Boot project..." -ForegroundColor Cyan
$mavenArguments = @(
	'-Dmaven.test.skip=true'
	'spring-boot:run'
)
& $mavenWrapper @mavenArguments
$mavenExitCode = $LASTEXITCODE

if ($mavenExitCode -ne 0) {
	throw "Spring Boot startup failed with exit code $mavenExitCode."
}

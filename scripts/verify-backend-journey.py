#!/usr/bin/env python3
"""Explicit localhost-only Android/Development/MySQL integration. Preserves its new database."""

import json, os, pathlib, secrets, shutil, subprocess, threading, time, urllib.request, uuid

ROOT = pathlib.Path(__file__).resolve().parents[1]
BACKEND = ROOT.parents[1] / "BackEnd" / "NovaTech.TerraTech.Platform"
DOTNET = os.environ.get(
    "TB1_DOTNET", "/private/tmp/terratech-review-runtime/dotnet/dotnet"
)
MYSQL = os.environ.get("TB1_MYSQL_CLI", "/usr/local/mysql/bin/mysql")
ADB = os.environ.get(
    "TB1_ADB", str(pathlib.Path.home() / "Library/Android/sdk/platform-tools/adb")
)
SERIAL = os.environ.get("TB1_ANDROID_SERIAL", "emulator-5554")
if not SERIAL.startswith("emulator-"):
    raise RuntimeError("This destructive test harness only accepts an emulator")
DB = "terratech_android_" + uuid.uuid4().hex
PORT = int(os.environ.get("TB1_MYSQL_PORT", "33308"))
API_PORT = int(os.environ.get("TB1_ANDROID_API_PORT", "55024"))
DRIVER = os.environ.get("TB1_E2E_DRIVER", "compose")
if DRIVER not in ["compose", "artemis"]:
    raise RuntimeError("Unknown E2E driver")
ARTEMIS = pathlib.Path(
    os.environ.get("TB1_ARTEMIS_ROOT", str(ROOT.parents[2] / ".tools/artemis"))
)
OUT = ROOT / os.environ.get(
    "TB1_EVIDENCE_DIR",
    (
        "docs/evidence/artemis"
        if DRIVER == "artemis"
        else "docs/evidence/backend-journey"
    ),
)
OUT.mkdir(parents=True, exist_ok=True)

env = os.environ.copy()
env.update(
    ASPNETCORE_ENVIRONMENT="Development",
    ConnectionStrings__DefaultConnection=f"server=127.0.0.1;port={PORT};user=root;database={DB}",
    TokenSettings__Secret=secrets.token_urlsafe(64),
)
if "DOTNET_ROOT" not in env:
    env["DOTNET_ROOT"] = str(pathlib.Path(DOTNET).parent)
    env["DOTNET_CLI_HOME"] = "/private/tmp/terratech-review-runtime"
    env["NUGET_PACKAGES"] = "/private/tmp/terratech-review-runtime/nuget"
mysql = [MYSQL, "--protocol=TCP", "-h127.0.0.1", f"-P{PORT}", "-uroot", "-N", "-B"]


def sql(query, database=None):
    command = mysql + (["-D", database] if database else []) + ["-e", query]
    return subprocess.check_output(command, text=True).strip()


def backend(*args):
    return [
        DOTNET,
        "run",
        "--project",
        str(BACKEND),
        "-c",
        "Debug",
        "--no-build",
        "--no-launch-profile",
        "--",
        *args,
    ]


process = None
recorder = None
recorder_pid = None
video_path = ROOT / "artifacts/TerraTech-TB1-emulator.mp4"


def finish_recording():
    global recorder_pid
    if recorder is None:
        return
    if recorder.poll() is None and recorder_pid:
        subprocess.run(
            [ADB, "-s", SERIAL, "shell", "kill", "-2", recorder_pid],
            check=False,
            stdout=subprocess.DEVNULL,
        )
        recorder.wait(timeout=15)
    recorder_pid = None


stopped = threading.Event()
errors = []
try:
    subprocess.run(
        [ADB, "-s", SERIAL, "emu", "avd", "name"], check=True, stdout=subprocess.DEVNULL
    )

    installed = subprocess.run(
        [ADB, "-s", SERIAL, "shell", "pm", "path", "com.novatech.terratech"],
        capture_output=True,
        text=True,
        check=False,
    ).stdout.strip()
    if installed:
        subprocess.run(
            [ADB, "-s", SERIAL, "shell", "pm", "clear", "com.novatech.terratech"],
            check=True,
            stdout=subprocess.DEVNULL,
        )
    sql(f"CREATE DATABASE `{DB}`")
    with (OUT / "catalog.log").open("w") as log:
        subprocess.run(
            backend("--demo-catalog"),
            env=env,
            stdout=log,
            stderr=subprocess.STDOUT,
            check=True,
        )
    api_log = (OUT / "api.log").open("w")
    process = subprocess.Popen(
        backend("--urls", f"http://127.0.0.1:{API_PORT}"),
        env=env,
        stdout=api_log,
        stderr=subprocess.STDOUT,
    )
    deadline = time.monotonic() + 45
    while time.monotonic() < deadline:
        if process.poll() is not None:
            raise RuntimeError("Local API stopped; inspect api.log")
        try:
            urllib.request.urlopen(
                f"http://127.0.0.1:{API_PORT}/swagger/v1/swagger.json", timeout=1
            ).close()
            break
        except Exception:
            time.sleep(0.5)
    else:
        raise RuntimeError("Local API did not become ready")

    def readings():
        try:
            while not stopped.wait(0.5):
                value = sql("SELECT id FROM devices ORDER BY id LIMIT 1", DB)
                if value:
                    with (OUT / "readings.log").open("w") as log:
                        subprocess.run(
                            backend("--demo-readings", "--device-id", value),
                            env=env,
                            stdout=log,
                            stderr=subprocess.STDOUT,
                            check=True,
                        )
                    return
        except Exception as error:
            errors.append(str(error))

    thread = threading.Thread(target=readings, daemon=True)
    thread.start()
    subprocess.run(
        [
            ADB,
            "-s",
            SERIAL,
            "shell",
            "settings",
            "put",
            "global",
            "window_animation_scale",
            "0",
        ],
        check=True,
    )
    subprocess.run(
        [
            ADB,
            "-s",
            SERIAL,
            "shell",
            "settings",
            "put",
            "global",
            "transition_animation_scale",
            "0",
        ],
        check=True,
    )
    if os.environ.get("TB1_RECORD_VIDEO") == "true":
        video_path.parent.mkdir(exist_ok=True)
        recorder = subprocess.Popen(
            [
                ADB,
                "-s",
                SERIAL,
                "shell",
                'screenrecord --bit-rate 2000000 --time-limit 180 /sdcard/Download/terratech-tb1.mp4 & recording_pid=$!; echo "$recording_pid"; wait "$recording_pid"',
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL,
            text=True,
        )
        recorder_pid = recorder.stdout.readline().strip()
        if not recorder_pid.isdigit() or int(recorder_pid) < 2:
            raise RuntimeError("Could not identify video recorder")

    if DRIVER == "artemis":
        with (OUT / "gradle.log").open("w") as log:
            subprocess.run(
                [
                    str(ROOT / "gradlew"),
                    ":app:assembleDebug",
                    f"-PTERRATECH_API_URL=http://10.0.2.2:{API_PORT}/",
                ],
                cwd=ROOT,
                stdout=log,
                stderr=subprocess.STDOUT,
                check=True,
            )
        subprocess.run(
            [
                ADB,
                "-s",
                SERIAL,
                "install",
                "-r",
                str(ROOT / "app/build/outputs/apk/debug/app-debug.apk"),
            ],
            check=True,
            stdout=subprocess.DEVNULL,
        )
        ui_env = os.environ.copy()
        ui_env.update(
            PYTHONPATH=str(ARTEMIS),
            ARTEMIS_DEVICE_ID=SERIAL,
            ARTEMIS_HIERARCHY_BACKEND="helper",
            ARTEMIS_HELPER_AUTO_INSTALL="false",
            ARTEMIS_DESKTOP_NOTIFY="false",
        )
        ui_env["PATH"] = (
            str(pathlib.Path(ADB).parent) + os.pathsep + ui_env.get("PATH", "")
        )
        with (OUT / "ui.log").open("w") as log:
            result = subprocess.run(
                [
                    str(ARTEMIS / ".venv/bin/python"),
                    str(ROOT / "scripts/verify-artemis-ui.py"),
                ],
                cwd=ARTEMIS,
                env=ui_env,
                stdout=log,
                stderr=subprocess.STDOUT,
            )
    else:
        with (OUT / "gradle.log").open("w") as log:
            result = subprocess.run(
                [
                    str(ROOT / "gradlew"),
                    ":app:connectedDebugAndroidTest",
                    f"-PTERRATECH_API_URL=http://10.0.2.2:{API_PORT}/",
                    "-Pandroid.testInstrumentationRunnerArguments.class=com.novatech.terratech.BackendJourneyTest",
                    "-Pandroid.testInstrumentationRunnerArguments.tb1BackendJourney=true",
                    "-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true",
                ],
                cwd=ROOT,
                stdout=log,
                stderr=subprocess.STDOUT,
            )
    stopped.set()
    thread.join(timeout=25)
    if errors:
        raise RuntimeError("; ".join(errors))
    if result.returncode:
        raise RuntimeError("Android journey failed; inspect driver logs")
    if DRIVER == "compose":
        reports = list(
            (ROOT / "app/build/outputs/androidTest-results/connected/debug").glob(
                "TEST-*.xml"
            )
        )
        if len(reports) != 1:
            raise RuntimeError("Expected one instrumented result report")
        shutil.copy2(reports[0], OUT / "instrumented-results.xml")
        for name in [
            "login",
            "registered-home",
            "map-polygon",
            "field-review",
            "profile",
            "fields",
            "sensor",
            "history",
            "reading-detail",
            "offline",
            "offline-recreated",
            "second-account",
            "home",
        ]:
            subprocess.run(
                [
                    ADB,
                    "-s",
                    SERIAL,
                    "pull",
                    f"/sdcard/Download/terratech-{name}.png",
                    str(OUT / f"{name}.png"),
                ],
                check=True,
                stdout=subprocess.DEVNULL,
            )

    boundary_points = int(
        sql("SELECT JSON_LENGTH(boundary) FROM fields ORDER BY id LIMIT 1", DB)
    )
    if boundary_points != 4:
        raise RuntimeError("Drawn polygon was not persisted by the backend")

    subprocess.run([ADB, "-s", SERIAL, "shell", "svc", "wifi", "disable"], check=True)
    subprocess.run([ADB, "-s", SERIAL, "shell", "svc", "data", "disable"], check=True)
    subprocess.run(
        [ADB, "-s", SERIAL, "shell", "am", "force-stop", "com.novatech.terratech"],
        check=True,
    )
    subprocess.run(
        [
            ADB,
            "-s",
            SERIAL,
            "shell",
            "am",
            "start",
            "-n",
            "com.novatech.terratech/.MainActivity",
        ],
        check=True,
        stdout=subprocess.DEVNULL,
    )
    deadline = time.monotonic() + 45
    while time.monotonic() < deadline:
        subprocess.run(
            [
                ADB,
                "-s",
                SERIAL,
                "shell",
                "uiautomator",
                "dump",
                "/sdcard/terratech-cold.xml",
            ],
            stdout=subprocess.DEVNULL,
            check=True,
        )
        tree = subprocess.check_output(
            [ADB, "-s", SERIAL, "shell", "cat", "/sdcard/terratech-cold.xml"], text=True
        )
        if "SIMULATED" in tree and "Parcela Norte" in tree and "Offline" in tree:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError(
            "Cold restart did not restore readings/selection with offline notice"
        )
    (OUT / "cold-restart.xml").write_text(tree)
    with (OUT / "cold-restart.png").open("wb") as image:
        subprocess.run(
            [ADB, "-s", SERIAL, "exec-out", "screencap", "-p"], stdout=image, check=True
        )
    if recorder is not None:
        finish_recording()
        subprocess.run(
            [
                ADB,
                "-s",
                SERIAL,
                "pull",
                "/sdcard/Download/terratech-tb1.mp4",
                str(video_path),
            ],
            check=True,
            stdout=subprocess.DEVNULL,
        )
    summary = {
        "driver": DRIVER,
        "database": DB,
        "environment": "Development",
        "configuration": "Debug",
        "device": "emulator",
        "journeyPassed": True,
        "coldProcessRestartOfflinePassed": True,
        "savedBoundaryPoints": boundary_points,
        "readingCount": int(sql("SELECT COUNT(*) FROM sensor_readings", DB)),
        "credentialsRecorded": False,
        "timestampUtc": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
    }
    (OUT / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps(summary))
finally:
    finish_recording()
    stopped.set()
    subprocess.run([ADB, "-s", SERIAL, "shell", "svc", "wifi", "enable"], check=False)
    subprocess.run([ADB, "-s", SERIAL, "shell", "svc", "data", "enable"], check=False)
    if process is not None:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()

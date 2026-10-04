#!/usr/bin/env python3
"""Explicit localhost-only Android/Development/MySQL integration. Preserves its new database."""
import json, os, pathlib, secrets, shutil, subprocess, threading, time, urllib.request, uuid
ROOT = pathlib.Path(__file__).resolve().parents[1]
BACKEND = ROOT.parents[1] / 'BackEnd' / 'NovaTech.TerraTech.Platform'
DOTNET = os.environ.get('TB1_DOTNET', '/private/tmp/terratech-review-runtime/dotnet/dotnet')
MYSQL = os.environ.get('TB1_MYSQL_CLI', '/usr/local/mysql/bin/mysql')
ADB = os.environ.get('TB1_ADB', str(pathlib.Path.home() / 'Library/Android/sdk/platform-tools/adb'))
DB = 'terratech_android_' + uuid.uuid4().hex
PORT = int(os.environ.get('TB1_MYSQL_PORT', '33308'))
API_PORT = int(os.environ.get('TB1_ANDROID_API_PORT', '55024'))
OUT = ROOT / 'docs/evidence/backend-journey'
OUT.mkdir(parents=True, exist_ok=True)
# This harness targets the dedicated local review runtime; no external/Production target is accepted.
env = os.environ.copy()
env.update(ASPNETCORE_ENVIRONMENT='Development', ConnectionStrings__DefaultConnection=f'server=127.0.0.1;port={PORT};user=root;database={DB}', TokenSettings__Secret=secrets.token_urlsafe(64))
if 'DOTNET_ROOT' not in env:
    env['DOTNET_ROOT'] = str(pathlib.Path(DOTNET).parent)
    env['DOTNET_CLI_HOME'] = '/private/tmp/terratech-review-runtime'
    env['NUGET_PACKAGES'] = '/private/tmp/terratech-review-runtime/nuget'
mysql = [MYSQL, '--protocol=TCP', '-h127.0.0.1', f'-P{PORT}', '-uroot', '-N', '-B']
def sql(query, database=None):
    command = mysql + (['-D', database] if database else []) + ['-e', query]
    return subprocess.check_output(command, text=True).strip()
def backend(*args):
    return [DOTNET, 'run', '--project', str(BACKEND), '-c', 'Debug', '--no-build', '--no-launch-profile', '--', *args]
process = None
stopped = threading.Event()
errors = []
try:
    sql(f'CREATE DATABASE `{DB}`')
    with (OUT / 'catalog.log').open('w') as log:
        subprocess.run(backend('--demo-catalog'), env=env, stdout=log, stderr=subprocess.STDOUT, check=True)
    api_log = (OUT / 'api.log').open('w')
    process = subprocess.Popen(backend('--urls', f'http://127.0.0.1:{API_PORT}'), env=env, stdout=api_log, stderr=subprocess.STDOUT)
    deadline = time.monotonic() + 45
    while time.monotonic() < deadline:
        if process.poll() is not None: raise RuntimeError('Local API stopped; inspect api.log')
        try:
            urllib.request.urlopen(f'http://127.0.0.1:{API_PORT}/swagger/v1/swagger.json', timeout=1).close()
            break
        except Exception: time.sleep(.5)
    else: raise RuntimeError('Local API did not become ready')
    def readings():
        try:
            while not stopped.wait(.5):
                value = sql('SELECT id FROM devices ORDER BY id LIMIT 1', DB)
                if value:
                    with (OUT / 'readings.log').open('w') as log:
                        subprocess.run(backend('--demo-readings', '--device-id', value), env=env, stdout=log, stderr=subprocess.STDOUT, check=True)
                    return
        except Exception as error: errors.append(str(error))
    thread = threading.Thread(target=readings, daemon=True)
    thread.start()
    subprocess.run([ADB, 'shell', 'settings', 'put', 'global', 'window_animation_scale', '0'], check=True)
    subprocess.run([ADB, 'shell', 'settings', 'put', 'global', 'transition_animation_scale', '0'], check=True)
    # Keep this evidence independent of default contract/cache suites.
    with (OUT / 'gradle.log').open('w') as log:
        result = subprocess.run([str(ROOT / 'gradlew'), ':app:connectedDebugAndroidTest', f'-PTERRATECH_API_URL=http://10.0.2.2:{API_PORT}/', '-Pandroid.testInstrumentationRunnerArguments.class=com.novatech.terratech.BackendJourneyTest', '-Pandroid.testInstrumentationRunnerArguments.tb1BackendJourney=true', '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'], cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
    stopped.set(); thread.join(timeout=25)
    if errors: raise RuntimeError('; '.join(errors))
    if result.returncode: raise RuntimeError('Android journey failed; inspect gradle.log')
    reports = list((ROOT / 'app/build/outputs/androidTest-results/connected/debug').glob('TEST-*.xml'))
    if len(reports) != 1: raise RuntimeError('Expected one instrumented result report')
    shutil.copy2(reports[0], OUT / 'instrumented-results.xml')
    for name in ['login', 'profile', 'fields', 'sensor', 'history', 'reading-detail', 'offline', 'offline-recreated', 'second-account', 'home']:
        subprocess.run([ADB, 'pull', f'/sdcard/Download/terratech-{name}.png', str(OUT / f'{name}.png')], check=True, stdout=subprocess.DEVNULL)
    # Actual cold process restart, with radios off and the database/saved session intact.
    subprocess.run([ADB, 'shell', 'svc', 'wifi', 'disable'], check=True)
    subprocess.run([ADB, 'shell', 'svc', 'data', 'disable'], check=True)
    subprocess.run([ADB, 'shell', 'am', 'force-stop', 'com.novatech.terratech'], check=True)
    subprocess.run([ADB, 'shell', 'am', 'start', '-n', 'com.novatech.terratech/.MainActivity'], check=True, stdout=subprocess.DEVNULL)
    deadline = time.monotonic() + 45
    while time.monotonic() < deadline:
        subprocess.run([ADB, 'shell', 'uiautomator', 'dump', '/sdcard/terratech-cold.xml'], stdout=subprocess.DEVNULL, check=True)
        tree = subprocess.check_output([ADB, 'shell', 'cat', '/sdcard/terratech-cold.xml'], text=True)
        if 'SIMULATED' in tree and 'Parcela Norte' in tree and 'Offline' in tree: break
        time.sleep(.5)
    else: raise RuntimeError('Cold restart did not restore readings/selection with offline notice')
    (OUT / 'cold-restart.xml').write_text(tree)
    with (OUT / 'cold-restart.png').open('wb') as image:
        subprocess.run([ADB, 'exec-out', 'screencap', '-p'], stdout=image, check=True)
    summary = {'database': DB, 'environment': 'Development', 'configuration': 'Debug', 'device': 'emulator', 'journeyPassed': True, 'coldProcessRestartOfflinePassed': True, 'readingCount': int(sql('SELECT COUNT(*) FROM sensor_readings', DB)), 'credentialsRecorded': False, 'timestampUtc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}
    (OUT / 'summary.json').write_text(json.dumps(summary, indent=2) + '\n')
    print(json.dumps(summary))
finally:
    stopped.set()
    subprocess.run([ADB, 'shell', 'svc', 'wifi', 'enable'], check=False)
    subprocess.run([ADB, 'shell', 'svc', 'data', 'enable'], check=False)
    if process is not None:
        process.terminate()
        try: process.wait(timeout=10)
        except subprocess.TimeoutExpired: process.kill()

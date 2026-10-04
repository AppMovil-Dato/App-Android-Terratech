#!/usr/bin/env python3
"""Package committed Android source and verified local evidence without SDK caches or secrets."""
import hashlib
import io
import json
import pathlib
import subprocess
import zipfile

root = pathlib.Path(__file__).resolve().parents[1]
if subprocess.check_output(['git', 'status', '--porcelain'], cwd=root, text=True).strip():
    raise SystemExit('Commit source changes before packaging a reviewable snapshot.')
commit = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=root, text=True).strip()
artifacts = root / 'artifacts'
apk = artifacts / 'TerraTech-TB1-debug.apk'
video = artifacts / 'TerraTech-TB1-emulator.mp4'
checks = json.loads((root / 'docs/evidence/checks/summary.json').read_text())
journey = json.loads((root / 'docs/evidence/backend-journey/summary.json').read_text())
if not apk.is_file() or not video.is_file():
    raise SystemExit('Generate the Debug APK and emulator video first.')
if hashlib.sha256(apk.read_bytes()).hexdigest() != checks['apkSha256']:
    raise SystemExit('APK differs from the recorded validation artifact.')
if checks['failed'] or checks['lintErrors'] or not journey['journeyPassed']:
    raise SystemExit('Validation is incomplete or failed.')
manifest = {
    'sourceCommit': commit,
    'configuration': 'Debug',
    'unitTestsPassed': checks['unitTestsPassed'],
    'independentInstrumentedTestsPassed': checks['instrumentedTestsPassed'],
    'integrationTestsPassed': 1,
    'physicalDeviceVerified': checks['physicalDeviceVerified'],
    'apiBaseUrl': checks['apiBaseUrl'],
    'files': {p.name: {'bytes': p.stat().st_size, 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in [apk, video]},
}
(artifacts / 'delivery-manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
source = subprocess.check_output(['git', 'archive', '--format=zip', 'HEAD'], cwd=root)
target = artifacts / 'TerraTech-TB1-android.zip'
with zipfile.ZipFile(io.BytesIO(source)) as tracked, zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as bundle:
    for entry in tracked.infolist():
        bundle.writestr('TerraTech/' + entry.filename, tracked.read(entry))
    for p in [apk, video, artifacts / 'delivery-manifest.json']:
        bundle.write(p, 'TerraTech/artifacts/' + p.name)
with zipfile.ZipFile(target) as bundle:
    if bundle.testzip(): raise SystemExit('ZIP checksum verification failed.')
print(json.dumps({'zip': str(target), 'bytes': target.stat().st_size, 'sourceCommit': commit}))

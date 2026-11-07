"""Prepara ferramentas portáteis do NÓS; não altera PATH, registro ou Python global."""
from pathlib import Path
import hashlib
import json
import re
import subprocess
import time
import urllib.request
import zipfile
import os
import sys

ROOT = Path(os.environ.get('NOS_CINEMA_TOOLS',str(Path.home()/'Documents'/'Codex'/'tools'/'nos-cinema')))
PYTHON = Path(sys.executable)
BASE = 'https://mirrors.dotsrc.org/blender/blender-release/Blender4.5/'
VERSION = '4.5.15'
ARCHIVE = f'blender-{VERSION}-windows-x64.zip'
ROOT.mkdir(parents=True, exist_ok=True)


def fetch(url, dest):
    dest = Path(dest)
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.exists() and dest.stat().st_size > 0:
        return
    request = urllib.request.Request(url, headers={'User-Agent': 'NOS-local-cinema/1.0'})
    with urllib.request.urlopen(request, timeout=90) as response, dest.open('wb') as output:
        total = int(response.headers.get('Content-Length', 0))
        count, announced = 0, 0
        start = time.monotonic()
        while True:
            block = response.read(1024*1024)
            if not block:
                break
            output.write(block)
            count += len(block)
            if count-announced >= 32*1024*1024:
                print(f'Download {dest.name}: {count//1048576}/{total//1048576} MiB', flush=True)
                announced = count
            # Limite de banda para reduzir impacto sobre outros trabalhos.
            delay = count/(10*1024*1024)-(time.monotonic()-start)
            if delay > 0:
                time.sleep(min(delay, .2))
    print(f'OK: {dest.name} ({count//1048576} MiB)', flush=True)


checksums = ROOT/f'blender-{VERSION}.sha256'
fetch(BASE+checksums.name, checksums)
expected = None
for line in checksums.read_text().splitlines():
    if line.strip().endswith(ARCHIVE):
        expected = line.split()[0]
if not expected or not re.fullmatch('[0-9a-fA-F]{64}', expected):
    raise SystemExit('Checksum do Blender não encontrado; instalação interrompida.')
archive = ROOT/ARCHIVE
fetch(BASE+ARCHIVE, archive)
actual = hashlib.file_digest(archive.open('rb'), 'sha256').hexdigest()
if actual.lower() != expected.lower():
    raise SystemExit('Checksum divergente; o Blender não será executado.')
blender = ROOT/f'blender-{VERSION}-windows-x64'/'blender.exe'
if not blender.exists():
    print('Extraindo Blender portátil...', flush=True)
    with zipfile.ZipFile(archive) as bundle:
        for entry in bundle.infolist():
            if not (ROOT/entry.filename).resolve().is_relative_to(ROOT.resolve()):
                raise SystemExit('Caminho inválido dentro do arquivo ZIP.')
        bundle.extractall(ROOT)
subprocess.run([str(blender), '--version'], check=True)

environment = ROOT/'voice-env'
voice_python = environment/'Scripts'/'python.exe'
if not voice_python.exists():
    subprocess.run([str(PYTHON), '-m', 'venv', str(environment)], check=True)
print('Preparando ferramentas de áudio no ambiente isolado...', flush=True)
log = ROOT/'audio-install.log'
with log.open('w') as stream:
    installed = subprocess.run([str(voice_python), '-m', 'pip', 'install', '--disable-pip-version-check',
        '--no-input', '--only-binary=:all:', 'numpy==2.5.4','tinysoundfont==0.3.7','soundfile==0.14.0'], stdout=stream, stderr=subprocess.STDOUT)
if installed.returncode:
    print('\n'.join(log.read_text(errors='replace').splitlines()[-25:]))
    raise SystemExit(installed.returncode)

manifest = {'blender': str(blender), 'blenderVersion': VERSION, 'blenderSha256': actual,
    'blenderSource': BASE+ARCHIVE, 'audioPython': str(voice_python),
    'audioPackages':{'tinysoundfont':'0.3.7','numpy':'2.5.4','soundfile':'0.14.0'}}
(ROOT/'tools.json').write_text(json.dumps(manifest, indent=2, ensure_ascii=False), encoding='utf-8')
fetch('https://raw.githubusercontent.com/makehumancommunity/makehuman/master/makehuman/data/3dobjs/base.obj',ROOT/'assets'/'makehuman-base.obj')
fetch('https://raw.githubusercontent.com/makehumancommunity/makehuman/master/LICENSE.md',ROOT/'assets'/'MAKEHUMAN-LICENSE.md')
print('Ferramentas locais prontas. Nenhuma alteração no jogo ou nas instalações existentes.', flush=True)

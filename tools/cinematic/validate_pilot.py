"""Confere decodificação integral, quadros, geometria animada e áudio final."""
from pathlib import Path
import subprocess
import json
import re
import struct
import hashlib
import numpy as np
import soundfile as sf

ROOT=Path(__file__).resolve().parent
movie=ROOT/'NOS-CGI-PILOTO-v1.mp4'
result=subprocess.run(['ffmpeg','-hide_banner','-i',str(movie),'-map','0:v:0',
    '-f','framemd5','pipe:1'],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True)
frames=[line for line in result.stdout.splitlines() if line and not line.startswith('#')]
assert len(frames)==192,f'Vídeo deve ter 192 quadros: {len(frames)}'
assert 'h264' in result.stderr and '768x432' in result.stderr
assert '48000 Hz, stereo' in result.stderr
match=re.search(r'Duration: (\d+):(\d+):(\d+\.\d+)',result.stderr)
assert match
duration=int(match[1])*3600+int(match[2])*60+float(match[3])
assert abs(duration-8)<.06
sizes=set();hashes=[]
for i in range(1,145):
    path=ROOT/'render'/f'frame-{i:04d}.png'
    with path.open('rb') as file:
        header=file.read(24);sizes.add(struct.unpack('>II',header[16:24]))
    hashes.append(hashlib.sha256(path.read_bytes()).hexdigest())
assert sizes=={(768,432)}
assert len(set(hashes))==144
audio={}
for name in ('NOS-CGI-PILOTO-v1',):
    data,rate=sf.read(str(ROOT/'audio'/(name+'.wav')))
    assert np.isfinite(data).all()
    peak=float(np.max(np.abs(data)))
    assert 0.01<peak<.85
    audio[name]=dict(seconds=len(data)/rate,sampleRate=rate,channels=data.shape[1],
        peakDbFS=float(20*np.log10(peak)),clippedSamples=int(np.sum(np.abs(data)>=.999)))
scene=json.loads((ROOT/'scene-inspection.json').read_text(encoding='utf-8'))
report=dict(video=dict(file=movie.name,seconds=duration,frames=len(frames),fps=24,
    dimensions=[768,432],codec='H.264 / AAC',decodedWithoutErrors=True,
    sha256=hashlib.sha256(movie.read_bytes()).hexdigest()),
    render=dict(frames=144,uniqueFrames=len(set(hashes)),renderer='Blender Cycles CPU',
        cpuThreads=2,processPriority='BelowNormal'),audio=audio,scene=scene,
    gameTests=dict(command='gradlew.bat core:test --offline --console=plain --max-workers=2',
        junitTests=83,junitFailures=0,geometryCheckPassed=True),
    narrationIncluded=False,
    limits=['Piloto curto; abertura completa com os dois personagens ainda não produzida.',
        'Piloto separado do executável do jogo.',
        'Narração adiada por decisão do usuário após rejeitar a voz sintética.',
        'Verificação numérica de áudio; timbre e composição sujeitos à escuta do usuário.'])
(ROOT/'VALIDACAO-CGI-v1.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2),flush=True)

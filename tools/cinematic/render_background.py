"""Renderização isolada, sem janela e com prioridade baixa no Windows."""
import os
import sys
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parent
TOOLS=Path(os.environ.get('NOS_CINEMA_TOOLS',str(Path.home()/'Documents'/'Codex'/'tools'/'nos-cinema')))
env=os.environ.copy()
env['BLENDER_USER_RESOURCES']=str(TOOLS/'blender-user')
command=[str(TOOLS/'blender-4.5.15-windows-x64'/'blender.exe'),
    '--background','--factory-startup','--threads','2','--python',str(ROOT/'cinematic_pilot.py'),
    '--','--width','768','--samples','12',*sys.argv[1:]]
with (ROOT/'animation-render.log').open('w',encoding='utf-8') as log:
    process=subprocess.Popen(command,stdout=log,stderr=subprocess.STDOUT,env=env,
        creationflags=subprocess.CREATE_NO_WINDOW|subprocess.BELOW_NORMAL_PRIORITY_CLASS)
    (ROOT/'render.pid').write_text(str(process.pid))
    print(f'Render PID {process.pid}; CPU: 2 threads; prioridade: baixa.',flush=True)
    code=process.wait()
    (ROOT/'render-exit.txt').write_text(str(code))
    print(f'Render terminou: {code}',flush=True)
    raise SystemExit(code)

"""Empacota a animação da água no arquivo Blender, sem scripts automáticos."""
import os
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parent
TOOLS=Path(os.environ.get('NOS_CINEMA_TOOLS',r'C:\Users\PC Gamer\Documents\Codex\tools\nos-cinema'))
env=os.environ.copy();env['NOS_CINEMA_WORK']=str(ROOT/'editable')
env['BLENDER_USER_RESOURCES']=str(TOOLS/'blender-user')
command=[str(TOOLS/'blender-4.5.15-windows-x64'/'blender.exe'),'--background','--factory-startup',
    '--threads','2','--python',str(ROOT/'cinematic_pilot.py'),'--','--save-only','--bake-water','--width','768','--samples','12']
with (ROOT/'editable-build.log').open('w',encoding='utf-8') as log:
    result=subprocess.run(command,stdout=log,stderr=subprocess.STDOUT,env=env,
        creationflags=subprocess.CREATE_NO_WINDOW|subprocess.BELOW_NORMAL_PRIORITY_CLASS)
print(f'Cena editável: código {result.returncode}',flush=True)
raise SystemExit(result.returncode)

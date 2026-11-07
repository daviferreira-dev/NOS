"""Piano amostrado gratuito, sem serviços de geração de áudio."""
from pathlib import Path
import urllib.request
import json
import hashlib
import os
ROOT=Path(os.environ.get('NOS_CINEMA_TOOLS',str(Path.home()/'Documents'/'Codex'/'tools'/'nos-cinema')))/'sound'
ROOT.mkdir(parents=True,exist_ok=True)
sources={
    'GeneralUser-GS.sf2':'https://raw.githubusercontent.com/mrbumpy409/GeneralUser-GS/main/GeneralUser-GS.sf2',
    'GeneralUser-LICENSE.txt':'https://raw.githubusercontent.com/mrbumpy409/GeneralUser-GS/main/documentation/LICENSE.txt',
    'TinySoundFont-LICENSE':'https://raw.githubusercontent.com/amberwhitehead/tinysoundfont-pybind/main/LICENSE',
}
manifest=[]
for name,url in sources.items():
    path=ROOT/name
    if not path.exists():
        request=urllib.request.Request(url,headers={'User-Agent':'NOSGame/0.1 (https://github.com/daviferreira-dev/NOS; local cinematic asset preparation)'})
        with urllib.request.urlopen(request,timeout=60) as response,path.open('wb') as output:
            while data:=response.read(1024*1024):output.write(data)
    manifest.append(dict(file=name,url=url,sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
    print(f'{name}: {path.stat().st_size} bytes',flush=True)
(ROOT/'sources.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')

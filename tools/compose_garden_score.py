"""Tema próprio do pátio: piano amostrado, pausas e variações, sem voz."""
from pathlib import Path
import os,json,subprocess
import numpy as np
import soundfile as sf
from tinysoundfont import Synth

ROOT=Path(__file__).resolve().parents[1]
TOOLS=Path(os.environ.get('NOS_CINEMA_TOOLS',r'C:\Users\PC Gamer\Documents\Codex\tools\nos-cinema'))
RATE=32000;DURATION=112
OUT=ROOT/'work'/'garden-score';OUT.mkdir(parents=True,exist_ok=True)
instrument=Synth(samplerate=RATE,gain=-7)
bank=instrument.sfload(str(TOOLS/'sound'/'GeneralUser-GS.sf2'))
instrument.program_select(0,bank,0,0)
instrument.program_select(1,bank,0,46)
chords=[(50,57,62,65),(46,53,60,65),(43,50,58,62),(45,52,57,62),
        (41,53,57,64),(48,55,62,67),(43,53,58,65),(45,52,58,61),
        (50,57,62,69),(46,53,60,65),(43,50,58,67),(50,57,64,69)]
events=[];notes=[]
def note(channel,pitch,start,length,velocity):
    notes.append(dict(channel=channel,pitch=pitch,time=start,duration=length,velocity=velocity))
    events.extend([(round(start*RATE),1,channel,pitch,velocity),(round((start+length)*RATE),0,channel,pitch,0)])
for section,chord in enumerate(chords):
    start=section*8.5+.3
    note(0,chord[0],start,6.8,32+(section%3)*3)
    for i,pitch in enumerate(chord[1:]):
        note(0,pitch,start+.06+i*.07,4.8+i*.4,24+(section+i)%5)
    # Motivo reconhecível com respostas e silêncios diferentes a cada frase.
    melody=[chord[2]+12,chord[3]+7,chord[1]+12,chord[3]+12]
    times=(1.52,3.83,5.12,6.65) if section%2==0 else (2.07,3.38,5.76,7.12)
    for i,(pitch,offset) in enumerate(zip(melody,times)):
        if (section+i)%5==0:continue
        note(0,pitch,start+offset,1.2+(i%2)*.9,28+(section*3+i*5)%12)
    if section in (2,5,8,10):
        note(1,chord[2]+12,start+4.55,3.2,20)
note(0,50,103.8,6.5,30);note(0,57,104.0,5.8,24);note(0,62,105.4,5.0,27)
events.sort();count=RATE*DURATION;mix=np.zeros((count,2),dtype=np.float32);cursor=0
for pos,on,channel,pitch,velocity in events+[(count,0,0,0,0)]:
    pos=min(pos,count)
    if pos>cursor:
        mix[cursor:pos]=np.frombuffer(instrument.generate_simple(pos-cursor),dtype=np.float32).reshape((-1,2));cursor=pos
    if on:instrument.noteon(channel,pitch,velocity)
    elif pitch:instrument.noteoff(channel,pitch)
dry=mix.copy()
for i,delay in enumerate((.037,.053,.079,.113,.163,.229,.307)):
    offset=round(delay*RATE);mix[offset:]+=(.10*np.exp(-i*.3))*dry[:-offset,::-1 if i%2 else 1]
fade=RATE*3;mix[:fade]*=np.linspace(0,1,fade)[:,None]
fade=RATE*6;mix[-fade:]*=np.linspace(1,0,fade)[:,None]
sf.write(str(OUT/'mix.wav'),mix,RATE,subtype='PCM_24')
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(OUT/'mix.wav'),
    '-af','highpass=f=50,loudnorm=I=-24:TP=-6:LRA=10','-ar',str(RATE),
    '-c:a','libvorbis','-q:a','4',str(ROOT/'assets'/'audio'/'o-jardim-escuta.ogg')],check=True)
(OUT/'score.json').write_text(json.dumps(notes,ensure_ascii=False,indent=2),encoding='utf-8')
print('Tema do jardim: 112 segundos, piano/harpa amostrados, sem voz. Nenhum som reproduzido.')

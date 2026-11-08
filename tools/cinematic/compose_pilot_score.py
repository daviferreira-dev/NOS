"""Prévia instrumental: piano amostrado, ambiente discreto e nenhuma voz."""
from pathlib import Path
import json
import os
import subprocess
import numpy as np
import soundfile as sf
from tinysoundfont import Synth

ROOT=Path(__file__).resolve().parent
TOOLS=Path(os.environ.get('NOS_CINEMA_TOOLS',str(Path.home()/'Documents'/'Codex'/'tools'/'nos-cinema')))
OUT=ROOT/'audio';OUT.mkdir(exist_ok=True)
RATE=48000
score=json.loads((ROOT/'score.json').read_text(encoding='utf-8'))
seconds=score['seconds'];n=round(RATE*seconds)
synth=Synth(samplerate=RATE,gain=-8)
sfid=synth.sfload(str(TOOLS/'sound'/'GeneralUser-GS.sf2'))
synth.program_select(0,sfid,0,0)
events=[]
for note in score['notes']:
    events.extend([(round(note['time']*RATE),1,note['pitch'],note['velocity']),
        (round((note['time']+note['duration'])*RATE),0,note['pitch'],0)])
events.sort()
music=np.zeros((n,2),dtype=np.float64);cursor=0
for position,on,pitch,velocity in events+[(n,0,0,0)]:
    position=min(position,n)
    if position>cursor:
        block=np.frombuffer(synth.generate_simple(position-cursor),dtype=np.float32).reshape((-1,2))
        music[cursor:position]=block;cursor=position
    if on:synth.noteon(0,pitch,velocity)
    elif pitch:synth.noteoff(0,pitch)

# Reflexões curtas e irregulares: cauda baixa, sem eco rítmico nem loop musical.
dry=music.copy()
for i,delay in enumerate((.037,.053,.071,.097,.131,.173,.229,.307,.389,.487)):
    offset=round(delay*RATE)
    music[offset:]+=(.09*np.exp(-i*.18))*dry[:-offset,::-1 if i%2 else 1]

rng=np.random.default_rng(1707)
ambient=np.zeros_like(music)
for channel in range(2):
    noise=rng.normal(size=n);hz=np.fft.rfftfreq(n,1/RATE)
    mask=(1/(1+(170/np.maximum(hz,1))**6))/(1+(hz/1700)**6)
    water=np.fft.irfft(np.fft.rfft(noise)*mask,n=n)
    water/=np.std(water)
    t=np.arange(n)/RATE
    ambient[:,channel]=water*.0025*(.75+.12*np.sin(t*.8+channel)+.10*np.sin(t*2.3))
mix=music+ambient
fade=round(.35*RATE);mix[:fade]*=np.linspace(0,1,fade)[:,None]
fade=round(1.15*RATE);mix[-fade:]*=np.linspace(1,0,fade)[:,None]
sf.write(str(OUT/'instrumental-raw.wav'),mix,RATE,subtype='PCM_24')
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(OUT/'instrumental-raw.wav'),
    '-af','highpass=f=55,lowpass=f=11500,loudnorm=I=-28:TP=-9:LRA=8',
    '-ar',str(RATE),'-ac','2','-c:a','pcm_s24le',str(OUT/'NOS-CGI-PILOTO-v1.wav')],check=True)
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(OUT/'NOS-CGI-PILOTO-v1.wav'),
    '-c:a','libmp3lame','-q:a','2',str(OUT/'TRILHA-PILOTO-v1.mp3')],check=True)
lines=json.loads((ROOT/'narration.json').read_text(encoding='utf-8'))
(ROOT/'NOS-CGI-PILOTO-v1.srt').write_text('1\n00:00:00,800 --> 00:00:04,200\n'+lines[0]['text']+'\n',encoding='utf-8')
print('Trilha instrumental concluída: piano amostrado e ambiente, sem voz sintética.',flush=True)

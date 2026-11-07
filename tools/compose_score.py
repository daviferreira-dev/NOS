"""Compõe e sintetiza dois temas originais do NÓS; não utiliza gravações externas."""
from pathlib import Path
import math
import wave
import subprocess
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
RATE = 32000
RNG = np.random.default_rng(1707)


def frequency(note):
    return 440 * 2 ** ((note - 69) / 12)


def compose(name, bpm, cycles, intimate=False):
    beat = 60 / bpm
    bar = beat * 4
    chords = [(50,57,60,64,69), (46,53,57,62,65), (41,48,52,55,60),
              (48,55,60,62,67), (43,50,53,57,62), (50,57,60,64,69),
              (46,53,57,62,65), (45,52,57,61,64)]
    melody = [(76,74,69), (77,74,72), (76,72,67), (74,72,67),
              (74,69,65), (76,77,81), (77,76,74), (73,76,69)]
    length = round(cycles * 8 * bar * RATE)
    dry = np.zeros((length, 2), np.float32)
    pads = np.zeros_like(dry)

    def add(target, signal, start, pan, gain):
        indices = (np.arange(len(signal)) + round(start * RATE)) % length
        target[indices, 0] += signal * gain * math.sqrt((1-pan)/2)
        target[indices, 1] += signal * gain * math.sqrt((1+pan)/2)

    def piano(note, start, velocity, pan):
        t = np.arange(round(RATE * (5 if note < 60 else 4)), dtype=np.float32) / RATE
        f = frequency(note)
        tone = np.zeros_like(t)
        for partial, strength in [(1,1), (2,.32), (3,.16), (4,.055), (5,.024), (7,.008)]:
            decay = np.exp(-t * (.64 + partial * .23))
            phase = 2*np.pi*f*partial*(1+.00011*partial*partial)*t
            tone += strength * np.sin(phase + .002*np.sin(t*5.7)) * decay
        tone *= (1-np.exp(-t/0.008)) * np.minimum(1, (len(t)/RATE-t)/.3)
        add(dry, tone, start, pan, velocity * .11)

    for cycle in range(cycles):
        for step, chord in enumerate(chords):
            start = (cycle*8+step)*bar
            # Cama harmônica lenta: tons ligeiramente desafinados, sem ataque abrupto.
            for voice, note in enumerate(chord[:4]):
                t = np.arange(round((bar+5)*RATE), dtype=np.float32)/RATE
                env = np.sin(np.pi * np.clip(t/(bar+5),0,1)) ** 2
                f = frequency(note)
                pad = (np.sin(2*np.pi*f*.9991*t) + np.sin(2*np.pi*f*1.0009*t+.3))*.5
                pad += .13*np.sin(2*np.pi*2*f*t)
                add(pads, pad*env, start-2.5, -.55+voice*.35, .019 if intimate else .024)
            piano(chord[0]-12, start+.03, .6, -.2)
            rhythm = [(.15,1), (1.5,2), (2.55,3)] if not intimate else [(.25,1), (2.6,3)]
            for offset, voice in rhythm:
                piano(chord[voice], start+offset*beat+RNG.uniform(-.012,.012), .48+RNG.uniform(0,.16), -.35+voice*.18)
            notes = melody[step]
            if cycle == 1:
                notes = (notes[1],notes[0],notes[2])
            for n, offset in zip(notes, [.7, 2.05, 3.35]):
                if intimate and offset == 2.05 and step%2:
                    continue
                piano(n, start+offset*beat, .47 if intimate else .62, .25)
            if cycle == cycles-1 and step in (2,5,6) and not intimate:
                piano(notes[0]+12, start+2.9*beat, .19, -.25)

    # Reflexos estéreo circulares: as caudas continuam na volta do loop.
    wet = np.zeros_like(dry)
    for delay, gain in [(.113,.18),(.197,.15),(.307,.12),(.461,.10),(.719,.075),(1.031,.055),(1.417,.035),(2.113,.018)]:
        wet += np.roll(dry[:, ::-1], round(delay*RATE), axis=0) * gain
    music = dry + wet + pads
    music -= music.mean(axis=0)
    music *= .77 / max(.01, float(np.max(np.abs(music))))
    peak = float(np.max(np.abs(music)))
    rms = float(np.sqrt(np.mean(music**2)))
    out = ROOT/'assets'/'audio'
    out.mkdir(parents=True, exist_ok=True)
    scratch = ROOT/'work'/f'{name}.wav'
    with wave.open(str(scratch), 'wb') as wav:
        wav.setnchannels(2); wav.setsampwidth(2); wav.setframerate(RATE)
        wav.writeframes((music*32767).astype('<i2').tobytes())
    subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(scratch),
                    '-c:a','libvorbis','-q:a','5',str(out/f'{name}.ogg')],check=True)
    print(f'{name}: {length/RATE:.2f}s, stereo {RATE}Hz, peak={peak:.3f}, RMS={rms:.3f}')


if __name__ == '__main__':
    compose('entre-margens',72,3)
    compose('o-que-permanece',64,2,True)

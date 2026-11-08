"""Monta o render real, a trilha e a marca; não altera a abertura do jogo."""
from pathlib import Path
import argparse
import subprocess
import json

ROOT=Path(__file__).resolve().parent
PROJECT=Path(r'C:\Users\PC Gamer\OneDrive\Documentos\ChatGPT\NÓS (GAME EM JAVA)')
FONT=Path(r'C:\Users\PC Gamer\Documents\Codex\2026-11-06\instale-o-jdk-17-por-exemplo\work\atkinsonhyperlegible.ttf')
parser=argparse.ArgumentParser();parser.add_argument('--test',action='store_true');args=parser.parse_args()
count=96 if args.test else 144
for frame in range(1,count+1):
    assert (ROOT/'render'/f'frame-{frame:04d}.png').is_file(),f'Falta quadro {frame}'
seconds=count/24
label='assembly-test' if args.test else 'NOS-CGI-PILOTO-v1'
lines=json.loads((ROOT/'narration.json').read_text(encoding='utf-8'))
(ROOT/'subtitle-line.txt').write_text(lines[0]['text'],encoding='utf-8')

def filterpath(path):
    return str(path).replace('\\','/').replace(':','\\:').replace("'","\\'")

filters=(
 f"[0:v]trim=end_frame={count},setpts=PTS-STARTPTS,setsar=1,format=yuv420p,"
 f"drawtext=fontfile='{filterpath(FONT)}':textfile='{filterpath(ROOT/'subtitle-line.txt')}':"
 "fontsize=23:fontcolor=0xf9f2e6:borderw=1.4:bordercolor=0x171a22@0.85:"
 "shadowcolor=black@0.65:shadowx=1:shadowy=2:x=(w-tw)/2:y=h-48:enable='between(t,0.8,4.2)',"
 f"fade=t=in:st=0:d=0.3,fade=t=out:st={max(0,seconds-.4)}:d=0.4[shot];"
 "[1:v]format=rgba,scale=360:-2[logo];"
 "[3:v][logo]overlay=x=(W-w)/2:y=(H-h)/2,format=yuv420p,setsar=1,"
 "fade=t=in:st=0:d=0.35,fade=t=out:st=1.7:d=0.3[title];"
 "[shot][title]concat=n=2:v=1:a=0[outv]"
)
command=['ffmpeg','-hide_banner','-loglevel','warning','-y','-threads','2',
    '-framerate','24','-i',str(ROOT/'render'/'frame-%04d.png'),
    '-loop','1','-framerate','24','-i',str(PROJECT/'assets'/'art'/'logo.png'),
    '-i',str(ROOT/'audio'/'NOS-CGI-PILOTO-v1.wav'),
    '-f','lavfi','-i','color=c=0xf5ecd8:s=768x432:r=24:d=2',
    '-filter_complex_threads','1','-filter_complex',filters,
    '-map','[outv]','-map','2:a','-t',str(seconds+2),
    '-c:v','libx264','-crf','17','-preset','medium','-threads','2',
    '-c:a','aac','-b:a','192k','-ar','48000','-movflags','+faststart',str(ROOT/(label+'.mp4'))]
subprocess.run(command,check=True)
print(str(ROOT/(label+'.mp4')))

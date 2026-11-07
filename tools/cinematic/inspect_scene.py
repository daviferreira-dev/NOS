"""Verificações da cena salva, carregada sem executar o script de produção."""
import bpy
import json
import sys
from pathlib import Path
import numpy as np
import math

OUT=Path(sys.argv[sys.argv.index('--')+1])
scene=bpy.context.scene
water=bpy.data.objects['Superfície animada']
rig=next(obj for obj in bpy.data.objects if obj.type=='ARMATURE')
positions=[];surfaces=[];fingers=[]
for frame in (1,80,144):
    scene.frame_set(frame);bpy.context.view_layer.update()
    bone=rig.pose.bones['Médio2']
    positions.append(list(rig.matrix_world@bone.head))
    fingers.append(list(bone.rotation_euler))
    evaluated=water.evaluated_get(bpy.context.evaluated_depsgraph_get())
    mesh=evaluated.to_mesh()
    co=np.empty(len(mesh.vertices)*3,dtype=np.float32)
    mesh.vertices.foreach_get('co',co);surfaces.append(co.reshape((-1,3))[:,2])
    evaluated.to_mesh_clear()
water_change=float(np.max(np.abs(surfaces[0]-surfaces[1])))
hand_change=float(np.linalg.norm(np.asarray(positions[0])-np.asarray(positions[1])))
assert water_change>.0001,'Água sem animação no arquivo salvo'
assert hand_change>.02,'Mão sem deslocamento no arquivo salvo'
assert len(water.data.shape_keys.key_blocks)>50
external_images=[image.filepath for image in bpy.data.images if image.source=='FILE' and not image.packed_file]
assert not external_images,external_images
hyperextensions=[]
for frame in range(1,145):
    scene.frame_set(frame)
    for digit in ('Indicador','Médio','Anelar','Mínimo'):
        for joint in (1,2):
            angle=rig.pose.bones[digit+str(joint)].rotation_euler.x
            if angle>math.radians(1):hyperextensions.append(dict(frame=frame,bone=digit+str(joint),degrees=math.degrees(angle)))
assert not hyperextensions,f'Falange dobrando para trás: {hyperextensions[:3]}'
report=dict(frames=[scene.frame_start,scene.frame_end],fps=scene.render.fps,
    render=[scene.render.resolution_x,scene.render.resolution_y],bones=len(rig.data.bones),
    handPositions=positions,fingerRotations=fingers,handDisplacementMeters=hand_change,
    waterDisplacementMeters=water_change,waterKeys=len(water.data.shape_keys.key_blocks),
    externalImages=external_images,needsAutoRunScripts=False,
    fingerHingeChecks=dict(framesChecked=144,backwardBends=hyperextensions,passed=True))
OUT.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report),flush=True)

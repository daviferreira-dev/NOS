"""Ensaio CGI original do NÓS: mão articulada, lã, margem e água animada.

Execute no Blender portátil em background. Render CPU, duas threads, sem interface.
"""
import bpy
import math
import random
import sys
import argparse
import os
from pathlib import Path
from mathutils import Vector
import numpy as np

ROOT=Path(os.environ.get('NOS_CINEMA_WORK',str(Path(__file__).resolve().parent)))
parser=argparse.ArgumentParser()
parser.add_argument('--still',action='store_true')
parser.add_argument('--preview-frame',type=int,default=80)
parser.add_argument('--save-only',action='store_true')
parser.add_argument('--bake-water',action='store_true')
parser.add_argument('--width',type=int,default=960)
parser.add_argument('--samples',type=int,default=24)
parser.add_argument('--start',type=int,default=1)
parser.add_argument('--end',type=int,default=144)
args=parser.parse_args(sys.argv[sys.argv.index('--')+1:] if '--' in sys.argv else [])
random.seed(1707)
bpy.ops.wm.read_factory_settings(use_empty=True)
scene=bpy.context.scene
scene.render.engine='CYCLES'
scene.cycles.device='CPU'
scene.cycles.samples=args.samples
scene.cycles.use_denoising=True
scene.cycles.adaptive_threshold=.045
scene.cycles.max_bounces=6
scene.cycles.transmission_bounces=4
scene.cycles.volume_bounces=0
scene.render.threads_mode='FIXED';scene.render.threads=2
scene.render.use_persistent_data=True
scene.render.resolution_x=args.width;scene.render.resolution_y=round(args.width*9/16)
scene.render.resolution_percentage=100
scene.render.fps=24;scene.frame_start=1;scene.frame_end=144
scene.render.image_settings.file_format='PNG'
scene.render.image_settings.color_mode='RGB'
scene.view_settings.view_transform='AgX'
scene.view_settings.look='AgX - Medium High Contrast'
scene.view_settings.exposure=-.5
scene.world=bpy.data.worlds.new('Ar de manhã')
scene.world.use_nodes=True
scene.world.node_tree.nodes['Background'].inputs['Color'].default_value=(.19,.23,.32,1)
scene.world.node_tree.nodes['Background'].inputs['Strength'].default_value=.18
sky=scene.world.node_tree.nodes.new('ShaderNodeTexSky');sky.sky_type='NISHITA';sky.sun_elevation=math.radians(12);sky.sun_rotation=math.radians(130);sky.sun_disc=False;sky.dust_density=3
scene.world.node_tree.links.new(sky.outputs['Color'],scene.world.node_tree.nodes['Background'].inputs['Color'])


def material(name,color,rough=.5,subsurface=0):
    mat=bpy.data.materials.new(name);mat.use_nodes=True
    bs=mat.node_tree.nodes.get('Principled BSDF')
    bs.inputs['Base Color'].default_value=(*color,1)
    bs.inputs['Roughness'].default_value=rough
    bs.inputs['Subsurface Weight'].default_value=subsurface
    return mat,bs


skin,bs=material('Pele — Sombra',(.40,.215,.145),.62,.18)
bs.inputs['Subsurface Scale'].default_value=.0015
bs.inputs['Subsurface Radius'].default_value=(1,.45,.25)
nodes=skin.node_tree.nodes;links=skin.node_tree.links
noise=nodes.new('ShaderNodeTexNoise');noise.inputs['Scale'].default_value=730
noise.inputs['Detail'].default_value=2
bump=nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.06;bump.inputs['Distance'].default_value=.00005
links.new(noise.outputs['Fac'],bump.inputs['Height']);links.new(bump.outputs['Normal'],bs.inputs['Normal'])
variation=nodes.new('ShaderNodeTexNoise');variation.inputs['Scale'].default_value=55;variation.inputs['Detail'].default_value=3
skin_ramp=nodes.new('ShaderNodeValToRGB');skin_ramp.color_ramp.elements[0].position=.1;skin_ramp.color_ramp.elements[0].color=(.38,.195,.131,1)
skin_ramp.color_ramp.elements[1].position=.9;skin_ramp.color_ramp.elements[1].color=(.42,.225,.16,1)
links.new(variation.outputs['Fac'],skin_ramp.inputs['Fac']);links.new(skin_ramp.outputs['Color'],bs.inputs['Base Color'])
nails,_=material('Unhas discretas',(.49,.29,.22),.38,.04)
wool,bs=material('Tricô índigo',(.095,.077,.145),.92)
bs.inputs['Sheen Weight'].default_value=.5
nodes=wool.node_tree.nodes;links=wool.node_tree.links
coord=nodes.new('ShaderNodeTexCoord')
mapping=nodes.new('ShaderNodeVectorMath');mapping.operation='MULTIPLY';mapping.inputs[1].default_value=(600,600,600)
links.new(coord.outputs['Generated'],mapping.inputs[0])
texture=nodes.new('ShaderNodeTexNoise');texture.inputs['Scale'].default_value=1.0;texture.inputs['Detail'].default_value=3
links.new(mapping.outputs['Vector'],texture.inputs['Vector'])
bump=nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.35;bump.inputs['Distance'].default_value=.00035
links.new(texture.outputs['Fac'],bump.inputs['Height']);links.new(bump.outputs['Normal'],bs.inputs['Normal'])
thread,_=material('Fios do punho',(.13,.105,.19),.95)
stone,bs=material('Pedra clara',(.39,.345,.265),.78)
nodes=stone.node_tree.nodes;links=stone.node_tree.links
noise=nodes.new('ShaderNodeTexNoise');noise.inputs['Scale'].default_value=37;noise.inputs['Detail'].default_value=4
ramp=nodes.new('ShaderNodeValToRGB');ramp.color_ramp.elements[0].color=(.15,.145,.12,1);ramp.color_ramp.elements[1].color=(.50,.445,.34,1)
links.new(noise.outputs['Fac'],ramp.inputs['Fac']);links.new(ramp.outputs['Color'],bs.inputs['Base Color'])
bump=nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.34;bump.inputs['Distance'].default_value=.002
links.new(noise.outputs['Fac'],bump.inputs['Height']);links.new(bump.outputs['Normal'],bs.inputs['Normal'])
sand,_=material('Areia sob a água',(.10,.12,.15),.88)
moss,_=material('Musgo na margem',(.095,.12,.035),1)
reed,_=material('Juncos',(.18,.23,.07),.8)
petal,_=material('Pétalas pálidas',(.67,.57,.72),.62)


def mesh_object(name,verts,faces,mat):
    data=bpy.data.meshes.new(name);data.from_pydata(verts,[],faces);data.update()
    obj=bpy.data.objects.new(name,data);scene.collection.objects.link(obj)
    obj.data.materials.append(mat)
    for polygon in data.polygons:polygon.use_smooth=True
    return obj


def loft(name,sections,mat,n=32,cap=True):
    verts=[]
    for y,w,h,z,x in sections:
        for i in range(n):
            a=2*math.pi*i/n
            verts.append((x+w*math.cos(a),y,z+h*math.sin(a)))
    faces=[]
    for row in range(len(sections)-1):
        for i in range(n):
            a=row*n+i;b=row*n+(i+1)%n
            faces.append((a,b,b+n,a+n))
    if cap:faces.append(tuple(reversed(range(n))));faces.append(tuple((len(sections)-1)*n+i for i in range(n)))
    return mesh_object(name,verts,faces,mat)


def sphere(name,location,scale,mat,detail=2):
    bpy.ops.mesh.primitive_ico_sphere_add(subdivisions=detail,radius=1,location=location)
    obj=bpy.context.object;obj.name=name;obj.scale=scale;obj.data.materials.append(mat)
    for p in obj.data.polygons:p.use_smooth=True
    return obj


root=bpy.data.objects.new('Gesto de Sombra',None);scene.collection.objects.link(root)
# Malha anatômica CC0 do MakeHuman, somente a mão e parte do punho.
raw_vertices=[];raw_faces=[];groups={};group=''
tools_root=Path(os.environ.get('NOS_CINEMA_TOOLS',r'C:\Users\PC Gamer\Documents\Codex\tools\nos-cinema'))
source=tools_root/'assets'/'makehuman-base.obj'
for line in source.read_text().splitlines():
    fields=line.split()
    if not fields:continue
    if fields[0]=='v':raw_vertices.append(Vector(tuple(map(float,fields[1:4]))))
    elif fields[0]=='g':group=fields[1];groups.setdefault(group,set())
    elif fields[0]=='f':
        face=[int(v.split('/')[0])-1 for v in fields[1:]];groups[group].update(face)
        if group=='body':raw_faces.append(face)
def joint(name):
    ids=groups['joint-l-'+name]
    return sum((raw_vertices[i] for i in ids),Vector())/len(ids)
origin=joint('hand')
forward=(joint('finger-3-4')-origin).normalized()
side=joint('finger-2-1')-joint('finger-5-1');side=(side-forward*side.dot(forward)).normalized()
normal=side.cross(forward).normalized()
def local(pos):
    d=pos-origin
    return Vector((d.dot(side)*.1,d.dot(forward)*.1,d.dot(normal)*.1))
converted=[local(v) for v in raw_vertices]
selected=[face for face in raw_faces if all(converted[i].y>-.065 and converted[i].y<.25
    and abs(converted[i].x)<.13 and abs(converted[i].z)<.075 for i in face)]
used=sorted({i for face in selected for i in face});lookup={old:new for new,old in enumerate(used)}
hand=mesh_object('Mão anatômica — MakeHuman CC0',[tuple(converted[i]) for i in used],
    [tuple(lookup[i] for i in face) for face in selected],skin)
digit_info=[];digit_joints={}
for name,number,radius in [('Polegar',1,.011),('Indicador',2,.009),('Médio',3,.0095),('Anelar',4,.009),('Mínimo',5,.007)]:
    points=[local(joint(f'finger-{number}-{i}')) for i in (1,2,3,4)]
    digit_joints[name]=points;digit_info.append((name,points[0],points[-1],radius))
print('Malha anatômica:',len(used),'vértices,',len(selected),'faces',flush=True)

# Esqueleto original: palma e três falanges por dedo.
armature=bpy.data.armatures.new('Ossos da mão');rig=bpy.data.objects.new('Rig da mão',armature);scene.collection.objects.link(rig)
bpy.context.view_layer.objects.active=rig;rig.select_set(True);hand.select_set(False)
bpy.ops.object.mode_set(mode='EDIT')
base=armature.edit_bones.new('Palma');base.head=(0,0,0);base.tail=(0,.08,0)
for name,start,end,radius in digit_info:
    parent=base
    for j in range(3):
        bone=armature.edit_bones.new(name+str(j));bone.head=digit_joints[name][j];bone.tail=digit_joints[name][j+1]
        bone.align_roll(Vector((0,0,1)))
        bone.parent=parent;parent=bone
bpy.ops.object.mode_set(mode='OBJECT')
groups={name:hand.vertex_groups.new(name=name) for name in armature.bones.keys()}
for v in hand.data.vertices:
    pos=v.co;best=None
    for name,start,end,radius in digit_info:
        line=end-start;t=(pos-start).dot(line)/line.length_squared
        dist=(pos-(start+line*max(0,min(1,t)))).length
        if t>.015 and dist<radius*1.6 and (best is None or dist<best[0]):best=(dist,name,t)
    if best is None:groups['Palma'].add([v.index],1,'REPLACE');continue
    _,name,t=best
    if t<.12:
        weight=max(0,t/.12);groups['Palma'].add([v.index],1-weight,'REPLACE');groups[name+'0'].add([v.index],weight,'REPLACE')
    else:
        u=max(0,min(2,t*3-.5));a=int(u);b=min(2,a+1);f=u-a
        groups[name+str(a)].add([v.index],1-f,'REPLACE')
        if b!=a:groups[name+str(b)].add([v.index],f,'REPLACE')
modifier=hand.modifiers.new('Articulação','ARMATURE');modifier.object=rig
hand.parent=root;rig.parent=root
sub=hand.modifiers.new('Pele suave','SUBSURF');sub.levels=2;sub.render_levels=2

sleeve=loft('Manga larga',[(y,rx,rz,z,0) for y,rx,rz,z in [(-.30,.073,.061,.008),(-.21,.064,.054,.005),
    (-.12,.053,.047,.006),(-.055,.045,.036,.003),(-.034,.038,.029,0),(-.006,.036,.027,0)]],wool,64,cap=False)
sub=sleeve.modifiers.new('Dobras suaves','SUBSURF');sub.levels=2
sleeve.parent=root
cuff=loft('Punho do suéter',[(-.045,.041,.030,0,0),(-.038,.040,.030,0,0),(-.014,.037,.0275,0,0),(-.003,.036,.027,0,0)],wool,64,cap=False)
cuff.parent=root
# Malha dos pontos do punho, construída como uma única curva para manter o render leve.
knit=bpy.data.curves.new('Pontos de tricô','CURVE');knit.dimensions='3D';knit.bevel_depth=.00035;knit.bevel_resolution=1
for row in range(9):
    for col in range(40):
        spline=knit.splines.new('POLY');spline.points.add(6)
        for i,point in enumerate(spline.points):
            t=i/6;a=2*math.pi*(col+(t-.5)*.75)/40
            y=-.040+row*.004+math.sin(math.pi*t)*.003
            rx=.0385+(1-((y+.04)/.04))*.0015;rz=.0285
            point.co=(rx*math.cos(a),y,rz*math.sin(a),1)
knit_object=bpy.data.objects.new('Pontos visíveis',knit);scene.collection.objects.link(knit_object);knit_object.data.materials.append(thread);knit_object.parent=root

# O cordão marfim do pulso mantém o detalhe da referência aprovada.
cord_material,_=material('Cordão marfim',(.62,.52,.37),.94)
cross_section=[v.co for v in hand.data.vertices if .002<v.co.y<.012]
if cross_section:
    xmin,xmax=min(v.x for v in cross_section),max(v.x for v in cross_section)
    zmin,zmax=min(v.z for v in cross_section),max(v.z for v in cross_section)
    cx,cz=(xmin+xmax)/2,(zmin+zmax)/2
    rx,rz=(xmax-xmin)/2+.0012,(zmax-zmin)/2+.0012
    cords=bpy.data.curves.new('Dois fios no pulso','CURVE');cords.dimensions='3D';cords.bevel_depth=.00065;cords.bevel_resolution=2
    for band in range(2):
        spline=cords.splines.new('POLY');spline.points.add(95);spline.use_cyclic_u=True
        for i,point in enumerate(spline.points):
            angle=2*math.pi*i/96
            point.co=(cx+rx*math.cos(angle),.008+band*.0022+.0004*math.sin(angle*3),cz+rz*math.sin(angle),1)
    obj=bpy.data.objects.new('Cordão da referência',cords);scene.collection.objects.link(obj);cords.materials.append(cord_material);obj.parent=root

fibers=bpy.data.curves.new('Fibras finas do tricô','CURVE');fibers.dimensions='3D';fibers.bevel_depth=.000028;fibers.bevel_resolution=0
fiber_random=random.Random(7731)
for i in range(650):
    angle=fiber_random.uniform(0,math.tau);y=fiber_random.uniform(-.042,-.005)
    rx=.0385+(1-((y+.04)/.04))*.0015;rz=.0285
    start=Vector((rx*math.cos(angle),y,rz*math.sin(angle)))
    normal=Vector((math.cos(angle),0,math.sin(angle)))
    length=fiber_random.uniform(.0004,.0014)
    spline=fibers.splines.new('POLY');spline.points.add(2)
    for j,point in enumerate(spline.points):
        co=start+normal*(length*j/2)+Vector((0,(j/2)**2*.00035,0))
        point.co=(*co,1)
obj=bpy.data.objects.new('Penugem do punho',fibers);scene.collection.objects.link(obj);fibers.materials.append(thread);obj.parent=root

for frame,z,angle,y in [(1,.126,-12,-.040),(40,.106,-17,-.037),(72,.089,-23,-.030),(88,.089,-23,-.030),
                         (110,.101,-19,-.034),(144,.128,-13,-.043)]:
    root.location=(0,y,z);root.rotation_euler=(math.radians(angle),0,math.radians(-5))
    root.keyframe_insert('location',frame=frame);root.keyframe_insert('rotation_euler',frame=frame)
for name,start,end,radius in digit_info:
    for j in range(3):
        pose=rig.pose.bones[name+str(j)];pose.rotation_mode='XYZ'
        for frame,contact in [(1,0),(50,.3),(72,1),(88,1),(115,.2),(144,0)]:
            # Flexão para a palma: as falanges não dobram para o dorso.
            # Anelar e mínimo levantam só na base e curvam as duas juntas.
            angles={
                'Polegar':(0,-.03,-.04),
                'Indicador':(-.02,-.06,-.04),
                'Médio':(-.03,-.08,-.05),
                'Anelar':(.22,-.40,-.22),
                'Mínimo':(.28,-.46,-.25),
            }
            action={
                'Polegar':(0,0,-.01),
                'Indicador':(-.03,-.05,-.02),
                'Médio':(-.04,-.04,-.03),
                'Anelar':(.03,-.06,-.03),
                'Mínimo':(.03,-.05,-.03),
            }
            angle=angles[name][j]+action[name][j]*contact
            pose.rotation_euler.x=angle
            pose.rotation_euler.z=({'Indicador':.05,'Anelar':-.08,'Mínimo':-.13}.get(name,0) if j==0 else 0)
            pose.keyframe_insert('rotation_euler',frame=frame)

# Ajustar o contato ao nível da água a partir da posição real dos ossos.
scene.frame_set(80);bpy.context.view_layer.update()
tips=[]
for name in ('Indicador','Médio'):
    bone=rig.pose.bones[name+'2'];tips.append(rig.matrix_world@(bone.matrix@Vector((0,bone.length,0))))
correction=.004+0.004-min(tip.z for tip in tips)
for frame in (72,88):
    scene.frame_set(frame);root.location.z+=correction;root.keyframe_insert('location',frame=frame)
scene.frame_set(80);bpy.context.view_layer.update()
tip=rig.pose.bones['Médio2'];point=rig.matrix_world@(tip.matrix@Vector((0,tip.length,0)))
evaluated=hand.evaluated_get(bpy.context.evaluated_depsgraph_get());evaluated_mesh=evaluated.to_mesh()
world_vertices=[evaluated.matrix_world@v.co for v in evaluated_mesh.vertices]
near=[v.z for v in world_vertices if math.hypot(v.x-point.x,v.y-point.y)<.014]
evaluated.to_mesh_clear()
if near:
    correction=.002-min(near)
    for frame in (72,88):
        scene.frame_set(frame);root.location.z+=correction;root.keyframe_insert('location',frame=frame)
scene.frame_set(80);bpy.context.view_layer.update()
point=rig.matrix_world@(tip.matrix@Vector((0,tip.length,0)))
tap_x,tap_y=point.x,point.y

water,bs=material('Água do rio',(.07,.13,.18),.035)
bs.inputs['Transmission Weight'].default_value=1
bs.inputs['IOR'].default_value=1.333
nodes=water.node_tree.nodes;links=water.node_tree.links
tex=nodes.new('ShaderNodeTexNoise');tex.inputs['Scale'].default_value=35;tex.inputs['Detail'].default_value=2;tex.inputs['Roughness'].default_value=.6
bump=nodes.new('ShaderNodeBump');bump.inputs['Strength'].default_value=.14;bump.inputs['Distance'].default_value=.001
links.new(tex.outputs['Fac'],bump.inputs['Height']);links.new(bump.outputs['Normal'],bs.inputs['Normal'])
nx,ny=181,161
gx=np.linspace(-.65,.65,nx);gy=np.linspace(-.30,1.1,ny)
xx,yy=np.meshgrid(gx,gy)
water_vertices=np.column_stack((xx.ravel(),yy.ravel(),np.zeros(nx*ny)))
water_faces=[]
for j in range(ny-1):
    for i in range(nx-1):a=j*nx+i;water_faces.append((a,a+1,a+1+nx,a+nx))
river=mesh_object('Superfície animada',water_vertices.tolist(),water_faces,water)
radius=np.sqrt((xx-tap_x)**2+(yy-tap_y)**2)

def update_water(scene):
    t=(scene.frame_current-1)/24
    wave=.0003*np.sin(xx*27+yy*19+t*.55)+.00015*np.cos(yy*44-xx*23+t*.4)
    age=max(0,t-2.65)
    if age>0:
        envelope=np.exp(-radius*9)*np.exp(-age*.45)*np.clip((age*.11-radius)*80,0,1)
        wave+=.0015*np.sin(radius*145-age*16)*envelope
    coordinates=water_vertices.copy();coordinates[:,2]=.004+wave.ravel()
    river.data.vertices.foreach_set('co',coordinates.ravel());river.data.update()
bpy.app.handlers.frame_change_pre.append(update_water)

bpy.ops.mesh.primitive_plane_add(size=3,location=(0,.35,-.048));bpy.context.object.name='Leito do rio';bpy.context.object.data.materials.append(sand)
for i in range(100):
    x=random.uniform(-.5,.5);y=random.uniform(-.27,.65)
    z=-.032 if y>-.06 else .002
    size=random.uniform(.007,.026)
    rock=sphere('Seixo', (x,y,z),(size,size*random.uniform(.6,1.4),size*.55),stone,2)
    rock.rotation_euler=(random.uniform(-.2,.2),random.uniform(-.2,.2),random.random()*6.28)
for i in range(13):
    x=random.choice([-1,1])*random.uniform(.16,.36);y=random.uniform(-.23,-.055)
    size=random.uniform(.025,.045)
    sphere('Pedra da margem',(x,y,-.005),(size,size*.8,size*.6),stone,3)
    sphere('Musgo discreto',(x,y,.018),(size*.8,size*.65,.005),moss,2)
for i in range(12):
    x=random.uniform(-.4,-.22);y=random.uniform(-.2,-.04);height=random.uniform(.13,.30)
    curve=bpy.data.curves.new('Folha','CURVE');curve.dimensions='3D';curve.bevel_depth=.0014;curve.bevel_resolution=2
    spline=curve.splines.new('BEZIER');spline.bezier_points.add(2)
    for p,co in zip(spline.bezier_points,[(x,y,.02),(x+.02,y+.03,height*.65),(x+.07,y+.04,height)]):
        p.co=co;p.handle_left_type='AUTO';p.handle_right_type='AUTO'
    obj=bpy.data.objects.new('Junco',curve);scene.collection.objects.link(obj);curve.materials.append(reed)
for i in range(5):
    p=sphere('Pétala', (random.uniform(-.25,.26),random.uniform(.18,.42),.007),(.007,.011,.0008),petal,2)
    p.rotation_euler.z=random.random()*6.28


def area(name,position,power,color,size,target):
    data=bpy.data.lights.new(name,'AREA');data.energy=power;data.color=color;data.shape='DISK';data.specular_factor=.15;data.size=size
    obj=bpy.data.objects.new(name,data);scene.collection.objects.link(obj);obj.location=position
    obj.rotation_euler=(Vector(target)-obj.location).to_track_quat('-Z','Y').to_euler()
    if hasattr(obj,'visible_glossy'):obj.visible_glossy=False
area('Luz suave da manhã',(-.25,.45,.85),22,(1,.84,.66),.7,(0,.08,0))
area('Reflexo do céu',(.5,-.15,.55),8,(.48,.61,1),.8,(0,.08,0))
area('Recorte quente',(-.45,-.15,.45),12,(1,.69,.42),.40,(0,.08,.1))

# Faixa de céu quente fora de quadro: reflexo estreito permite ler a ondulação.
glint,glint_bs=material('Reflexo da manhã',(.48,.25,.12),.8)
glint_bs.inputs['Emission Color'].default_value=(1,.58,.30,1)
glint_bs.inputs['Emission Strength'].default_value=2
bpy.ops.mesh.primitive_plane_add(size=1,location=(-.4,-.22,.34))
sky_strip=bpy.context.object;sky_strip.name='Clareira refletida fora de quadro'
sky_strip.scale=(.30,.035,1)
sky_strip.rotation_euler=(Vector((tap_x,tap_y,.004))-sky_strip.location).to_track_quat('Z','Y').to_euler()
sky_strip.data.materials.append(glint);sky_strip.visible_camera=False

focus=bpy.data.objects.new('Foco no gesto',None);scene.collection.objects.link(focus);focus.location=(.005,.10,.042)
camera_data=bpy.data.cameras.new('Lente');camera=bpy.data.objects.new('Câmera',camera_data);scene.collection.objects.link(camera)
scene.camera=camera;camera_data.lens=44;camera_data.clip_start=.005
camera_data.dof.use_dof=True;camera_data.dof.focus_object=focus;camera_data.dof.aperture_fstop=5.6
for frame,location in [(1,(.30,.36,.235)),(144,(.28,.335,.225))]:
    camera.location=location;camera.rotation_euler=(Vector((.0,.095,.045))-camera.location).to_track_quat('-Z','Y').to_euler()
    camera.keyframe_insert('location',frame=frame);camera.keyframe_insert('rotation_euler',frame=frame)

ROOT.mkdir(parents=True,exist_ok=True);frames=ROOT/'render';frames.mkdir(exist_ok=True)
if args.bake_water:
    # Shape keys tornam a água editável e reproduzível ao abrir o .blend,
    # sem exigir execução automática de Python no computador de outra pessoa.
    frames_to_bake=sorted(set([*range(1,145,2),144]))
    snapshots=[]
    for frame in frames_to_bake:
        scene.frame_set(frame)
        coordinates=np.empty(len(river.data.vertices)*3,dtype=np.float32)
        river.data.vertices.foreach_get('co',coordinates)
        snapshots.append(coordinates)
    bpy.app.handlers.frame_change_pre.remove(update_water)
    river.shape_key_add(name='Base')
    for i,(frame,coordinates) in enumerate(zip(frames_to_bake,snapshots)):
        key=river.shape_key_add(name=f'Água {frame:03d}')
        key.data.foreach_set('co',coordinates)
        if i>0:
            key.value=0;key.keyframe_insert('value',frame=frames_to_bake[i-1])
        key.value=1;key.keyframe_insert('value',frame=frame)
        if i+1<len(frames_to_bake):
            key.value=0;key.keyframe_insert('value',frame=frames_to_bake[i+1])
    for curve in river.data.shape_keys.animation_data.action.fcurves:
        for point in curve.keyframe_points:point.interpolation='LINEAR'
scene.frame_set(args.preview_frame if args.still else 80)
bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/'NOS-plano-mao-v1.blend'),compress=True)
if args.save_only:
    pass
elif args.still:
    scene.render.filepath=str(ROOT/'preview-mao.png');bpy.ops.render.render(write_still=True)
else:
    scene.frame_start=args.start;scene.frame_end=args.end;scene.render.filepath=str(frames/'frame-')
    bpy.ops.render.render(animation=True)
print('NÓS: render concluído.',flush=True)

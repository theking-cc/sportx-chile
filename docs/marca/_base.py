from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.boundsPen import BoundsPen
import json
f=TTFont('/tmp/logo/package/files/archivo-black-latin-400-normal.woff')
gs=f.getGlyphSet(); cmap=f.getBestCmap(); cap=f['OS/2'].sCapHeight
NAVY="#14213D"; RED="#DC3545"
def word(text, track=-10):
    pen=SVGPathPen(gs); x=0
    for ch in text:
        g=cmap[ord(ch)]; gs[g].draw(TransformPen(pen,(1,0,0,-1,x,cap))); x+=gs[g].width+track
    return pen.getCommands(), x-track
# medir trazo vertical de la T para igualar grosor
bp=BoundsPen(gs); gs[cmap[ord('I')]].draw(bp); stem=bp.bounds[2]-bp.bounds[0]
xg=cmap[ord('X')]; bp=BoundsPen(gs); gs[xg].draw(bp); xw=bp.bounds[2]-bp.bounds[0]
def bands(x0,w,h,t):
    # "\" banda azul y "/" banda roja, grosor horizontal t
    back=f"M{x0:.0f} 0 L{x0+t:.0f} 0 L{x0+w:.0f} {h:.0f} L{x0+w-t:.0f} {h:.0f} Z"
    fwd=f"M{x0+w-t:.0f} 0 L{x0+w:.0f} 0 L{x0+t:.0f} {h:.0f} L{x0:.0f} {h:.0f} Z"
    return back,fwd
def v1(x0,w,h,t):
    back,fwd=bands(x0,w,h,t)
    # cima nevada en la punta superior izquierda de la banda azul + segunda cumbre
    snow=f"M{x0:.0f} 0 L{x0+t:.0f} 0 L{x0+t*0.86:.0f} {h*0.1:.0f} L{x0+t*0.55:.0f} {h*0.06:.0f} L{x0+t*0.3:.0f} {h*0.12:.0f} Z"
    return [(NAVY,back),("#FFFFFF",snow),(RED,fwd)]
def v2(x0,w,h,t):
    back,fwd=bands(x0,w,h,t)
    cx=x0+w/2; cy=h/2
    # muesca inferior rellena = macizo (Λ) bajo el cruce, con nieve en la cumbre
    mass=f"M{cx:.0f} {cy-t*0.2:.0f} L{x0+w-t*0.2:.0f} {h:.0f} L{x0+t*0.2:.0f} {h:.0f} Z"
    snow=f"M{cx:.0f} {cy+t*0.62:.0f} L{cx+t*0.36:.0f} {cy+t*1.18:.0f} L{cx+t*0.12:.0f} {cy+t*1.05:.0f} L{cx:.0f} {cy+t*1.22:.0f} L{cx-t*0.12:.0f} {cy+t*1.05:.0f} L{cx-t*0.36:.0f} {cy+t*1.18:.0f} Z"
    return [(NAVY,back),(NAVY,mass),("#FFFFFF",snow),(RED,fwd)]
d,wx=word("SPORT")
out={}
for name,fn in (("v1",v1),("v2",v2)):
    w=xw*1.02; t=stem*1.05; x0=wx+cap*0.04
    shapes=fn(x0,w,cap,t)
    W=x0+w+cap*0.02
    for tone,txt in (("",NAVY),("-blanco","#FFFFFF")):
        parts="".join(f'<path fill="{(txt if (c==NAVY) else c) if not (tone and c=="#FFFFFF") else NAVY}" d="{p}"/>' for c,p in shapes)
        svg=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -12 {W:.0f} {cap+24}" role="img" aria-labelledby="t"><title id="t">SportX Chile</title><path fill="{txt}" d="{d}"/>{parts}</svg>'
        open(f'/home/claude/logo/{name}{tone}.svg','w').write(svg)
    # icono
    S=512; p=104; ih=S-2*p; t2=ih*stem/cap*1.05; w2=ih*xw/cap*1.02; x2=(S-w2)/2
    sh=fn(x2,w2,ih,t2)
    parts="".join(f'<path fill="{"#FFFFFF" if c==NAVY else (NAVY if c=="#FFFFFF" else c)}" d="{pp}"/>' for c,pp in sh)
    open(f'/home/claude/logo/{name}-icono.svg','w').write(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {S} {S}" role="img" aria-labelledby="t"><title id="t">SportX</title><rect width="{S}" height="{S}" rx="112" fill="{NAVY}"/><g transform="translate(0 {p})">{parts}</g></svg>')
print("stem",stem,"xw",xw,"cap",cap)

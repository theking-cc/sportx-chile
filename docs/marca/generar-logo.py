exec(open('docs/marca/_base.py').read().split("d,wx=word")[0])
def inter(p1,p2,p3,p4):
    (x1,y1),(x2,y2),(x3,y3),(x4,y4)=p1,p2,p3,p4
    den=(x1-x2)*(y3-y4)-(y1-y2)*(x3-x4)
    px=((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/den
    py=((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/den
    return px,py
def v4(x0,w,h,t):
    back,fwd=bands(x0,w,h,t)
    FL=(x0+t,h); FR=(x0+w-t,h)
    A=inter((x0+t,h),(x0+w,0),(x0,0),(x0+w-t,h))   # vértice de la abertura inferior
    g=t*0.14
    # triángulo interior con respiro g
    def pt(u,k):  # u: 0..1 de izquierda a derecha en la base, k: 0..1 altura hacia A
        bx=FL[0]+(FR[0]-FL[0])*u; return (bx+(A[0]-bx)*k, h+(A[1]-h)*k)
    hh=h-A[1]; s=g/hh
    def ins(u,k):  # punto interior, alejado de los bordes
        k=k*(1-s*2.2); return pt(0.5+(u-0.5)*(1-s*1.6),k)
    L=ins(0.0,0); P1=ins(0.40,0.80); V=ins(0.64,0.40); P2=ins(0.78,0.58); R=ins(1.0,0)
    rng="M"+" L".join(f"{x:.0f} {y:.0f}" for x,y in (L,P1,V,P2,R))+" Z"
    def lerp(a,b,k): return (a[0]+(b[0]-a[0])*k, a[1]+(b[1]-a[1])*k)
    BL=lerp(P1,L,0.30); BR=lerp(P1,V,0.42); m1=lerp(BL,BR,1/3); m2=lerp(BL,BR,2/3); dy=hh*0.06
    snow="M"+" L".join(f"{x:.0f} {y:.0f}" for x,y in (P1,BR,(m2[0],m2[1]-dy),((m1[0]+m2[0])/2,(m1[1]+m2[1])/2+dy*0.4),(m1[0],m1[1]-dy),BL))+" Z"
    return [(NAVY,back),(RED,fwd),(NAVY,rng),("#FFFFFF",snow)]
d,wx=word("SPORT")
WW,TW=1.24,0.98
W_X=1.30; T_X=0.88
w=xw*WW; t=stem*TW; x0=wx+cap*0.05; W=x0+w+cap*0.02
shapes=v4(x0,w,cap,t)
for tone,txt in (("",NAVY),("-blanco","#FFFFFF")):
    parts="".join(f'<path fill="{(txt if c==NAVY else c) if not (tone and c=="#FFFFFF") else NAVY}" d="{p}"/>' for c,p in shapes)
    open(f'frontend/public/brand/sportx-logo{tone}.svg','w').write(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 -12 {W:.0f} {cap+24}" role="img" aria-labelledby="t"><title id="t">SportX Chile</title><path fill="{txt}" d="{d}"/>{parts}</svg>')
S=512; p=112; ih=S-2*p; t2=ih*stem/cap*T_X; w2=ih*xw/cap*W_X; x2=(S-w2)/2
sh=v4(x2,w2,ih,t2)
parts="".join(f'<path fill="{"#FFFFFF" if c==NAVY else (NAVY if c=="#FFFFFF" else c)}" d="{pp}"/>' for c,pp in sh)
open('frontend/public/brand/sportx-icono.svg','w').write(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {S} {S}" role="img" aria-labelledby="t"><title id="t">SportX</title><rect width="{S}" height="{S}" rx="112" fill="{NAVY}"/><g transform="translate(0 {p})">{parts}</g></svg>')

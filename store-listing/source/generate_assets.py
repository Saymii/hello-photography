#!/usr/bin/env python3
"""Render original Hello Photography listing artwork. Requires Pillow; no network or photos.

Run from any directory: python3 store-listing/source/generate_assets.py
SVG sources are editable without this renderer. Artwork is original to this project.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SCALE = 3
CHARCOAL = '#18231f'
GREEN = '#2d4638'
GOLD = '#dfc68a'
CREAM = '#f9f6ee'


def image(size):
    canvas = Image.new('RGB', tuple(v * SCALE for v in size), CHARCOAL)
    return canvas, ImageDraw.Draw(canvas)


def box(draw, bounds, fill, radius=0, outline=None, width=1):
    xy = tuple(round(x * SCALE) for x in bounds)
    if radius:
        draw.rounded_rectangle(xy, radius * SCALE, fill=fill, outline=outline, width=width*SCALE)
    else:
        draw.rectangle(xy, fill=fill, outline=outline, width=width*SCALE)


def line(draw, points, color, width=1):
    pts = [(round(x*SCALE), round(y*SCALE)) for x,y in points]
    draw.line(pts, fill=color, width=width*SCALE, joint='curve')


def circle(draw, bounds, fill=None, outline=None, width=1):
    draw.ellipse(tuple(round(x*SCALE) for x in bounds), fill=fill, outline=outline, width=width*SCALE)


def font(size, bold=False):
    family='DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf'
    return ImageFont.truetype('/usr/share/fonts/truetype/dejavu/'+family, size*SCALE)


def label(draw, xy, value, size, fill=CREAM, bold=False):
    draw.text(tuple(round(x*SCALE) for x in xy), value, font=font(size,bold), fill=fill)


def camera(draw, x, y, scale=1):
    # A lens and subtle smile establish a friendly original camera mark.
    def rect(bounds, color, radius=0, outline=None, width=1):
        box(draw, [x+bounds[0]*scale,y+bounds[1]*scale,x+bounds[2]*scale,y+bounds[3]*scale],color,radius*scale,outline,width)
    rect((0,32,280,224),None,30,GOLD,9)
    rect((66,0,158,38),GOLD,10)
    circle(draw,(x+79*scale,y+69*scale,x+201*scale,y+191*scale),None,GOLD,9)
    circle(draw,(x+103*scale,y+93*scale,x+177*scale,y+167*scale),None,CREAM,3)
    circle(draw,(x+229*scale,y+62*scale,x+245*scale,y+78*scale),GOLD)
    line(draw,[(x+118*scale,y+119*scale),(x+140*scale,y+101*scale),(x+159*scale,y+117*scale)],GOLD,4)
    line(draw,[(x+119*scale,y+141*scale),(x+140*scale,y+157*scale),(x+160*scale,y+140*scale)],GOLD,4)


def save(canvas, name, size, mode='RGB'):
    target=ROOT/name
    canvas.resize(size,Image.Resampling.LANCZOS).convert(mode).save(target,format='PNG',optimize=True)
    print(target)


canvas, d=image((512,512))
d.polygon([(0,360*SCALE),(512*SCALE,70*SCALE),(512*SCALE,512*SCALE),(0,512*SCALE)],fill=GREEN)
circle(d,(34,34,478,478),None,'#3c5142',1)
camera(d,116,124)
line(d,[(198,399),(256,410),(314,399)],GOLD,4)
save(canvas,'app-icon-512.png',(512,512),mode='RGBA')

canvas,d=image((1024,500))
d.polygon([(624*SCALE,0),(1024*SCALE,0),(1024*SCALE,500*SCALE),(421*SCALE,500*SCALE)],fill=GREEN)
# Editorial registration lines give structure without a device or fake screenshot.
line(d,[(48,46),(976,46)],'#45604b',1)
line(d,[(48,454),(976,454)],'#45604b',1)
camera(d,58,80,.26)
label(d,(48,182),'Hello',62,bold=True)
label(d,(48,253),'Photography',58,bold=True)
label(d,(51,354),'Learn to pose. Capture better photos.',20,GOLD)
label(d,(51,392),'YOUR NEXT GREAT FRAME',12,CREAM,True)
# Original fashion/portrait line illustration in an editorial composition frame.
box(d,(727,82,944,417),None,26, '#9eaa86',2)
line(d,[(749,337),(917,337)],'#668064',1)
circle(d,(805,128,858,184),None,GOLD,4)
line(d,[(828,185),(822,244),(855,277),(873,348)],GOLD,5)
line(d,[(822,244),(791,289),(779,353)],GOLD,5)
line(d,[(826,204),(787,229),(773,202)],CREAM,4)
line(d,[(836,207),(875,230),(891,199)],CREAM,4)
line(d,[(750,104),(766,104),(766,120)],GOLD,2)
line(d,[(921,394),(905,394),(905,378)],GOLD,2)
save(canvas,'feature-graphic-1024x500.png',(1024,500))

# Editable SVGs intentionally match the composition of the raster listing assets.
(ROOT/'source/app-icon.svg').write_text('''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512">
<title>Hello Photography original app icon</title>
<rect width="512" height="512" fill="#18231f"/><path d="M0 360L512 70V512H0Z" fill="#2d4638"/>
<circle cx="256" cy="256" r="222" fill="none" stroke="#3c5142"/>
<g transform="translate(116 124)" fill="none" stroke="#dfc68a" stroke-width="9">
<rect x="0" y="32" width="280" height="192" rx="30"/>
<rect x="66" y="0" width="92" height="38" rx="10" fill="#dfc68a" stroke="none"/>
<circle cx="140" cy="130" r="61"/><circle cx="140" cy="130" r="37" stroke="#f9f6ee" stroke-width="3"/>
<circle cx="237" cy="70" r="8" fill="#dfc68a" stroke="none"/>
<path d="M118 119L140 101L159 117M119 141L140 157L160 140" stroke-width="4"/></g>
<path d="M198 399L256 410L314 399" fill="none" stroke="#dfc68a" stroke-width="4"/></svg>''')
(ROOT/'source/feature-graphic.svg').write_text('''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 500" width="1024" height="500">
<title>Hello Photography original store feature graphic</title>
<rect width="1024" height="500" fill="#18231f"/><path d="M624 0H1024V500H421Z" fill="#2d4638"/>
<path d="M48 46H976M48 454H976" stroke="#45604b"/>
<g transform="translate(58 80) scale(.26)" fill="none" stroke="#dfc68a" stroke-width="9">
<rect y="32" width="280" height="192" rx="30"/><rect x="66" width="92" height="38" rx="10" fill="#dfc68a" stroke="none"/>
<circle cx="140" cy="130" r="61"/><circle cx="140" cy="130" r="37" stroke="#f9f6ee" stroke-width="3"/>
<circle cx="237" cy="70" r="8" fill="#dfc68a" stroke="none"/><path d="M118 119L140 101L159 117M119 141L140 157L160 140" stroke-width="4"/></g>
<g fill="#f9f6ee" font-family="DejaVu Sans,sans-serif" font-weight="bold"><text x="48" y="240" font-size="62">Hello</text><text x="48" y="307" font-size="58">Photography</text></g>
<text x="51" y="376" font-size="20" font-family="DejaVu Sans,sans-serif" fill="#dfc68a">Learn to pose. Capture better photos.</text>
<text x="51" y="405" font-size="12" font-family="DejaVu Sans,sans-serif" fill="#f9f6ee" font-weight="bold">YOUR NEXT GREAT FRAME</text>
<rect x="727" y="82" width="217" height="335" rx="26" stroke="#9eaa86" stroke-width="2" fill="none"/>
<path d="M749 337H917" stroke="#668064"/><ellipse cx="831.5" cy="156" rx="26.5" ry="28" fill="none" stroke="#dfc68a" stroke-width="4"/>
<path d="M828 185L822 244L855 277L873 348M822 244L791 289L779 353" fill="none" stroke="#dfc68a" stroke-width="5"/>
<path d="M826 204L787 229L773 202M836 207L875 230L891 199" fill="none" stroke="#f9f6ee" stroke-width="4"/>
<path d="M750 104H766V120M921 394H905V378" fill="none" stroke="#dfc68a" stroke-width="2"/></svg>''')

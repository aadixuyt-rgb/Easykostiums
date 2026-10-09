"""Generuje tekstury armoru (64x32) dla EasySnup.
Uruchom z katalogu repo:  python3 tools/gen_textures.py
"""
from PIL import Image
import os

A = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/assets/easysnup')
OUT_H = os.path.join(A, 'textures/entity/equipment/humanoid')
OUT_L = os.path.join(A, 'textures/entity/equipment/humanoid_leggings')


def rect(px, x0, y0, x1, y1, c):
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = c


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)


# ---------------- SNUP ----------------
BLUE = (58, 179, 218, 255)  # 0x3AB3DA
WHITE = (245, 250, 255, 255)


def snup_humanoid():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    px = im.load()
    # TUŁÓW (16,16)-(40,32): tył, boki, góra/dół = błękit
    rect(px, 16, 16, 40, 32, BLUE)
    rect(px, 20, 16, 36, 20, shade(BLUE, 0.85))   # góra/dół
    rect(px, 16, 20, 20, 32, shade(BLUE, 0.9))    # bok
    rect(px, 28, 20, 32, 32, shade(BLUE, 0.9))    # bok
    rect(px, 32, 20, 40, 32, shade(BLUE, 0.8))    # TYŁ - błękit
    # PRZÓD (20,20)-(28,32): błękit + BIAŁY ŚRODEK BRZUCHA
    rect(px, 20, 20, 28, 32, BLUE)
    rect(px, 22, 22, 26, 30, WHITE)
    # RAMIONA (40,16)-(56,32): całe błękitne
    rect(px, 40, 16, 56, 32, BLUE)
    rect(px, 44, 16, 52, 20, shade(BLUE, 0.85))
    rect(px, 48, 20, 52, 32, shade(BLUE, 0.9))
    rect(px, 52, 20, 56, 32, shade(BLUE, 0.8))
    # BUTY (dolne rzędy nóg)
    dk = shade(BLUE, 0.6)
    rect(px, 0, 26, 16, 31, dk)
    rect(px, 0, 31, 16, 32, WHITE)
    return im


def snup_leggings():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    px = im.load()
    dk = shade(BLUE, 0.75)
    rect(px, 16, 20, 40, 24, dk)                  # pas
    rect(px, 16, 23, 40, 24, shade(BLUE, 0.55))
    rect(px, 0, 16, 16, 28, dk)                   # nogi
    rect(px, 0, 16, 16, 17, shade(BLUE, 0.55))
    return im


# ---------------- ADIX (ze skina) ----------------
skin = Image.open(os.path.join(A, 'textures/item/adix_skin.png')).convert('RGBA')


def adix_humanoid():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    im.paste(skin.crop((16, 16, 40, 32)), (16, 16))   # tułów
    im.paste(skin.crop((40, 16, 56, 32)), (40, 16))   # ramię
    leg = skin.crop((0, 16, 16, 32))
    boots = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    boots.paste(leg.crop((0, 12, 16, 16)), (0, 12))   # dolne 4 rzędy = buty
    im.paste(boots, (0, 16))
    return im


def adix_leggings():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    body = skin.crop((16, 16, 40, 32))
    waist = Image.new('RGBA', (24, 16), (0, 0, 0, 0))
    waist.paste(body.crop((0, 4, 24, 8)), (0, 4))
    im.paste(waist, (16, 16))
    leg = skin.crop((0, 16, 16, 32))
    legs = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    legs.paste(leg.crop((0, 0, 16, 12)), (0, 0))
    im.paste(legs, (0, 16))
    return im


if __name__ == '__main__':
    snup_humanoid().save(os.path.join(OUT_H, 'snup.png'))
    snup_leggings().save(os.path.join(OUT_L, 'snup.png'))
    adix_humanoid().save(os.path.join(OUT_H, 'adix.png'))
    adix_leggings().save(os.path.join(OUT_L, 'adix.png'))
    print('OK')

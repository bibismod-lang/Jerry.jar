# Genera las texturas de Jerry. Reemplaza los PNG con tu propio arte si quieres.
import random
from PIL import Image
random.seed(7)
OUT = "src/main/resources/assets/jerrymod/textures/entity/"

def noise(c, v):
    d = random.randint(-v, v)
    return tuple(max(0, min(255, x + d)) for x in c) + (255,)

def dog(eye_type):
    im = Image.new("RGBA", (64, 32))
    px = im.load()
    for y in range(32):
        for x in range(64):
            px[x, y] = noise((200, 200, 206), 9)
    # hombros rojizos (parte superior del cuerpo delantero)
    for y in range(0, 7):
        for x in range(21, 37):
            if random.random() < 0.55:
                px[x, y] = noise((170, 45, 45), 18)
    # orejas rosadas oscuras
    for y in range(14, 18):
        for x in range(16, 22):
            px[x, y] = noise((125, 70, 75), 8)
    # nariz / hocico
    for y in range(14, 17):
        for x in range(4, 7):
            px[x, y] = (25, 20, 22, 255)
    # cara frontal de la cabeza: x 4..9, y 4..9. Ojos 3x3 blancos
    black, white = (10, 10, 10, 255), (245, 245, 245, 255)
    for ex in (4, 7):
        for y in range(5, 8):
            for x in range(ex, ex + 3):
                px[x, y] = white
    for x in range(4, 10):
        px[x, 4] = (90, 90, 96, 255)   # ceja
    # pupilas (vista del espectador): 0 frente, 1 izquierda, 2 derecha, 3 cada ojo a un lado distinto
    pupils = {
        0: [(5, 6), (8, 6)],
        1: [(4, 6), (7, 6)],
        2: [(6, 6), (9, 6)],
        3: [(4, 5), (9, 7)],
    }[eye_type]
    for p in pupils:
        px[p] = black
    return im

for i in range(4):
    dog(i).save(OUT + f"jerry_{i}.png")

# Monstruo 64x64: carne y venas
m = Image.new("RGBA", (64, 64))
px = m.load()
for y in range(64):
    for x in range(64):
        px[x, y] = noise((224, 172, 162), 12)
for _ in range(260):
    x, y = random.randrange(64), random.randrange(64)
    for _ in range(random.randint(3, 9)):
        px[x, y] = noise((150, 25, 28), 20)
        x = (x + random.choice((-1, 0, 1))) % 64
        y = (y + random.choice((0, 1, 1))) % 64
for _ in range(40):
    x, y = random.randrange(62), random.randrange(62)
    for dx in range(2):
        for dy in range(2):
            px[x + dx, y + dy] = noise((110, 15, 20), 10)
# cara del monstruo: frente de la cabeza (cubo 6x8x6 en uv 0,0 -> frente x 6..11, y 6..13)
for p in [(7, 9), (10, 9)]:
    px[p] = (0, 0, 0, 255)
    px[(p[0], p[1] - 1)] = (0, 0, 0, 255)
for x in range(7, 11):
    px[(x, 12)] = (30, 5, 8, 255)
m.save(OUT + "jerry_monster.png")
print("ok")

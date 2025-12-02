# synthetic/generate_synthetic.py
# Generates synthetic sample images for training the multiplier-detection model.
# Each sample is a small image representing a cropped button for choices (3x,8x,20x,100x)
from PIL import Image, ImageDraw, ImageFont
import random, os, json, math, argparse
labels = ['3x','8x','20x','100x']
def rnd_color():
    return (random.randint(30,220), random.randint(30,220), random.randint(30,220))
def make_sample(outdir, label, idx):
    w,h = 128,128
    img = Image.new('RGB', (w,h), (20,20,20))
    draw = ImageDraw.Draw(img)
    # draw a circle button
    cx,cy = w//2, h//2
    r = 44 + random.randint(-6,6)
    draw.ellipse((cx-r,cy-r,cx+r,cy+r), fill=rnd_color())
    # draw text label
    try:
        font = ImageFont.truetype('arial.ttf', 28)
    except:
        font = ImageFont.load_default()
    text = label
    tw,th = draw.textsize(text, font=font)
    draw.text((cx-tw//2, cy-th//2), text, fill=(255,255,255), font=font)
    # add noise
    for _ in range(100):
        x = random.randint(0,w-1); y = random.randint(0,h-1)
        draw.point((x,y), fill=(random.randint(0,255),random.randint(0,255),random.randint(0,255)))
    os.makedirs(outdir, exist_ok=True)
    fname = os.path.join(outdir, f"sample_{label}_{idx}.png")
    img.save(fname)
    return fname
def generate(outdir, per_label=200):
    manifest = []
    i = 0
    for L in labels:
        for n in range(per_label):
            p = make_sample(outdir, L, n)
            manifest.append({'file': os.path.basename(p), 'label': L, 'idx': i})
            i += 1
    with open(os.path.join(outdir, 'manifest.json'), 'w') as f:
        json.dump(manifest, f)
if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--out', default='synthetic_dataset', help='output folder')
    parser.add_argument('--per', type=int, default=200, help='samples per class')
    args = parser.parse_args()
    generate(args.out, args.per)

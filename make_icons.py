import math
from PIL import Image, ImageDraw

def create_clock_icon(size, is_round=False):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Outer background
    center = size / 2.0
    radius = size * 0.46
    
    if is_round:
        draw.ellipse([center - radius, center - radius, center + radius, center + radius], fill=(0, 0, 0, 255), outline=(0, 240, 255, 200), width=max(2, int(size * 0.04)))
    else:
        # squircle / rounded rect
        corner = int(size * 0.22)
        draw.rounded_rectangle([center - radius, center - radius, center + radius, center + radius], radius=corner, fill=(0, 0, 0, 255), outline=(0, 240, 255, 200), width=max(2, int(size * 0.04)))
    
    # Clock dial markings (subtle dots for 12, 3, 6, 9)
    dot_r = max(1.5, size * 0.02)
    inner_r = radius * 0.75
    for i in range(12):
        angle = i * (math.pi / 6)
        x = center + inner_r * math.sin(angle)
        y = center - inner_r * math.cos(angle)
        if i % 3 == 0:
            draw.ellipse([x - dot_r, y - dot_r, x + dot_r, y + dot_r], fill=(0, 240, 255, 240))
        else:
            draw.ellipse([x - dot_r*0.6, y - dot_r*0.6, x + dot_r*0.6, y + dot_r*0.6], fill=(100, 100, 100, 180))

    # Clock Hands
    h_angle = math.radians(305)
    h_len = radius * 0.45
    h_w = max(2, int(size * 0.05))
    hx = center + h_len * math.sin(h_angle)
    hy = center - h_len * math.cos(h_angle)
    draw.line([center, center, hx, hy], fill=(255, 255, 255, 255), width=h_w)
    
    m_angle = math.radians(60)
    m_len = radius * 0.65
    m_w = max(2, int(size * 0.035))
    mx = center + m_len * math.sin(m_angle)
    my = center - m_len * math.cos(m_angle)
    draw.line([center, center, mx, my], fill=(0, 240, 255, 255), width=m_w)
    
    s_angle = math.radians(210)
    s_len = radius * 0.72
    s_w = max(1, int(size * 0.02))
    sx = center + s_len * math.sin(s_angle)
    sy = center - s_len * math.cos(s_angle)
    draw.line([center, center, sx, sy], fill=(255, 59, 48, 255), width=s_w)
    
    pin_r = max(2.5, size * 0.04)
    draw.ellipse([center - pin_r, center - pin_r, center + pin_r, center + pin_r], fill=(0, 240, 255, 255))
    draw.ellipse([center - pin_r*0.5, center - pin_r*0.5, center + pin_r*0.5, center + pin_r*0.5], fill=(255, 59, 48, 255))
    
    return img

densities = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

for name, sz in densities.items():
    icon_square = create_clock_icon(sz, False)
    icon_round = create_clock_icon(sz, True)
    icon_square.save(f'/root/clock-app/res/mipmap-{name}/ic_launcher.png')
    icon_round.save(f'/root/clock-app/res/mipmap-{name}/ic_launcher_round.png')

print("Icons generated successfully!")

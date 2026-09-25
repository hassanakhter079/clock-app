import math
from PIL import Image, ImageDraw, ImageFilter

def create_master_icon(size=768, is_round=False):
    # Create image in high resolution for supersampling
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    center = size / 2.0
    radius = size * 0.44

    # 1. Background Mask
    mask = Image.new('L', (size, size), 0)
    draw_mask = ImageDraw.Draw(mask)
    if is_round:
        draw_mask.ellipse([center - radius, center - radius, center + radius, center + radius], fill=255)
    else:
        corner = int(size * 0.22)
        draw_mask.rounded_rectangle([center - radius, center - radius, center + radius, center + radius], radius=corner, fill=255)

    # 2. Outer Neon Glow
    glow = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw_glow = ImageDraw.Draw(glow)
    if is_round:
        draw_glow.ellipse([center - radius, center - radius, center + radius, center + radius], outline=(0, 240, 255, 180), width=int(size * 0.035))
    else:
        corner = int(size * 0.22)
        draw_glow.rounded_rectangle([center - radius, center - radius, center + radius, center + radius], radius=corner, outline=(0, 240, 255, 180), width=int(size * 0.035))
    glow = glow.filter(ImageFilter.GaussianBlur(int(size * 0.025)))
    img = Image.alpha_composite(img, glow)

    # 3. Base Dial Surface (Deep AMOLED Radial Gradient)
    base_surface = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw_base = ImageDraw.Draw(base_surface)
    steps = 40
    for s in range(steps):
        r_step = radius * (1.0 - (s / float(steps)) * 0.95)
        val = int(8 + 24 * (s / float(steps)))
        col = (int(val * 0.7), int(val * 0.9), int(val * 1.3), 255)
        if is_round:
            draw_base.ellipse([center - r_step, center - r_step, center + r_step, center + r_step], fill=col)
        else:
            c_step = int(corner * (r_step / radius))
            draw_base.rounded_rectangle([center - r_step, center - r_step, center + r_step, center + r_step], radius=c_step, fill=col)

    # Apply mask to base
    base_clipped = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    base_clipped.paste(base_surface, (0, 0), mask)
    img = Image.alpha_composite(img, base_clipped)

    # 4. Dial Inner Bezel Ring
    dial = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw_dial = ImageDraw.Draw(dial)
    
    # Outer stroke
    if is_round:
        draw_dial.ellipse([center - radius, center - radius, center + radius, center + radius], outline=(0, 240, 255, 230), width=max(2, int(size * 0.02)))
    else:
        corner = int(size * 0.22)
        draw_dial.rounded_rectangle([center - radius, center - radius, center + radius, center + radius], radius=corner, outline=(0, 240, 255, 230), width=max(2, int(size * 0.02)))

    # Dial track circle
    inner_dial_r = radius * 0.82
    draw_dial.ellipse([center - inner_dial_r, center - inner_dial_r, center + inner_dial_r, center + inner_dial_r], outline=(30, 42, 56, 180), width=max(1, int(size * 0.008)))

    # Active Progress Arc along rim (from -90 deg to 30 deg - 10 o'clock to 2 o'clock cyan sweep)
    draw_dial.arc([center - inner_dial_r, center - inner_dial_r, center + inner_dial_r, center + inner_dial_r], start=-90, end=45, fill=(0, 240, 255, 255), width=max(3, int(size * 0.015)))

    # Precision Tick Marks
    tick_outer = inner_dial_r
    tick_inner_minor = inner_dial_r - size * 0.025
    tick_inner_major = inner_dial_r - size * 0.055

    for i in range(60):
        ang = i * (math.pi / 30.0)
        sin_a = math.sin(ang)
        cos_a = math.cos(ang)
        x_out = center + tick_outer * sin_a
        y_out = center - tick_outer * cos_a

        if i % 15 == 0:
            # Major Cardinal Ticks (12, 3, 6, 9)
            x_in = center + (tick_inner_major - size * 0.02) * sin_a
            y_in = center - (tick_inner_major - size * 0.02) * cos_a
            draw_dial.line([x_in, y_in, x_out, y_out], fill=(0, 240, 255, 255), width=max(3, int(size * 0.018)))
        elif i % 5 == 0:
            # 5-minute ticks
            x_in = center + tick_inner_major * sin_a
            y_in = center - tick_inner_major * cos_a
            draw_dial.line([x_in, y_in, x_out, y_out], fill=(220, 240, 255, 220), width=max(2, int(size * 0.012)))
        else:
            # Minor second ticks
            x_in = center + tick_inner_minor * sin_a
            y_in = center - tick_inner_minor * cos_a
            draw_dial.line([x_in, y_in, x_out, y_out], fill=(80, 100, 120, 120), width=max(1, int(size * 0.005)))

    # Modern Geometric Hands (10:10:35 aesthetic composition)
    # Hour Hand: 305 degrees (pointing to 10:10)
    h_angle = math.radians(305)
    h_len = radius * 0.48
    h_w = size * 0.045
    
    # Hour hand sword polygon
    hx_tip = center + h_len * math.sin(h_angle)
    hy_tip = center - h_len * math.cos(h_angle)
    hx_tail = center - (h_len * 0.18) * math.sin(h_angle)
    hy_tail = center + (h_len * 0.18) * math.cos(h_angle)
    perp_h = h_angle + math.pi / 2
    h_side1_x = center + (h_w * 0.5) * math.sin(perp_h)
    h_side1_y = center - (h_w * 0.5) * math.cos(perp_h)
    h_side2_x = center - (h_w * 0.5) * math.sin(perp_h)
    h_side2_y = center + (h_w * 0.5) * math.cos(perp_h)
    
    draw_dial.polygon([
        (hx_tail, hy_tail),
        (h_side1_x, h_side1_y),
        (hx_tip, hy_tip),
        (h_side2_x, h_side2_y)
    ], fill=(255, 255, 255, 245), outline=(180, 200, 220, 255))

    # Minute Hand: 55 degrees (pointing towards 2)
    m_angle = math.radians(55)
    m_len = radius * 0.70
    m_w = size * 0.035
    mx_tip = center + m_len * math.sin(m_angle)
    my_tip = center - m_len * math.cos(m_angle)
    mx_tail = center - (m_len * 0.18) * math.sin(m_angle)
    my_tail = center + (m_len * 0.18) * math.cos(m_angle)
    perp_m = m_angle + math.pi / 2
    m_side1_x = center + (m_w * 0.5) * math.sin(perp_m)
    m_side1_y = center - (m_w * 0.5) * math.cos(perp_m)
    m_side2_x = center - (m_w * 0.5) * math.sin(perp_m)
    m_side2_y = center + (m_w * 0.5) * math.cos(perp_m)

    draw_dial.polygon([
        (mx_tail, my_tail),
        (m_side1_x, m_side1_y),
        (mx_tip, my_tip),
        (m_side2_x, m_side2_y)
    ], fill=(0, 240, 255, 245), outline=(0, 180, 220, 255))

    # Second Hand: Neon Crimson/Coral needle pointing at ~215 degrees
    s_angle = math.radians(215)
    s_len = radius * 0.80
    s_tail = radius * 0.22
    s_w = max(2, int(size * 0.016))

    sx_tip = center + s_len * math.sin(s_angle)
    sy_tip = center - s_len * math.cos(s_angle)
    sx_tail = center - s_tail * math.sin(s_angle)
    sy_tail = center + s_tail * math.cos(s_angle)

    draw_dial.line([sx_tail, sy_tail, sx_tip, sy_tip], fill=(255, 45, 75, 255), width=s_w)
    
    # Counterbalance circle on second hand
    cb_center_x = center - (s_tail * 0.6) * math.sin(s_angle)
    cb_center_y = center + (s_tail * 0.6) * math.cos(s_angle)
    cb_r = size * 0.024
    draw_dial.ellipse([cb_center_x - cb_r, cb_center_y - cb_r, cb_center_x + cb_r, cb_center_y + cb_r], fill=(255, 45, 75, 255))

    # Center Metallic Pinion
    pin_outer = size * 0.05
    pin_inner = size * 0.025
    draw_dial.ellipse([center - pin_outer, center - pin_outer, center + pin_outer, center + pin_outer], fill=(20, 24, 32, 255), outline=(0, 240, 255, 255), width=max(2, int(size * 0.01)))
    draw_dial.ellipse([center - pin_inner, center - pin_inner, center + pin_inner, center + pin_inner], fill=(255, 45, 75, 255))

    img = Image.alpha_composite(img, dial)
    return img

densities = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

for name, sz in densities.items():
    sq_master = create_master_icon(size=768, is_round=False)
    sq_resized = sq_master.resize((sz, sz), Image.Resampling.LANCZOS)
    sq_resized.save(f'/root/clock-app/res/mipmap-{name}/ic_launcher.png')

    rd_master = create_master_icon(size=768, is_round=True)
    rd_resized = rd_master.resize((sz, sz), Image.Resampling.LANCZOS)
    rd_resized.save(f'/root/clock-app/res/mipmap-{name}/ic_launcher_round.png')

print("Dope custom AMOLED neon clock icons generated across all densities!")

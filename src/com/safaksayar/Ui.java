package com.safaksayar;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public final class Ui {

    public static final int BG_TOP = 0x0B1220;
    public static final int BG_BOTTOM = 0x182A44;
    public static final int PANEL = 0x1B2C47;
    public static final int PANEL_EDGE = 0x2D4566;
    public static final int TEXT = 0xF1F5FB;
    public static final int MUTED = 0x94A6BF;
    public static final int DIM = 0x5D708C;
    public static final int ACCENT = 0xFFB347;
    public static final int ACCENT_DARK = 0xC8872E;
    public static final int GREEN = 0x6FE3A1;
    public static final int SEG_OFF = 0x22344F;
    public static final int TRACK = 0x2A3E5C;

    private Ui() {
    }

    public static void background(Graphics g, int w, int h) {
        int bands = 18;
        for (int i = 0; i < bands; i++) {
            int t = i * 255 / (bands - 1);
            g.setColor(blend(BG_TOP, BG_BOTTOM, t));
            int y0 = i * h / bands;
            int y1 = (i + 1) * h / bands;
            g.fillRect(0, y0, w, y1 - y0);
        }
    }

    public static int blend(int a, int b, int t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int r = ar + (br - ar) * t / 255;
        int g = ag + (bg - ag) * t / 255;
        int bl = ab + (bb - ab) * t / 255;
        return (r << 16) | (g << 8) | bl;
    }

    public static void panel(Graphics g, int x, int y, int w, int h) {
        g.setColor(PANEL);
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(PANEL_EDGE);
        g.drawRoundRect(x, y, w, h, 12, 12);
    }

    public static void center(Graphics g, Font font, int color, String text, int y) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, g.getClipWidth() / 2, y, Graphics.TOP | Graphics.HCENTER);
    }

    public static void text(Graphics g, Font font, int color, String text,
                            int x, int y, int anchor) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y, anchor);
    }

    public static void hintBar(Graphics g, int w, int y, int color) {
        g.setColor(color);
        g.fillRect(14, y, w - 28, 1);
    }
}

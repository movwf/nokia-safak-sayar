package com.safaksayar;

import javax.microedition.lcdui.Graphics;

/**
 * Draws the big countdown digits as a seven-segment display.
 * This scales cleanly on every screen size and keeps the JAR tiny.
 */
public final class SevenSegment {

    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 4;
    private static final int D = 8;
    private static final int E = 16;
    private static final int F = 32;
    private static final int G = 64;

    private static final int[] DIGITS = {
        A | B | C | D | E | F,          // 0
        B | C,                          // 1
        A | B | G | E | D,              // 2
        A | B | G | C | D,              // 3
        F | G | B | C,                  // 4
        A | F | G | C | D,              // 5
        A | F | G | E | C | D,          // 6
        A | B | C,                      // 7
        A | B | C | D | E | F | G,      // 8
        A | B | C | D | F | G           // 9
    };

    private SevenSegment() {
    }

    public static int digitWidth(int height) {
        return (height * 5) / 9;
    }

    public static int thickness(int height) {
        int t = height / 8;
        return t < 3 ? 3 : t;
    }

    public static int width(int digits, int height) {
        if (digits <= 0) {
            return 0;
        }
        int w = digitWidth(height);
        int gap = thickness(height) + 2;
        return digits * w + (digits - 1) * gap;
    }

    public static void draw(Graphics g, String text, int x, int y, int height,
                            int onColor, int offColor, boolean drawOff) {
        int w = digitWidth(height);
        int gap = thickness(height) + 2;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch < '0' || ch > '9') {
                continue;
            }
            int mask = DIGITS[ch - '0'];
            int dx = x + i * (w + gap);
            if (drawOff) {
                g.setColor(offColor);
                drawMask(g, dx, y, w, height, A | B | C | D | E | F | G);
            }
            g.setColor(onColor);
            drawMask(g, dx, y, w, height, mask);
        }
    }

    private static void drawMask(Graphics g, int x, int y, int w, int h, int mask) {
        int t = thickness(h);
        int half = h / 2;
        int t2 = t / 2;

        int hx = x + t;
        int hw = w - 2 * t;
        if ((mask & A) != 0) {
            g.fillRect(hx, y, hw, t);
        }
        if ((mask & G) != 0) {
            g.fillRect(hx, y + half - t2, hw, t);
        }
        if ((mask & D) != 0) {
            g.fillRect(hx, y + h - t, hw, t);
        }

        int topY = y + t;
        int topH = half - t - t2;
        if ((mask & F) != 0) {
            g.fillRect(x, topY, t, topH);
        }
        if ((mask & B) != 0) {
            g.fillRect(x + w - t, topY, t, topH);
        }

        int bottomY = y + half + t2;
        int bottomH = h - half - t - t2;
        if ((mask & E) != 0) {
            g.fillRect(x, bottomY, t, bottomH);
        }
        if ((mask & C) != 0) {
            g.fillRect(x + w - t, bottomY, t, bottomH);
        }
    }
}

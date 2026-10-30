package com.safaksayar;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public final class CalendarCanvas extends Canvas implements CommandListener {

    private final SafakSayarMIDlet app;
    private final Command cmdBack = new Command("Geri", Command.BACK, 1);
    private final Command cmdToday = new Command("Bugün", Command.SCREEN, 2);

    private final Font fSmall;
    private final Font fSmallBold;
    private final Font fTitle;

    private int year;
    private int month;

    public CalendarCanvas(SafakSayarMIDlet app) {
        this.app = app;
        int[] ymd = DateUtil.todayYmd();
        year = ymd[0];
        month = ymd[1];
        addCommand(cmdBack);
        addCommand(cmdToday);
        setCommandListener(this);
        fSmall = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fSmallBold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        fTitle = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
    }

    protected void showNotify() {
        repaint();
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Ui.background(g, w, h);

        g.setFont(fTitle);
        g.setColor(Ui.TEXT);
        String title = DateUtil.MONTHS_UPPER[month - 1] + " " + year;
        g.drawString(title, w / 2, 5, Graphics.TOP | Graphics.HCENTER);

        int arrowY = 18;
        g.setColor(Ui.ACCENT);
        g.fillTriangle(18, arrowY - 6, 18, arrowY + 6, 10, arrowY);
        g.fillTriangle(w - 18, arrowY - 6, w - 18, arrowY + 6, w - 10, arrowY);

        g.setColor(Ui.PANEL_EDGE);
        g.fillRect(14, 34, w - 28, 1);

        int cellW = w / 7;
        for (int i = 0; i < 7; i++) {
            g.setFont(fSmallBold);
            g.setColor(i >= 5 ? Ui.ACCENT_DARK : Ui.MUTED);
            g.drawString(DateUtil.DAY_INITIALS[i], i * cellW + cellW / 2, 39,
                    Graphics.TOP | Graphics.HCENTER);
        }

        int gridY = 55;
        int footerLine = h - 30;
        int cellH = (footerLine - gridY) / 6;
        if (cellH > 34) {
            cellH = 34;
        }
        if (cellH < 18) {
            cellH = 18;
        }
        int used = cellH * 6;
        int spare = footerLine - gridY - used;
        if (spare > 0) {
            gridY += spare / 2;
        }

        long firstDays = DateUtil.daysFromCivil(year, month, 1);
        int firstWeekday = DateUtil.weekday(firstDays);
        int dim = DateUtil.daysInMonth(year, month);

        long todayDays = DateUtil.today();
        int[] todayYmd = DateUtil.civilFromDays(todayDays);

        boolean configured = app.getStore().configured;
        long targetDays = configured ? app.getStore().dischargeDays() : -1;
        int[] targetYmd = configured ? DateUtil.civilFromDays(targetDays) : null;

        for (int day = 1; day <= dim; day++) {
            int index = firstWeekday + day - 1;
            int row = index / 7;
            int col = index % 7;
            int cx = col * cellW + cellW / 2;
            int cy = gridY + row * cellH + cellH / 2;

            long dayDays = firstDays + day - 1;
            boolean isToday = year == todayYmd[0] && month == todayYmd[1]
                    && day == todayYmd[2];
            boolean isTarget = configured && year == targetYmd[0]
                    && month == targetYmd[1] && day == targetYmd[2];

            int boxW = cellW - 8;
            int boxH = cellH - 4;
            if (boxW < 12) {
                boxW = 12;
            }
            if (boxH < 12) {
                boxH = 12;
            }
            int boxX = cx - boxW / 2;
            int boxY = cy - boxH / 2;

            if (isTarget) {
                g.setColor(isToday ? Ui.GREEN : Ui.ACCENT);
                g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
            } else if (isToday) {
                g.setColor(Ui.PANEL);
                g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
                g.setColor(Ui.ACCENT);
                g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
            }

            int color;
            if (isTarget) {
                color = Ui.BG_TOP;
            } else if (isToday) {
                color = Ui.ACCENT;
            } else if (dayDays < todayDays) {
                color = Ui.DIM;
            } else {
                color = Ui.TEXT;
            }

            Font dayFont = (isToday || isTarget) ? fSmallBold : fSmall;
            g.setFont(dayFont);
            g.setColor(color);
            g.drawString(String.valueOf(day), cx,
                    cy - dayFont.getHeight() / 2,
                    Graphics.HCENTER | Graphics.TOP);
        }

        g.setColor(Ui.PANEL_EDGE);
        g.fillRect(14, h - 30, w - 28, 1);

        g.setFont(fSmall);
        if (configured) {
            int remaining = (int) (targetDays - todayDays);
            String left;
            if (remaining > 0) {
                left = "ŞAFAK " + remaining + " GÜN";
            } else if (remaining == 0) {
                left = "BUGÜN TERHİS!";
            } else {
                left = "TERHİS " + (-remaining) + " GÜN ÖNCE";
            }
            g.setColor(remaining > 0 ? Ui.ACCENT : Ui.GREEN);
            g.drawString(left, 14, h - 22, Graphics.TOP | Graphics.LEFT);
        } else {
            g.setColor(Ui.MUTED);
            g.drawString("Tarih ayarlanmadı", 14, h - 22,
                    Graphics.TOP | Graphics.LEFT);
        }

        g.setColor(Ui.MUTED);
        g.drawString("0: BUGÜN", w - 14, h - 22,
                Graphics.TOP | Graphics.RIGHT);
    }

    private void goToday() {
        int[] ymd = DateUtil.todayYmd();
        year = ymd[0];
        month = ymd[1];
    }

    private void goDischarge() {
        if (!app.getStore().configured) {
            return;
        }
        int[] ymd = DateUtil.civilFromDays(app.getStore().dischargeDays());
        year = ymd[0];
        month = ymd[1];
    }

    private void previousMonth() {
        month--;
        if (month < 1) {
            month = 12;
            year--;
        }
        clampYear();
    }

    private void nextMonth() {
        month++;
        if (month > 12) {
            month = 1;
            year++;
        }
        clampYear();
    }

    private void clampYear() {
        if (year < 1970) {
            year = 1970;
            month = 1;
        }
        if (year > 2100) {
            year = 2100;
            month = 12;
        }
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == Canvas.LEFT) {
            previousMonth();
            repaint();
            return;
        }
        if (action == Canvas.RIGHT) {
            nextMonth();
            repaint();
            return;
        }
        if (action == Canvas.UP) {
            year--;
            clampYear();
            repaint();
            return;
        }
        if (action == Canvas.DOWN) {
            year++;
            clampYear();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM0) {
            goToday();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_STAR) {
            goDischarge();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM4) {
            previousMonth();
            repaint();
            return;
        }
        if (keyCode == Canvas.KEY_NUM6) {
            nextMonth();
            repaint();
        }
    }

    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdBack) {
            app.showMain(false);
        } else if (c == cmdToday) {
            goToday();
            repaint();
        }
    }
}

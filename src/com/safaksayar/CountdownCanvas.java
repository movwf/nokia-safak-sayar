package com.safaksayar;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public final class CountdownCanvas extends Canvas implements CommandListener {

    private static final String[] QUOTES = {
        "Sabret, her gün bir adım daha.",
        "Her şafak yeni bir başlangıç.",
        "Az kaldı, dayan.",
        "Bugün de geride kaldı.",
        "Güneş her sabah yeniden doğar.",
        "Geri sayım devam ediyor.",
        "Yarın bugünden daha yakın.",
        "Hayırlı tezkereler!"
    };

    private final SafakSayarMIDlet app;
    private final Command cmdCalendar = new Command("Takvim", Command.SCREEN, 1);
    private final Command cmdMenu = new Command("Menü", Command.SCREEN, 2);

    private final Font fSmall;
    private final Font fSmallBold;
    private final Font fMediumBold;

    public CountdownCanvas(SafakSayarMIDlet app) {
        this.app = app;
        addCommand(cmdCalendar);
        addCommand(cmdMenu);
        setCommandListener(this);
        fSmall = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fSmallBold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        fMediumBold = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    }

    public void refresh() {
        repaint();
    }

    protected void showNotify() {
        repaint();
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Ui.background(g, w, h);

        long today = DateUtil.today();
        int[] todayYmd = DateUtil.civilFromDays(today);

        g.setFont(fSmallBold);
        g.setColor(Ui.TEXT);
        g.drawString("ŞAFAK SAYAR", 12, 8, Graphics.TOP | Graphics.LEFT);
        g.setFont(fSmall);
        g.setColor(Ui.MUTED);
        g.drawString(DateUtil.formatShort(todayYmd[0], todayYmd[1], todayYmd[2]),
                w - 12, 8, Graphics.TOP | Graphics.RIGHT);

        g.setColor(Ui.ACCENT);
        g.fillRect(12, 25, 22, 2);
        g.setColor(Ui.PANEL_EDGE);
        g.fillRect(38, 25, w - 50, 1);

        if (!app.getStore().configured) {
            paintEmpty(g, w, h);
            return;
        }

        Store store = app.getStore();
        long target = store.dischargeDays();
        long start = store.startDays();
        int remaining = (int) (target - today);
        int heroValue = remaining > 0 ? remaining : (remaining == 0 ? 0 : -remaining);
        String hero = String.valueOf(heroValue);

        int heroHeight = h / 4;
        if (heroHeight > 84) {
            heroHeight = 84;
        }
        if (heroHeight < 54) {
            heroHeight = 54;
        }
        int heroY = 38;
        int heroWidth = SevenSegment.width(hero.length(), heroHeight);
        int heroX = (w - heroWidth) / 2;
        int heroColor = remaining > 0 ? Ui.ACCENT : Ui.GREEN;
        SevenSegment.draw(g, hero, heroX, heroY, heroHeight,
                heroColor, Ui.SEG_OFF, true);

        String label;
        if (remaining > 0) {
            label = "GÜN KALDI";
        } else if (remaining == 0) {
            label = "TERHİS GÜNÜ!";
        } else {
            label = "GÜN GEÇTİ";
        }
        int labelY = heroY + heroHeight + 7;
        g.setFont(fMediumBold);
        g.setColor(remaining > 0 ? Ui.TEXT : Ui.GREEN);
        g.drawString(label, w / 2, labelY, Graphics.TOP | Graphics.HCENTER);

        // Date pill: "TERHİS  15.10.2027 · CUMA"
        String dateText = DateUtil.formatShort(store.dischargeYear,
                store.dischargeMonth, store.dischargeDay);
        String dayName = DateUtil.DAYS_UPPER[DateUtil.weekday(target)];
        String fullDate = dateText + " · " + dayName;
        int prefixW = fSmall.stringWidth("TERHİS");
        int dateW = fSmallBold.stringWidth(fullDate);
        int pillW = prefixW + dateW + 32;
        if (pillW > w - 24) {
            fullDate = dateText;
            dateW = fSmallBold.stringWidth(fullDate);
            pillW = prefixW + dateW + 32;
        }
        int pillH = 24;
        int pillY = labelY + 22;
        int pillX = (w - pillW) / 2;
        Ui.panel(g, pillX, pillY, pillW, pillH);
        g.setFont(fSmall);
        g.setColor(Ui.MUTED);
        g.drawString("TERHİS", pillX + 12,
                pillY + (pillH - fSmall.getHeight()) / 2,
                Graphics.LEFT | Graphics.TOP);
        g.setFont(fSmallBold);
        g.setColor(Ui.ACCENT);
        g.drawString(fullDate, pillX + 12 + prefixW + 8,
                pillY + (pillH - fSmallBold.getHeight()) / 2,
                Graphics.LEFT | Graphics.TOP);

        // Progress
        int progressTop = pillY + pillH + 28;
        int barX = 20;
        int barW = w - 40;
        int barH = 9;

        long total = target - start;
        long elapsed = today - start;
        int pct;
        if (total <= 0) {
            pct = 100;
        } else {
            long p = elapsed * 100 / total;
            if (p < 0) {
                p = 0;
            }
            if (p > 100) {
                p = 100;
            }
            pct = (int) p;
        }

        g.setFont(fSmall);
        g.setColor(Ui.MUTED);
        g.drawString("BAŞLANGIÇ " + DateUtil.formatShort(store.startYear,
                store.startMonth, store.startDay),
                barX, progressTop - 14, Graphics.LEFT | Graphics.TOP);
        g.setColor(Ui.ACCENT);
        g.drawString("%" + pct, barX + barW, progressTop - 14,
                Graphics.RIGHT | Graphics.TOP);

        g.setColor(Ui.TRACK);
        g.fillRoundRect(barX, progressTop, barW, barH, barH, barH);
        int fillW = barW * pct / 100;
        if (fillW > 0) {
            g.setColor(Ui.ACCENT);
            g.fillRoundRect(barX, progressTop, fillW, barH, barH, barH);
        }

        String stats;
        if (remaining > 0) {
            if (elapsed < 0) {
                stats = "Henüz başlamadı";
            } else {
                stats = elapsed + " / " + total + " gün geçti";
            }
        } else if (remaining == 0) {
            stats = "Bugün terhis günü. Hayırlı tezkereler!";
        } else {
            stats = "Terhisin üzerinden " + (-remaining) + " gün geçti.";
        }
        g.setFont(fSmall);
        g.setColor(Ui.MUTED);
        g.drawString(stats, w / 2, progressTop + 17,
                Graphics.TOP | Graphics.HCENTER);

        // A small daily line at the bottom.
        int quoteY = h - 24;
        if (quoteY > progressTop + 34) {
            Ui.hintBar(g, w, quoteY - 8, Ui.PANEL_EDGE);
            String quote;
            if (remaining == 0) {
                quote = "Hayırlı tezkereler!";
            } else if (remaining < 0) {
                quote = "Geçmiş olsun, asker!";
            } else {
                int qi = (int) (today % QUOTES.length);
                if (qi < 0) {
                    qi += QUOTES.length;
                }
                quote = QUOTES[qi];
            }
            g.setFont(fSmall);
            g.setColor(Ui.DIM);
            g.drawString(quote, w / 2, quoteY,
                    Graphics.TOP | Graphics.HCENTER);
        }
    }

    private void paintEmpty(Graphics g, int w, int h) {
        int panelH = 136;
        int panelY = (h - panelH) / 2;
        if (panelY < 44) {
            panelY = 44;
        }
        Ui.panel(g, 22, panelY, w - 44, panelH);

        g.setFont(fMediumBold);
        g.setColor(Ui.ACCENT);
        g.drawString("HOŞ GELDİN", w / 2, panelY + 16,
                Graphics.TOP | Graphics.HCENTER);

        g.setFont(fSmall);
        g.setColor(Ui.TEXT);
        g.drawString("Terhis tarihini ayarla,", w / 2, panelY + 46,
                Graphics.TOP | Graphics.HCENTER);
        g.drawString("geri sayım başlasın.", w / 2, panelY + 62,
                Graphics.TOP | Graphics.HCENTER);

        g.setColor(Ui.MUTED);
        g.drawString("Menü > Tarih Ayarla", w / 2, panelY + 98,
                Graphics.TOP | Graphics.HCENTER);

        int quoteY = h - 24;
        if (quoteY > panelY + panelH + 12) {
            g.setColor(Ui.DIM);
            g.drawString("Asker Şafak Sayar", w / 2, quoteY,
                    Graphics.TOP | Graphics.HCENTER);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdCalendar) {
            app.showCalendar();
        } else if (c == cmdMenu) {
            app.showMenu();
        }
    }

    protected void keyPressed(int keyCode) {
        if (getGameAction(keyCode) == Canvas.FIRE) {
            app.showCalendar();
        }
    }
}

package com.safaksayar;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

public final class SettingsForm extends Form implements CommandListener {

    private final SafakSayarMIDlet app;
    private final DateField dischargeField;
    private final DateField startField;
    private final Command cmdSave = new Command("Kaydet", Command.OK, 1);
    private final Command cmdCancel = new Command("İptal", Command.BACK, 2);

    public SettingsForm(SafakSayarMIDlet app, boolean firstRun) {
        super("Tarih Ayarla");
        this.app = app;

        Font small = Font.getFont(Font.FACE_PROPORTIONAL,
                Font.STYLE_PLAIN, Font.SIZE_SMALL);
        Font smallBold = Font.getFont(Font.FACE_PROPORTIONAL,
                Font.STYLE_BOLD, Font.SIZE_SMALL);

        StringItem intro = new StringItem("", firstRun
                ? "Terhis tarihini seç, geri sayım başlasın."
                : "Terhis gününe kalan süre bu tarihe göre hesaplanır.");
        intro.setFont(small);
        append(intro);

        dischargeField = new DateField("Terhis tarihi", DateField.DATE);
        startField = new DateField("Başlangıç tarihi", DateField.DATE);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Store store = app.getStore();
        if (store.configured) {
            setDate(c, store.dischargeYear, store.dischargeMonth, store.dischargeDay);
            dischargeField.setDate(c.getTime());

            setDate(c, store.startYear, store.startMonth, store.startDay);
            startField.setDate(c.getTime());
        } else {
            int[] discharge = DateUtil.civilFromDays(DateUtil.today() + 180);
            setDate(c, discharge[0], discharge[1], discharge[2]);
            dischargeField.setDate(c.getTime());

            int[] start = DateUtil.todayYmd();
            setDate(c, start[0], start[1], start[2]);
            startField.setDate(c.getTime());
        }

        append(dischargeField);
        append(startField);

        StringItem note = new StringItem("",
                "Başlangıç tarihi ilerleme çubuğu için kullanılır. "
                + "Takvimde sol/sağ ay, yukarı/aşağı yıl değiştirir.");
        note.setFont(small);
        append(note);

        StringItem tip = new StringItem("", "Hayırlı tezkereler!");
        tip.setFont(smallBold);
        append(tip);

        addCommand(cmdSave);
        addCommand(cmdCancel);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdSave) {
            save();
        } else if (c == cmdCancel) {
            app.showMain(false);
        }
    }

    private void save() {
        Date discharge = dischargeField.getDate();
        Date start = startField.getDate();
        if (discharge == null || start == null) {
            app.showAlert("Eksik tarih", "Lütfen iki tarihi de seç.", this);
            return;
        }

        int[] dy = toYmd(discharge);
        int[] sy = toYmd(start);
        long dischargeDays = DateUtil.daysFromCivil(dy[0], dy[1], dy[2]);
        long startDays = DateUtil.daysFromCivil(sy[0], sy[1], sy[2]);
        if (dischargeDays < startDays) {
            app.showAlert("Tarih hatası",
                    "Terhis tarihi başlangıçtan önce olamaz.", this);
            return;
        }

        app.setDates(dy, sy);
        app.showMain(true);
    }

    /**
     * CLDC 1.1's Calendar has no set(year, month, day) convenience method,
     * so set the fields one by one. Day 1 avoids month-length rollover.
     */
    private static void setDate(Calendar c, int y, int m, int d) {
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.YEAR, y);
        c.set(Calendar.MONTH, m - 1);
        c.set(Calendar.DAY_OF_MONTH, d);
    }

    private static int[] toYmd(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return new int[] {
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        };
    }
}

package com.safaksayar;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public final class SafakSayarMIDlet extends MIDlet implements CommandListener {

    private Display display;
    private Store store;
    private CountdownCanvas main;
    private List menu;
    private boolean initialized;

    private final Command cmdSelect = new Command("Seç", Command.OK, 1);
    private final Command cmdBack = new Command("Geri", Command.BACK, 2);

    protected void startApp() throws MIDletStateChangeException {
        if (!initialized) {
            initialized = true;
            display = Display.getDisplay(this);
            store = new Store();
            store.load();
            main = new CountdownCanvas(this);

            if (store.configured) {
                display.setCurrent(main);
            } else {
                Alert welcome = new Alert("Şafak Sayar",
                        "Terhis tarihini ayarla, geri sayım başlasın.",
                        null, AlertType.INFO);
                welcome.setTimeout(4000);
                display.setCurrent(welcome, new SettingsForm(this, true));
            }
        } else {
            main.refresh();
        }
    }

    protected void pauseApp() {
        // Nothing to do.
    }

    protected void destroyApp(boolean unconditional)
            throws MIDletStateChangeException {
        // Nothing to do.
    }

    public void showMenu() {
        if (menu == null) {
            menu = new List("Menü", List.IMPLICIT);
            menu.append("Tarih Ayarla", null);
            menu.append("Takvim", null);
            menu.append("Hakkında", null);
            menu.append("Çıkış", null);
            menu.addCommand(cmdSelect);
            menu.addCommand(cmdBack);
            menu.setCommandListener(this);
        }
        display.setCurrent(menu);
    }

    public void showMain(boolean saved) {
        main.refresh();
        if (saved) {
            Alert done = new Alert("Kaydedildi",
                    "Terhis tarihi güncellendi.", null,
                    AlertType.CONFIRMATION);
            done.setTimeout(1500);
            display.setCurrent(done, main);
        } else {
            display.setCurrent(main);
        }
    }

    public void showCalendar() {
        display.setCurrent(new CalendarCanvas(this));
    }

    public void showSettings(boolean firstRun) {
        display.setCurrent(new SettingsForm(this, firstRun));
    }

    public void showAbout() {
        display.setCurrent(new AboutForm(this));
    }

    public void showAlert(String title, String message, Displayable next) {
        Alert alert = new Alert(title, message, null, AlertType.ERROR);
        alert.setTimeout(3000);
        display.setCurrent(alert, next);
    }

    public void setDates(int[] discharge, int[] start) {
        store.configured = true;
        store.dischargeYear = discharge[0];
        store.dischargeMonth = discharge[1];
        store.dischargeDay = discharge[2];
        store.startYear = start[0];
        store.startMonth = start[1];
        store.startDay = start[2];
        store.save();
        main.refresh();
    }

    public Store getStore() {
        return store;
    }

    public Display getDisplay() {
        return display;
    }

    public void exit() {
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException e) {
            // Ignore.
        }
        notifyDestroyed();
    }

    public void commandAction(Command c, Displayable d) {
        if (d != menu) {
            return;
        }
        if (c == cmdBack) {
            display.setCurrent(main);
            return;
        }
        if (c != List.SELECT_COMMAND && c != cmdSelect) {
            return;
        }

        int index = menu.getSelectedIndex();
        if (index == 0) {
            showSettings(false);
        } else if (index == 1) {
            showCalendar();
        } else if (index == 2) {
            showAbout();
        } else if (index == 3) {
            exit();
        }
    }
}

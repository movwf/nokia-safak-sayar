package com.safaksayar;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.StringItem;

public final class AboutForm extends Form implements CommandListener {

    private final SafakSayarMIDlet app;
    private final Command cmdBack = new Command("Geri", Command.BACK, 1);

    public AboutForm(SafakSayarMIDlet app) {
        super("Hakkında");
        this.app = app;

        Font small = Font.getFont(Font.FACE_PROPORTIONAL,
                Font.STYLE_PLAIN, Font.SIZE_SMALL);

        try {
            Image icon = Image.createImage("/icon.png");
            append(new ImageItem("", icon, ImageItem.LAYOUT_CENTER, "Şafak Sayar"));
        } catch (Exception e) {
            // Icon is optional.
        }

        StringItem title = new StringItem("Şafak Sayar", "Sürüm 1.0");
        title.setFont(small);
        append(title);

        StringItem body = new StringItem("",
                "Terhis gününe kalan süreyi gösterir.\n\n"
                + "Takvim ekranında bugün ve terhis tarihi işaretlenir.\n\n"
                + "Takvim tuşları: sol/sağ ay, yukarı/aşağı yıl, 0 bugün, "
                + "* terhis ayı.\n\n"
                + "Nokia 5130c-2 ve MIDP 2.0 uyumlu cihazlar için hazırlandı.\n\n"
                + "Asker Şafak Sayar, hayırlı tezkereler!");
        body.setFont(small);
        append(body);

        addCommand(cmdBack);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdBack) {
            app.showMain(false);
        }
    }
}

package com.safaksayar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.rms.RecordStore;

public final class Store {

    private static final String RS_NAME = "safak-sayar";
    private static final int VERSION = 1;

    public boolean configured;

    public int dischargeYear;
    public int dischargeMonth;
    public int dischargeDay;

    public int startYear;
    public int startMonth;
    public int startDay;

    public void load() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RS_NAME, true);
            if (rs.getNumRecords() > 0) {
                byte[] data = rs.getRecord(1);
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
                int version = in.readInt();
                if (version == VERSION) {
                    configured = in.readBoolean();
                    dischargeYear = in.readInt();
                    dischargeMonth = in.readInt();
                    dischargeDay = in.readInt();
                    startYear = in.readInt();
                    startMonth = in.readInt();
                    startDay = in.readInt();
                }
                in.close();
            }
        } catch (Exception e) {
            configured = false;
        } finally {
            close(rs);
        }
    }

    public void save() {
        RecordStore rs = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            out.writeInt(VERSION);
            out.writeBoolean(configured);
            out.writeInt(dischargeYear);
            out.writeInt(dischargeMonth);
            out.writeInt(dischargeDay);
            out.writeInt(startYear);
            out.writeInt(startMonth);
            out.writeInt(startDay);
            out.flush();
            byte[] data = bos.toByteArray();
            out.close();

            rs = RecordStore.openRecordStore(RS_NAME, true);
            if (rs.getNumRecords() == 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
        } catch (Exception e) {
            // Storage is a nice-to-have; the app still works without it.
        } finally {
            close(rs);
        }
    }

    public long dischargeDays() {
        return DateUtil.daysFromCivil(dischargeYear, dischargeMonth, dischargeDay);
    }

    public long startDays() {
        return DateUtil.daysFromCivil(startYear, startMonth, startDay);
    }

    private static void close(RecordStore rs) {
        if (rs != null) {
            try {
                rs.closeRecordStore();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}

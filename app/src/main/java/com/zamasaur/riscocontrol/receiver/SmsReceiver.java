package com.zamasaur.riscocontrol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.zamasaur.riscocontrol.db.LogDatabase;
import com.zamasaur.riscocontrol.db.LogEntry;

import java.util.concurrent.Executors;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        String centralNumber = context.getSharedPreferences("risco_prefs", Context.MODE_PRIVATE)
                .getString("central_number", null);
        if (centralNumber == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            String sender = sms.getOriginatingAddress();
            String body = sms.getMessageBody();

            if (sender != null && normalizeNumber(sender).equals(normalizeNumber(centralNumber))) {
                LogEntry entry = new LogEntry(body, System.currentTimeMillis());
                Executors.newSingleThreadExecutor().execute(() ->
                        LogDatabase.getInstance(context).logDao().insert(entry)
                );
            }
        }
    }

    private String normalizeNumber(String number) {
        return number.replaceAll("[\\s\\-\\(\\)]", "");
    }
}
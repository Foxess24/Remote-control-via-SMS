package com.example.remotelabour;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;

public class RicezioneSMS extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public String testo;
    public String numero;


    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if(intent.getAction() == SMS_RECEIVED){
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Object[] smsExtra = (Object[]) extras.get("pdus");
                for (Object o : smsExtra) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) o);
                    testo = sms.getMessageBody();
                    numero = sms.getOriginatingAddress();
                }
            }
        }
    }
}

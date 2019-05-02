package in.wadersgroup.companion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by romil_wadersgroup on 3/3/16.
 */
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();
                    String otp = "";
                    Pattern p = Pattern.compile(": (\\d+)\\D*");
                    Matcher m = p.matcher(message);
                    if (m.find()) {
                        otp = m.group(1);
                        System.out.println("OTP: " + otp);
                    }
                    if (senderNum.contains("WGCOMP")) {
                        try {
                            System.out.println("Setting OTP: " + otp);
                            OTPActivity.getInstance().setOtp(otp);

                        } catch (Exception e) {
                            AnalyticsApplication.getInstance().trackException(e);
                        }
                    }

                }
            }

        } catch (Exception e) {
            AnalyticsApplication.getInstance().trackException(e);

        }

    }
}

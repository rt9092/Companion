package in.wadersgroup.companion;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by romil_wadersgroup on 5/2/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    Context c;


    @Override
    public void onReceive(Context context, Intent intent) {

        c = context;
        context.startService(new Intent(context, PassiveLocationUpdateService.class));

        if (CommonMethods.readStringPreference(c, "contactsApi", "contacts").trim().contentEquals("yes")) {

        } else {
            new PickContacts().execute();
            //CommonMethods.showShortToast(c, "Contacts Saving Hit");
        }

    }

    class PickContacts extends AsyncTask<Void, Void, Void> {

        String res;

        @Override
        protected Void doInBackground(Void... params) {

            ContentResolver cr = c.getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            String allContacts = "";

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));

                            allContacts = allContacts + "\"" + name + "\"" + "," + "\"" + phoneNo + "\"" + "\n";

                        }
                        pCur.close();
                    }
                }

                System.out.println("Contacts: " + allContacts);

                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                try {
                    String email = CommonMethods
                            .readStringPreference(c,
                                    "userDetails", "email");
                    String secret = CommonMethods
                            .readStringPreference(c, "secret",
                                    "secretKey");

                    String data = allContacts;


                    JSONObject jsonContacts = new JSONObject();
                    jsonContacts.put("email", email);
                    jsonContacts.put("secret_key", secret);
                    jsonContacts.put("data", data);

                    RequestBody body = RequestBody.create(JSON, jsonContacts.toString());

                    Request request = new Request.Builder()
                            .url(Constants.CONTACTS_API)
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    res = response.body().string();
                    System.out.println("Send: " + jsonContacts.toString());
                    System.out.println("Response of Contacts: " + res);
                    if (res != null) {
                        JSONObject jsonObj = new JSONObject(res.trim());
                        String success = jsonObj.getString("success").trim();
                        if (success.contentEquals("yes")) {
                            CommonMethods.createStringPreference(c, "contactsApi", "contacts", "yes");
                        } else {
                            // No success
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                } catch (IOException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                } catch (JSONException e) {
                    AnalyticsApplication.getInstance().trackException(e);
                    e.printStackTrace();
                }


            }

            return null;
        }
    }


}

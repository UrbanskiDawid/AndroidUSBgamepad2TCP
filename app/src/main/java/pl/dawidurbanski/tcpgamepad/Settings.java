package pl.dawidurbanski.tcpgamepad;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.ApplicationTestCase;
import android.widget.Toast;

import java.io.FileOutputStream;

/**
 * Created by dawid on 23.01.2016.
 */
public class Settings {

    public String adress ="";
    public int port = -1;

    public static Settings settings;

    private static final Settings instance = new Settings();
    public static Settings getInstance() {return instance;}

    final String mSharedPreferencesName = "pl.dawidurbanski.tcpgamepad";

    public void save(Context context) {

        SharedPreferences userDetails = context.getSharedPreferences(mSharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.clear();
        edit.putString("adress", adress.trim());
        edit.putInt("port", port);
        edit.commit();
        Toast.makeText(context, "settings are saved.",Toast.LENGTH_SHORT).show();
    }

    public void load(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        adress = userDetails.getString("adress", "127.0.0.1");
        port = userDetails.getInt("port", 8080);

        Toast.makeText(context, "settings loaded.",Toast.LENGTH_SHORT).show();
    }
}

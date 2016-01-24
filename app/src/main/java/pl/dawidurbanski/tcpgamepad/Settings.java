package pl.dawidurbanski.tcpgamepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Dawid on 23.01.2016.
 */
public class Settings {

    public String adress ="";
    public int port = -1;

    public static Settings settings;

    private static final Settings instance = new Settings();
    public static Settings getInstance() {return instance;}

    private static String
       SETTINGS_ADDRESS = "address",
       SETTINGS_PORT = "port";

    private static String SETTINGS_ADDRESS_DEFAULT = "127.0.0.1";
    private static Integer SETTINGS_PORT_DEFAULT = 8080;

    final String mSharedPreferencesName = "pl.dawidurbanski.tcpgamepad";

    public void save(Context context) {

        if(context==null)
        {
            Log.e(Settings.class.getName()+":"," save( !contest is null! )");
            return;
        }
        SharedPreferences userDetails = context.getSharedPreferences(mSharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userDetails.edit();
        edit.clear();
        edit.putString(SETTINGS_ADDRESS, adress.trim());
        edit.putInt(SETTINGS_PORT, port);
        edit.commit();
        Toast.makeText(context, "settings are saved.",Toast.LENGTH_SHORT).show();
    }

    public void load(Context context) {
        SharedPreferences userDetails = context.getSharedPreferences(mSharedPreferencesName, Context.MODE_PRIVATE);
        try {
            adress = userDetails.getString(SETTINGS_ADDRESS, SETTINGS_ADDRESS_DEFAULT);
            port = userDetails.getInt(SETTINGS_PORT, SETTINGS_PORT_DEFAULT);
        }catch (Exception e){
            adress = SETTINGS_ADDRESS_DEFAULT;
            port = SETTINGS_PORT_DEFAULT;
        }
        Toast.makeText(context, "settings loaded."+adress+":"+port,Toast.LENGTH_SHORT).show();
    }
}

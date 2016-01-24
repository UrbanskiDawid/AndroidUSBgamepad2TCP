package pl.dawidurbanski.tcpgamepad;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class ConnectionSettingsActivity extends AppCompatActivity  {

    // UI references.
    private AutoCompleteTextView mAdressView;
    private EditText mPortView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mAdressView = (AutoCompleteTextView) findViewById(R.id.adress);
        mAdressView.setText( Settings.getInstance().adress );

        mPortView = (EditText) findViewById(R.id.port);
        mPortView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPortView.setText("" + Settings.getInstance().port);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

     private boolean canRunAttemptLogin = true;
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if(!canRunAttemptLogin) return;
        canRunAttemptLogin = false;

        // Reset errors.
        mAdressView.setError(null);
        mPortView.setError(null);

        // Store values at the time of the login attempt.
        String adress = mAdressView.getText().toString();
        String port = mPortView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(port) && !isPortValid(port)) {
            mPortView.setError(getString(R.string.error_invalid_port));
            focusView = mPortView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(adress)) {
            mAdressView.setError(getString(R.string.error_field_required));
            focusView = mAdressView;
            cancel = true;
        } else if (!isAdressValid(adress)) {
            mAdressView.setError(getString(R.string.error_invalid_adress));
            focusView = mAdressView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //ApplicationTest applicationTest = getApplicationContext();
            Settings.getInstance().adress = adress;
            Settings.getInstance().port   = Integer.parseInt(port);
            Settings.getInstance().save( getApplicationContext() );
            finish();
        }
    }

    private boolean isAdressValid(String email) {
        return email.matches("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}$");
    }

    private boolean isPortValid(String password) {
        return password.length() > 1;
    }

}


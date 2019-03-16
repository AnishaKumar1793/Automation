package com.example.digipump;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SMSActivity extends AppCompatActivity {
    private static final String File_Name = "Contact_Info.txt";
    private static final String TAG = "";
    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText pump_Count;
    private EditText time_Duration;
    private EditText inputContact;
    private Button turn_ON;
    private Button turn_OFF;
    private String Contact_Info = null;
    private ImageView contact_Button;
    private final int REQUEST_CODE = 99;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        turn_ON = findViewById(R.id.turnON);
        turn_OFF = findViewById(R.id.turnOFF);
        pump_Count = findViewById(R.id.inputPumpCount);
        time_Duration = findViewById(R.id.inputTimer);
        turn_ON.setEnabled(false);
        turn_OFF.setEnabled(false);
        contact_Button = findViewById(R.id.contacts_button);
        inputContact = findViewById(R.id.inputContact);

        if (checkpermission(Manifest.permission.SEND_SMS)) {
            turn_ON.setEnabled(true);
            turn_OFF.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
            turn_ON.setEnabled(true);
            turn_OFF.setEnabled(true);
        }

        if (!checkpermission(Manifest.permission.READ_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        if (!checkpermission(Manifest.permission.RECEIVE_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }


        contact_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkpermission(Manifest.permission.READ_CONTACTS)) {
                    ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, SEND_SMS_PERMISSION_REQUEST_CODE);
                }

                if (!checkpermission(Manifest.permission.WRITE_CONTACTS)) {
                    ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, SEND_SMS_PERMISSION_REQUEST_CODE);
                }
                Toast.makeText(SMSActivity.this, "Image clicked", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);


            }
        });
    }


    public boolean checkpermission(String Permission) {
        int check = ContextCompat.checkSelfPermission(this, Permission);
        return ( check == PackageManager.PERMISSION_GRANTED );
    }

    public void Write_File(String data) {
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(File_Name, MODE_PRIVATE);
            fos.write(data.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void Read_File() {

        FileInputStream fis = null;
        try {
            fis = openFileInput(File_Name);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while (( Contact_Info = br.readLine() ) != null) {
                sb.append(Contact_Info).append("\n");
                Contact_Info.trim();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri result = data.getData();
            Log.v(TAG, "Got a result: " + result.toString());

// get the contact id from the Uri
            String id = result.getLastPathSegment();

// query for phone numbers for the selected contact id
            Cursor c = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[]{id}, null);

            int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

            if (c.getCount() >= 1) { // contact has multiple phone numbers
                final CharSequence[] numbers = new CharSequence[c.getCount()];
                int i = 0;
                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) { // for each phone number, add it to the numbers array
                        String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                        String number = type + ": " + c.getString(phoneIdx);
                        numbers[i++] = number;
                        c.moveToNext();
                    }
                    // build and show a simple dialog that allows the user to select a number
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Test");
                    //builder.setTitle(R.string.select_contact_phone_number_and_type);
                    builder.setItems(numbers, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            String number = (String) numbers[item];
                            int index = number.indexOf(":");
                            number = number.substring(index + 2);
                            loadContactInfo(number); // do something with the selected number
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setOwnerActivity(this);
                    alert.show();

                } else Log.w(TAG, "No results");
            } //else if(c.getCount() == 1) {
            // contact has a single phone number, so there's no need to display a second dialog
        }
    }

    private void loadContactInfo(String number) {
        Toast.makeText(SMSActivity.this, "Setting number" + number, Toast.LENGTH_SHORT).show();
        inputContact.setText(number);
    }
}

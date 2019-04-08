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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
    final int READ_CONTACTS_PERMISSION_REQUEST_CODE = 1;
    private EditText pump_Count;
    private EditText time_Duration;
    private EditText inputContact;
    private EditText total_Pumps;
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
        total_Pumps = findViewById(R.id.total_Pumps);
        turn_ON.setEnabled(false);
        turn_OFF.setEnabled(false);
        contact_Button = findViewById(R.id.contacts_button);
        inputContact = findViewById(R.id.inputContact);

        if (!checkpermission(Manifest.permission.SEND_SMS)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        } else {
            turn_OFF.setEnabled(true);
            turn_ON.setEnabled(true);
        }

        Contact_Info = Read_File();
        if (Contact_Info != null) {
            inputContact.setText(Contact_Info);
        }


        contact_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkpermission(Manifest.permission.READ_CONTACTS)) {
                    ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });

        turn_ON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pump_Count.getText().toString().equals("") || time_Duration.getText().toString().equals("") || total_Pumps.getText().toString().equals("")) {
                    if (pump_Count.getText().toString().equals("")) {
                        pump_Count.setError("Pump count cannot be empty");
                    }
                    if (time_Duration.getText().toString().equals("")) {
                        time_Duration.setError("Time duration cannot be empty");
                    }
                    if (total_Pumps.getText().toString().equals("")) {
                        total_Pumps.setError("Total pump count cannot be empty");
                    }

                } else if (Integer.valueOf(pump_Count.getText().toString()) == 0 || Integer.valueOf(time_Duration.getText().toString()) == 0 || Integer.valueOf(total_Pumps.getText().toString()) == 0) {
                    if (Integer.valueOf(pump_Count.getText().toString()) == 0) {
                        pump_Count.setError("Pump count should be greater than \"0\"");
                    }
                    if (Integer.valueOf(time_Duration.getText().toString()) == 0) {
                        time_Duration.setError("Time duration should be greater than \"0\"");
                    }
                    if (Integer.valueOf(total_Pumps.getText().toString()) == 0) {
                        total_Pumps.setError("Total pump count should be greater than \"0\"");
                    }
                } else if (Integer.valueOf(pump_Count.getText().toString()) > Integer.valueOf(total_Pumps.getText().toString())) {
                    pump_Count.setError("\"Simultaneous pump count\" should be smaller than or equal to \"Total pump count\"");
                    total_Pumps.setError("\"Total pump count\" should be greater than or equal to \"Simultaneous pump count\"");
                }
            }
        });
    }

    private void requestPermission(String Message, String Permission) {
        switch (Permission) {
            //Manifest.permission.READ_CONTACTS
            //Manifest.permission.SEND_SMS
            case "SEND_SMS":
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Needed!")
                            .setMessage(Message)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
                break;

            case "READ_CONTACTS":
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Needed!")
                            .setMessage(Message)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST_CODE);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                }
                break;

        }
        //else {
//            ActivityCompat.requestPermissions(SMSActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_REQUEST_CODE);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String permission = permissions[0];
        switch (permission) {
            case "android.permission.READ_CONTACTS":
                if (requestCode == READ_CONTACTS_PERMISSION_REQUEST_CODE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        requestPermission("Permission to read contacts is needed to select contact from your contact list. \n\n We don't use your contact information, it's safe with you.", "READ_CONTACTS");
                    }
                }
                break;

            case "android.permission.SEND_SMS":
                if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        turn_ON.setEnabled(true);
                        turn_OFF.setEnabled(true);
                    } else {
                        requestPermission("Permission to send SMS is needed to enable the ability of app to send SMS to your farm to perform operations. \n\n We won't send any SMS until you click on the button", "SEND_SMS");
                    }
                }
                break;
        }
    }

    public boolean checkpermission(String Permission) {
        int check = ContextCompat.checkSelfPermission(this, Permission);
        return ( check == PackageManager.PERMISSION_GRANTED );
    }

    public void Write_File(String data) {
        FileOutputStream fos = null;
        data = data.replaceAll(" ", "");
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

    public String Read_File() {
        String number = null;
        FileInputStream fis = null;
        try {
            fis = openFileInput(File_Name);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while (( Contact_Info = br.readLine() ) != null) {
                sb.append(Contact_Info).append("\n");
                number = Contact_Info.trim();

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
        return number;
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
            int displayidx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String displayName;
            if (c.getCount() >= 1) { // contact has multiple phone numbers
                final CharSequence[] numbers = new CharSequence[c.getCount()];
                int i = 0;
                if (c.moveToFirst()) {
                    displayName = c.getString(displayidx);
                    while (!c.isAfterLast()) { // for each phone number, add it to the numbers array
                        String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                        String number = type + ": " + c.getString(phoneIdx);

                        numbers[i++] = number;
                        c.moveToNext();
                    }
                    final String finalDisplayName = displayName;
                    // build and show a simple dialog that allows the user to select a number
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(Html.fromHtml("Please select from below number(s) for " + "<i><font color='#FF7F27'>" + displayName + "<i></font>"))
                            .setCancelable(false)
                            .setItems(numbers, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    String number = (String) numbers[item];
                                    int index = number.indexOf(":");
                                    number = number.substring(index + 2);
                                    loadContactInfo(number, finalDisplayName); // do something with the selected number
                                }
                            })
                            .create().show();

                } else Log.w(TAG, "No results");
            } else {
                Toast.makeText(SMSActivity.this, "Contact number not found for selected entry \nPlease choose another entry", Toast.LENGTH_SHORT).show();

            }
            c.close();
        }
    }

    private void loadContactInfo(String number, String name) {
        name = name.trim();
        number = number.replaceAll(" ", "");
        String data = name.trim() + " (" + number + ")";
        Write_File(data);
        Toast.makeText(SMSActivity.this, "Selecting " + data, Toast.LENGTH_SHORT).show();
        inputContact.setText(data);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Confirmation")
                .setCancelable(false)
                .setMessage("Do you really want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
}

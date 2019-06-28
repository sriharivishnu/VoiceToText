package com.magnitudestudios.sriharivishnu.voicetotext;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextToSpeech textToSpeech, ttsResponse;
    private ImageButton recordButton;
    private TextView message, response;
    private final int REQ_CODE_SPEECH_OUTPUT = 142;
    public String command = "";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 143;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 144;
    public String[] spliced;
    private String[] functions = {"hello","how","what","who","why","where","create", "call"};
    private HashMap<String,String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "LANGUAGE ERROR");
                    }
                }
            }

        });
        ttsResponse = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int result = ttsResponse.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "LANGUAGE ERROR");
                    }
                }
            }

        });
        map = new HashMap<String, String>();
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceRecognition();

            }
        });
        message = (TextView) findViewById(R.id.message);
        response = (TextView) findViewById(R.id.response);
    }

    private void voiceRecognition() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Recording Speech...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        }
        catch(ActivityNotFoundException e) {
            Toast.makeText(this,"ERROR", Toast.LENGTH_LONG);
        }

    }
    private void voiceRecognition(String message) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Recording Speech...");
        intent.putExtra("message", message);

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        }
        catch(ActivityNotFoundException e) {
            Toast.makeText(this,"ERROR", Toast.LENGTH_LONG);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_OUTPUT: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> voiceInText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    command = voiceInText.get(0);
                    message.setText(command);
                    spliced = command.split(" ");
                    if (Arrays.asList(functions).contains(spliced[0])){
                        doCommand(command);
                    }
                    if (!data.getStringExtra("message").equals("")) {
                        speakandresponse("message");
                    }

                }
                break;
            }
        }
    }

    private void doCommand(String command1) {
        if (spliced[0].equals("hello")) {
            speak("hello");
        }
        if (command1.equals("how are you doing")) {
            speak("Good! How about you?");
        }
        if (spliced[0].equals("call")) {
            call(spliced);
        }
        if (spliced[0].equals("what")) {

        }
        if (command1.equals("create new contact")|| command1.equals("add new contact")) {
            createNewContact();
        }

    }
    private void speak (String phrase) {
        response.setText(phrase);
        textToSpeech.speak(phrase,TextToSpeech.QUEUE_ADD,null);
    }
    private void speakandresponse(String phrase) {

        response.setText(phrase);
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
        ttsResponse.speak(phrase, TextToSpeech.QUEUE_ADD,map);
        ttsResponse.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                voiceRecognition();


            }

            @Override
            public void onError(String s) {

            }
        });
    }
    private void call(String[] spliced) {
        String phoneNumber = "";
        if (spliced.length==1) {
            speak("Who do you want to call?");
            voiceRecognition();
            return;
        }
        if (Character.isDigit(spliced[1].charAt(0))) {
            //user has said phone number
            phoneNumber = spliced[1].replaceAll("[\\s\\-()]", "");
        }
        else {
            //user has given a contact name
            ContentResolver cr = getContentResolver();
            String contactName = "";
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                return;

            }
            for (int i = 1; i<spliced.length; i++) {
                contactName += spliced[i].substring(0, 1).toUpperCase() + spliced[i].substring(1);
                if (i < spliced.length-1) {
                    contactName += " ";
                }
            }

            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                    "DISPLAY_NAME = '" + contactName + "'", null, null);
            if (cursor.moveToFirst()) {
                String contactId =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //
                //  Get all phone numbers.
                //
                Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (phones.moveToNext()) {
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = number;
                    int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    switch (type) {
                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                            // do something with the Home number here...
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                            // do something with the Mobile number here...
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                            // do something with the Work number here...
                            break;
                    }
                }
                phones.close();
            }
            cursor.close();

        }
        if (phoneNumber.equals("")) {
            speak("Contact not found. Would you like to create a new one?");
        } else {
            Uri number = Uri.parse("tel:" + phoneNumber);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            startActivity(callIntent);
        }
    }
    private void createNewContact() {
        String contactName;
        String contactNumber = null;
        String email = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS},
                    MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);
            return;

        }
        speakandresponse("What is your new contact's name?");
        Log.d("SDF","DSF");
        if (spliced[0].equals("cancel")) {
            speak("Cancelled");
            return;
        }
        else {
            contactName = command;
            speakandresponse("Any other details?");

            if (command.contains("number") || command.contains("phone")) {
                speakandresponse("What is "+contactName+"'s number?");
                contactNumber = spliced[0];
            } else if (command.contains("email")) {
                speakandresponse("What is "+contactName+"'s email?");
                email = spliced[0];
            }
        }
        ArrayList <ContentProviderOperation> ops = new ArrayList < ContentProviderOperation > ();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        if (contactName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            contactName).build());
        }
        if (contactNumber != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }
        if (email != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted


                } else {

                    // permission denied
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                }
                return;
            }
        }
    }
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

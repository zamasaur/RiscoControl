package com.zamasaur.riscocontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etNumber;
    private SharedPreferences prefs;

    private final ActivityResultLauncher<Intent> contactPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    String[] projection = {
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                    };
                    try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int colNormalized = cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                            int colNumber = cursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String normalized = cursor.getString(colNormalized);
                            String number = cursor.getString(colNumber);
                            etNumber.setText(normalized != null ? normalized : number);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("risco_prefs", MODE_PRIVATE);
        etNumber = findViewById(R.id.et_central_number);
        Button btnPickContact = findViewById(R.id.btn_pick_contact);
        Button btnSave = findViewById(R.id.btn_save);

        String saved = prefs.getString("central_number", "");
        etNumber.setText(saved);

        btnPickContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactPicker.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String number = etNumber.getText().toString().trim();
            if (number.isEmpty()) {
                Toast.makeText(this, "Inserisci un numero valido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!number.startsWith("+")) {
                new AlertDialog.Builder(this)
                        .setTitle("Attenzione")
                        .setMessage("Il numero non include il prefisso internazionale (es. +39). Vuoi salvarlo comunque?")
                        .setPositiveButton("Salva", (dialog, which) -> {
                            prefs.edit().putString("central_number", number).apply();
                            Toast.makeText(this, "Numero salvato", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        })
                        .setNegativeButton("Modifica", null)
                        .show();
                return;
            }
            prefs.edit().putString("central_number", number).apply();
            Toast.makeText(this, "Numero salvato", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}
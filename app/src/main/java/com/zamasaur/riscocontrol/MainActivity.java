package com.zamasaur.riscocontrol;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zamasaur.riscocontrol.db.LogDatabase;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private LogAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Gestione insets toolbar (status bar)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Gestione insets per navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_bar), (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomInset + 12);
            return insets;
        });

        prefs = getSharedPreferences("risco_prefs", MODE_PRIVATE);

        // Primo avvio: forza impostazioni
        if (prefs.getString("central_number", null) == null) {
            settingsLauncher.launch(new Intent(this, SettingsActivity.class));
        }

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_log);
        adapter = new LogAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Osserva il database
        LogDatabase.getInstance(this).logDao().getAllEntries().observe(this, entries ->
                adapter.setEntries(entries));

        // SwipeRefresh
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> showPinDialog("STATO"));

        // Tasti
        findViewById(R.id.btn_inserisci).setOnClickListener(v -> showPinDialog("INS"));
        findViewById(R.id.btn_disinserisci).setOnClickListener(v -> showPinDialog("DIS"));

        // Permessi
        requestPermissions();
    }

    private void showPinDialog(String command) {
        swipeRefresh.setRefreshing(false);

        EditText etPin = new EditText(this);
        etPin.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etPin.setHint("Inserisci PIN");

        new AlertDialog.Builder(this)
                .setTitle("PIN")
                .setView(etPin)
                .setPositiveButton("Invia", (dialog, which) -> {
                    String pin = etPin.getText().toString().trim();
                    if (pin.isEmpty()) {
                        Toast.makeText(this, "PIN non inserito", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendSmsCommand(pin + command + "RP");
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void sendSmsCommand(String message) {
        String number = prefs.getString("central_number", null);
        if (number == null) {
            Toast.makeText(this, "Numero centralina non configurato", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permesso SMS non concesso", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            Toast.makeText(this, "Comando inviato", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Errore invio SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS
        };
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Aggiorna");
        menu.add(0, 2, 1, "Impostazioni");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showPinDialog("STATO");
            return true;
        } else if (item.getItemId() == 2) {
            settingsLauncher.launch(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
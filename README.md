# RiscoControl

App Android per il controllo remoto della centrale d'allarme **Risco LightSYS 2** tramite SMS, senza abbonamenti né servizi cloud.

## Motivazione

La centrale Risco LightSYS 2 è distribuita con un'app ufficiale (iRisco) che comunica via cloud. Nel tempo questo servizio è diventato a pagamento. Poiché la centrale supporta nativamente i comandi via SMS, questa app sfrutta quel canale per offrire un controllo completo e indipendente da qualsiasi abbonamento.

## Funzionalità

- **Inserimento totale** dell'allarme
- **Disinserimento** dell'allarme
- **Richiesta stato** del sistema
- **Log persistente** di tutti gli SMS ricevuti dalla centrale, con data e ora
- **Pull-to-refresh** per aggiornare lo stato
- **Notifiche automatiche** — il BroadcastReceiver intercetta gli SMS spontanei della centrale (allarme scattato, tamper, ecc.) e li aggiunge al log in tempo reale anche con app in background
- **Impostazioni** — numero centralina configurabile manualmente o tramite selezione dai contatti del telefono

## Architettura

- **Linguaggio:** Java
- **Database locale:** Room (SQLite) per la persistenza del log
- **Ricezione SMS:** BroadcastReceiver filtrato sul numero della centrale
- **Invio comandi:** SmsManager con conferma RP
- **UI:** RecyclerView scrollabile + SwipeRefreshLayout

## Comandi SMS utilizzati

| Azione | Comando inviato |
|---|---|
| Inserimento totale | `[PIN]INSRP` |
| Disinserimento | `[PIN]DISRP` |
| Stato sistema | `[PIN]STATORP` |

I comandi seguono la sintassi ufficiale del manuale italiano del LightSYS 2. Il suffisso `RP` richiede alla centrale una conferma via SMS dell'operazione eseguita.

## Sicurezza

- Il PIN non viene mai salvato dall'app — viene richiesto tramite dialog mascherato ad ogni operazione
- Il numero della centrale è l'unico dato persistente (SharedPreferences)
- La centrale accetta comandi SMS solo dai numeri di telefono autorizzati in fase di configurazione
- **Nota:** gli SMS inviati restano visibili nell'app SMS nativa di Android — limitazione di sistema non aggirabile senza essere l'app SMS predefinita

## Permessi richiesti

- `SEND_SMS` — invio comandi alla centrale
- `RECEIVE_SMS` — ricezione risposte e notifiche
- `READ_SMS` — lettura SMS per il filtraggio
- `READ_CONTACTS` — selezione numero centralina dai contatti

## Compatibilità

- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 16 (API 36)
- Testato su Risco LightSYS 2 con firmware italiano

## Build

Il progetto è sviluppato con Android Studio. Per compilare:

1. Clona il repository
2. Apri il progetto in Android Studio
3. Premi **Run** o **Ctrl+F9** per compilare
4. Per generare un APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

## Package

```
com.zamasaur.riscocontrol
├── MainActivity.java
├── SettingsActivity.java
├── LogAdapter.java
├── receiver/
│   └── SmsReceiver.java
└── db/
    ├── LogDatabase.java
    ├── LogDao.java
    └── LogEntry.java
```

## Licenza

Progetto personale per uso privato. Non affiliato con RISCO Group.

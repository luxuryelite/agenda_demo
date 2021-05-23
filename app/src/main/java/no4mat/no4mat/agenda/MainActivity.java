package no4mat.no4mat.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no4mat.no4mat.agenda.api.ApiInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String[] CATEGORY_DATA = new String[]{
            "Actividad Física",
            "Trabajo",
            "Compras",
            "Recreativo",
            "Otros"
    };

    ArrayList<AgendaEntry> listAgenda = new ArrayList<>();
    SQLiteDatabase db;

    Spinner category;
    EditText etDate, etTime, etName, etLastName, etPhone;
    ImageButton ibDate, ibTime;
    Button saveButton;
    Button cancelButton;
    LinearLayout listLayout;
    LinearLayout inputLayout;
    ListView list;

    AgendaEntry agendaEntry;

    boolean status_menu;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status_menu = true;

        LocalDatabase lcdb = new LocalDatabase(this);
        db = lcdb.getWritableDatabase();

        etDate = (EditText) findViewById(R.id.editTextDate);
        etTime = (EditText) findViewById(R.id.editTextTime);
        etName = (EditText) findViewById(R.id.editTextName);
        etName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etLastName = (EditText) findViewById(R.id.editTextLastName);
        etLastName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etPhone = (EditText) findViewById(R.id.editTextPhone);
        ibDate = (ImageButton) findViewById(R.id.imageButtonDate);
        ibTime = (ImageButton) findViewById(R.id.imageButtonTime);
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        listLayout = (LinearLayout) findViewById(R.id.listLayout);
        inputLayout = (LinearLayout) findViewById(R.id.inputLayout);
        list = (ListView) findViewById(R.id.list);

        ibDate.setOnClickListener(this);
        ibTime.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        agendaEntry = new AgendaEntry();
        updateEditTextDateTime();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, CATEGORY_DATA);
        category = (Spinner) findViewById(R.id.sp_category);
        category.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                showDeleteMessage(listAgenda.get(position));
            }
        });

        //temp();
        readDatabase();
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(0).setVisible(status_menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemAddElement:
                changeStatus();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Método temporal para generar datos */
    private void temp () {
        for (int i = 0; i < 20; i++) {
            AgendaEntry entry = new AgendaEntry();
            entry.name = "Nombre " + i;
            listAgenda.add(entry);
        }
    }

    private void updateList () {
        String[] names = new String[listAgenda.size()];
        String[] lastNames = new String[listAgenda.size()];
        String[] dates = new String[listAgenda.size()];
        String[] times = new String[listAgenda.size()];

        for (int i = 0; i < listAgenda.size(); i++ ){
            names[i] = listAgenda.get(i).name;
            lastNames[i] = listAgenda.get(i).lastName;
            dates[i] = listAgenda.get(i).getDateFormat();
            times[i] = listAgenda.get(i).getTimeFormat();
        }

        ListViewAdapter listAdapter = new ListViewAdapter(this, names, lastNames, dates, times);
        list.setAdapter(listAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButtonDate:
                DatePickerFragment dateFragment = new DatePickerFragment();
                dateFragment.listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        agendaEntry.year = year;
                        agendaEntry.month = month + 1;
                        agendaEntry.day = dayOfMonth;
                        updateEditTextDateTime();
                    }
                };
                dateFragment.show(getSupportFragmentManager(), "Fecha");
                break;
            case R.id.imageButtonTime:
                TimePickerFragment timeFragment = new TimePickerFragment();
                timeFragment.listener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        agendaEntry.hour = hourOfDay;
                        agendaEntry.minute = minute;
                        updateEditTextDateTime();
                    }
                };
                timeFragment.show(getSupportFragmentManager(), "Hora");
                break;
            case R.id.saveButton:
                agendaEntry.name = etName.getText().toString();
                agendaEntry.lastName = etLastName.getText().toString();
                agendaEntry.phoneNumber = etPhone.getText().toString();

                listAgenda.add(agendaEntry);
                insertDatabase(agendaEntry);
                agendaEntry = new AgendaEntry();
                updateList();
                Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
                changeStatus();
                clearAllEditText();
                break;
            case R.id.cancelButton:
                changeStatus();
                agendaEntry = new AgendaEntry();
                clearAllEditText();
                break;
        }
    }

    private void changeStatus () {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation b = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        if (status_menu) {
            listLayout.startAnimation(b);
            inputLayout.startAnimation(a);
            inputLayout.setVisibility(View.VISIBLE);
            listLayout.setVisibility(View.GONE);
            status_menu = false;
        } else {
            inputLayout.startAnimation(b);
            listLayout.startAnimation(a);
            inputLayout.setVisibility(View.GONE);
            listLayout.setVisibility(View.VISIBLE);
            status_menu = true;
        }
        this.invalidateOptionsMenu();
        readDatabase();
    }

    private void updateEditTextDateTime () {
        etDate.setText(agendaEntry.getDateFormat());
        etTime.setText(agendaEntry.getTimeFormat());
    }

    private void clearAllEditText () {
        etName.setText(null);
        etLastName.setText(null);
        etPhone.setText(null);
    }

    /* Métodos para almacenar en la base de datos*/

    private void insertDatabase (AgendaEntry agendaEntry) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<AgendaEntry> call = apiInterface.addEntry(agendaEntry);
        call.enqueue(new Callback<AgendaEntry>() {
            @Override
            public void onResponse(Call<AgendaEntry> call, Response<AgendaEntry> response) {
                Toast.makeText(getApplicationContext(),"Guardado", Toast.LENGTH_SHORT).show();
                updateList();
            }

            @Override
            public void onFailure(Call<AgendaEntry> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Posible error en la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void insertDatabase (AgendaEntry agendaEntry) {
        ContentValues cv = new ContentValues();
        cv.put("name", agendaEntry.name);
        cv.put("lastName", agendaEntry.lastName);
        cv.put("phone", agendaEntry.phoneNumber);
        cv.put("date", agendaEntry.getDateFormat());
        cv.put("time", agendaEntry.getTimeFormat());
        cv.put("category", "");

        db.insert("list_agenda", null, cv);
    } */

    private void readDatabase () {
        listAgenda = new ArrayList<AgendaEntry>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<List<AgendaEntry>> call = apiInterface.getEntries();
        call.enqueue(new Callback<List<AgendaEntry>>() {
            @Override
            public void onResponse(Call<List<AgendaEntry>> call, Response<List<AgendaEntry>> response) {
                if(!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Ocurrio un error", Toast.LENGTH_SHORT).show();
                } else {
                    List<AgendaEntry> list = response.body();
                    for (AgendaEntry entry: list) {
                        listAgenda.add(entry);
                    }
                    updateList();
                }
            }

            @Override
            public void onFailure(Call<List<AgendaEntry>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Problema de conexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    private void readDatabase () {
        if (db!=null) {
            listAgenda = new ArrayList<AgendaEntry>();
            Cursor cursor = db.rawQuery("SELECT id, name, lastName, phone, category, date, time FROM list_agenda", null);
            AgendaEntry tempEntry = new AgendaEntry();
            if (cursor.moveToFirst()) {
                do {
                    tempEntry.id = cursor.getInt(0);
                    tempEntry.name = cursor.getString(1);
                    tempEntry.lastName = cursor.getString(2);
                    tempEntry.phoneNumber = cursor.getString(3);
                    tempEntry.category = cursor.getString(4);
                    tempEntry.setDate(cursor.getString(5));
                    tempEntry.setTime(cursor.getString(6));
                    listAgenda.add(tempEntry);
                    tempEntry = new AgendaEntry();
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    } */

    /* Metodo para mostrar dialogo de eliminar */
    private void showDeleteMessage (AgendaEntry agendaEntry) {
        /*
        DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Toast.makeText(getApplicationContext(), "ELIMINANDO", Toast.LENGTH_SHORT).show();
                db.delete("list_agenda", "id = " + id, null);
                readDatabase();
                updateList();
            }
        };*/

        DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiInterface.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiInterface apiInterface = retrofit.create(ApiInterface.class);
                Call<AgendaEntry> call = apiInterface.deleteEntry(agendaEntry.id);
                call.enqueue(new Callback<AgendaEntry>() {
                    @Override
                    public void onResponse(Call<AgendaEntry> call, Response<AgendaEntry> response) {
                        Toast.makeText(getApplicationContext(), "Eliminado", Toast.LENGTH_SHORT).show();
                        readDatabase();
                        updateList();
                    }

                    @Override
                    public void onFailure(Call<AgendaEntry> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error en la conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            }
        };

        AlertDialogDelete adl = new AlertDialogDelete(positive, negative);
        adl.show(getSupportFragmentManager(), "¿Eliminar?");

    }
}
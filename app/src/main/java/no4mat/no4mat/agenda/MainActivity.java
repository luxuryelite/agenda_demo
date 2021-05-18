package no4mat.no4mat.agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String[] CATEGORY_DATA = new String[]{
            "Actividad Física",
            "Trabajo",
            "Compras",
            "Recreativo",
            "Otros"
    };

    ArrayList<AgendaEntry> listAgenda = new ArrayList<>();

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
                String message = position + " " + id;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        //temp();
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
            entry.id = i;
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
}
package vigi.patient.view.patient.treatment.booking;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import vigi.patient.R;
import vigi.patient.model.entities.Agenda;
import vigi.patient.model.entities.CareProvider;
import vigi.patient.model.services.Appointment;
import vigi.patient.model.services.Treatment;
import vigi.patient.presenter.service.agenda.agenda.api.AgendaService;
import vigi.patient.presenter.service.agenda.agenda.impl.AgendaConverter;
import vigi.patient.presenter.service.agenda.agenda.impl.FirebaseAgendaService;
import vigi.patient.presenter.service.appointment.api.AppointmentService;
import vigi.patient.presenter.service.appointment.impl.AppointmentConverter;
import vigi.patient.presenter.service.appointment.impl.FirebaseAppointmentService;
import vigi.patient.presenter.service.careProvider.api.CareProviderService;
import vigi.patient.presenter.service.careProvider.firebase.CareProviderConverter;
import vigi.patient.presenter.service.careProvider.firebase.FirebaseCareProviderService;
import vigi.patient.presenter.service.treatment.impl.firebase.TreatmentConverter;
import vigi.patient.view.patient.cart.CartActivity;
import vigi.patient.view.patient.home.HomePatientActivity;
import vigi.patient.view.patient.search.SearchActivity;
import vigi.patient.view.patient.treatment.booking.viewHolder.BookingViewAdapter;
import vigi.patient.view.patient.treatment.viewHolder.TreatmentsViewAdapter;
import vigi.patient.view.utils.drawable.CountDrawable;
import vigi.patient.view.utils.recyclerView.ItemOffsetDecoration;
import vigi.patient.view.vigi.activity.VigiActivity;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("FieldCanBeLocal")
public class BookingActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, AdapterView.OnItemSelectedListener, VigiActivity {

    private String TAG = getClass().getName();
    private final static String CHOSEN_TREATMENT_NAME = "chosenTreatmentName";
    private final static String CHOSEN_TREATMENT_ID = "chosenTreatmentId";
    private final static String ADMITTED_CLINICIANS = "admittedClinicians";
    private final static String CHOSEN_CAREPROVIDER = "chosenCareProvider";

    private Toolbar toolbar;
    private ArrayList<CareProvider> careProvidersList;
    private ArrayList<Agenda> agendaList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Spinner spinnerOrder;
    private SimpleDateFormat sdfFullDate, sdfMinutes, sdfDate;
    private ImageView decreaseTime, increaseTime;
    private EditText hours_time, minutes_time;
    private Date minTimeDate;
    private Calendar cal;
    private CareProviderService careProviderService;
    private AgendaService agendaService;
    private List<String> careProviderIds;
    private ValueEventListener appointmentListener, careProviderListener, agendaListener;
    private AppointmentService appointmentService;
    private List<Agenda> agendaInstances;
    private List<CareProvider> careProvidersWithFilter, careProvidersWithTreatment;
    private BookingViewAdapter bookingAdapter;
    private String currentPatientId, admittedJobs, time, treatmentName, treatmentId, minTime, selectedDate, selectedFullDate, filter;
    MenuInflater menuInflater;
    Menu menuToUpdate;
    private Boolean menuPrepared;
    private ArrayList<Appointment> cartAppointmentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch care providers that can do the selected treatment for that day,
        // and display information according to that
        Intent intent = getIntent();
        treatmentName = Objects.requireNonNull(intent.getExtras()).getString(CHOSEN_TREATMENT_NAME);
        treatmentId = Objects.requireNonNull(intent.getExtras()).getString(CHOSEN_TREATMENT_ID);
        admittedJobs = Objects.requireNonNull(intent.getExtras()).getString(ADMITTED_CLINICIANS);

        currentPatientId = "1"; //TODO change to tokenid
        menuPrepared = false;
        cartAppointmentsList = new ArrayList<>();
        careProvidersList = new ArrayList<>();
        agendaList = new ArrayList<>();
        careProvidersWithFilter = new ArrayList<>();
        careProvidersWithTreatment = new ArrayList<>();
        agendaInstances = new ArrayList<>();
        sdfFullDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdfMinutes = new SimpleDateFormat("mm");
        sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        filter = "Rating";
        time = "10:00";

        setContentView(R.layout.treatment_booking);
        setupUiComponents();
        setUpServices();
        setupClickListeners();
    }

    private void setUpSpinner(){

        List<String> categories = new ArrayList<>();
        categories.add("Rating");
        categories.add("Price");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.adapter_treatment_booking_spinner,categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrder.setAdapter(adapter);
        spinnerOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filter = adapterView.getItemAtPosition(i).toString();
                careProviderService.readCareProviders(careProviderListener);
                agendaService.readAgendas(agendaListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setUpCalendar(){
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH,2);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH,2);

        selectedDate = sdfDate.format(startDate.getTime());
        selectedFullDate = selectedDate + " " + time;

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this,R.id.calendar)
                .range(startDate,endDate)
                .datesNumberOnScreen(7)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                selectedDate = sdfDate.format(date.getTime());
                selectedFullDate = selectedDate + " " + time;
                careProviderService.readCareProviders(careProviderListener);

            }
        });
    }

    private void setUpServices(){
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.recycler_view_vertical_margin);
        recyclerView.addItemDecoration(itemDecoration);

        appointmentListener = new BookingActivity.AppointmentValueEventListener();
        appointmentService = new FirebaseAppointmentService();
        appointmentService.init();

        careProviderListener = new BookingActivity.CareProviderValueEventListener();
        careProviderService = new FirebaseCareProviderService();
        careProviderService.init();

        agendaListener = new BookingActivity.AgendaValueEventListener();
        agendaService = new FirebaseAgendaService();
        agendaService.init();

        appointmentService.readAppointments(appointmentListener, currentPatientId);

    }

    @Override
    public void setupUiComponents() {
        recyclerView = findViewById(R.id.recycler_view);
        spinnerOrder = findViewById(R.id.order);
        decreaseTime = findViewById(R.id.decrease_time);
        increaseTime = findViewById(R.id.increase_time);
        hours_time = findViewById(R.id.hours_time);
        minutes_time = findViewById(R.id.minutes_time);

        customizeActionBar();
        customizeToolBar();
        setUpCalendar();
        setUpSpinner();
    }

    @Override
    public void setupClickListeners() {
        decreaseTime.setOnClickListener(view -> {
            minTime = minutes_time.getText().toString();
            try {
                minTimeDate = sdfMinutes.parse(minTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal = Calendar.getInstance();
            cal.setTime(minTimeDate);
            cal.add(Calendar.MINUTE, -1);
            minutes_time.setText(sdfMinutes.format(cal.getTime()));
        });

        increaseTime.setOnClickListener(view -> {
            minTime = minutes_time.getText().toString();
            try {
                minTimeDate = sdfMinutes.parse(minTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal = Calendar.getInstance();
            cal.setTime(minTimeDate);
            cal.add(Calendar.MINUTE, 1);
            minutes_time.setText(sdfMinutes.format(cal.getTime()));
        });

        hours_time.setOnClickListener(view -> hours_time.getText().clear());

        minutes_time.setOnClickListener(view -> {
            minutes_time.getText().clear();
        });

        hours_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                time = editable.toString() + ":" + minutes_time.getText().toString();
                selectedFullDate = selectedDate + " " + time;
                if(editable.length()==2) {
                    careProviderService.readCareProviders(careProviderListener);
                }
            }
        });

        minutes_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                time = hours_time.getText().toString() + ":" + editable.toString();
                selectedFullDate = selectedDate + " " + time;

                if(editable.length()==2) {
                    careProviderService.readCareProviders(careProviderListener);
                }
            }
        });

    }

    private void customizeActionBar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void customizeToolBar() {
        checkNotNull(getSupportActionBar());
        getSupportActionBar().setTitle(treatmentName);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void notifyCareProviderDataChanged(List<CareProvider> careProvidersList) {
        careProviderService.setAllCareProviders(careProvidersList);
        careProvidersWithTreatment = careProviderService.readCareProviderWithTreatment(admittedJobs);
        careProviderService.setAllCareProviders(careProvidersWithTreatment);
    }

    private void notifyAgendaDataChanged(List<Agenda> agendaList){
        agendaService.setAllAgendas(agendaList);

        try {
            sdfFullDate.parse(selectedFullDate);

            agendaInstances = agendaService.readAgendaWithDate(selectedFullDate);
            agendaService.setAllAgendas(agendaInstances);

            agendaInstances = agendaService.readAgendaWithFilter(filter, careProvidersWithTreatment);
            bookingAdapter = new BookingViewAdapter(this,careProvidersWithTreatment, agendaInstances, currentPatientId, treatmentId , selectedFullDate);
            recyclerView.setAdapter(bookingAdapter);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this,"Confirm time format HH:mm", Toast.LENGTH_LONG).show();
        }

    }

    public class AppointmentValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            cartAppointmentsList = new ArrayList<>();

            for (DataSnapshot snapshotAppointment : dataSnapshot.getChildren()) {
                if (snapshotAppointment.child("status").getValue().equals("cart")){
                    cartAppointmentsList.add(AppointmentConverter.getAppointmentFromDataSnapshot(snapshotAppointment));
                }
            }

            //set count after new purchase is made
            if (menuPrepared) {
                setCount(BookingActivity.this, String.valueOf(cartAppointmentsList.size()), menuToUpdate); // In case there was one request in the cart
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }


    public class CareProviderValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            careProvidersList = new ArrayList<>();

            for (DataSnapshot snapshotCareProvider : dataSnapshot.getChildren()) {
                careProvidersList.add(CareProviderConverter.getCareProviderFromDataSnapshot(snapshotCareProvider));
            }
            notifyCareProviderDataChanged(careProvidersList);
            agendaService.readAgendas(agendaListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            notifyCareProviderDataChanged(new ArrayList<>());
        }
    }

    public class AgendaValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            agendaList = new ArrayList<>();
            careProviderIds = new ArrayList<>();
            careProviderIds = careProvidersWithTreatment.stream().map(careProvider -> careProvider.getId()).collect(toList());

            for (DataSnapshot snapshotAgendaInstance : dataSnapshot.getChildren()) {
                if(careProviderIds.contains(snapshotAgendaInstance.child("careProviderId").getValue().toString())){
                    agendaList.add(AgendaConverter.getAgendaFromDataSnapshot(snapshotAgendaInstance));
                }
            }
            notifyAgendaDataChanged(agendaList);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            notifyAgendaDataChanged(new ArrayList<>());
        }
    }

    public void setCount(Context context, String count, Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.cart);
        LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        CountDrawable badge;

        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_group_count);
        if (reuse instanceof CountDrawable) {
            badge = (CountDrawable) reuse;
        }else{
            badge = new CountDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_group_count, badge);
    }


    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        setCount(BookingActivity.this, String.valueOf(cartAppointmentsList.size()), menu); // In case there was one request in the cart
        menuToUpdate = menu;
        menuPrepared = true;

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            case R.id.search:
                Intent careProviderSearchIntent = new Intent(this, SearchActivity.class);
                startActivity(careProviderSearchIntent);
                return true;
            case R.id.cart:
                Intent checkCartIntent = new Intent(this, CartActivity.class);
                startActivity(checkCartIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
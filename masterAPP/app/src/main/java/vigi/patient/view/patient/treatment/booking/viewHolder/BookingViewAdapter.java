package vigi.patient.view.patient.treatment.booking.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import vigi.patient.R;
import vigi.patient.model.entities.Agenda;
import vigi.patient.model.entities.CareProvider;
import vigi.patient.model.services.Appointment;
import vigi.patient.presenter.service.appointment.api.AppointmentService;
import vigi.patient.presenter.service.appointment.impl.FirebaseAppointmentService;
import vigi.patient.view.patient.careProvider.ProfileActivity;
import vigi.patient.view.patient.cart.CartActivity;
import vigi.patient.view.patient.search.SearchActivity;
import vigi.patient.view.patient.treatment.booking.BookingActivity;

import static java.util.stream.Collectors.toList;


public class BookingViewAdapter extends RecyclerView.Adapter<BookingViewAdapter.ViewHolder>{

    private String TAG = getClass().getName();
    private List<CareProvider> careProviders, careProvidersDisplayed;
    private List<Agenda> agendaInstances;
    private Context context;
    private CareProvider careProvider, careProviderToBeSaved;
    private Agenda agendaToBeSaved;
    private Appointment appointmentToBeSaved;
    private final static String CHOSEN_CAREPROVIDER = "chosenCareProvider";
    private final static String CHOSEN_AGENDAINSTANCE = "chosenAgendaInstance";
    private AppointmentService appointmentService;
    private String currentPatientId;
    private String treatmentId, date;


    public BookingViewAdapter(Context context, List<CareProvider> careProvidersList, List<Agenda> agendaInstances, String currentPatientId, String treatmentId, String date) {
        this.context = context;
        this.careProviders = careProvidersList;
        this.agendaInstances = agendaInstances;
        this.careProvidersDisplayed = new ArrayList<>();
        this.currentPatientId = currentPatientId;
        this.treatmentId = treatmentId;
        this.date = date;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_treatment_booking,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        careProvider = careProviders.stream()
                .filter(careProvider -> careProvider.getId().equals(agendaInstances.get(i).getCareProviderId())).findFirst().orElse(null);
        careProvidersDisplayed.add(careProvider);

        viewHolder.name.setText(careProvider.getName());
        viewHolder.field.setText(careProvider.getJob());
        //TODO change duration with beginning hour from agenda class
        viewHolder.duration.setText(careProvider.getExpectedtime()+"min");
        viewHolder.rating.setText(String.valueOf(careProvider.getRating()));
        viewHolder.price.setText(careProvider.getPrice()+"€");

        //viewHolder.image.setImageDrawable(careProviders.get(i).getImage());
        viewHolder.addToCart.setOnClickListener(view -> {
            // TODO add request to database, so we can access request from CartActivity
            appointmentService = new FirebaseAppointmentService();
            appointmentService.init();

            careProviderToBeSaved = careProvidersDisplayed.get(i);
            agendaToBeSaved = agendaInstances.get(i);

            appointmentToBeSaved = new Appointment();
            appointmentToBeSaved.setCareProviderId(careProviderToBeSaved.getId());
            appointmentToBeSaved.setPrice(careProviderToBeSaved.getPrice());
            appointmentToBeSaved.setStatus("cart");
            appointmentToBeSaved.setDate(date);
            appointmentToBeSaved.setPatientId(currentPatientId);
            appointmentToBeSaved.setTreatmentId(treatmentId);
            appointmentToBeSaved.setMinutesOfDuration(careProviderToBeSaved.getExpectedtime());
            appointmentToBeSaved.setPaymentCode( UUID.randomUUID().toString());
            appointmentService.setFirebaseAppointments(context,appointmentToBeSaved);

        });
        viewHolder.profileCareProvider.setOnClickListener(view -> {
            Intent careProviderIntent = new Intent(context, ProfileActivity.class);
            careProviderIntent.putExtra(CHOSEN_CAREPROVIDER, (Serializable) careProvidersDisplayed.get(i));
            context.startActivity(careProviderIntent);
        });
    }

    @Override
    public int getItemCount() {
        return agendaInstances.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView field;
        TextView duration;
        TextView rating;
        TextView price;
        CircleImageView image;
        CardView profileCareProvider;
        FloatingActionButton addToCart;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            field = itemView.findViewById(R.id.field);
            duration = itemView.findViewById(R.id.duration);
            rating = itemView.findViewById(R.id.rating);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.image);
            profileCareProvider = itemView.findViewById(R.id.profile);
            addToCart = itemView.findViewById(R.id.add_to_cart_btn);

        }

    }

}

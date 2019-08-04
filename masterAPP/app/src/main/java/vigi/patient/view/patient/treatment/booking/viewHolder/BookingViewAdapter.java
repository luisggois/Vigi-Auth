package vigi.patient.view.patient.treatment.booking.viewHolder;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vigi.patient.R;
import vigi.patient.model.entities.Agenda;
import vigi.patient.model.entities.CareProvider;

import static java.util.stream.Collectors.toList;


public class BookingViewAdapter extends RecyclerView.Adapter<BookingViewAdapter.ViewHolder>{

    private String TAG = getClass().getName();
    private List<CareProvider> careProviders;
    private List<Agenda> agendaInstances;
    private Context context;
    private CareProvider careProvider;

    public BookingViewAdapter(Context context, List<CareProvider> careProvidersList, List<Agenda> agendaInstances) {
        this.context = context;
        this.careProviders = careProvidersList;
        this.agendaInstances = agendaInstances;
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
        Log.d("NAMASTE careProvider name", careProvider.getName());

        viewHolder.name.setText(careProvider.getName());
        viewHolder.field.setText(careProvider.getJob());
        //TODO change duration with beginning hour from agenda class
        viewHolder.duration.setText(careProvider.getExpectedtime()+"min");
        viewHolder.rating.setText(careProvider.getRating());
        viewHolder.price.setText(careProvider.getPrice()+"€");

        //viewHolder.image.setImageDrawable(careProviders.get(i).getImage());
        viewHolder.addToCart.setOnClickListener(view -> {
            Toast.makeText(context,"Request has been added to the cart!", Toast.LENGTH_LONG).show();
            // TODO add request to database, so we can access request from CartActivity
        });
        viewHolder.profileCareProvider.setOnClickListener(view -> {
            // TODO open care provider BookingActivity
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
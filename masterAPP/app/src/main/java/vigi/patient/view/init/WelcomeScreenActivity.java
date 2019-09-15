package vigi.patient.view.init;

import android.app.ActivityOptions;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import vigi.patient.R;
import vigi.patient.view.authentication.home.HomeAuthActivity;
import vigi.patient.view.patient.careProvider.ProfileActivity;
import vigi.patient.view.patient.home.HomePatientActivity;

import static vigi.patient.view.utils.activity.ActivityUtils.jumpToActivity;

@SuppressWarnings("FieldCanBeLocal")
public class  WelcomeScreenActivity extends AppCompatActivity {

    private String TAG = getClass().getName();
    private LinearLayout welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.authentication_welcome);

        welcomeText = findViewById(R.id.welcome_to_vigi);

        Handler handler = new Handler();
        handler.postDelayed(this::launchHomeAuthActivity, 3000);
    }

    private void launchHomeAuthActivity() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(WelcomeScreenActivity.this,
                welcomeText,"welcomeToVigi");
        jumpToActivity(this, HomeAuthActivity.class, false, options.toBundle());
    }

}


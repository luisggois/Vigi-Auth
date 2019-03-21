package vigi.patient.view.utils.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import vigi.patient.R;
import static com.google.common.base.Preconditions.checkNotNull;

public class ErrorDialog {

    public void showDialog(final Activity activity, String msg){

            final Dialog dialog = new Dialog(activity);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.utils_error_dialog);
            checkNotNull(dialog.getWindow());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            // Create objects

            TextView msgError = dialog.findViewById(R.id.message);

            // Set text

            msgError.setText(msg);

            // Set on click listener on try again button

            Button dialogButton = dialog.findViewById(R.id.ok_btn);
            dialogButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();

        }
    }


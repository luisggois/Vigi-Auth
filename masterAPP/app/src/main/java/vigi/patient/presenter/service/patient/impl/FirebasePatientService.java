package vigi.patient.presenter.service.patient.impl;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import vigi.patient.model.entities.Patient;
import vigi.patient.model.firebase.FirebaseConstants;
import vigi.patient.model.services.Appointment;
import vigi.patient.presenter.service.patient.api.PatientService;


public class FirebasePatientService implements PatientService {

    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    DatabaseReference databaseReference, databaseReferencePatient;
    StorageReference storageReference;
    StorageReference imageStorageReference;

    DatabaseReference currentReference;
    private Query databaseQueryPatientAppointment;

    Patient patient;

    @Override
    public void init() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = firebaseDatabase.getReference(Patient.class.getSimpleName());
        databaseReferencePatient = firebaseDatabase.getReference(Patient.class.getSimpleName());

        storageReference = firebaseStorage.getReference();
        patient = new Patient();
    }

    @Override
    public void readPatients(ValueEventListener listener) {

        databaseReference.addValueEventListener(listener);

    }

    @Override
    public boolean createPatient(Patient patient) {
        this.patient = patient;
        try {
            currentReference = databaseReference.push();

            uploadPatientImage(patient);

            savePatientInDatabase(patient);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void uploadPatientImage(Patient patient) throws InterruptedException {
        storageReference.child(FirebaseConstants.IMAGE_PATH);

        //save image in firestore
        imageStorageReference = storageReference.child(currentReference.getKey() + FirebaseConstants.JPG_EXTENSION);
        UploadTask uploadTask = imageStorageReference.putFile(Uri.parse(patient.getImage()));

        uploadTask.addOnCompleteListener(new OnImageUploadCompleteListener()).wait();

        //Wait for it to end
        //completableFuture.get();
    }

    private class OnImageUploadCompleteListener implements OnCompleteListener {
        @Override
        public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                imageStorageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    patient.setImage(downloadUri.toString()); //get path to firestore
                }).addOnFailureListener(exception -> {
                    patient.setImage("No image provided");
                });
            } else {
                Log.d(FirebasePatientService.class.getName(), "image not uploaded successfully, exception");
                Log.e(FirebasePatientService.class.getName(), task.getException().toString());
            }
        }
    }

    private void savePatientInDatabase(Patient patient) {
        databaseReference.child(currentReference.getKey()).setValue(patient);
    }

    @Override
    public void readPatient(ValueEventListener valueEventListener, String patientId) {

        databaseQueryPatientAppointment = firebaseDatabase.getReference(Patient.class.getSimpleName())
                .orderByChild("tokenid").equalTo(patientId);

        databaseQueryPatientAppointment.addValueEventListener(valueEventListener);
    }

    @Override
    public void updatePatient(Context context, String patientKey, String parameter, String value) {

        databaseReferencePatient.child(patientKey).child(parameter).setValue(value).addOnCompleteListener(task -> {
            Toast.makeText(context,"New "+ parameter + " was saved!", Toast.LENGTH_LONG).show();

        });


    }

    @Override
    public boolean deletePatient(Long patientId) {
        return true;
    }
}

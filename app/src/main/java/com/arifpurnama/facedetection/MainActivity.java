package com.arifpurnama.facedetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.arifpurnama.facedetection.Helper.GraphicOverlay;
import com.arifpurnama.facedetection.Helper.RectOverlay;
import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private Button faceDetectButton;
    private GraphicOverlay graphicOverlay;
    private CameraKitView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceDetectButton = findViewById(R.id.detect_face_btn);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraView = findViewById(R.id.camera_view);

        final SpotsDialog alertDialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Please Wait...")
                .setCancelable(false)
                .build();

        faceDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.onStart();
                cameraView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] captureImage) {
                        alertDialog.show();
                        File savedphoto = new File(Environment.getExternalStorageState(), "photo.jpg");
                        try {
                            FileOutputStream outputStream = new FileOutputStream(savedphoto.getPath());
                            outputStream.write(captureImage);
                            outputStream.close();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                graphicOverlay.clear();
            }
        });
    }

    private void processFaceDetection(Bitmap bitmap) {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions = new FirebaseVisionFaceDetectorOptions.Builder().build();
        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(firebaseVisionFaceDetectorOptions);
        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                getFaceResults(firebaseVisionFaces);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFaceResults(List<FirebaseVisionFace> firebaseVisionFaces) {
        int counter = 0;
        for (FirebaseVisionFace face : firebaseVisionFaces) {
            Rect rect = face.getBoundingBox();
            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, rect);
            graphicOverlay.add(rectOverlay);
            counter = counter + 1;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onStart();
    }
}
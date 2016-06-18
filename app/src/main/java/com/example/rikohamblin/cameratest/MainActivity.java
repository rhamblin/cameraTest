package com.example.rikohamblin.cameratest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static String logtag = "cameraApp8";
    private static int TAKE_PICTURE = 1;
    private Uri imageUri;

    ImageView imageView;
    SurfaceView surfaceView;
    TextView qrThatWasRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_camera);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        qrThatWasRead = (TextView) findViewById(R.id.code);


        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();


        final CameraSource cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(500, 400)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try{
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e){}
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }

        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {


                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {

                    System.out.println(detections.getDetectedItems().valueAt(0).valueFormat);
                    qrThatWasRead.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            qrThatWasRead.setText("Number Found: "      // Update the TextView
                          //   +       barcodes.valueAt(0).displayValue
                            );
                        }
                    });
                }

            }
        });
    }

    public void stopPhoto(View v){    }

    private View.OnClickListener cameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            takePhoto(v);
        }
    };
    public void takePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");

        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent, TAKE_PICTURE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);

        if(resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;

            //this line was giving null pointer exception
           // getContentResolver().notifyChange(imageUri,null);

            ContentResolver cr = getContentResolver();

            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr,selectedImage);

                BarcodeDetector barcodeDetector =
                        new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

              /* CameraSource cameraSource = new CameraSource
                        .Builder(this, barcodeDetector)
                        .setRequestedPreviewSize(640, 480)
                        .build(); */

                Frame mFrame = new Frame.Builder()
                        .setBitmap(bitmap)
                        .build();

                //this is an array containing all qr codes found in the bitmap
                SparseArray<Barcode> barcodes = barcodeDetector.detect(mFrame);

                if(barcodes.size()!=0) {
                    qrThatWasRead.setText("Number Found: " + barcodes.valueAt(0).valueFormat);
                }

                imageView.setImageBitmap(bitmap);

               // Toast.makeText(getApplicationContext(),selectedImage.toString(),Toast.LENGTH_LONG).show();

            }catch(Exception e){
                Log.e(logtag,e.toString());
            }
        }
    }
}

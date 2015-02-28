package entena.com.homework2;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1024;
    private ImageView img;
    private Button camBtn, rotateBtn;
    private ProgressBar progressBar;
    private PhotoFragment photoFrag;
    private EditText degrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        rotateBtn = (Button) findViewById(R.id.btnRotate);
        camBtn = (Button) findViewById(R.id.btnCamera);
        img = (ImageView) findViewById(R.id.imageView);
        degrees = (EditText) findViewById(R.id.editTextDegrees);
        FragmentTransaction fragTran = getFragmentManager().beginTransaction();
        if(getFragmentManager().findFragmentByTag("photofrag") == null) {
            photoFrag = new PhotoFragment();
            fragTran.add(photoFrag, "photofrag");
            fragTran.commit();
        } else {
            photoFrag = (PhotoFragment) getFragmentManager().findFragmentByTag("photofrag");
        }
        setVisibility();
        setIMG();
        setProgressBar();
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(photoFrag.getBtmp() == null){
                    return;
                }
                if(degrees.getText().toString().length() > 0) {
                    photoFrag.setDegree(Integer.parseInt(degrees.getText().toString()));
                    rotatePic();
                }
            }
        });
        if(photoFrag.getDegree() != 0){
            rotatePic();
        }
    }

    public void rotatePic(){
        img.setRotation(img.getRotation()+ (float) photoFrag.getDegree());
    }

    public void setProgressBar(){
        progressBar.setProgress(photoFrag.getProgress());
    }

    public void setVisibility(){
        if(photoFrag.showBar){
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setIMG(){
        if(photoFrag.getBtmp() != null){
            img.setImageBitmap(photoFrag.getBtmp());
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // to save picture remove comment
            File file = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
            if(file.exists()){//If the file exists delete it and overwrite
                file.delete();
            }
            Log.e("CREATEINTENT", file.getPath());//Give the intent the location to save to
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e("INTENTDONE", "Loading image");
            photoFrag.setDegree(0);
            photoFrag.setBtmp(BitmapFactory.decodeFile(new File(Environment.getExternalStorageDirectory(), "photo.jpg").getAbsolutePath()));
            img.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "photo.jpg")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            super.finish();
            return true;
        }

        switch(id){
            case R.id.grey:
                Log.e("THREADING", "BEGIN GREY");
                photoFrag.runImageTask("grey");
                break;
            case R.id.floyd:
                Log.e("THREADING", "BEGIN FLOYD");
                photoFrag.runImageTask("floyd");
                break;
            case R.id.hokie:
                Log.e("THREADING", "BEGIN HOKIE");
                photoFrag.runImageTask("hokie");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

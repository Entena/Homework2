package entena.com.homework2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private Button camBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = (ImageView) findViewById(R.id.imageView);
        camBtn = (Button) findViewById(R.id.btnCamera);
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
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
            // start camera activity
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e("INTENTDONE", "Loading image");
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
        if(id == R.id.grey){
            ImageTask task = new ImageTask();
            try {
                //Bitmap bmp = task.execute("grey").get();
                Log.e("THREADING", "BEGIN ASYNC");
                task.execute("grey");
                //Log.e("THREADING", "GREY CONVERTED NOW SET");
                //Log.e("THREADING", bmp.toString());
                //img.setImageBitmap(bmp);
            }catch (Exception e){
                Log.e("THREADING", e.toString());
            }
        }
        if(id == R.id.floyd){
            //Call floyd
        }
        if(id == R.id.hokie){
            //Call hokie filter
        }
        return super.onOptionsItemSelected(item);
    }



    private class ImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String... params) {
            try {
               String mode = params[0];
               switch(mode){
                   case "grey":
                       Log.e("THREADING", "CALLING GREY CONVERSION");
                       return greyScale();
                   case "":

                   default:
                       Log.e("THREADING", "UNKNOWN MODE "+mode);
                       return null;
               }
            } catch (Exception e) {
                Log.e("THREADING", "ERROR "+e.toString());
                return null;
            }
        }

       protected void onProgressUpdate(int progress) {
            super.onProgressUpdate();
            if(progress % 10 == 0) {
                Log.e("PROGRESS", "UPDATING " + progress);
            }
            progressBar.setProgress(progress);
        }

        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(View.INVISIBLE);
            if(result != null) {
                Log.e("UPDATE UI", "UPDATE "+result.toString());
                img.setImageBitmap(result);
            } else {
                Toast.makeText(getApplicationContext(), "ERROR CONVERTING IMAGE", Toast.LENGTH_SHORT).show();
            }
        }



        private Bitmap greyScale(){
            File file = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
            if(file.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                myBitmap = convertToMutable(myBitmap);
                int imgWidth = myBitmap.getWidth();
                int imgHeight = myBitmap.getHeight();
                for(int x =0;x<imgWidth;x++) {
                    for(int y=0;y<imgHeight;y++) {
                        int s = myBitmap.getPixel(x, y);
                        int A = Color.alpha(s);
                        int R = Color.red(s);
                        int G = Color.green(s);
                        int B = Color.blue(s);
                        R = G = B = (int)((R+G+B)/3);
                        myBitmap.setPixel(x, y, Color.argb(A, R, G, B));
                    }
                    //Have to use doubles or less value is always zero
                    double i = ((double)x/(double)imgWidth)*100;
                    int prog = (int) i;//Cast double to int then pass to update progress bar
                    onProgressUpdate(prog);
                }
                Log.e("THREADING", "Returning grey image "+myBitmap.toString());
                return myBitmap;
            } else {
                return null;
            }
        }

        /**
         * Converts a immutable bitmap to a mutable bitmap. This operation doesn't allocates
         * more memory that there is already allocated.
         *
         * @param imgIn - Source image. It will be released, and should not be used more
         * @return a copy of imgIn, but muttable.
         */
        public Bitmap convertToMutable(Bitmap imgIn) {
            try {
                //this is the file going to use temporally to save the bytes.
                // This file will not be a image, it will store the raw image data.
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

                //Open an RandomAccessFile
                //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
                //into AndroidManifest.xml file
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

                // get the width and height of the source bitmap.
                int width = imgIn.getWidth();
                int height = imgIn.getHeight();
                Bitmap.Config type = imgIn.getConfig();

                //Copy the byte to the file
                //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
                FileChannel channel = randomAccessFile.getChannel();
                MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
                imgIn.copyPixelsToBuffer(map);
                //recycle the source bitmap, this will be no longer used.
                imgIn.recycle();
                System.gc();// try to force the bytes from the imgIn to be released

                //Create a new bitmap to load the bitmap again. Probably the memory will be available.
                imgIn = Bitmap.createBitmap(width, height, type);
                map.position(0);
                //load it back from temporary
                imgIn.copyPixelsFromBuffer(map);
                //close the temporary file and channel , then delete that also
                channel.close();
                randomAccessFile.close();

                // delete the temp file
                file.delete();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return imgIn;
        }
    }
}

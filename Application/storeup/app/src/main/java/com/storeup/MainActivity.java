package com.storeup;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.storeup.Extras.CustomJSONObjectRequest;
import com.storeup.Extras.VolleyController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private String url = "http://10.0.2.2:3000/ocr/getImageOcr";
    private static String KEY_SUCCESS = "success";
    private static String KEY_USERID  = "userid";
    TextView test;
    String s;
    private String userChoosenTask;
    private int REQUEST_CAMERA = 1;
    private int UPLOAD_FLAG = 0;
    //
    private ImageView imageView;

    private Button buttonUpload;
    private int PICK_IMAGE_REQUEST = 2;
    private Uri filePath;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private StorageReference uploadRef;
    //
    AppSessionManager appSessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appSessionManager=new AppSessionManager(getApplicationContext());
        appSessionManager.checkLoginStatus();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        imageView = (ImageView) findViewById(R.id.imageView);


        //attaching listener
        buttonUpload.setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        s = appSessionManager.getUserName();
        //s= getIntent().getStringExtra("username");
        Toast.makeText(getApplicationContext(),"Welcome "+s, Toast.LENGTH_LONG).show();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == buttonUpload) {
            uploadFile();
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Upload your Receipt!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    UPLOAD_FLAG = 0;
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    UPLOAD_FLAG = 1;
                    showFileChooser();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void showFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK ) {  /*&&&& data != null && data.getData() != null*/
            if (requestCode == PICK_IMAGE_REQUEST){
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bitmap=null;
        if (data != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis() + ".jpeg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        filePath = Uri.fromFile(destination);
        data.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
        imageView.setImageBitmap(thumbnail);
    }

    //this method will upload the file
    private void uploadFile() {
        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            final String[] arr = filePath.toString().split("/");
            if(UPLOAD_FLAG == 1) {
                uploadRef = storageReference.child("images/" + arr[arr.length - 1]);
                Toast.makeText(getApplicationContext(), "Storage Uri: " + arr[arr.length - 1], Toast.LENGTH_LONG).show();
            } else if (UPLOAD_FLAG == 0) {
                Random random = new Random();
                int key =random.nextInt(1000);
                uploadRef = storageReference.child("pictures/" + "pic" + key + ".jpg");
                Toast.makeText(getApplicationContext(), "Storage Uri: " + "pic" + key, Toast.LENGTH_LONG).show();
            }
            uploadRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progressDialog.dismiss();

                            //and displaying a success toast
                            @SuppressWarnings("VisibleForTests")StorageReference downloadUri = taskSnapshot.getStorage();
                            Toast.makeText(getApplicationContext(), "File Uploaded "+downloadUri.toString(), Toast.LENGTH_LONG).show();



                            CustomJSONObjectRequest rq = new CustomJSONObjectRequest(Request.Method.POST, url, null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getString(KEY_SUCCESS) != null) {
                                                    int success = Integer.parseInt(response.getString(KEY_SUCCESS));
                                                    if (success == 1) {
                                                        Toast.makeText(getApplicationContext(), R.string.registered, Toast.LENGTH_LONG).show();

                                                    } else if (success == 0) {
                                                        Toast.makeText(getApplicationContext(), R.string.email_exists, Toast.LENGTH_LONG).show();
                                                    }else if (success == 2) {
                                                        Toast.makeText(getApplicationContext(), R.string.username_exists, Toast.LENGTH_LONG).show();
                                                    }else {
                                                        Toast.makeText(getApplicationContext(), R.string.invalid_post, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Response Error", error.toString());
                                    Toast.makeText(getApplicationContext(), R.string.invalid_post, Toast.LENGTH_LONG).show();
                                }
                            }) {

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    HashMap<String, String> headers = new HashMap<String, String>();
                                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                                    headers.put("User-agent","My agent");
                                    return headers;
                                }

                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("tag", "register");
                                    params.put("email", "sdkjflslkdfksalj");
                                    params.put("StorageReference",arr[arr.length-1]);
                                    return params;
                                }

                            };
                            rq.setRetryPolicy(new DefaultRetryPolicy(
                                    0,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                            VolleyController.getInstance(getApplicationContext()).addToRequestQueue(rq);




                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            @SuppressWarnings("VisibleForTests") double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "Please select a file to upload", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        test =(TextView) findViewById(R.id.textView);
        test.setText(s);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedNavScreen(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.user_profile:
                fragment = new UserProfile();
                getUserDetails();
                break;
            case R.id.user_receipts:
                fragment = new UserReceipts();
                break;
            case R.id.user_coupons:
                fragment = new UserCoupons();
                break;
            case R.id.faqs:
                fragment = new Faqs();
                break;
        }
        if(fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            ft.replace(R.id.content_main, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void getUserDetails() {
        String email = appSessionManager.getKeyEmail();
        final String URL = "http://10.0.2.2:3000/users/userDetails" + "?email=" + email;

        // pass second argument as "null" for GET requests

        CustomJSONObjectRequest req = new CustomJSONObjectRequest(URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString(4));
                            if (response.getString(KEY_SUCCESS) != null) {
                                int success = Integer.parseInt(response.getString(KEY_SUCCESS));
                                if (success == 1) {
                                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();

                                } else if (success == 0) {
                                    Toast.makeText(getApplicationContext(), R.string.email_exists, Toast.LENGTH_LONG).show();
                                }else if (success == 2) {
                                    Toast.makeText(getApplicationContext(), R.string.username_exists, Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(getApplicationContext(), R.string.invalid_post, Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response Error", error.toString());
                Toast.makeText(getApplicationContext(), R.string.invalid_post, Toast.LENGTH_LONG).show();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyController.getInstance(getApplicationContext()).addToRequestQueue(req);

        Toast.makeText(getApplicationContext(),URL, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        displaySelectedNavScreen(id);

        return true;
    }
}

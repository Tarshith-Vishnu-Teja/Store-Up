package com.storeup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class nav_scanReceipts extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    TextView test;
    String s;
    private static String KEY_SUCCESS = "success";
    private String url = "http://10.0.2.2:3000/ocr/getImageOcr";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_nav_scan_receipts);

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
    }

    public void onClick(View view) {
        if (view == buttonUpload) {
            uploadFile();
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(nav_scanReceipts.this);
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
                                    HashMap<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                                    headers.put("User-agent","My agent");
                                    return headers;
                                }

                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.user_profile:
                Intent userProfile = new Intent(nav_scanReceipts.this, nav_userProfile.class);
                startActivity(userProfile);
                break;
            case R.id.user_receipts:
                Intent userReceipts = new Intent(nav_scanReceipts.this, nav_userReceipts.class);
                startActivity(userReceipts);
                break;
            case R.id.user_coupons:
                Intent userCoupons = new Intent(nav_scanReceipts.this, nav_userCoupons.class);
                startActivity(userCoupons);
                break;
            case R.id.faqs:
                Intent faqs = new Intent(nav_scanReceipts.this, nav_faqs.class);
                startActivity(faqs);
                break;
            case R.id.scan_receipts:
                Intent i = new Intent(nav_scanReceipts.this, nav_scanReceipts.class);
                startActivity(i);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

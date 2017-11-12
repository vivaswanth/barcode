package com.harika.vvh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

//import com.google.android.gms.common.api.Response;

public class TicketResultActivity extends AppCompatActivity {
    private static final String TAG = TicketResultActivity.class.getSimpleName();

    // url to search barcode
    private static final String URL = "https://api.androidhive.info/barcodes/search.php?code=";
    private Barcode barcode;
    private TextView txtName, txtDuration, txtDirector, txtGenre, txtRating, txtPrice, txtError;
    private ImageView imgPoster;
    private Button btnBuy;
    private ProgressBar progressBar;
    private TicketView ticketView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName =  (TextView) findViewById(R.id.name);
        txtDirector = (TextView) findViewById(R.id.director);
        txtDuration = (TextView) findViewById(R.id.duration);
        txtPrice =  (TextView) findViewById(R.id.price);
        txtRating =  (TextView) findViewById(R.id.rating);
        imgPoster = (ImageView) findViewById(R.id.poster);
        txtGenre =  (TextView) findViewById(R.id.genre);
        btnBuy = (Button) findViewById(R.id.btn_buy);
        imgPoster = (ImageView) findViewById(R.id.poster);
        txtError =  (TextView)findViewById(R.id.txt_error);
        ticketView = (TicketView) findViewById(R.id.layout_ticket);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        String barcode = getIntent().getStringExtra("code");
        barcode = getIntent().getStringExtra("code");
        // close the activity in case of empty barcode
        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(getApplicationContext(), "Barcode is empty!", Toast.LENGTH_LONG).show();
            finish();
        }

        // search the barcode
        searchBarcode(barcode);
    }

    /**
     * Searches the barcode by making http call
     * Request was made using Volley network library but the library is
     * not suggested in production, consider using Retrofit
     */
    private void searchBarcode(final String barcode) {
        // making volley's json request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL + barcode, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, "Ticket response: " + response.toString()+barcode);

                        // check for success status
                        if (!response.has("error")) {
                            // received movie response
                            renderMovie(response);
                        } else {
                            // no movie found
                            showNoTicket();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                showNoTicket();
            }
        });

        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }


    private void showNoTicket() {
        txtError.setVisibility(View.VISIBLE);
        ticketView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("code", barcode);
        startActivity(intent);
    }

    /**
     * Rendering movie details on the ticket
     */
    private void renderMovie(JSONObject response) {
        try {

            // converting json to movie object
            Movie movie = new Gson().fromJson(response.toString(), Movie.class);

            if (movie != null) {
                txtName.setText(movie.getName());
                txtDirector.setText(movie.getDirector());
                txtDuration.setText(movie.getDuration());
                txtGenre.setText(movie.getGenre());
                txtRating.setText("" + movie.getRating());
                txtPrice.setText(movie.getPrice());
                Glide.with(this).load(movie.getPoster()).into(imgPoster);

                if (movie.isReleased()) {
                    btnBuy.setText(getString(R.string.btn_buy_now));
                    btnBuy.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                } else {
                    btnBuy.setText(getString(R.string.btn_coming_soon));
                    btnBuy.setTextColor(ContextCompat.getColor(this, R.color.btn_disabled));
                }
                ticketView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } else {
                // movie not found
                showNoTicket();
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON Exception: " + e.getMessage());
            showNoTicket();
            Toast.makeText(getApplicationContext(), "Error occurred. Check your LogCat for full report", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // exception
            showNoTicket();
            Toast.makeText(getApplicationContext(), "Error occurred. Check your LogCat for full report", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private class Movie {
        String name;
        String director;
        String poster;
        String duration;
        String genre;
        String price;
        float rating;

        @SerializedName("released")
        boolean isReleased;

        public String getName() {
            return name;
        }

        public String getDirector() {
            return director;
        }

        public String getPoster() {
            return poster;
        }

        public String getDuration() {
            return duration;
        }

        public String getGenre() {
            return genre;
        }

        public String getPrice() {
            return price;
        }

        public float getRating() {
            return rating;
        }

        public boolean isReleased() {
            return isReleased;
        }
    }
}


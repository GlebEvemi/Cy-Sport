package com.example.cysport;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.moonlightbastion.com/hack/areas";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, // Для GET-запросов тело обычно null
                response -> {
                    // Успешный ответ
                    try {
                        List<CustomInfoWindowData> locations = parseMarkerJson(response); // Парсинг JSON
                        addMarkersToMap(locations); // Добавляем маркеры на карту
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Обработка ошибки
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonObjectRequest);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        Boolean LocationIsEnabled = enableMyLocation();



        //Задаю стиль карте
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // uiSettings - это класс отображения UI(очень много полезных методов)
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setMapToolbarEnabled(true);

        // Определяем границы Кипра
        LatLngBounds cyprusBounds = new LatLngBounds(
                new LatLng(34.5, 32.0), // Юго-западная точка (широта, долгота)
                new LatLng(35.7, 34.7)  // Северо-восточная точка (широта, долгота)
        );

        // Устанавливаем границы карты
        map.setLatLngBoundsForCameraTarget(cyprusBounds);

        // Устанавливаем начальную позицию камеры и зум
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(35.0, 33.0), 8f);
        map.moveCamera(cameraUpdate);

        // минимальный зум для детализации
        map.setMinZoomPreference(9.5f);




        map.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return false; // Возвращаем false, чтобы карта обрабатывала стандартное поведение
        });




        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // Получаем данные из маркера
                Object tag = marker.getTag();
                if (tag instanceof CustomInfoWindowData) {
                    CustomInfoWindowData data = (CustomInfoWindowData) tag;
                    CustomInfoWindowData markerTag = (CustomInfoWindowData) marker.getTag();

                    // Создаем и показываем BottomSheetDialog
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
                    View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottom_sheet, null);

                    // Ищем commentCard в bottomSheetView
                    LinearLayout commentCard = bottomSheetView.findViewById(R.id.commentCard);

                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();

                    // Volley запрос для получения комментариев
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    String url2 = "https://api.moonlightbastion.com/hack/areas/comments/" + data.getId();

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            url2,
                            null, // Для GET-запросов тело null
                            response -> {
                                // Успешный ответ
                                try {
                                    List<Comment> comments = parseCommentJson(response); // Парсинг JSON

                                    // Добавляем комментарии в commentCard
                                    if (commentCard != null) {
                                        addCommentsToCard(comments, commentCard);
                                    } else {
                                        Log.e("ERROR", "commentCard равен null!");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> {
                                // Обработка ошибки
                                Log.e("VolleyError", error.toString());
                                Toast.makeText(MainActivity.this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
                            }
                    );

                    queue.add(jsonObjectRequest);



                    Comment comment = new Comment();
                    RatingBar rating = bottomSheetView.findViewById(R.id.commentRating);
                    TextInputLayout text = bottomSheetView.findViewById(R.id.textFieldLayout);
                    TextInputEditText edittext = bottomSheetView.findViewById(R.id.editText);
                    TextInputLayout author = bottomSheetView.findViewById(R.id.username);
                    TextInputEditText author_edit = bottomSheetView.findViewById(R.id.usernameEditText);
                    MaterialButton submitBtn = bottomSheetView.findViewById(R.id.submit);
                    submitBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(edittext.getText().toString().isEmpty()){
                                text.setError("Please type something!");
                            }else if(author_edit.getText().toString().isEmpty()){
                                text.setError("Please type authors name!");
                            }else{
                                comment.setAreaId(markerTag.getId());
                                comment.setUsername(author_edit.getText().toString());
                                comment.setText(edittext.getText().toString());
                                comment.setRating((int)rating.getRating());
                                postComment(comment);

                                Toast.makeText(MainActivity.this, "Submitted!", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });


    }
    private void addCommentsToCard(List<Comment> comments, LinearLayout commentCard) {
        // Удаляем старые комментарии из commentCard
        commentCard.removeAllViews();

        // Для каждого комментария создаем представление
        for (Comment comment : comments) {
            // Загружаем макет item_comment.xml
            View commentView = LayoutInflater.from(commentCard.getContext()).inflate(R.layout.item_comment, commentCard, false);

            // Ищем виджеты в загруженном макете
            RatingBar ratingBar = commentView.findViewById(R.id.item_users_rating);
            TextView authorView = commentView.findViewById(R.id.item_authors_comment_id);
            TextView commentTextView = commentView.findViewById(R.id.item_users_comment);

            // Устанавливаем данные комментария
            ratingBar.setRating(comment.getRating());
            authorView.setText(comment.getUsername());
            commentTextView.setText(comment.getText());

            // Добавляем заполненный виджет в commentCard
            commentCard.addView(commentView);
        }
    }


    private void postComment(Comment sendPackage){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.moonlightbastion.com/hack/comment";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //todo
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //todo
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return sendPackage == null ? null : sendPackage.toJson().getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", sendPackage, "utf-8");
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        };
        queue.add(stringRequest);

        }

    private void addMarkersToMap(List<CustomInfoWindowData> locations) {
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(this)); // Установить адаптер

        for (CustomInfoWindowData data : locations) {
            // Загружаем изображение для маркера
            Glide.with(this)
                    .asBitmap()
                    .load(data.getImage())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // Когда изображение загружено, создаем маркер
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng location = new LatLng(data.getLat(), data.getLon());
                            markerOptions.position(location);
                            int group = data.getPlaceGroup();
                            switch (group){
                                case 1:
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.sport_club))); // Устанавливаем иконку маркера
                                    break;
                                case 2:
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.pull_up_bar))); // Устанавливаем иконку маркера
                                    break;
                                case 3:
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.gym))); // Устанавливаем иконку маркера
                                    break;
                            }


                            Marker marker = map.addMarker(markerOptions);
                            marker.setTag(data); // Привязываем данные к маркеру
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Если загрузка прервана или изображение очищено
                        }
                    });
        }
    }

    private List<Comment> parseCommentJson(JSONObject response) throws JSONException {
        List<Comment> comments = new ArrayList<>();

        JSONArray jsonArray = response.getJSONArray("data");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            int rating = jsonObject.getInt("rating");
            String username = jsonObject.getString("username");
            String text = jsonObject.getString("comment");



            comments.add(new Comment(username, text, rating, -1));
        }

        return comments;
    }


    private List<CustomInfoWindowData> parseMarkerJson(JSONObject response) throws JSONException {
        List<CustomInfoWindowData> locations = new ArrayList<>();

        JSONArray jsonArray = response.getJSONArray("data");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String name = jsonObject.getString("name");
            int id = jsonObject.getInt("id");
            int group = jsonObject.getInt("placeGroup");
            String description = jsonObject.getString("description");
            double latitude = jsonObject.getDouble("latitude");
            double longitude = jsonObject.getDouble("longitude");
            String imageUrl = jsonObject.getString("imageUrl");
            float rating = (float)jsonObject.getDouble("rating");


            locations.add(new CustomInfoWindowData(id, name, group, description, imageUrl, latitude, longitude, rating));
        }

        return locations;
    }

    private Bitmap getBitmapFromDrawable(int resId) {
        Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), resId, null);

        if (drawable != null) {
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }


        return bitmap;
    }


    private boolean enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return true;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, false);
        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}

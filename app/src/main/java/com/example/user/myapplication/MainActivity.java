package com.example.user.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private HotelArrayAdapter adapter;
    static final int LIST_PETS = 1;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIST_PETS: {
                    List<Hotel> hotels = (List<Hotel>)msg.obj;
                    refreshPetList(hotels);
                    break;
                }
            }
        }
    };
    private void refreshPetList(List<Hotel> hotels) {
        adapter.clear();
        adapter.addAll(hotels);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvHotels = (ListView)findViewById(R.id.listview_hotel);
        adapter = new HotelArrayAdapter(this, new ArrayList<Hotel>());
        lvHotels.setAdapter(adapter);


        getHotelsFromFirebase();
    }
    private void getHotelsFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseThread(dataSnapshot).start();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("AdoptPet", databaseError.getMessage());
            }
        });
    }

    class FirebaseThread extends Thread {
        private DataSnapshot dataSnapshot;
        public FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }
        @Override
        public void run() {
            List<Hotel> lsHotels = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                DataSnapshot dsSName = ds.child("Name");
                DataSnapshot dsAAdd = ds.child("Add");
                DataSnapshot dsTTel = ds.child("Tel");
                DataSnapshot dsImg = ds.child("Picture1");

                String shelterName = (String)dsSName.getValue();
                String add = (String)dsAAdd.getValue();
                String tel = (String)dsTTel.getValue();
                String imgUrl = (String) dsImg.getValue();
                Bitmap hotelImg = getImgBitmap(imgUrl);

                Hotel aHotel = new Hotel();
                aHotel.setImgUrl(hotelImg);
                aHotel.setKind(add);
                aHotel.setShelter(shelterName);
                aHotel.setTel(tel);

                Log.v("AdoptPet", shelterName + ";" + add);
                lsHotels.add(aHotel);
            }
            Message msg = new Message();
            msg.what = LIST_PETS;
            msg.obj = lsHotels;
            handler.sendMessage(msg);
        }
    }

    private Bitmap getImgBitmap(String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            Bitmap bm = BitmapFactory.decodeStream(
                    url.openConnection()
                            .getInputStream());
            return bm;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    class HotelArrayAdapter extends ArrayAdapter<Hotel> {
        Context context;
        public HotelArrayAdapter(Context context, List<Hotel> items) {
            super(context, 0, items);
            this.context = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout itemlayout = null;
            if (convertView == null) {
                itemlayout = (LinearLayout) inflater.inflate(R.layout.hotel_item, null);
            } else {
                itemlayout = (LinearLayout) convertView;
            }
            Hotel item = (Hotel) getItem(position);
            TextView tvShelter = (TextView) itemlayout.findViewById(R.id.textView);
            tvShelter.setText("  店名 : "+item.getShelter());
            TextView tvKind = (TextView) itemlayout.findViewById(R.id.textView2);
            tvKind.setText("  地址 : "+item.getKind());
            TextView tvTel = (TextView) itemlayout.findViewById(R.id.textView3);
            tvTel.setText("  電話 : "+item.getTel());
            ImageView ivPet = (ImageView) itemlayout.findViewById(R.id.imageView);
            ivPet.setImageBitmap(item.getImgUrl());

            return itemlayout;
        }
    }

}



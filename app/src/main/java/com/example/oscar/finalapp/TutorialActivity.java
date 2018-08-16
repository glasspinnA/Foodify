package com.example.oscar.finalapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Klass som sköter Activityn för hantera den guide som startar första gången applikationen körs
 */

public class TutorialActivity extends AppCompatActivity {

    /**
     * Metod initerar kompontenter som finns i denna activityn
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        ViewPager mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new CustomAdapter());

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager, true);
    }

    /**
     * Adapter som möjliggör att man kan ändra mellan bilderna i guiden
     */
    private class CustomAdapter extends PagerAdapter {
        public int getCount() {
            return 4;
        }


        /**
         * Metod som ändrar vilken bild (layout fil) som användaren ser
         * @param container
         * @param position - Vilken bild användaren är på
         * @return - Den layout fil som ska visas
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;
            LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (position) {
                case 0:
                    view = inflater.inflate(R.layout.first_tutorial_slide,null);
                    break;
                case 1:
                    view = inflater.inflate(R.layout.second_tutorial_slide,null);
                    break;
                case 2:
                    view = inflater.inflate(R.layout.third_tutorial_slide,null);
                    break;
                case 3:
                    view = inflater.inflate(R.layout.last_tutorial_slide,null);
                    Button readyBtn = view.findViewById(R.id.btnDone);
                    readyBtn.setOnClickListener(new ButtonListener());
                    break;
            }
            (container).addView(view, 0);

            return view;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        /**
         * Metod som startar MapsActivity när knappen som har knapplyssnare på sig blir tryckt.
         * Är det knapp på den sista sidan.
         */
        private class ButtonListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                finish();
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        }
    }
}
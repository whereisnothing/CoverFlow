package com.chenxu.coverflow;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
        Integer[] imageIdArray = new Integer[]{R.drawable.load_1,R.drawable.load_2,R.drawable.load_3,R.drawable.load_4,
                R.drawable.load_5,R.drawable.load_6,R.drawable.load_7,R.drawable.load_8};
        List<Integer> imageIdList = Arrays.asList(imageIdArray);
        CoverFlow coverFlow = new CoverFlow(this, imageIdList, new CoverFlow.CoverFlowListener() {
            @Override
            public void coverFlowDidClick(int index) {
                Snackbar.make(root, "index "+index+" is clicked!", Snackbar.LENGTH_SHORT).show();
            }
        });
        RelativeLayout.LayoutParams coverFlowParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        coverFlow.setLayoutParams(coverFlowParams);
        root.addView(coverFlow);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
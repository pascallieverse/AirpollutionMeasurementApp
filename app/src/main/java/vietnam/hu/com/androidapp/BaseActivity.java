package vietnam.hu.com.androidapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Pascal on 11/5/2017.
 */

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    /**
     *   When back is pressed close the menu.
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     *   Add the options to the menu
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Boolean>
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Boolean>
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(BaseActivity.this, SettingsActivity.class);
            BaseActivity.this.startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     *   When back is pressed close the menu.
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent myIntent = new Intent(BaseActivity.this, MainActivity.class);
            BaseActivity.this.startActivity(myIntent);
        } else if (id == R.id.nav_map) {
            Intent myIntent = new Intent(BaseActivity.this, MapActivity.class);
            BaseActivity.this.startActivity(myIntent);
        }
//        } else if (id == R.id.nav_statistics) {
//            Intent myIntent = new Intent(BaseActivity.this, StatisticsActivity.class);
//            BaseActivity.this.startActivity(myIntent);
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

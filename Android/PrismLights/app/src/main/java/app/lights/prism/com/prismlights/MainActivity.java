package app.lights.prism.com.prismlights;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.brian_oh.prismlights.R;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTabHost tabHost = new FragmentTabHost(this);
        setContentView(R.layout.activity_main);
        Dialog dialog = new ProgressDialog(this);
        dialog.show();
        TextView textView = new TextView(this);
        textView.setText("Searching For Bridge...");
        textView.setTextColor(Color.WHITE);
        dialog.setContentView(textView);
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

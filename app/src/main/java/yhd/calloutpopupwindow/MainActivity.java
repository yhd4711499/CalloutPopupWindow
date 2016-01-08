package yhd.calloutpopupwindow;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Random;

import yhd.widget.CalloutPopupWindow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    int[] buttonIds = new int[]{
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6
    };

    int[] colors = new int[]{
            Color.parseColor("#7D005A"),
            Color.parseColor("#7F2E69"),
            Color.parseColor("#9E0019"),
            Color.parseColor("#4D9134"),
            Color.parseColor("#278F00"),
            Color.parseColor("#243002"),
            Color.parseColor("#300209"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        for (int id :
                buttonIds) {
            findViewById(id).setOnClickListener(this);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        CalloutPopupWindow.Builder builder = CalloutPopupWindow.builder(this)
                .setLifetime(3)
                .setText("Click anywhere to dismiss.");
        if (v.getId() == R.id.button3 || v.getId() == R.id.button2) {
            builder.setPosition(CalloutPopupWindow.Position.ABOVE);
            builder.setText("Click [x] to dismiss.\nClick [x] to dismiss.\nClick [x] to dismiss.\nClick [x] to dismiss.");
            builder.setAutoDismiss(false);
        } else if (v.getId() == R.id.button4) {
            builder.setPosition(CalloutPopupWindow.Position.LEFT);
            builder.setText("Click anywhere to dismiss.\nClick anywhere to dismiss.\nClick anywhere to dismiss.\nClick anywhere to dismiss.");
        } else if (v.getId() == R.id.button5) {
            builder.setPosition(CalloutPopupWindow.Position.RIGHT);
        }
        shuffleArray(colors);

        CalloutPopupWindow.DrawableBuilder drawableBuilder = new CalloutPopupWindow.DrawableBuilder(builder)
                .setBackgroundColor(colors[0])
                .setBackgroundRadius(5);

        if (v.getId() == R.id.button6) {
            Handler handler = new Handler(Looper.getMainLooper());
            final CalloutPopupWindow.DrawableBuilder finalBuilder = drawableBuilder;
            int gap = 100;
            final CalloutPopupWindow.Position[] positions = new CalloutPopupWindow.Position[]{
                    CalloutPopupWindow.Position.ABOVE, CalloutPopupWindow.Position.RIGHT, CalloutPopupWindow.Position.BELOW, CalloutPopupWindow.Position.LEFT
            };
            for (int i = 0; i < positions.length; i++) {
                final int finalI = i;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finalBuilder.setBackgroundColor(colors[finalI]);
                        finalBuilder.build().setPosition(positions[finalI]).build().showAsPointer(v);
                    }
                }, gap * i);
            }
        } else {
            drawableBuilder.build().build().showAsPointer(v);
        }
    }

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(int[] ar)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}

package pl.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(Color.rgb(2, 6, 23));

        TextView name = new TextView(this);
        name.setText("Jarvis");
        name.setTextColor(Color.rgb(56, 189, 248));
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setLetterSpacing(0.18f);
        name.setGravity(Gravity.CENTER);

        TextView message = new TextView(this);
        message.setText("aplikacja działa");
        message.setTextColor(Color.rgb(226, 232, 240));
        message.setTextSize(42);
        message.setTypeface(Typeface.DEFAULT_BOLD);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 24, 0, 0);

        layout.addView(name);
        layout.addView(message);
        setContentView(layout);
    }
}

package epic.weishu.me.epic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by weishu on 17/3/15.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button test = new Button(this);
        test.setText("show hello world!");
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helloWorld();
            }
        });

        Button hook = new Button(this);
        hook.setText("hook");
        hook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Method origin = MainActivity.class.getDeclaredMethod("helloWorld");
                    Method replace = MainActivity.class.getDeclaredMethod("helloArt");
                    Hook.hook(origin, replace);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        layout.addView(test);
        layout.addView(hook);
        setContentView(layout);

    }

    public void helloWorld() {
        Toast.makeText(this, "hello world!", Toast.LENGTH_SHORT).show();
    }

    public void helloArt() {
        Toast.makeText(this, "hello art!", Toast.LENGTH_SHORT).show();
    }
}

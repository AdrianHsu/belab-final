package dots.adrianhsu.bioexp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by adrianhsu on 2017/6/12.
 */

public class AnalysisActivity extends AppCompatActivity {

    ArrayList mylist = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Intent i = getIntent();
        mylist = i.getStringArrayListExtra("mylist");
        Log.d("TAG", mylist.toString());
    }
}

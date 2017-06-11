package dots.adrianhsu.bioexp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.OnCancelAction;
import me.drozdzynski.library.steppers.OnChangeStepAction;
import me.drozdzynski.library.steppers.OnFinishAction;
import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;


public class MainActivity extends AppCompatActivity {

    Button btlistBtn;
    Button showChartBtn;
    Button disconBtn;
    Button weightBtn;
    Button oxygenBtn;
    Button bpmBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SteppersView.Config steppersViewConfig = new SteppersView.Config();
        steppersViewConfig.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {
                // Action on last step Finish button
            }
        });

        steppersViewConfig.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {
                // Action when click cancel on one of steps
            }
        });

        steppersViewConfig.setOnChangeStepAction(new OnChangeStepAction() {
            @Override
            public void onChangeStep(int position, SteppersItem activeStep) {
                // Action when click continue on each step
            }
        });

// Setup Support Fragment Manager for fragments in steps
        steppersViewConfig.setFragmentManager(getSupportFragmentManager());


        ArrayList<SteppersItem> steps = new ArrayList<>();

        SteppersItem stepFirst = new SteppersItem();

        stepFirst.setLabel("Title of step");
        stepFirst.setSubLabel("Subtitle of step");
        stepFirst.setFragment(new MainFragment());
        stepFirst.setPositiveButtonEnable(false);

        steps.add(stepFirst);

        SteppersView steppersView = (SteppersView) findViewById(R.id.steppersView);
        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
    }
}

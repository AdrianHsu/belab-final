package dots.adrianhsu.bioexp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.OnCancelAction;
import me.drozdzynski.library.steppers.OnChangeStepAction;
import me.drozdzynski.library.steppers.OnFinishAction;
import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SteppersView.Config steppersViewConfig = new SteppersView.Config();
        steppersViewConfig.setOnFinishAction(new OnFinishAction() {
            @Override
            public void onFinish() {
                // Action on last step Finish button
                MainActivity.this.startActivity(new Intent(MainActivity.this, MainActivity.class));
                MainActivity.this.finish();
            }
        });

        steppersViewConfig.setOnCancelAction(new OnCancelAction() {
            @Override
            public void onCancel() {
                // Action when click cancel on one of steps
                MainActivity.this.startActivity(new Intent(MainActivity.this, MainActivity.class));
                MainActivity.this.finish();
            }
        });

        steppersViewConfig.setOnChangeStepAction(new OnChangeStepAction() {
            @Override
            public void onChangeStep(int position, SteppersItem activeStep) {
                // Action when click continue on each step
                Toast.makeText(MainActivity.this, "Step changed to: " + activeStep.getLabel() + " (" + position + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });

// Setup Support Fragment Manager for fragments in steps
        steppersViewConfig.setFragmentManager(getSupportFragmentManager());


        ArrayList<SteppersItem> steps = new ArrayList<>();

        SteppersItem stepFirst = new SteppersItem();
        stepFirst.setLabel("STEP1. Weight 本週折線圖");
        stepFirst.setSubLabel("請確認您的測量已經完成");
        stepFirst.setFragment(new FirstFragment());
        stepFirst.setPositiveButtonEnable(true);
        steps.add(stepFirst);

        SteppersItem stepSec = new SteppersItem();
        stepSec.setLabel("STEP2. BPM 本週折線圖");
        stepSec.setSubLabel("請確認您的測量已經完成");
        stepSec.setFragment(new SecondFragment());
        stepSec.setPositiveButtonEnable(true);
        steps.add(stepSec);

        SteppersItem stepThi = new SteppersItem();
        stepThi.setLabel("STEP3. SpO2 血氧本週折線圖");
        stepThi.setSubLabel("請確認您的測量已經完成");
        stepThi.setFragment(new ThirdFragment());
        stepThi.setPositiveButtonEnable(true);
        steps.add(stepThi);

        SteppersItem stepFou = new SteppersItem();
        stepFou.setLabel("STEP4. Bluetooth 藍芽模組測試");
        stepFou.setSubLabel("請確認您的 Arduino 已經正確連線");
        stepFou.setFragment(new FourthFragment());
        stepFou.setPositiveButtonEnable(true);
        steps.add(stepFou);

        SteppersItem stepFif = new SteppersItem();
        stepFif.setLabel("STEP5. ECG (Electrocardiography) 折線圖");
        stepFif.setSubLabel("請再次檢查您的藍芽連線配對是否正確");
        stepFif.setFragment(new FifthFragment());
        stepFif.setPositiveButtonEnable(true);
        steps.add(stepFif);

        SteppersView steppersView = (SteppersView) findViewById(R.id.steppersView);
        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
    }
}

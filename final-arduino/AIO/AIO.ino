#include <hx711.h>
#include <MAX30100_PulseOximeter.h>
#include <Arduino.h>
#include <math.h>
#include <Wire.h>
#define REPORTING_PERIOD_MS     1000

///////////////////
//some parameters//
///////////////////
Hx711 scale(A1, A0);
float offset = 0;
float ratio = 20651.94;
int av_times = 10;
int thres = 3;
int de = av_times - thres - 1;

PulseOximeter pox;
uint32_t tsLastReport = 0;

void setup() {
//////////////
//Setting up//
//////////////
  //set up all
  Serial.begin(115200);

  //set up hx711
  Serial.print("Initializing HX711...");
  for (int  i = 0; i < 2; i++){
    float plus = scale.averageValue();
    offset += plus;
  }
  Serial.println("SUCCESS");
  offset /= 2;
  scale.setOffset(offset);
  scale.setScale(ratio);
  
  //set up max30100
  Serial.print("Initializing MAX30100...");
  if (!pox.begin()) {
    Serial.println("FAILED");
    for(;;);
  } 
  else {
    Serial.println("SUCCESS");
  }
  
  //set up wifi
  //TODO
  //set up wifi
  //TODO


/////////////
//Measuring//
/////////////
  //get weight
  Serial.print("Weighting...");
  float weight = (scale.averageValue() - offset) / ratio;
  weight += (scale.averageValue() - offset) / ratio;
  weight /= 2;
  Serial.println("SUCCESS");

  //get max30100 info
  float BPM = 0;
  float SaO2 = 0;
  float temp = 0;
  Serial.print("Measuring...");
  for (int  i = 0; i < av_times; i++){
    pox.update();
    float tmp1 = pox.getHeartRate();
    float tmp2 = pox.getSpO2();
    float tmp3 = pox.getTemperature();
    if (millis() - tsLastReport > REPORTING_PERIOD_MS && tmp1>50 && tmp1<110) {
      Serial.print(av_times - i);
      Serial.print("..");
      if (i>thres) {
        BPM += tmp1;
        SaO2 += tmp2;
        temp += tmp3;
      }
      tsLastReport = millis();
    }
    else
      i--;
  }
  Serial.println("SUCCESS");
  BPM /= de;
  SaO2 /= de;
  temp /= de;


////////////
//Printing//
////////////
  Serial.print("Weight: ");
  Serial.print(weight, 1);
  Serial.print(" kg | ");
  Serial.print( "Heart Rate: " );
  Serial.print( BPM,1 );
  Serial.print( " bpm | " );
  Serial.print( "SaO2: " );
  Serial.print( SaO2*0.99,1 );
  Serial.print( "% | " );
  Serial.print( "Temperature: " );
  Serial.print( temp*0.9,1 );
  Serial.println( "C" );


/////////////
//Uploading//
/////////////
//TODO
}

void loop() {
  // put your main code here, to run repeatedly:

}

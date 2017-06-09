#include <hx711.h>

Hx711 scale(A1, A0);
float offset = 0;
float ratio = 20651.94;
int w_time = 10;
int av_times = 10;
float w_weitht = 64;

void setup() {
  Serial.begin(115200);
  
  for(int i = 0; i < w_time; i++){
    Serial.print("Start correction in ");
    Serial.print(w_time - i);
    Serial.println("...");
    delay(1000);
  }

  for (int  i = 0; i < av_times; i++){
    Serial.print("Getting average raw data: ");
    float plus = scale.averageValue();
    Serial.println(plus);
    offset += plus;
  }
  offset /= av_times;
  Serial.print("offset: ");
  Serial.println(offset);
  /*
  Serial.println("Put the weight on the cell.");
  for(int i = 0; i < w_time; i++){
    Serial.print("Start in ");
    Serial.print(w_time - i);
    Serial.println("...");
    delay(1000);
  }

  for (int  i = 0; i < av_times; i++){
    Serial.print("Getting average raw data: ");
    float plus = scale.averageValue();
    Serial.println(plus);
    ratio += plus;
  }
  Serial.print("Weight: ");
  Serial.println(ratio);
  ratio = (ratio / av_times - offset) / w_weitht;
  Serial.print("Ratio: ");
  Serial.println(ratio);
  */
  Serial.println("Correcting...");  
  scale.setOffset(offset);
  scale.setScale(ratio);
  Serial.println("Correction done.");
}

void loop() {
  float weight = (scale.averageValue() - offset) / ratio;
  Serial.print("Weight: ");
  Serial.print(weight, 1);
  Serial.println(" kg");
}

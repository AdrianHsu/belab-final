// ref: https://www.arduino.cc/en/Reference/analogRead
// ref: http://www.egr.msu.edu/classes/ece480/capstone/spring14/group01/docs/appnote/Wirsing-SendingAndReceivingDataViaBluetoothWithAnAndroidDevice.pdf
// ref: http://blog.cavedu.com/programming-language/appinventor/%E9%9B%99a%E8%A8%88%E5%8A%83-part1%EF%BC%9Aapp-inventor-%E7%B6%93%E7%94%B1%E8%97%8D%E7%89%99%E6%8E%A7%E5%88%B6-arduino-led-%E4%BA%AE%E6%BB%85/
// ref: https://wingoodharry.wordpress.com/2014/03/15/simple-android-to-arduino-via-bluetooth-serial-part-2/

#include <SoftwareSerial.h>
#include <Wire.h>

const int RX_PIN = 10; // 10
const int TX_PIN = 11; // 11
SoftwareSerial I2CBT(RX_PIN, TX_PIN);

const int ANALOG_PIN = A2;     // potentiometer wiper (middle terminal) connected to analog pin 0
                              // outside leads to ground and +5V
int val = 0;                  // variable to store the value read

void setup()

{
  Serial.begin(9600);          //  setup serial
  I2CBT.begin(38400); // bluetooth baud rate
}


unsigned long time;

void loop()
{
    val = analogRead(ANALOG_PIN);    // read the input pin

    time=micros();
    //Serial.print("Current time: ");
    //Serial.println(time);
  //  Current time: 5336596 - 5309556 = 27040
  //  1000 * (1 / 27040) = 37hz
    byte pack[2];
    pack[0] = val/128;
    pack[1] = val%128;
    String str = (String)val;
    I2CBT.write(pack[0]);
    I2CBT.write(pack[1]);
    //Serial.println(pack[0]);
    //Serial.println(pack[1]);
    Serial.println(val);
}

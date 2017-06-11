#include <hx711.h>
#include <MAX30100_PulseOximeter.h>
#include <Arduino.h>
#include <math.h>
#include <Wire.h>
#include <SoftwareSerial.h>


///////////////////
//some parameters//
///////////////////
//to send
float weight = 0;
float BPM = 0;
float SaO2 = 0;
float temp = 0;
//HX711
Hx711 scale(A1, A0);
float offset = 0;
float ratio = 20651.94;
int av_times = 5;
int thres = 1;
int de = av_times - thres - 1;
//MAX30100
PulseOximeter pox;
uint32_t tsLastReport = 0;
#define REPORTING_PERIOD_MS     1000
//BT
const int RX_PIN = 10;
const int TX_PIN = 11;
SoftwareSerial I2CBT(RX_PIN, TX_PIN);
const int ANALOG_PIN = A2;
int val = 0;
unsigned long time;
//WIFI
#define _baudrate   115200
#define _rxpin      4
#define _txpin      5
SoftwareSerial debug( _rxpin, _txpin ); // RX, TX
//*-- IoT Information
#define SSID "Mike"
#define PASS "299792458"
#define IP "184.106.153.149" // ThingSpeak IP Address: 184.106.153.149
// GET /update?key=[THINGSPEAK_KEY]&field1=[data 1]&filed2=[data 2]...;
String GET = "GET /update?key=CR17G8H34ZQKXJMN";


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
  
  //set up bt
  I2CBT.begin(38400);
  
  //set up wifi
  debug.begin( _baudrate );
  //sendDebug("AT");
  //Loding("sent AT");
  connectWiFi();


/////////////
//Measuring//
/////////////
  //get weight
  Serial.print("Weighting...");
  weight = (scale.averageValue() - offset) / ratio;
  weight += (scale.averageValue() - offset) / ratio;
  weight /= 2;
  Serial.println("SUCCESS");

  //get max30100 info
  BPM = 0;
  SaO2 = 0;
  temp = 0;
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
//wifi
  Serial.print("WIFI Uploading...");
  while(!SentOnCloud(String(weight), String(BPM), String(SaO2))){delay(100);}
  Serial.println("SUCCESS");
}


void loop() {
  // put your main code here, to run repeatedly:
  val = analogRead(ANALOG_PIN);
  time = micros();
  byte pack[2];
  pack[0] = val / 128;
  pack[1] = val % 128;
  String str = (String)val;
  I2CBT.write(pack[0]);
  I2CBT.write(pack[1]);
  Serial.println(val);
}


////////////////
//all for wifi//
////////////////
boolean connectWiFi()
{
    debug.println("AT+CWMODE=1");
    Wifi_connect();
}

bool SentOnCloud( String T, String H, String W )
{

    bool ret = false;
    String cmd = "AT+CIPSTART=\"TCP\",\"";
    cmd += IP;
    cmd += "\",80";
    sendDebug(cmd);
    if( debug.find( "Error" ) )
    {
        Serial.print( "RECEIVED: Error\nExit1" );
        return;
    }
    cmd = GET + "&field1=" + T + "&field2=" + H + "&field3=" + W + "\r\n";
    debug.print( "AT+CIPSEND=" );
    debug.println( cmd.length() );
    if(debug.find( ">" ) )
    {
        //Serial.print(">");
        //Serial.print(cmd);
        debug.print(cmd);
        ret = true;
    }
    else
    {
        debug.print( "AT+CIPCLOSE" );
    }
    return ret;
}
void Wifi_connect()
{
    String cmd="AT+CWJAP=\"";
    cmd+=SSID;
    cmd+="\",\"";
    cmd+=PASS;
    cmd+="\"";
    sendDebug(cmd);
    Loding("Wifi_connect");
}
void Loding(String state){
    for (int timeout=0 ; timeout<10 ; timeout++)
    {
      if(debug.find("OK"))
      {
          Serial.println("RECEIVED: OK");
          break;
      }
      else if(timeout==9){
        Serial.print( state );
        Serial.println(" fail...\nExit2");
      }
      else
      {
        Serial.print("Wifi Loading...");
        delay(750);
      }
    }
}
void sendDebug(String cmd)
{
    Serial.print("SEND: ");
    Serial.println(cmd);
    debug.println(cmd);
} 

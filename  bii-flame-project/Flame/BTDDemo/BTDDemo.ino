#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

// This is for Ardunino Mega ADK
#define O0 11

#define O1 10
#define O2 9
#define D4 4

AndroidAccessory acc("Google, Inc.",
                                   "BeyondTheDesktop",
                                    "BeyondTheDesktop",
                                    "1.0",
                                     "http://www.android.com",
                                      "0000000012345678");

boolean lightsOn = false;

long timestamp = 0;

void setup()
{
        Serial.begin(115200);
        Serial.print("\r\nStart");
        digitalWrite(O0,LOW);
        digitalWrite(O1,LOW);
        digitalWrite(O2,LOW);
        
        pinMode(D4,INPUT);
        delay(1000);
        acc.powerOn();
}

void loop()
{
           byte msg[1];
           if (acc.isConnected()) {
             int len = acc.read(msg, sizeof(msg), 1);
             if (len > 0) {
            Serial.print("Received message: "); Serial.println(msg[0],DEC);	
                        
            if (msg[0] == 13) {
              Serial.println("I got a 13!");
                          
              if (lightsOn == false) {
                digitalWrite(O1,HIGH);
                lightsOn = true;
              } else {
                digitalWrite(O1,LOW);
                lightsOn = false;
              }
                          
            }		
	  }	
          if ( digitalRead(D4) == HIGH & millis()-timestamp>500 ) {
             msg[0] = (byte) 66;
             acc.write(msg, 1);
             Serial.println("Button was pressed: "); Serial.println(msg[0],DEC);
             timestamp = millis();
           }      
       }
       delay(50);
}


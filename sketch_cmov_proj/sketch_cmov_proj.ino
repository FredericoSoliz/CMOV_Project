#include <Servo.h>

const int sensorPin = A0;
const int ledPin = 8;
const int servoPin = 9;

Servo servo;
bool rega = false;

void setup() {
  Serial.begin(9600);

  pinMode(ledPin, OUTPUT);

  servo.attach(servoPin);
  servo.write(0); // closed valve
}

unsigned long lastHumiditySend = 0;
const unsigned long HUM_INTERVAL = 10000; 

void loop() {

  // sensor
  if (millis() - lastHumiditySend >= HUM_INTERVAL) {
    lastHumiditySend = millis();

    int hum = analogRead(sensorPin);

    Serial.print("HUM=");
    Serial.println(hum);

    // LED 
    if (hum < 200) {
      digitalWrite(ledPin, HIGH);         
    } 
    else if (hum < 400) {
      digitalWrite(ledPin, HIGH);
      delay(150);
      digitalWrite(ledPin, LOW);
    } 
    else {
      digitalWrite(ledPin, LOW);          
    }
  }

  // zigbee
  if (Serial.available()) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd.startsWith("ON")) {
      rega = true;
      Serial.println("REGA=ON");
    }

    if (cmd.startsWith("OFF")) {
      rega = false;
      Serial.println("REGA=OFF");
    }
  }

  // servo
  servo.write(rega ? 90 : 0);
}

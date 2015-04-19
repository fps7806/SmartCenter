#include "LPD8806.h"
#include "SPI.h"

#define nLEDs  180
int dataPin = 2;
int clockPin = 3;
int LEDColors[nLEDs];
int sensorPin = A0;
LPD8806 strip = LPD8806(nLEDs, dataPin, clockPin);
uint32_t Wheel(uint16_t WheelPos, float scale=1.0f);
void doWaves(float scale);

void setup() {
  memset(LEDColors, 0, sizeof(LEDColors));
  // Start up the LED strip
  strip.begin();
  // Update the strip, to start they are all 'off'
  strip.show();
  
  Serial.begin(9600);
  doRandomRainbow(0);
}

float brightness = 1;
float t = 0;
bool move = false;
bool autoLight = true;
void loop() {
    int sensorValue = analogRead(sensorPin); 
    Serial.println(sensorValue, DEC);
    brightness = min((sensorValue-149.0f)/(670.0f-149.0f),1.0f);
    doWaves(brightness);
    static long startTime = millis();
    long elapsedTime = startTime-millis();
    startTime = millis();
    t += 0.001f*elapsedTime;
}

void doRandomRainbow(float scale)
{
  LEDColors[0] = random();
  for(int i=nLEDs-1;i>0;--i)
  {
    LEDColors[i] = LEDColors[(i- 1)%nLEDs];
    strip.setPixelColor(i, Wheel(LEDColors[i], scale));
  }
  strip.show();
}

void doWaves(float scale)
{
  double s2t = sin(t*2);
  double c2t = cos(t*2);
  double ct = cos(t);
  double st = sin(t);
  
  for(int i=nLEDs-1;i>0;--i)
  {
    double c = cos(i*0.1f+t);
    double s = sin(1*0.1f+t);
    byte r = (s2t*s+1)/2*128;
    byte g = (c2t*c+1)/2*128;
    byte b = (s2t*c+1)/2*128;
    strip.setPixelColor(i, strip.Color(r*scale,g*scale,b*scale));
  }
  strip.show();
}

void SetPixelColor(int i, uint32_t color)
{
  if(LEDColors[i] != color)
  {
    LEDColors[i] = color;
    strip.setPixelColor(i, LEDColors[i]);
  }
}

uint32_t ScaleColor(uint32_t color, float scale)
{
  byte r = color >> 24;
  byte g = (color & 0xFF0000) >> 16;
  byte b = (color & 0xFF00) >> 8;
  return(strip.Color(r*scale,g*scale,b*scale));
}

uint32_t Wheel(uint16_t WheelPos, float scale)
{
  byte r=0, g=0, b=0;
  switch(WheelPos % 4)
  {
    case 0:
    r = 127;
    break; 
    case 1:
    g = 127;
    break;
    case 2:
    b = 127;
    break;
    case 3:
    r = g = b = 127;
    break;
  };
  return(strip.Color(r*scale,g*scale,b*scale));
  switch(WheelPos / 128)
  {

    case 0:
      r = 127 - WheelPos % 128;   //Red down
      g = WheelPos % 128;      // Green up
      b = 0;                  //blue off
      break; 
    case 1:
      g = 127 - WheelPos % 128;  //green down
      b = WheelPos % 128;      //blue up
      r = 0;                  //red off
      break; 
    case 2:
      b = 127 - WheelPos % 128;  //blue down 
      r = WheelPos % 128;      //red up
      g = 0;                  //green off
      break; 
  }
  return(strip.Color(r*scale,g*scale,b*scale));
}


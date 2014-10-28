/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//#define DEBUG

#include <EEPROM.h>
#include <SPI.h>
#include <Wire.h>
#include "commands.h"
#include "I2Cdev.h"
#include "MPU6050_6Axis_MotionApps20.h"
#include "MPU6050.h"
#include <util/atomic.h>

#define BOT_NAME "BOTKAA1"

// = (16*10^6) / (1000*1024) - 1 (must be <65536) (1 kHz)
#define TIMER1_CMP_MATCH 16
// CS10 and CS12 bits for 1024 prescaler
#define TIMER1_PRESCALER ((1 << CS12) | (1 << CS10))

#define LED_PIN 13

#define SONAR_TRIGGER_PIN  12
#define SONAR_ECHO_PIN     13
// speed = 340 m/s = 1 / 29.412 ms/cm
#define SONAR_SOUND_VELOCITY  29.412
#define SONAR_TIMEOUT      15000
#define SONAR_MAX_DISTANCE 254.0

/**
 * Motor1 control
 * EnA   IN 1   IN 2   Status
 * 1       1        0   Forward
 * 1       0        1   Reverse
 * 1       0        0   Brake
 * 0       X        X   Stop
 */
#define MOTOR1_DIRECTION_PIN1 2
#define MOTOR1_DIRECTION_PIN2 4
#define MOTOR1_SPEED_PIN 3

#define MOTOR2_DIRECTION_PIN1 7
#define MOTOR2_DIRECTION_PIN2 8
#define MOTOR2_SPEED_PIN 11

#define RIGHT_TURN_SPEED_MOTOR_LEFT 150
#define RIGHT_TURN_SPEED_MOTOR_RIGHT 150
#define LEFT_TURN_SPEED_MOTOR_LEFT 150
#define LEFT_TURN_SPEED_MOTOR_RIGHT 150
//#define RIGHT_TURN_ANGLE 83.5
//#define LEFT_TURN_ANGLE  86.0
#define RIGHT_TURN_ANGLE 88.0
#define LEFT_TURN_ANGLE  88.0

#define MOVE_FORWARD_TIMER_COUNTER 2500
#define MOVE_BACKWARD_TIMER_COUNTER 2500
#define MOVE_FORWARD_RIGHT_SPEED 150
#define MOVE_FORWARD_LEFT_SPEED 125
#define MOVE_BACKWARD_RIGHT_SPEED 150
#define MOVE_BACKWARD_LEFT_SPEED 125

#define MAX_DISTANCE_TO_WALL  5.0
#define MIN_DISTANCE_TO_WALL  4.0
#define DISTANCE_TO_WALL_IN_CELL 20.0

volatile char command_in_progress = COMMAND_NONE;
volatile boolean is_moving = false;
volatile int move_counter = 0;
volatile int gyro_counter = 0;

MPU6050 mpu;
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount;     // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q;           // [w, x, y, z]         quaternion container
VectorFloat gravity;
float ypr[3];           // [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector

float mpu_yaw = 0.0;

void send_command(commands_t cmd) {
  Serial.print((char) cmd);
}

void init_accelgyro() {
  mpu.initialize();
  mpu.setXGyroOffset(220);
  mpu.setYGyroOffset(76);
  mpu.setZGyroOffset(-85);
  mpu.setZAccelOffset(1788);
  mpu.dmpInitialize();
  mpu.setDMPEnabled(true);
  packetSize = mpu.dmpGetFIFOPacketSize();
}

void init_motors() {
  pinMode(MOTOR1_DIRECTION_PIN1, OUTPUT);
  pinMode(MOTOR1_DIRECTION_PIN2, OUTPUT);
  pinMode(MOTOR1_SPEED_PIN, OUTPUT);
  pinMode(MOTOR2_DIRECTION_PIN1, OUTPUT);
  pinMode(MOTOR2_DIRECTION_PIN2, OUTPUT);
  pinMode(MOTOR2_SPEED_PIN, OUTPUT);
}

void init_sonar() {
  pinMode(SONAR_ECHO_PIN, INPUT);
  pinMode(SONAR_TRIGGER_PIN, OUTPUT);
  digitalWrite(SONAR_TRIGGER_PIN, LOW);
}

void init_timer1() {
  TCCR1A = 0; // set entire TCCR1A register to 0
  TCCR1B = 0; // same for TCCR1B
  TCNT1  = 0; //initialize counter value to 0

  OCR1A = TIMER1_CMP_MATCH; // setting counter
  TCCR1B |= (1 << WGM12); // turn on CTC mode
  TCCR1B |= TIMER1_PRESCALER; // setting prescaler
  TIMSK1 |= (1 << OCIE1A); // enable timer compare interrupt
}

void on_timer1() {
  if (is_moving) {
    ++move_counter;
  }
  ++gyro_counter;
}

void on_command_done() {
  command_in_progress = COMMAND_NONE;
}

float get_mpu_result() {
  int counter;
  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    counter = gyro_counter;
  }
  if (counter >= 10) {
    mpu.resetFIFO();
    fifoCount = 0;
    while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();
    mpu.getFIFOBytes(fifoBuffer, packetSize);
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &q);
    mpu.dmpGetYawPitchRoll(ypr, &q, &gravity);

    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      gyro_counter = 0;
    }
  }
  return ypr[0] * 180/M_PI;
}

float get_ping_result() {
  // starting measurement
  digitalWrite(SONAR_TRIGGER_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(SONAR_TRIGGER_PIN, LOW);
  // making a pause to let the sensor receive a signal
  delayMicroseconds(500);

  long start = micros();
  // waiting when the signal on echo pin become low
  while (digitalRead(SONAR_ECHO_PIN) == HIGH) {
    // timeout case
    if(micros() - start > SONAR_TIMEOUT) {
      break;
    }
  }

  long duration = micros() - start; // duration of measurment

  return ((float) duration) / SONAR_SOUND_VELOCITY / 2; // converting into centimeters
}

void cmd_ping_wall() {
  float result = get_ping_result();
  send_command(COMMAND_PING);
  Serial.print(result);
  on_command_done();
}

void cmd_get_accelgyro_result() {
  send_command(COMMAND_GET_MPU);
  Serial.print(mpu_yaw);
  on_command_done();
}

void correct_distance_to_wall(byte left_speed, byte right_speed) {
  delay(500);
  float ping_result = get_ping_result();
#ifdef DEBUG
  Serial.print("Start distance: ");
  Serial.println(ping_result);
#endif
  while ((ping_result > MAX_DISTANCE_TO_WALL || ping_result < MIN_DISTANCE_TO_WALL) && (ping_result < DISTANCE_TO_WALL_IN_CELL || ping_result > SONAR_MAX_DISTANCE)) {
#ifdef DEBUG
    Serial.print("Distance before correction: ");
    Serial.println(ping_result);
#endif
    boolean forward = (ping_result > MAX_DISTANCE_TO_WALL && ping_result < SONAR_MAX_DISTANCE);
    digitalWrite(MOTOR1_DIRECTION_PIN1, forward ? LOW : HIGH);
    digitalWrite(MOTOR1_DIRECTION_PIN2, forward ? HIGH : LOW);
    digitalWrite(MOTOR2_DIRECTION_PIN1, forward ? LOW : HIGH);
    digitalWrite(MOTOR2_DIRECTION_PIN2, forward ? HIGH : LOW);

    for (; (forward ? (ping_result > MAX_DISTANCE_TO_WALL) : (ping_result < MIN_DISTANCE_TO_WALL || ping_result > SONAR_MAX_DISTANCE)); ping_result = get_ping_result()) {
      analogWrite(MOTOR2_SPEED_PIN, left_speed); // left motor
      analogWrite(MOTOR1_SPEED_PIN, right_speed); // right motor
      delay(30);
      analogWrite(MOTOR1_SPEED_PIN, 0); // right motor
      analogWrite(MOTOR2_SPEED_PIN, 0); // left motor
      delay(30);
    }
//    ping_result = get_ping_result();

#ifdef DEBUG
    Serial.print("Distance after correction: ");
    Serial.println(ping_result);
#endif
  }
}

void move_to_next_cell(boolean forward, boolean calibration) {
  if (!is_moving) {
    is_moving = true;
    digitalWrite(MOTOR1_DIRECTION_PIN1, forward ? LOW : HIGH);
    digitalWrite(MOTOR1_DIRECTION_PIN2, forward ? HIGH : LOW);
    digitalWrite(MOTOR2_DIRECTION_PIN1, forward ? LOW : HIGH);
    digitalWrite(MOTOR2_DIRECTION_PIN2, forward ? HIGH : LOW);

    analogWrite(MOTOR1_SPEED_PIN, forward ? MOVE_FORWARD_RIGHT_SPEED : MOVE_BACKWARD_RIGHT_SPEED); // right motor
    analogWrite(MOTOR2_SPEED_PIN, forward ? MOVE_FORWARD_LEFT_SPEED : MOVE_BACKWARD_LEFT_SPEED); // left motor
  } else {
    int counter = 0;
    ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
    {
      counter = move_counter;
    }
    if (counter >= (forward ? MOVE_FORWARD_TIMER_COUNTER : MOVE_BACKWARD_TIMER_COUNTER)) {
      is_moving = false;
      move_counter = 0;
      analogWrite(MOTOR1_SPEED_PIN, 0); // right motor
      analogWrite(MOTOR2_SPEED_PIN, 0); // left motor
      delay(100);

      if (calibration) {
        correct_distance_to_wall(forward ? MOVE_FORWARD_LEFT_SPEED - 20 : MOVE_BACKWARD_LEFT_SPEED - 20, forward ? MOVE_FORWARD_RIGHT_SPEED - 20 : MOVE_BACKWARD_RIGHT_SPEED - 20);
      }

      send_command(forward ? COMMAND_MOVE_FORWARD_WO_CB : COMMAND_MOVE_BACKWARD);
      on_command_done();
    }
  }
}

void cmd_move_forward(boolean calibration) {
  move_to_next_cell(true, calibration);
}

void cmd_move_backward() {
  move_to_next_cell(false, false);
}

float get_diff_angles(float start_angle, float fin_angle) {
  if ((fin_angle < 0.0 && start_angle > 0.0) || (fin_angle > 0.0 && start_angle < 0.0)) {
    if (abs(fin_angle) > 90) {
      return abs(abs(start_angle) - (360.0 - abs(fin_angle)));
    } else {
      return abs(start_angle) + abs(fin_angle);
    }
  } else {
    return abs(abs(start_angle) - abs(fin_angle));
  }
}

void turn(boolean left, boolean calibration) {
  float diff_angle = 0.0;
  float start_angle = get_mpu_result();

  digitalWrite(MOTOR1_DIRECTION_PIN1, left ? LOW : HIGH);
  digitalWrite(MOTOR1_DIRECTION_PIN2, left ? HIGH : LOW);
  digitalWrite(MOTOR2_DIRECTION_PIN1, left ? HIGH : LOW);
  digitalWrite(MOTOR2_DIRECTION_PIN2, left ? LOW : HIGH);

  analogWrite(MOTOR1_SPEED_PIN, 110); // right motor
  analogWrite(MOTOR2_SPEED_PIN, 110); // left motor

  while (diff_angle < (left ? LEFT_TURN_ANGLE : RIGHT_TURN_ANGLE)) {
    diff_angle = get_diff_angles(start_angle, get_mpu_result());
  }

  analogWrite(MOTOR1_SPEED_PIN, 0);
  analogWrite(MOTOR2_SPEED_PIN, 0);

  if (calibration) {
    correct_distance_to_wall(MOVE_FORWARD_LEFT_SPEED - 20, MOVE_FORWARD_RIGHT_SPEED - 20);
  }
  send_command(left ? COMMAND_TURN_LEFT_WO_CB : COMMAND_TURN_RIGHT_WO_CB);
  on_command_done();
}

void cmd_turn_right(boolean calibration) {
  turn(false, calibration);
}

void cmd_turn_left(boolean calibration) {
  turn(true, calibration);
}

void setup() {
  Serial.begin(9600);
  Serial.print("AT+NAME"BOT_NAME);
  pinMode(LED_PIN, OUTPUT);

  Wire.begin();

  init_motors();
  init_accelgyro();
  init_sonar();

  cli(); // disable interrupts

  init_timer1();

  sei(); // allow interrupts

#ifdef DEBUG
  Serial.println("Ready");
#endif
}

ISR(TIMER1_COMPA_vect) {
  on_timer1();
}

void loop_serial() {
  if (Serial.available()) {
    int read_result = Serial.read();
    if (read_result == COMMAND_KEEP_ALIVE) {
      send_command(COMMAND_KEEP_ALIVE);
    } else if (!command_in_progress) {
      command_in_progress = (char) read_result;
    } else {
      send_command(COMMAND_BUSY);
    }
  }
}

void loop_mpu() {
  mpu_yaw = get_mpu_result();
}

void loop() {
  loop_serial();
  switch (command_in_progress) {
    case COMMAND_NONE:
      break;
    case COMMAND_PING:
      cmd_ping_wall();
      break;
    case COMMAND_MOVE_FORWARD_WO_CB:
      cmd_move_forward(false);
      break;
    case COMMAND_MOVE_FORWARD:
      cmd_move_forward(true);
      break;
    case COMMAND_MOVE_BACKWARD:
      cmd_move_backward();
      break;
    case COMMAND_TURN_RIGHT_WO_CB:
      cmd_turn_right(false);
      break;
    case COMMAND_TURN_RIGHT:
      cmd_turn_right(true);
      break;
    case COMMAND_TURN_LEFT_WO_CB:
      cmd_turn_left(false);
      break;
    case COMMAND_TURN_LEFT:
      cmd_turn_left(true);
      break;
    case COMMAND_GET_MPU:
      cmd_get_accelgyro_result();
      break;
    default:
      send_command(COMMAND_INVALID);
      on_command_done();
      break;
  }
  loop_mpu();

}

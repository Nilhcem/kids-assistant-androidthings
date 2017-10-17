#include <SPI.h>
#include <MFRC522.h>

#define RST_PIN 9
#define SDA_PIN 10

MFRC522 mfrc522;

void setup() {
  Serial.begin(9600);
  while (!Serial);

  SPI.begin();
  mfrc522.PCD_Init(SDA_PIN, RST_PIN);
}

void loop() {
  if (mfrc522.PICC_IsNewCardPresent() && mfrc522.PICC_ReadCardSerial()) {
    writeUidToSerial(mfrc522.uid.uidByte, mfrc522.uid.size);
    mfrc522.PICC_HaltA();
  }
}

void writeUidToSerial(byte *uid, byte size) {
  for (byte i = 0; i < size; i++) {
    if (uid[i] < 0x10) {
      Serial.print("0");
    }
    Serial.print(uid[i], HEX);
  }
  Serial.println();
}

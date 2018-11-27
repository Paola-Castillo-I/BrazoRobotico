#include <EEPROM.h>                   //librería externa para poder controlar la memoria eeprom
#include <Servo.h>                    //librería externa para poder controlar el servo desde Arduino
#include <LiquidCrystal_I2C.h>        //librería externa para poder controlar la pantalla LCD desde Arduino

LiquidCrystal_I2C lcd(0x3F, 16, 2);   //establece la dirección de la pantalla LCD en 0x27 para una pantalla de 16 caracteres y 2 líneas
String instruccion = ""; //Variable que contendra la instruccion recibida de java
Servo pinza, codo, hombro; //Variables para controlar los 3 servos
int retardo = 5;        // Tiempo de retardo en milisegundos (Velocidad del Motor)
int dato_rx;            // valor recibido en grados
int numero_pasos = 0;   // Valor en grados donde se encuentra el motor
#define btnParo 2       // Boton Paro de emergencia
boolean paro = false;   // Variable boolean que permitirá identificar si el boton de paro fue accionado, true para detener el movimiento del brazo y false para reanudarlo

void setup() {
  Serial.begin(9600);    //Comienza comunicación serial
  pinMode(6, OUTPUT);    //Salida del motor a pasos
  pinMode(5, OUTPUT);    //Salida del motor a pasos
  pinMode(9, OUTPUT);    //Salida del motor a pasos
  pinMode(8, OUTPUT);    //Salida del motor a pasos
  lcd.init();            //Inicializa la pantalla LCD
  lcd.clear();           //Limpieza de pantalla LCD  
  lcd.backlight();       //Acrivación de luz de fonfo en LCD
  pinza.attach(7);       //Se inicia el servo de la pinza para que empiece a trabajar con el pin 7
  codo.attach(4);        //Se inicia el servo del codo para que empiece a trabajar con el pin 4
  hombro.attach(3);      //Se inicia el servo del hombro para que empiece a trabajar con el pin 3
  if (EEPROM.read(0) == 0)  //Se lee lo que hay en la posición 0 de la memoria eeprom, si el valor es igual a 0 se encauentra en modo programador
    colocarPosicionInicial(); //Llama al método "colocarPosicionInicial()" el cual asignará una posición inicial a los servos
  attachInterrupt(digitalPinToInterrupt(btnParo), interrupcionParo, RISING); //se prepara y/o define el tipo de interrupción a usar
}

void colocarPosicionInicial() { //Método que asignará una posición inicial a los servos
  pinza.write(10); //El servo de la pinza a 10 grados
  hombro.write(90);//El servo del hombro a 90 grados
  codo.write(0);   //El servo del codo a 0 grados
}

void loop() {   //función ciclica
  if (EEPROM.read(0) == 0) { //Se lee lo que hay en la posición 0 de la memoria eeprom, si el valor es igual a 0, comienza el modo programador
    
    while (Serial.available() > 0)  //Si la cantidad de bytes (caracteres) disponibles para leer desde el puerto serie es mayor a 0
      instruccion = instruccion + Decimal_to_ASCII(Serial.read()); //concatena en instruccion, instruccion + la conversión ascii de los datos de serie entrantes.
      
    if (instruccion != "")  //Si la variable instruccion contiene algo
      identificarComando(); //Llama al método encargado de identificar a que servo o motor mover
      
    while (dato_rx > numero_pasos) { //Giro hacia la izquierda en grados, mientras dato_rx sea mayor que numero_pasos
      paso_izq();                    //Llama al método encargado de girar el motor a pasos hacia la izquierda
      numero_pasos = numero_pasos + 1;//Incrementa a la variable "numero_pasos" en uno
    }
    
    while (dato_rx < numero_pasos) { //Giro hacia la derecha en grados, mientras dato_rx sea menor que numero_pasos
      paso_der();                    //Llama al método encargado de girar el motor a pasos hacia la derecha
      numero_pasos = numero_pasos - 1;//Decrementa a la variable "numero_pasos" en uno
    }
    
    instruccion = "";  //Limpia el contenido de la variable "instruccion"
    apagado();         // Apagado del Motor para que no se caliente
    delay(1000);       //Se espera un segundo
    
  }
  else  //Si el valor de la posición 0 de la memoria eeprom no es 0, comienza el modo ejecucion
    ejecutarComandos();//Llama al método ejecutarComandos encargado de mover el brazo
}

void giraBase() {
  while (dato_rx > numero_pasos) { //Giro hacia la izquierda en grados, mientras dato_rx sea mayor que numero_pasos
    paso_izq();                    //Llama al método encargado de girar el motor a pasos hacia la izquierda
    numero_pasos = numero_pasos + 1;//Incrementa a la variable "numero_pasos" en uno
  }
  while (dato_rx < numero_pasos) { //Giro hacia la derecha en grados, mientras dato_rx sea menor que numero_pasos
    paso_der();                    //Llama al método encargado de girar el motor a pasos hacia la derecha
    numero_pasos = numero_pasos - 1;//Decrementa a la variable "numero_pasos" en uno
  }
  apagado();          // Apagado del Motor para que no se caliente
  delay(3000);        //Se espera tres segundos
}

void interrupcionParo() {//Método de interrupción encargado de detener o reanudar la ejecución del brazo
  if (paro == false)     //Si la variable boolean "paro" es igual a false
    paro = true;         //Asigna el valor true a la variable paro, lo que significa que detendrá la ejecución o movimiento del brazo
  else                   //Si la variable boolean no es igual a false
    paro = false;        //Asigna el valor false a la variable paro, lo que significa que reanudará la ejecución o movimiento del brazo
}

void identificarComando() {
  String comando = instruccion.substring(0, 1); //Asigna a la variable "comando" el primer caracter contenido en la variable "instruccion" la cual indentificara a que servo o motor mover
  String grados = instruccion.substring(2, instruccion.length()); //Asigna a la variable "grados" el resto del contenido de la variable "instruccion" lo cual son los grados a tratar en el servo o motor

  if (comando.equals("p"))        //Si el contenido de la variable "comando" es igual a "p" significa que el servo a tratar es el de las pinzas
    trataPinza(grados.toInt());   //Llama al método "trataPinza" mandando como parametro los grados

  else if (comando.equals("c"))   //Si el contenido de la variable "comando" es igual a "c" significa que el servo a tratar es el del codo
    trataCodo(grados.toInt());    //Llama al método "trataCodo" mandando como parametro los grados

  else if (comando.equals("h"))   //Si el contenido de la variable "comando" es igual a "h" significa que el servo a tratar es el del hombro
    trataHombro(grados.toInt());  //Llama al método "trataHombro" mandando como parametro los grados

  else if (comando.equals("b"))   //Si el contenido de la variable "comando" es igual a "b" significa que se debe tratar el motor a pasos (la base)
    trataBase(grados.toInt());    //Llama al método "trataBase" mandando como parametro los grados

  else if (comando.equals("m"))   //Si el contenido de la variable "comando" es igual a "m" significa que guardará en la memoria eeprom en que modo se ejecutara el programa
    trataModo(grados.toInt());    //Llama al método "trataModo" mandando como parametro los grados

  else if (comando.equals("g"))   //Si el contenido de la variable "comando" es igual a "g" significa que guardará en la memoria eeprom los grados 
    guardarPasos(grados);         //Llama al método "guardarPasos" mandando como parametro los grados
}


void trataModo(int grados) { //Método que guardará en la memoria eeprom en que modo se ejecutara el programa
  EEPROM.write(0, grados);   //Escribe en la posición 0 de la memoria eeprom lo que contiene la variable "grados"
}

void trataPinza(int grados) {//Método encargado de tratar el movimiento del servo de las pinzas
  lcd.clear();               //Limpia el contenido de la lcd
  if (grados == 1)           //Si el contenido de la variable "grados" es igual a 1
    lcd.print("Pinza Abierta");//Escribe en la pantalla lcd "Pinza Abierta"
  else                         //Si el contenido de la variable "grados" no es igual a 1
    lcd.print("Pinza Cerrada");//Escribe en la pantalla lcd "Pinza Cerrada"
  pinza.write(grados);         //Mueve el servo de la pinza a los grados señalados en la variable "grados"
  delay(3000);                 //Se espera tres segundos
}

void trataCodo(int grados) {//Método encargado de tratar el movimiento del servo del codo
  lcd.clear();              //Limpia el contenido de la lcd
  lcd.print("Codo a: " + String(grados) + " g");//Escribe en la pantalla lcd "Codo a: "grados" g"
  codo.write(grados);       //Mueve el servo del codo a los grados señalados en la variable "grados"
  delay(3000);              //Se espera tres segundos
}

void trataHombro(int grados) {//Método encargado de tratar el movimiento del servo del hombro
  lcd.clear();                //Limpia el contenido de la lcd
  lcd.print("Hombro a: " + String(grados) + " g");//Escribe en la pantalla lcd "Hombro a: "grados" g"
  hombro.write(grados);       //Mueve el servo del hombro a los grados señalados en la variable "grados"
  delay(3000);                //Se espera tres segundos
}


void trataBase(int grados) {//Método encargado de tratar el movimiento del motor a pasos de la base
  lcd.clear();              //Limpia el contenido de la lcd
  lcd.print("Base a: " + String(grados) + " g");//Escribe en la pantalla lcd "Base a: "grados" g"
  dato_rx = (grados * 1.4222222222);            //Grados a girar en el motor a pasos
}

void ejecutarComandos() {        //Método encargado de mover el brazo de acuerdo a lo señalado en la memoria eeprom
  int apuntador = EEPROM.read(1);//Asigna a la variable "apuntador" lo que contiene la memoria eeprom en la posición 1 que señala los grados a mover en los servos o motor a pasos
  int motor = EEPROM.read(2);    //Asigna a la variable "motor" lo que contiene la memoria eeprom en la posición 2 que señala que motor se debe mover
  do {
    if (paro == true)                      //Si el contenido de la variable "paro" es igual a true
      break;                               //Rompe el ciclo, por lo cual el brazo deja de moverse 
    else {                                 //Si el contenido de la variable "paro" no es igual a true comienza a mover el brazo
      if (motor == 0) {                    //Si el contenido de la variable "motor" es igual a 0, significa que el servo a tratar es el de las pinzas
        trataPinza(EEPROM.read(apuntador));//Llama al método "trataPinza" mandando como parametro los grados obtenidos de la memoria eeprom en la posición señalada en la variable "apuntador"
        motor++;                           //Aumenta la variable "motor" en uno, para avanzar al siguiente servo
      }
      else if (motor == 1) {               //Si el contenido de la variable "motor" es igual a 1, significa que el servo a tratar es el del codo
        trataCodo(EEPROM.read(apuntador)); //Llama al método "trataCodo" mandando como parametro los grados obtenidos de la memoria eeprom en la posición señalada en la variable "apuntador"
        motor++;                           //Aumenta la variable motor en uno, para avanzar al siguiente servo
      }
      else if (motor == 2) {               //Si el contenido de la variable "motor" es igual a 2, significa que el servo a tratar es el del hombro
        trataHombro(EEPROM.read(apuntador));//Llama al método "trataHombro" mandando como parametro los grados obtenidos de la memoria eeprom en la posición señalada en la variable "apuntador"
        motor++;                           //Aumenta la variable "motor" en uno, para avanzar al siguiente servo
      }
      else if (motor == 3) {               //Si el contenido de la variable "motor" es igual a 3, significa que el motor a tratar es el de la base
        trataBase(EEPROM.read(apuntador)); //Llama al método "trataBase" mandando como parametro los grados obtenidos de la memoria eeprom en la posición señalada en la variable "apuntador"
        giraBase();                        //Llama al método "giraBase"
        motor = 0;                         //Asigna a la variable "motor" 0, para comenzar otra vez la rutina
      }
      EEPROM.write(2, motor);              //Escribe en la memoria eeprom en la posición 2 el contenido de la variable "motor"
      apuntador++;                         //Aumenta en uno la variable "apuntador" en uno
      EEPROM.write(1, apuntador);          //Escribe en la memoria eeprom en la posición 1 el contenido de la variable "apuntador"
      if (EEPROM.read(apuntador) == 255)   //Si el contenido en la memoria eeprom en la posición maracada en variable "apuntador" es igual a 255
        break;                             //Rompe el ciclo infinito, debido a que ya no hay mas instrucciones para mover el brazo
    }
  }
  while (true);                            //Ciclo infinito
  if (paro == false)                       //Si la variable boolean "paro" es igual a false, significa que aun esta en modo de ejecución
    limpiaMemoria();                       //Llama al método "limpiaMemoria" encargado de borrar las intrucciones guardadas en la memoria eeprom una vez terminada la rutina
}

void limpiaMemoria() {//Método encargado de borrar las intrucciones guardadas en la memoria eeprom una vez terminada la rutina
  int i = 3;          //Asigna a la variable "i" el valor de 3
  EEPROM.write(0, 0); //Escribe en la posición 0 de la memoria eeprom el valor de 0 para señalar que entrara a modo programador
  EEPROM.write(1, 3); //Escribe en la posición 1 de la memoria eeprom el valor de 3, recordando que esta posición de la memoria eeprom es desiganada para la variable "apuntador" la cual apunta a los grados para los sercos o motor
  EEPROM.write(2, 0); //Escribe en la posición 2 de la memoria eeprom el valor de 0, para señalar el servo de las pinzas
  do {                //Leera el contenido de la memoria eeprom en la posición "i" (3) para borrar los grados marcados
    if (EEPROM.read(i) > 0 && EEPROM.read(i) < 255) //Si el contenido en la posición señalada en la memoria eeprom es mayor a 0 y menor a 255
      EEPROM.write(i, 255);                         //Escribira en la posición señalada un 255
    else                                            //Si el contenido en la posición señalada en la memoria eeprom no es mayor a 0 y menor a 255
      break;                                        //Rompe el ciclo infinito, lo que significa que ha terminado de borrar los grados marcados de la rutina terminada                                       
    i++;                                            //Incrementa la variable "i" en uno
  }
  while(true);                                      //Ciclo infinito
}

void guardarPasos(String gradosPos) {       //Método encargado de guardar en la memoria eeprom los grados 
  String auxPos = gradosPos.substring(0, 3);//Asigna a la variable "auxPos" lo señalado de la posición 0 a la 3 en la variable "gradosPos"
  String auxGra = gradosPos.substring(4, gradosPos.length());//Asigna a la variable "auxGra" lo señalado del resto de la variable "gradosPos"
  int pos = auxPos.toInt();                 //Asigna a la variable "pos" el contenido de la variable "auxPos"
  int gra = auxGra.toInt();                 //Asigna a la variable "gra" el contenido de la variable "auxGra"
  EEPROM.write(pos, gra);                   //Escribe en la posición marcada por la variable "pos" en la memoria eeprom, el valor señalado en la variable "gra"
}

void paso_der() {       //Método de encargar el motor a pasos hacia la derecha
  digitalWrite(6, LOW);
  digitalWrite(5, LOW);
  digitalWrite(9, HIGH);
  digitalWrite(8, HIGH);
  delay(retardo);       //Retardo de 5 milisegundos
  digitalWrite(6, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(9, HIGH);
  digitalWrite(8, LOW);
  delay(retardo);       //Retardo de 5 milisegundos
  digitalWrite(6, HIGH);
  digitalWrite(5, HIGH);
  digitalWrite(9, LOW);
  digitalWrite(8, LOW);
  delay(retardo);       //Retardo de 5 milisegundos
  digitalWrite(6, HIGH);
  digitalWrite(5, LOW);
  digitalWrite(9, LOW);
  digitalWrite(8, HIGH);
  delay(retardo);       //Retardo de 5 milisegundos
}

void paso_izq() {        //Método de encargar el motor a pasos hacia la izquierda
  digitalWrite(6, HIGH);
  digitalWrite(5, HIGH);
  digitalWrite(9, LOW);
  digitalWrite(8, LOW);
  delay(retardo);        //Retardo de 5 milisegundos
  digitalWrite(6, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(9, HIGH);
  digitalWrite(8, LOW);
  delay(retardo);        //Retardo de 5 milisegundos
  digitalWrite(6, LOW);
  digitalWrite(5, LOW);
  digitalWrite(9, HIGH);
  digitalWrite(8, HIGH);
  delay(retardo);        //Retardo de 5 milisegundos
  digitalWrite(6, HIGH);
  digitalWrite(5, LOW);
  digitalWrite(9, LOW);
  digitalWrite(8, HIGH);
  delay(retardo);       //Retardo de 5 milisegundos
}

void apagado() {         // Método encargado del apagado del motor a pasos para que no se caliente
  digitalWrite(6, LOW);
  digitalWrite(5, LOW);
  digitalWrite(9, LOW);
  digitalWrite(8, LOW);
}

char Decimal_to_ASCII(int entrada) {  //Método encargado de convertir a formato ascii los datos entrantes
  char salida = ' ';
  switch (entrada) {                  //Se evalua la varriable entera recibida
    case 32:
      salida = ' ';
      break;
    case 33:
      salida = '!';
      break;
    case 34:
      salida = '"';
      break;
    case 35:
      salida = '#';
      break;
    case 36:
      salida = '$';
      break;
    case 37:
      salida = '%';
      break;
    case 38:
      salida = '&';
      break;
    case 39:
      salida = ' ';
      break;
    case 40:
      salida = '(';
      break;
    case 41:
      salida = ')';
      break;
    case 42:
      salida = '*';
      break;
    case 43:
      salida = '+';
      break;
    case 44:
      salida = ',';
      break;
    case 45:
      salida = '-';
      break;
    case 46:
      salida = '.';
      break;
    case 47:
      salida = '/';
      break;
    case 48:
      salida = '0';
      break;
    case 49:
      salida = '1';
      break;
    case 50:
      salida = '2';
      break;
    case 51:
      salida = '3';
      break;
    case 52:
      salida = '4';
      break;
    case 53:
      salida = '5';
      break;
    case 54:
      salida = '6';
      break;
    case 55:
      salida = '7';
      break;
    case 56:
      salida = '8';
      break;
    case 57:
      salida = '9';
      break;
    case 58:
      salida = ':';
      break;
    case 59:
      salida = ';';
      break;
    case 60:
      salida = '<';
      break;
    case 61:
      salida = '=';
      break;
    case 62:
      salida = '>';
      break;
    case 63:
      salida = '?';
      break;
    case 64:
      salida = '@';
      break;
    case 65:
      salida = 'A';
      break;
    case 66:
      salida = 'B';
      break;
    case 67:
      salida = 'C';
      break;
    case 68:
      salida = 'D';
      break;
    case 69:
      salida = 'E';
      break;
    case 70:
      salida = 'F';
      break;
    case 71:
      salida = 'G';
      break;
    case 72:
      salida = 'H';
      break;
    case 73:
      salida = 'I';
      break;
    case 74:
      salida = 'J';
      break;
    case 75:
      salida = 'K';
      break;
    case 76:
      salida = 'L';
      break;
    case 77:
      salida = 'M';
      break;
    case 78:
      salida = 'N';
      break;
    case 79:
      salida = 'O';
      break;
    case 80:
      salida = 'P';
      break;
    case 81:
      salida = 'Q';
      break;
    case 82:
      salida = 'R';
      break;
    case 83:
      salida = 'S';
      break;
    case 84:
      salida = 'T';
      break;
    case 85:
      salida = 'U';
      break;
    case 86:
      salida = 'V';
      break;
    case 87:
      salida = 'W';
      break;
    case 88:
      salida = 'X';
      break;
    case 89:
      salida = 'Y';
      break;
    case 90:
      salida = 'Z';
      break;
    case 91:
      salida = '[';
      break;
    case 92:
      salida = ' ';
      break;
    case 93:
      salida = ']';
      break;
    case 94:
      salida = '^';
      break;
    case 95:
      salida = '_';
      break;
    case 96:
      salida = '`';
      break;
    case 97:
      salida = 'a';
      break;
    case 98:
      salida = 'b';
      break;
    case 99:
      salida = 'c';
      break;
    case 100:
      salida = 'd';
      break;
    case 101:
      salida = 'e';
      break;
    case 102:
      salida = 'f';
      break;
    case 103:
      salida = 'g';
      break;
    case 104:
      salida = 'h';
      break;
    case 105:
      salida = 'i';
      break;
    case 106:
      salida = 'j';
      break;
    case 107:
      salida = 'k';
      break;
    case 108:
      salida = 'l';
      break;
    case 109:
      salida = 'm';
      break;
    case 110:
      salida = 'n';
      break;
    case 111:
      salida = 'o';
      break;
    case 112:
      salida = 'p';
      break;
    case 113:
      salida = 'q';
      break;
    case 114:
      salida = 'r';
      break;
    case 115:
      salida = 's';
      break;
    case 116:
      salida = 't';
      break;
    case 117:
      salida = 'u';
      break;
    case 118:
      salida = 'v';
      break;
    case 119:
      salida = 'w';
      break;
    case 120:
      salida = 'x';
      break;
    case 121:
      salida = 'y';
      break;
    case 122:
      salida = 'z';
      break;
    case 123:
      salida = '{';
      break;
    case 124:
      salida = '|';
      break;
    case 125:
      salida = '}';
      break;
    case 126:
      salida = '~';
      break;
  }
  return salida;                   //retorna el valor resultante de acuerdo al caso
}

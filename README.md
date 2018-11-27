# BrazoRobotico

INSTITUTO TECNOLÓGICO DE LEÓN

PROYECTO DE BRAZO ROBOTICO CON PROGRAMACIÓN EN JAVA Y ARDUINO.

ALUMNOS:

Paola Castillo Irene.

Roberto Ismael Ramírez Rios.
	
	PROFESOR:

ING. Levy Rojas Carlos Rafael.

Fecha de Entrega: 27-nov-2018.
  
  INTRODUCCIÓN
Proyecto de simulación del funcionamiento de un brazo robótico industrial capaz de grabar pasos de ejecución y notificar vía  voz artificial secuencia de ejecución mediante control de aplicativo Java y uso de microcontrolador ARDUINO UNO.
# Descripción General
El circuito desarrollado ejemplifica el trabajo de un brazo robótico industrial, el cual cuenta con dos modos de trabajo: programador y ejecución. El brazo robótico planteado puede ser controlado y programado en secuencia de pasos de ejecución mediante un aplicativo desarrollado con la tecnología Java. El brazo robótico posee cuatro grados de libertad para el movimiento, por lo cual tendrá la capacidad de movimiento en cuatro motores distintos (tres servomotores y un motor a pasos). Además de poder ser controlado por un aplicativo en Java, el brazo planteado  cuenta con la funcionalidad de guardar pasos de ejecución (en memoria EEPROM de del dispositivo Arduino) según el usuario lo desee y ejecutarlos en cualquier momento a menos que un botón de aborto sea accionado. 
Finalmente, cuando se trabaja en modo de ejecución el brazo debe seguir con su flujo normal de ejecución de pasos, sin importar si ocurrió un fallo en energía y perdida de comunicación serial con el aplicativo Java.
Las ventanas hechas en java serán ejecutadas únicamente en una computadora un ordenador, ya que hasta el momento solamente hemos creado ese tipo de interfaces, aunque se prevé crearlas dentro del ambiente Android para que puedas escribir mensajes desde tu celular.

  MATERIAL
  
* Microcontrolador Arduino UNO
* Protoboard
* Equipo de computo trabajando con Sistema Operativo Windows, MacOS o alguna distribución Linux
* Arduino IDE y drivers instalados en el equipo de computo para conexión de placa Arduino UNO al computador
* 3 servomotores (microservomotores)
* 1 Motor a pasos con controladora 28BYJ-48
* 1 Resistencia de 220 Ohms
* 1 Pulsador
* Conectores rápidos MM (20 Apróximadamente)
* 4 Conectores rápidos HM
* Pantalla LCD de 16x2
* Driver i2c
* Contar con JDK y/o JVM en la computadora en la cual se ejecutará la aplicación Java 
* IDE de desarrollo Java (NetBeans, Eclipse, etc)
* Kit de armado para brazo robótico de 4 grados de libertad (material de plastico, cartón, etc)
* Libreria para emisión de voz FreeTTS (libre y disponible en https://sourceforge.net/projects/freetts/files/)
* Librería y archivos para comunicación vía puerto serial Panamahitek(libre y disponible en https://sourceforge.net/projects/arduinoyjava/files/v2.7.0/)


  DESARROLLO

--CÓDIGO ARDUINO--
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

--CÓDIGO JAVA--
/*-------------------Clase MenuData.java------------------------------------
Clase orientada a la capa de la lógica del nogocio en el desarrollo de software,
dedicada a establecer la lógica del negocio.
La clase MenuData.java establece la logica (orientada a la navegabilidad) de la 
del menú que se muestra al usuario en primera instancia gracias a la clase 
MenuView.java
*/
package data;

//Impotaciones
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import view.MenuView;
import view.PanelProgramacionView;

/*Clase MenuData que implementa la interface ActionLister para manejo de los
eventos relacionados con la GUI MenuView.java*/
public class MenuData implements ActionListener{
    //Atributos
    private MenuView o;
    
    //Constructor que recibe como parametro un objeto del tipo MenuView
    public MenuData(MenuView o){
        this.o=o; 
    }

    //public MenuData(MenuView aThis) {
      //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    //}
   
    /*Método actionPermormed como compromiso de implementación de la interface
    ActionListener, que se activa al momento de accionar algún botón
    al cual le fue registrado el oyente*/
    @Override
    public void actionPerformed(ActionEvent e) {
       if (e.getSource()==o.getBtnProgramar()) //Detecta si el botón btnProgramar ha sido accionado
           new PanelProgramacionView(); //Objeto anonimo para lanzar la vista del panel de control
           
    }  
}

/*-------------------Clase PanelProgramacionData.java-------------------------
Clase orientada a la capa de la lógica del nogocio en el desarrollo de software,
dedicada a establecer la lógica del negocio.
La clase PanelProgramacionData.java establece la logica del panel programación,
con el cual el usuario podrá mover el brazo robotico en cualquier posición y grabar
un "programa" al brazo robotico, para que realice la serie de pasos indicados*/
package data;

//importaciones
import com.panamahitek.ArduinoException;
import com.panamahitek.PanamaHitek_Arduino;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jssc.SerialPortException;
import view.PanelProgramacionView;

public class PanelProgramacionData implements ActionListener {
    private PanelProgramacionView o;
    PanamaHitek_Arduino ino;
    //Contadores de posicion de servomotores de codo y hombro
    private int gradosHombro;
    private int gradosCodo;
    private int gradosCintura;
    private int gradosPinza;
    //Array para posiciones en motores
    private ArrayList <Integer> pinza = new ArrayList();
    private ArrayList <Integer> codo = new ArrayList();
    private ArrayList <Integer> cintura = new ArrayList();
    private ArrayList <Integer> hombro = new ArrayList();
    
    /*Constructor que recibe como parametro un objeto del tipo 
    PanelProgramacionView, para poder manjer correctamente la interface 
    gráfica del usuario*/
    public PanelProgramacionData(PanelProgramacionView o){
        /*Instancia el objeto ino del tipo panamaHitek_Arduino para 
        comunicación serial con microcontroladora arduino */
        ino = new PanamaHitek_Arduino();
        //Inicialización de variables
        this.o=o;
        gradosHombro= 90;
        gradosCodo= 0;
        gradosCintura= 0;
        gradosPinza= 1;
        conectar();
    }
    
    /*Método que establece la conexión con la microcontroladora Arduino*/
    public void conectar(){
        try {
            //Establecimiento de conexión a Arduino indicando puerto y frecuencia
            ino.arduinoTX("COM9", 9600);
        } catch (ArduinoException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    /*Método actionPermormed como compromiso de implementación de la interface
    ActionListener, que se activa al momento de accionar algún botón
    al cual le fue registrado el oyente*/
    @Override
    public void actionPerformed(ActionEvent e) {
	/*Decisión sobre que método ejecutar, según el botón accionado 
	gracias al metodo getSource()*/
        if(e.getSource()==o.getBtnAbrePinza())
            abrirPinza();
        
        else if (e.getSource()==o.getBtnCierraPinza())
            cerrarPinza();
        
        else if (e.getSource()==o.getBtnAvanzaCodo())
            avanzarCodo();
        
        else if (e.getSource()==o.getBtnRetrocedeCodo())
            retrocederCodo();
        
        else if (e.getSource()==o.getBtnLevantarHombro())
            levantarHombro();
        
        else if (e.getSource()==o.getBtnBajarHombro())
            bajarHombro();
        
        else if (e.getSource()==o.getBtnGirarCintura())
            girarCintura();
        
        else if (e.getSource()==o.getBtnGuardarPosicion())
            agregarPosicion();
        
        else if (e.getSource()==o.getBtnGuardar())
            grabarPasos();
       
        else if (e.getSource()==o.getBtnEjecutar())
            ejecutarPasos();
    }
    
     /*Método que indica al arduino que ejecute los pasos 
    grabados en memoria EEPROM*/
    public void ejecutarPasos(){        
        try {
            /*Envio de instrucción "m,1" a Arduino, el cual indicará que debe 
            de ejecutar automaticamente todos los pasos guardados en su 
            memoria EEPROM*/
            ino.sendData("m,1");
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    /*Método que indica al arduino el abrir la pinza*/
     public void abrirPinza() {
        try {
            /*Envio de instrucción "p,1" a Arduino, el cual indicará que debe 
            de mover el servomotor que controla la pinza y colocarlo a 1 grado*/
            ino.sendData("p,1");
            //Guardado de posición (grados) de la pinza
            gradosPinza=1;
            /*Creación de VoiceManager para indicar mediante voz al usuario el 
            movimiento realizado por el brazo robotico*/
            VoiceManager manager=VoiceManager.getInstance();
            Voice voz= manager.getVoice("kevin16"); //Tipo de voz
            //Activación de voz
            voz.allocate();
            voz.speak("Abriendo la pinza"); 
            voz.deallocate();
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }

     /*Método que indica al arduino el cerrar la pinza*/
    public void cerrarPinza() {
        try {
            /*Envio de instrucción "p,55" a Arduino, el cual indicará que debe 
            de mover el servomotor que controla la pinza y colocarlo 
            a 55 grados*/
            ino.sendData("p,55");
            //Guardado de posción (grados) de la pinza
            gradosPinza=55;
            /*Creación de VoiceManager para indicar mediante voz al usuario el 
            movimiento realizado por el brazo robotico*/
            VoiceManager manager=VoiceManager.getInstance();
            Voice voz= manager.getVoice("kevin16"); //tipo de voz
            //Activación de voz
            voz.allocate();
            voz.speak("Cerrando la pinza");
            voz.deallocate();
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }

    
    /*Método que indica al arduino que avance el codo del brazo robotico*/
    public void avanzarCodo() {
        try {
            /*Conversión a entero el texto entrante en la caja 
            de texto txtGradosCodo*/
            int grados=Integer.parseInt(o.getTxtGradosCodo().getText());
            /*incremento de grados del codo para indicar la 
            próxima nueva posición */
            gradosCodo=gradosCodo+grados;
            /*Decisión para saber si la nueva posición es menor o igual que 
            180 y mayor o igual a 0*/
            if (gradosCodo>=0 && gradosCodo<=180){
                /*Envio de instrucción "c,xx" a Arduino, el cual indicará que debe 
                de mover el servomotor que controla el codo y colocarlo 
                a xx grados*/
                ino.sendData("c,"+gradosCodo);
                //Limpieza de la caja de tezto de los grados entrantes al codo
                o.getTxtGradosCodo().setText("");
                /*Creación de VoiceManager para indicar mediante voz al usuario el 
                movimiento realizado por el brazo robotico*/
                VoiceManager manager=VoiceManager.getInstance();
                Voice voz= manager.getVoice("kevin16"); //tipo de voz
                //Acctivación de voz
                voz.allocate();
                voz.speak("Avanzando codo");
                voz.deallocate();
            }
            else{
                //Anuncio al usuario de que su petición no es posible de realizar
                JOptionPane.showMessageDialog(null, "Lo siento , la posición absoluta debe de estar entre 0 y 180 grados \n"
                        + "y la posicion absoluta estará en: "+gradosCodo);
                //regresión de la posición del codo a su estado anterior
                gradosCodo=gradosCodo-grados;
                //Limpieza de la caja de texto de los grados de codo
                o.getTxtGradosCodo().setText("");
            }
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }

     /*Método que hace que la posición del hombro baje*/
    public void retrocederCodo() {
        try {
	    /*Conversión a entero el texto entrante en la caja 
            de texto txtGradosCodo*/
            int grados=Integer.parseInt(o.getTxtGradosCodo().getText());
	    /*decremento de grados del codo para indicar la 
            próxima nueva posición */
            gradosCodo=gradosCodo-grados;
             /*Decisión para saber si la nueva posición es menor o igual que 
            180 y mayor o igual a 0*/
            if (gradosCodo>=0 && gradosCodo<=180){
                /*Envio de instrucción "c,xx" a Arduino, el cual indicará que debe 
                de mover el servomotor que controla el codo y colocarlo a 
                xx grados*/
                ino.sendData("c,"+gradosCodo);
                o.getTxtGradosCodo().setText("");
                /*Creación de VoiceManager para indicar mediante voz al usuario el 
                movimiento realizado por el brazo robotico*/
                VoiceManager manager=VoiceManager.getInstance();
                Voice voz= manager.getVoice("kevin16"); //Tipo de voz
		//Ejecición de voz
                voz.allocate();
                voz.speak("Retroceder codo");
                voz.deallocate();
            }
            else{
		 //Anuncio al usuario de que su petición no es posible de realizar
                JOptionPane.showMessageDialog(null, "Lo siento , la posición absoluta debe de estar entre 0 y 180 grados \n"
                        + "y la posicion absoluta estará en: "+gradosCodo);
		//Regresión a posición anterior del codo
                gradosCodo=gradosCodo+grados;
                o.getTxtGradosCodo().setText("");
            }
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

     /*Método que hace que la posición del hombro se levante*/
    public void levantarHombro() {
        try {
	    /*Conversión a entero el texto entrante en la caja 
            de texto txtGradosHombro*/
            int grados=Integer.parseInt(o.getTxtGradosHombro().getText());
	    /*incremento de grados del hombro para indicar la 
            próxima nueva posición */
            gradosHombro=gradosHombro+grados;
             /*Decisión para saber si la nueva posición es menor o igual que 
            180 y mayor o igual a 0*/
            if (gradosHombro>=0 && gradosHombro<=180){
                /*Envio de instrucción "h,xx" a Arduino, el cual indicará que debe 
                de mover el servomotor que controla el hombro y colocarlo a 
                xx grados*/
                ino.sendData("h,"+gradosHombro);
                o.getTxtGradosHombro().setText("");
                /*Creación de VoiceManager para indicar mediante voz al usuario el 
                movimiento realizado por el brazo robotico*/
                VoiceManager manager=VoiceManager.getInstance();
                Voice voz= manager.getVoice("kevin16"); //Tipo de voz
		//Ejecución de voz
                voz.allocate();
                voz.speak("Levantando hombro");
                voz.deallocate();
            }
            else{
		 //Anuncio al usuario de que su petición no es posible de realizar
                JOptionPane.showMessageDialog(null, "Lo siento , la posición absoluta debe de estar entre 0 y 180 grados \n"
                        + "y la posicion absoluta estará en: "+gradosHombro);
		//REgresión a posición anterior del hombro
                gradosHombro=gradosHombro-grados;
                o.getTxtGradosHombro().setText("");
            }
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    /*Método que hace que la posición del hombro baje*/
    public void bajarHombro() {
        try {
	    /*Conversión a entero el texto entrante en la caja 
            de texto txtGradosHombro*/
            int grados=Integer.parseInt(o.getTxtGradosHombro().getText());
	    /*decremento de grados del hombro para indicar la 
            próxima nueva posición */
            gradosHombro=gradosHombro-grados;
             /*Decisión para saber si la nueva posición es menor o igual que 
            180 y mayor o igual a 0*/
            if (gradosHombro>=0 && gradosHombro<=180){
                /*Envio de instrucción "h,xx" a Arduino, el cual indicará que debe 
                de mover el servomotor que controla el hombro y colocarlo a 
                xx grados*/
                ino.sendData("h,"+gradosHombro);
                o.getTxtGradosHombro().setText("");
                /*Creación de VoiceManager para indicar mediante voz al usuario el 
                movimiento realizado por el brazo robotico*/
                VoiceManager manager=VoiceManager.getInstance();
                Voice voz= manager.getVoice("kevin16"); //Tipo de voz
	        //Ejecución de voz
                voz.allocate();
                voz.speak("Bajando hombro");
                voz.deallocate();
            }
            else{
		 //Anuncio al usuario de que su petición no es posible de realizar
                JOptionPane.showMessageDialog(null, "Lo siento , la posición absoluta debe de estar entre 0 y 180 grados \n"
                        + "y la posicion absoluta estará en: "+gradosHombro);
		//REgresión a posición anterior del hombro
                gradosHombro=gradosHombro+grados;
                o.getTxtGradosHombro().setText("");
            }
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        
    }

    /*Método que guarda una posición para posteriormente ser grabada en 
    el arduino*/
    public void agregarPosicion() {
        /*Variable para guardar la confirmación si el usuario desea guardar 
        los pasos introducidos por el usuario*/
        String conf="";
        try{
            /*Variable que le indica al usuario si la posición de la pinza que 
            desea guardar es cerrada o abierta */ 
            String auxPinza="";
            if (gradosPinza==1)
                auxPinza="Abierta";
           else
               auxPinza="Cerrada";
            
            //Pregunta de confirmación
            conf=JOptionPane.showInputDialog(null,
                    "Se agregará la el paso con las sguinetes posiciones \n"
                            +"Base: "+gradosCintura+" grados \n"
                            +"Hombro: "+gradosHombro+" grados \n"
                            +"Codo: "+gradosCodo+" grados \n"
                            +"Pinza: "+auxPinza+" \n"
                            + "¿Desea agregar esta posición al programa?",
                    "Ejecución del programa", 
                    JOptionPane.QUESTION_MESSAGE, null,
                    new Object[] {"SI","NO"},"Seleccione").toString();
            }
            catch(Exception a){}
            /*Decisión para saber que acción tomar según lo deseado por el 
             usuario, si agregar el paso deseado o no */
            if(conf.equals("SI")){
                try{
                    /*Llamado al método agregarPasos() para guardar la 
                    posción aactual */
                    agregarPasos();
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
            }
        
    }
    
    /*Método para guardar una posición del brazo y/o "Paso" del programa a 
    grabar por el usuario*/
    public void agregarPasos(){
        if (gradosCodo==0)
            gradosCodo++;
        if (gradosHombro==0)
            gradosHombro++;
        if (gradosCintura==0)
            gradosCintura++;
        /*Agregar la posición (grados) de los motores a sus respectivos espacios 
        de memoria "ArrayList" que forman un paso en el programa a grabar por el usuario*/
        pinza.add(gradosPinza);
        codo.add(gradosCodo);
        hombro.add(gradosHombro);
        cintura.add(gradosCintura);
        /*Variable que le indica al usuario si la posición de la pinza que 
        desea guardar es cerrada o abierta */ 
        String auxPinza="";
        if (gradosPinza==1)
            auxPinza="Abierta";
        else
            auxPinza="Cerrada";
        /*Creación de instrucción que se mostrará en el JTextArea para 
        visualización de los pasos proximos a guardar para el usuario*/
        String instruccion=o.getAreaPasos().getText()+"Pinza: "+auxPinza+
                ", Codo: "+gradosCodo+", "+ "Hombro: "+gradosHombro+
                ", Cintura: "+gradosCintura+"\n";
        /*Asignar el texto al JTextArea*/
        o.getAreaPasos().setText(instruccion);
    }

    /*Método que graba los pasos para ejecución próxima en el arduino*/
    public void grabarPasos() {
	/*Variable que especifica que a partir de la psoción 3 de la memoria 
	EEPROM se empezará a grabar los pasos deseados*/
        int auxPos=3;
	//Inicio de ciclo para grabar pasos
        for(int i=0; i<pinza.size();i++){
            String auxPinza="";
            String auxCodo="";
            String auxHombro="";
            String auxBase="";
            
	    /*Decisión para identificar si los grados de la pinza que se almacenaran 
	    se enviaran completando con ceros a la izquierda para completar 
	    una cadena de logintud de 3 digitos para tratarla con arduino*/
            /*Si cantidad en grados es de dos digitos, solo se completa con 
	    un cero a la izquierda */
            if(pinza.get(i)>=1 && pinza.get(i)<=9)
                auxPinza="00"+pinza.get(i);
	    /*Si cantidad en grados es de dos digitos, solo se completa con 
	    un cero a la izquierda */
            else if (pinza.get(i)>=10 && pinza.get(i)<=99)
                auxPinza="0"+pinza.get(i);
            else
                auxPinza=pinza.get(i)+"";
            
 	   /*Decisión para identificar si los grados del codo que se almacenaran 
	    se enviaran completando con ceros a la izquierda para completar 
	    una cadena de logintud de 3 digitos para tratarla con arduino*/
            /*Si cantidad en grados es de dos digitos, solo se completa con 
	    un cero a la izquierda */
           if(codo.get(i)>=1 && codo.get(i)<=9)
                auxCodo="00"+codo.get(i);
            else if (codo.get(i)>=10 && codo.get(i)<=99)
                auxCodo="0"+codo.get(i);
            else
                auxCodo=codo.get(i)+"";
           
	   /*Decisión para identificar si los grados del hombro que se almacenaran 
	    se enviaran completando con ceros a la izquierda para completar 
	    una cadena de logintud de 3 digitos para tratarla con arduino*/
            /*Si cantidad en grados es de dos digitos, solo se completa con 
	    un cero a la izquierda */
           if(hombro.get(i)>=1 && hombro.get(i)<=9)
                auxHombro="00"+hombro.get(i);
            else if (hombro.get(i)>=10 &&hombro.get(i)<=99)
                auxHombro="0"+hombro.get(i);
            else
                auxHombro=hombro.get(i)+"";
           
	   /*Decisión para identificar si los grados de la cintura que se almacenaran 
	    se enviaran completando con ceros a la izquierda para completar 
	    una cadena de logintud de 3 digitos para tratarla con arduino*/
            /*Si cantidad en grados es de dos digitos, solo se completa con 
	    un cero a la izquierda */
           if(cintura.get(i)>=1 && cintura.get(i)<=9)
                auxBase="00"+cintura.get(i);
            else if (cintura.get(i)>=10 &&cintura.get(i)<=99)
                auxBase="0"+cintura.get(i);
            else
                auxBase=cintura.get(i)+"";
           
            /*Variable String para indicar la ubicación de la memoria EEPROM donde 
	   se guardaran los valores en grados de los respectivos motores*/
            String pos="";
            if(auxPos>=1 && auxPos<=9)
                pos="00"+auxPos;
            else if (auxPos>=10 &&auxPos<=99)
                pos="0"+auxPos;
            else
                pos=auxPos+"";
            
	    /*Construcción de instrucción que se mandará al arduino para indicar 
	    que se guardarán pasos en la memoria EEPROM*/
            String instruccionPinza="g,"+pos+","+auxPinza;
            
            try {
		//Mandar instrucción a arduino
                ino.sendData(instruccionPinza);
            } catch (SerialPortException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            } catch (ArduinoException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
           
	    /*Llamado al método de dormirHilo() para dar tiempo al arduino 
	    de ejecutar la instrucción que previamente se le inidico*/ 
            dormirHilo();
	    //Aumentar la posición de la localización de la memoria
            auxPos++;
	    /*se vuelve a inicializar la variable auxiliar para inidicar 
	    la posición de memoria donde guardará la siguiente instrucción*/
            pos="";
            if(auxPos>=1 && auxPos<=9)
                pos="00"+auxPos;
            else if (auxPos>=10 &&auxPos<=99)
                pos="0"+auxPos;
            else
                pos=auxPos+"";
            
            //Guardar posicion del Codo
            String instruccionCodo="g,"+pos+","+auxCodo;
            try {
		//Mandar instrucción a arduino
                ino.sendData(instruccionCodo);
            } catch (ArduinoException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            } catch (SerialPortException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
            
	    /*Llamado al método de dormirHilo() para dar tiempo al arduino 
	    de ejecutar la instrucción que previamente se le inidico*/
            dormirHilo();
	    //Aumentar la posición de la localización de la memoria
            auxPos++;
            pos="";
            if(auxPos>=1 && auxPos<=9)
                pos="00"+auxPos;
            else if (auxPos>=10 &&auxPos<=99)
                pos="0"+auxPos;
            else
                pos=auxPos+"";
            //Guardar posicion del Codo
            String instruccionHombro="g,"+pos+","+auxHombro;
            try {
		//Mandar instrucción a arduino
                ino.sendData(instruccionHombro);
            } catch (ArduinoException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            } catch (SerialPortException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
            
	    /*Llamado al método de dormirHilo() para dar tiempo al arduino 
	    de ejecutar la instrucción que previamente se le inidico*/ 
            dormirHilo();
	    //Aumentar la posición de la localización de la memoria
            auxPos++;
            pos="";
            if(auxPos>=1 && auxPos<=9)
                pos="00"+auxPos;
            else if (auxPos>=10 &&auxPos<=99)
                pos="0"+auxPos;
            else
                pos=auxPos+"";
            //Guardar posicion del Codo
            String instruccionCintura="g,"+pos+","+auxBase;
            try {
		//Mandar instrucción a arduino
                ino.sendData(instruccionCintura);
            } catch (ArduinoException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            } catch (SerialPortException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
	    //Aumentar la posición de la localización de la memoria
            auxPos++;
	    /*Llamado al método de dormirHilo() para dar tiempo al arduino 
	    de ejecutar la instrucción que previamente se le inidico*/ 
            dormirHilo();
            
        }
        JOptionPane.showMessageDialog(o,"Guardado Exitoso!!");
	/*Llamado al metodo limpiarPasos() para limpiar ArrayList (temporales) 
	donde se almacenaban secuencia de pasos a grabar en memoria EEPROM 
	de arduino*/
        limpiarPasos();
    }
    
    /*Método que duerme el hilo principal por un tiempo determinado*/
    public void dormirHilo(){
        try{
            //Dormir el hilo por 5 segundos
            Thread.sleep(3000);
           }
        catch(InterruptedException e){}
    }

    /*Método que gira la cintura del brazo*/
    public void girarCintura() {
        try {
	    /*Conversión a entero el texto entrante en la caja 
            de texto txtGradosCintura*/
            int grados=Integer.parseInt(o.getTxtGradosCintura().getText());
	    /*igualación de los grados d ela cintura con los grados entrantes 
	    para llevar registro de prosición en un momento determinado*/
            gradosCintura=grados;
	    /*Envio de instrucción "b,xx" a Arduino, el cual indicará que debe 
            de mover el motor a pasos que controla la cintura colocarlo a 
	    xx grados*/
            ino.sendData("b,"+grados);
	    //Limpieza de la caja de texto de los grados a mover el motor a pasos
            o.getTxtGradosCintura().setText("");
	    /*Creación de VoiceManager para indicar mediante voz al usuario el 
            movimiento realizado por el brazo robotico*/
            VoiceManager manager=VoiceManager.getInstance();
            Voice voz= manager.getVoice("kevin16"); //Tipo de voz
	    //Ejecución de voz
            voz.allocate();
            voz.speak("Girando cintura");
            voz.deallocate();
        } 
        catch (ArduinoException ex) {
           JOptionPane.showMessageDialog(null, ex.getMessage());
        } 
        catch (SerialPortException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
    /*Método que limpia los ArrayList de los pasos guardados una vez que estos
    han sido grabados en el arduino*/
    public void limpiarPasos(){
        //Limpieza de ArrayList de posiciones
        pinza.clear();
        codo.clear();
        hombro.clear();
        cintura.clear();
        //Limpieza de JTextAarea para pasos
        o.getAreaPasos().setText("");
    }
}

/*---------------------Clase MenuView.java---------------------------------
Clase orientada a la capa del usuario en el desarrollo de software, dedicada 
exclusivamente a la creación y muestra de la vista del menú principal 
y registro de oyentes a los componentes que lo ameriten.
La clase MenuView.java es la primer interface grafica del mostrada al usuario
con el objetivo de crear una interacción buena a primera instancia con las personas
que harán uso de la misma.
*/

package view;

/*Importaciones necesarias para la creación del la GUI y registro de oyente, 
creado en otro paquete dentro del mismo proyecto*/
import data.MenuData;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MenuView extends JFrame /*Clase MenuView que hereda de JFrame 
    para tomar el papel de JFrame mostrado al usuario*/{
    
    //Atributos de la interface gráfica
    private JLabel lblFondo;
    private JButton btnProgramar;
    private MenuData o;
    
    /*Constructor donde se llaman los métodos definidos para la creación de 
    la interface gráfica del usuario */
    public MenuView(){
        //Titulo del JFrame
        super("Panel de control Brazo Robotico 4 DOF"); 
        /*Llamado al método  inicializarComponentes() para instanciar 
        atributos de la interface gráfica */
        inicializarComponentes();
        /*Llamado al método establecerAspectosBasicosFrame() para definir 
        propiedades básicas del JFrame */
        establecerAspectosBasicosFrame();
        distribuirComponentes();
        registrarOyente();
    }

    /*Método para estabelcer los aspectos y/o  propiedes básicas del JFrame
    a mostrar al usuario*/
    public void establecerAspectosBasicosFrame() {
        this.setSize(712, 235); //Tamaño del frame
        this.setLocationRelativeTo(null); //Localización del frame al centro
        this.setLayout(null); //Establecimiento del Layout nulo al JFrame
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); 
        this.setVisible(true); //Hace visible al JFrame
    }

    /*Método dedicado a instanciar loo componentes del JFrame*/
    public void inicializarComponentes() {
        /*Instancia de atributo lblFondo que será el fondo del menú 
        mostrado al usuario */
        lblFondo= new JLabel(new ImageIcon("menu.png"));
        //Instanciamiento del boton "Comenzar"
        btnProgramar= new JButton("Comenzar");
        /*Instancia del atributo "o" del tipo MenuData que será el 
        oyente al registrar*/
        o= new MenuData(this); 
    }

    /*Método que distribuye los elementos en el menú mostrado al usuario*/
    public void distribuirComponentes() {
        lblFondo.setBounds(0, 0, 712, 235); //Tamaño y ubicación del fondo
        this.add(lblFondo); //Inclusión del fondo al JFrame
        btnProgramar.setBounds(445,68, 155,38); //Tamaño y ubicación del botón
        lblFondo.add(btnProgramar); //Inclusión del botón al fondo 
    }
    
    //Método que registra oyente a los elementos del menú necesarios
    public void registrarOyente() {
       btnProgramar.addActionListener(o); //Registro de oyente al botón
    }
    
    /*Método get del botón btnProgramar para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnProgramar() {
        return btnProgramar;
    }
    
    public static void main(String[] args) {
        new MenuView();
    }  
}

/*---------------------Clase PanelProgramacionView.java----------------------
Clase orientada a la capa del usuario en el desarrollo de software, dedicada 
exclusivamente a la creación y muestra de la vista del panel de programación del
brazo robotico y y registro de oyentes a los componentes que lo ameriten.
La calse PanelProgramacionView.java es la interface gráfica principal, donde
el usuario podrá mover el brazo robotico en la posisción que desee, incluyendo
la grabación de pasos en la microcontroladora Arduino encargada de controlar el 
brazo robotico creado
*/
package view;

/*Importaciones necesarias para la creación del la GUI y registro de oyente, 
creado en otro paquete dentro del mismo proyecto*/
import data.PanelProgramacionData;
import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*Clase MenuView que hereda de JFrame para tomar el papel de JFrame 
mostrado al usuario*/
public class PanelProgramacionView extends JFrame {
    
    //Atributos de la interface gráfica
    private JLabel lblTitulo, lblPinzas, lblCodo, lblHombro, lblCintura;
    private JButton btnAbrePinza, btnCierraPinza, btnAvanzaCodo, 
            btnRetrocedeCodo, btnLevantarHombro, btnBajarHombro, 
            btnGirarCintura, btnGuardar, btnEjecutar, btnGuardarPosicion;
    private JTextField txtGradosCodo, txtGradosHombro, txtGradosCintura;
    private JTextArea areaPasos;
    private JScrollPane scrollPasos;
    private PanelProgramacionData o;
    
    /*Constructor donde se llaman los métodos definidos para la creación de 
    la interface gráfica del usuario */
    public PanelProgramacionView(){
        //Titulo del JFrame
        super("Panel de Control");
        /*Llamado al método establecerAspectosBasicosFrame() para definir 
        propiedades básicas del JFrame */
        establecerAspectosBasicosFrame();
        /*Llamado al método  inicializarComponentes() para instanciar 
        atributos de la interface gráfica y el oyente a registrar*/
        inicializarComponentes();
        /*Llamado al método establecerPropiedadesComponentes() para estabelcer las 
        propiedades necesarias a cada componente de la interface gráfica*/
        establecerPropiedadesComponentes();
        /*Llamado al método registrarOyente() para hacer el registro de oyente a 
        los componentes de la interface gráfica que lo ameriten */
        registrarOyente();
        /*Llamado al método distribuirComponentes() para distribuir todos los
        componentes de la interface gráfica a lo largo y ancho de la misma*/
        distribuirComponentes();
    }
    
    /*Método para estabelcer los aspectos y/o  propiedes básicas del JFrame
    a mostrar al usuario*/
    public void establecerAspectosBasicosFrame() {
        this.setSize(1040,680); //Tamaño del JFrame
        this.getContentPane().setBackground(new Color(166,216,221)); //Color del JFrame
        this.setLocationRelativeTo(null); //Localización al centro del JFrame
        this.setLayout(null); //Establecimiento del layout nulo al JFrame
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true); //Visibilidad del JFrame
    }
    
    /*Método dedicado a instanciar loo componentes del JFrame*/
    public void inicializarComponentes() {
       lblTitulo= new JLabel("Panel de Control");
       //Pinzas 
       lblPinzas= new JLabel("Pinzas");
       btnAbrePinza= new JButton("Abrir");
       btnCierraPinza= new JButton("Cerrar");
       //Codo
       lblCodo= new JLabel("Codo");
       btnAvanzaCodo= new JButton("Arriba");
       btnRetrocedeCodo=new JButton("Abajo");
       //Hombro
       lblHombro= new JLabel("Hombro");
       btnLevantarHombro=new JButton("Arriba");
       btnBajarHombro=new JButton("Abajo");
       //Cintura
       lblCintura= new JLabel("Cintura");
       btnGirarCintura= new JButton("Rotación");
       //Cajas de texto para los grados
       txtGradosCodo= new JTextField();
       txtGradosHombro= new JTextField();
       txtGradosCintura= new JTextField();
       //Botones guardar
       btnGuardar= new JButton("Guardar");
       btnGuardarPosicion= new JButton("Guardar Posición");
       //Boton Ejecutar
       btnEjecutar = new JButton("Ejecutar");
       //Area de pasos
       areaPasos= new JTextArea();
       scrollPasos= new JScrollPane();
       o= new PanelProgramacionData (this);
    }
    
    /*Método para establecer de propiedades de los componentes de la GUI*/
    public void establecerPropiedadesComponentes() {
        //Prpiedades del Label de titulo
        lblTitulo.setFont(new Font("Arial",Font.BOLD,24));
        //Prpiedades del Label de pinzas
        lblPinzas.setFont(new Font("Arial",Font.BOLD,18));
        //Propiedades del Label codo
        lblCodo.setFont(new Font("Arial",Font.BOLD,18));
        //Prpoiedades del Label hombro
        lblHombro.setFont(new Font("Arial",Font.BOLD,18));
        //Propiedades del Label cintura
        lblCintura.setFont(new Font("Arial",Font.BOLD,18));
        //Propiedades del area de pasos
        areaPasos.setLineWrap(true);
        scrollPasos.setViewportView(areaPasos);
    }

    /*Método que distribuye los elementos en el Panel de control
    mostrado al usuario*/
    public void distribuirComponentes() {
        lblTitulo.setBounds(420,30, 280, 40); //Tamaño y ubicación del titulo
        this.add(lblTitulo); //Inclusión del label lblTitulo titulo al JFrame
        lblPinzas.setBounds(16,80,100,20); //Tamaño y ubicación del label pinzas
        this.add(lblPinzas); //Inclusión del label lblPinzas pinzas al JFrame
        btnAbrePinza.setBounds(16, 120, 165,48); //Tamaño y ubicación del boton btnAbrePinza
        this.add(btnAbrePinza); //Inclusión del botón btnAbrePinza al JFrame
        btnCierraPinza.setBounds(240, 120, 165,48); //Tamaño y ubicación del boton btnCierraPinza
        this.add(btnCierraPinza); //Inclusión del botón btnCierraPinza al JFrame
        lblCodo.setBounds(16,200,100,20); //Tamaño y ubicación del label lblCodo
        this.add(lblCodo); //Inclusión label lblCodo codo al JFrame
        btnAvanzaCodo.setBounds(16, 240, 99,97); //Tamaño y ubicación del boton btnAvanzaCodo
        this.add(btnAvanzaCodo); //Inclusión del botón btnAvanzaCodo al JFrame
        btnRetrocedeCodo.setBounds(160,240, 99,97);  //Tamaño y ubicación del boton btnRetrocedeCodo
        this.add(btnRetrocedeCodo); //Inclusión del botón btnRetrocedeCodo al JFrame
        txtGradosCodo.setBounds(280,300,80,30); /*Tamaño y ubicación del 
                                                    JTextField txtGradosCodo*/
        this.add(txtGradosCodo); /*Inclusión del JTextField 
                                    txtGradosCodo al JFrame*/
        lblHombro.setBounds(16, 360, 100,20); //Tamaño y ubicación del label hombro
        this.add(lblHombro); //Inclusión del label lblHombro al JFrame
        btnLevantarHombro.setBounds(16,400,80,80); //Tamaño y ubicación del boton btnLevantarHombro
        this.add(btnLevantarHombro); //Inclusión del botón LevantarHombro al JFrame
        btnBajarHombro.setBounds(160,400,80,80);  //Tamaño y ubicación del boton btnBajarCodo
        this.add(btnBajarHombro); //Inclusión del botón btnBajarHombro al JFrame
        txtGradosHombro.setBounds(280,440,80,30); //Inclusión del JTextField txtGradosHombro al JFrame
        this.add(txtGradosHombro); /*Inclusión del JTextField 
                                    txtGradosHombro al JFrame*/
        lblCintura.setBounds(16,520,100,20); //Tamaño y ubicación del label cintura
        this.add(lblCintura); //Inclusión del label lblCintura al JFrame
        btnGirarCintura.setBounds(16,560, 114,58); //Tamaño y ubicación del boton btnGirarCintura
        this.add(btnGirarCintura); //Inclusión del botón btnGirarCintura al JFrame
        txtGradosCintura.setBounds(180,580,80,30); //Tamaño y ubicación del JTextField txtGradosCintura
        this.add(txtGradosCintura); /*Inclusión del JTextField 
                                    txtGradosCiintura al JFrame*/
        scrollPasos.setBounds(423,180,537,241); //Tamaño y ubicación del JScrollPane scrollPasos
        this.add(scrollPasos); //Inclusión del JScrollPane scrollPasos al JFrame
        btnGuardar.setBounds(805,447,143,33);  //Tamaño y ubicación del boton btnGuardar
        this.add(btnGuardar); //Inclusión del botón btnGuardar al JFrame
        btnGuardarPosicion.setBounds(606, 447,163,33);  //Tamaño y ubicación del boton btnGuardarPosicion
        this.add(btnGuardarPosicion); //Inclusión del botón btnGuardarPosicion al JFrame
        btnEjecutar.setBounds(706, 500,163,33);  //Tamaño y ubicación del boton btnEjecutar
        this.add(btnEjecutar); //Inclusión del botón btnEjecutar al JFrame
    }

    //Método que registra oyente a los componentes del menú necesarios
    public void registrarOyente() {
       /*Registro de oyente a los botones de la interface gráfica*/
       btnAbrePinza.addActionListener(o); 
       btnCierraPinza.addActionListener(o); 
       btnAvanzaCodo.addActionListener(o);
       btnRetrocedeCodo.addActionListener(o);
       btnLevantarHombro.addActionListener(o);
       btnBajarHombro.addActionListener(o);
       btnGirarCintura.addActionListener(o);
       btnGuardarPosicion.addActionListener(o);
       btnGuardar.addActionListener(o);
       btnEjecutar.addActionListener(o);
    }

    /*Método get del botón btnAbrePinza para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnAbrePinza() {
        return btnAbrePinza;
    }

    /*Método get del botón btnCierraPinza para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnCierraPinza() {
        return btnCierraPinza;
    }

    /*Método get del botón btnAvanzaCodo para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnAvanzaCodo() {
        return btnAvanzaCodo;
    }

    /*Método get del botón btnRetrocederCodo para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnRetrocedeCodo() {
        return btnRetrocedeCodo;
    }

    /*Método get del botón btnLevantarCodo para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnLevantarHombro() {
        return btnLevantarHombro;
    }

    /*Método get del botón btnBajarHombro para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnBajarHombro() {
        return btnBajarHombro;
    }

    /*Método get del botón btnGirarCintura para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnGirarCintura() {
        return btnGirarCintura;
    }

    /*Método get del botón btnGuardar para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    /*Método get del botón btnGuardarPosicioon para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnGuardarPosicion() {
        return btnGuardarPosicion;
    }

    /*Método get del botón btnEjecutar para poder acceder al él desde el 
    oyente registrado */
    public JButton getBtnEjecutar() {
        return btnEjecutar;
    }

    /*Método get de la caja de texto txtGradosCodo para poder acceder 
    al él desde el oyente registrado */
    public JTextField getTxtGradosCodo() {
        return txtGradosCodo;
    }

    /*Método get de la caja de texto txtGradosHombro para poder acceder 
    al él desde el oyente registrado */
    public JTextField getTxtGradosHombro() {
        return txtGradosHombro;
    }

    /*Método get de la caja de texto txtGradosCintura para poder acceder 
    al él desde el oyente registrado */
    public JTextField getTxtGradosCintura() {
        return txtGradosCintura;
    }

    /*Método get del área de texto areaPasos para poder acceder 
    al él desde el oyente registrado */
    public JTextArea getAreaPasos() {
        return areaPasos;
    }  
}

CONCLUSIÓN

Tuvimos bastantes errores al momento de codificar el programa, incluso se nos hizo muy difícil la parte de conectar el Arduino con java. Tuvimos que investigar todos esos temas de conexión para poder terminar el proyecto.

Cuando logramos corregir la mayoría de los errores se nos presentaron algunos otros más como lo que fue la conectividad de los mensajes escritos con la reproducción de los mismos en la pantalla LCD. Sin embargo, después de tanta prueba y error logramos finalizar de una manera correcta nuestro programa.

Finalmente logramos el objetivo, aprendí mucho con este proyecto y me gustaria aprender mucho mas.

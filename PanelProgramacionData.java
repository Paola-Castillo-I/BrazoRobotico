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
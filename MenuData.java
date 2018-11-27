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
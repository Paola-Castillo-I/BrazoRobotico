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
        lblFondo= new JLabel(new ImageIcon("1.JPG"));
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
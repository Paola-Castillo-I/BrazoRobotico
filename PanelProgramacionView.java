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
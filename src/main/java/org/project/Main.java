package org.project;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Clase para el manejo y control de cada uno de los clientes que se conectan al
 * juego.
 *
 * @author Saul Lázaro Hermoso
 */
public class Main {

    /**
     * Socket del cliente
     */
    Socket cliente;
    /**
     * Input Stream para llevar el control de los flujos de entrada
     */
    private DataInputStream flujoEntrada = null;
    /**
     * Input Stream para llevar el control de los flujos de salida
     */
    private DataOutputStream flujoSalida = null;
    /**
     * Nº Entero para llevar el control de las rondas de cada cliente
     */
    private int ronda = 1;

    /**
     * Lista de caracteres. Se usa para llevar el control de las letras que va
     * diciendo cada cliente.
     */
    private List<Character> letras = new ArrayList<Character>();

    /**
     * Scanner para las lecturas
     */
    Scanner scanner;

    /**
     * Atributos para colorear mensajes de consola
     */
    public static String black = "\033[30m";
    public static String cyan = "\033[36m";
    public static String red = "\033[31m";
    public static String green = "\033[32m";
    public static String blue = "\033[34m";
    public static String purple = "\033[35m";
    public static String reset = "\u001B[0m";

    /**
     * Constructor de la clase cliente. Inicializa el socket con el host y
     * puertos pactados. Mientras no se acierte la palabra, el cliente permanece
     * activo.
     */
    public Main() {

        try {
            this.scanner = new Scanner(System.in);
            //Conectamos al servidor por el puerto
            this.cliente = new Socket(Configuration.HOST, Configuration.PUERTO);
            boolean fin = false;
            int estado = 1;
            while (!fin) {

                switch (estado) {
                    case (1)://logueo
                        estado = this.logueo();
                        break;
                    case (2)://configuración
                        //Configuramos la palabra
                        estado = this.configuracion();
                        break;
                    case (3)://jugamos
                        estado = this.jugar();
                        break;
                    case (4)://salir porque se acierta.
                        fin = true;
                        System.out.println(this.green + "HAS GANADO!!" + this.reset);
                        break;
                }
            }  //FIN

            //cerramos los streams y el socket
            this.closeStreamsSockets();
        } catch (IOException ex) {
            System.out.println(this.red + "Ha ocurrido una error inesperado." + ex.getMessage() + this.reset);
        }
    }

    /**
     * Método para llevar el control del logueo del cliente.
     *
     * @return Retorna un entero con el valor del estado del diagrama
     */
    public int logueo() {
        int estado = 1;
        do {
            String msj, respuesta, stat;
            msj = this.getMensajeServidor();
            System.out.println(msj); //introduce el usuario
            respuesta = this.scanner.nextLine();
            this.setMensajeServidor(respuesta);
            msj = this.getMensajeServidor(); //la respuesta
            stat = this.getMensajeServidor(); //el estado
            System.out.println(msj); //mostrar la respuesta
            estado = Integer.parseInt(stat); //actualizar estado
        } while (estado == 1);

        return estado;
    }

    /**
     * Método que implementa la cinfuguración del juego.
     *
     * @return Retorna un entero con el valor del estado del diagrama
     */
    public int configuracion() {
        int estado = 0;
        System.out.println(this.purple + "Introduce la palabra: " + this.reset);
        String mensaje_ASE = this.scanner.nextLine();
        this.setMensajeServidor(mensaje_ASE);
        System.out.println(this.purple + "**** JUEGO CONFIGURADO ****" + this.reset);
        estado = 1;
        return estado;
    }

    /**
     * Método para llevar todo el control de la partida de cada cliente.
     *
     * @return Retorna un entero con el valor del estado del diagrama
     */
    public int jugar() {

        int estado = 3;

        String msj, respuesta;
        System.out.println("########## va a empezar el juego ##########");

        do {
            System.out.println(this.cyan + "RONDA: " + this.ronda);
            this.ronda = this.ronda += 1;
            msj = this.getMensajeServidor(); //la palabra tiene... di una letra
            System.out.println(msj);
            respuesta = this.scanner.nextLine(); //escribo la letra

            if (respuesta.length() == 1) {
                this.letras.add(respuesta.charAt(0));
            }

            this.setMensajeServidor(respuesta); //envio la letra
            this.setMensajeServidor(letras.toString()); //envio el array dicho
            msj = this.getMensajeServidor(); //has descubierto...
            System.out.println(msj); //o has descubierno o no has indicado... ¿quieres resolver?


            //resolver
            respuesta = this.scanner.nextLine(); //escribo si o no
            this.setMensajeServidor(respuesta); //lo envio

            if (respuesta.equalsIgnoreCase("si")) {
                msj = this.getMensajeServidor();
                System.out.println(msj); //resuelva la palabra
                respuesta = this.scanner.nextLine();
                this.setMensajeServidor(respuesta); //envio la palabra

                msj = this.getMensajeServidor();
                if (msj.equals("¡GANASTE!")) {
                    estado = 4;
                } else if (msj.equals("fallaste")) {
                    estado = 3;
                    System.out.println(this.red + "has fallado. La palabra NO es " + respuesta);

                }

            } else if (respuesta.equalsIgnoreCase("no")) {
                estado = 3;
            }
        } while (estado == 3);
        return estado;
    }

    /**
     * Método para recibir mensajes desde el Servidor
     *
     * @return Mensaje que llega del servidor. Será un String
     */
    public String getMensajeServidor() {
        String respuesta = "";
        try {
            this.flujoEntrada = new DataInputStream(this.cliente.getInputStream());
            // El servidor me envía un mensaje
            respuesta = this.flujoEntrada.readUTF();
        } catch (IOException e) {
            System.out.println("setFlujoEntrada " + e.getMessage());
        }
        return respuesta;
    }

    /**
     * Método para enviar mensajes al servidor
     *
     * @param msj Será el mensaje que se envia al servidor.
     */
    public void setMensajeServidor(String msj) {
        try {
            this.flujoSalida = new DataOutputStream(this.cliente.getOutputStream());
            this.flujoSalida.writeUTF(msj);
        } catch (IOException e) {
            System.out.println("setFlujoSalida " + e.getMessage());
        }
    }

    /**
     * Cerramos los flujos de entrada/salida y el Socket Cliente
     */
    public void closeStreamsSockets() {
        try {
            this.flujoEntrada.close();
            this.flujoSalida.close();
            this.cliente.close();
        } catch (IOException e) {
            System.out.println("closeStreamSockets " + e.getMessage());
        }
    }

    /**
     * Método máin que instancia un objeto de de la clase.
     *
     * @param arg
     * @throws IOException
     */
    public static void main(String[] arg) throws IOException {
        new Main();
    }

    public static class Configuration {

        final static String HOST = "localhost";
        final static int PUERTO = 4000;
    }
}

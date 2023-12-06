
import java.net.*;
import java.io.*;
import java.util.*;

import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorHttp {

    public static final int PUERTO = 8000;
    ServerSocket ss;

    class Manejador extends Thread {

        protected Socket socket;
        protected PrintWriter pw;
        protected BufferedOutputStream bos;
        protected BufferedReader br;
        protected String FileName = "";

        public Manejador(Socket _socket) throws Exception {
            this.socket = _socket;
        }

        public void run() {
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bos = new BufferedOutputStream(socket.getOutputStream());
                pw = new PrintWriter(new OutputStreamWriter(bos));
                String line = br.readLine();

                if (line == null) {
                    pw.print("<html><head><title>Servidor WEB");
                    pw.print("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>");
                    pw.print("</body></html>");
                    socket.close();
                    return;
                }

                System.out.println("\nCliente Conectado desde: " + socket.getInetAddress());
                System.out.println("Por el puerto: " + socket.getPort());
                System.out.println("Datos: " + line + "\r\n\r\n");
                if (line.toUpperCase().startsWith("GET")) {
                    StringTokenizer tokens = new StringTokenizer(line, "?");
                    String req_a = tokens.nextToken();
                    String req = tokens.nextToken().toString();

                    Map<String, String> params = new HashMap<>();
                    for (String s : req.split("\\?")) {
                        String[] t = s.split("=");
                        params.put(t[0], t[1].split(" ")[0]);
                    }
                    if (params.get("archivo") == null) {
                        SendA("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\parametro-no-valido.html");
                    } else {
                        SendA("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\" + params.get("archivo") + ".html");
                    }
                } else if (line.toUpperCase().startsWith("POST")) {
                    // Si la solicitud es de tipo POST, procesa los datos enviados
                    StringBuilder requestBody = new StringBuilder();
                    int contentLength = 0;

                    // Leer la cabecera para obtener la longitud del cuerpo del mensaje
                    while (br.ready() && !(line = br.readLine()).isEmpty()) {
                        if (line.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(line.substring(16).trim());
                        }
                    }

                    // Leer el cuerpo del mensaje
                    if (contentLength > 0) {
                        char[] buffer = new char[contentLength];
                        int bytesRead = br.read(buffer, 0, contentLength);
                        requestBody.append(buffer, 0, bytesRead);
                    }

                    // Mostrar los datos recibidos en la solicitud POST
                    System.out.println("Datos recibidos en la solicitud POST.");

                    String req = "";
                    Map<String, String> params = new HashMap<>();
                    String[] pairs = requestBody.toString().replaceAll("[{}]", "").split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        String key = keyValue[0].replaceAll("\"", "").trim();
                        String value = keyValue[1].replaceAll("\"", "").trim();
                        params.put(key, value);
                    }

                    if (params.get("archivo") == null) {
                        SendA("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\parametro-no-valido.html");
                    } else {
                        SendA("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\" + params.get("archivo") + ".html");
                    }

                } else if (line.toUpperCase().startsWith("PUT")) {
                    StringBuilder requestBody = new StringBuilder();
                    int contentLength = 0;

                    // Leer la cabecera para obtener la longitud del cuerpo del mensaje
                    while (br.ready() && !(line = br.readLine()).isEmpty()) {
                        if (line.toLowerCase().startsWith("content-length:")) {
                            contentLength = Integer.parseInt(line.substring(16).trim());
                        } else if (line.toLowerCase().startsWith("info-contenido:")) {
                            String[] parts = line.split(";")[2].trim().split("=");
                            FileName = parts[1].replace("\"", "");
                        }
                    }

                    // Leer el cuerpo del mensaje (contenido del archivo)
                    if (contentLength > 0) {
                        char[] buffer = new char[contentLength];
                        int bytesRead = br.read(buffer, 0, contentLength);
                        requestBody.append(buffer, 0, bytesRead);

                        // Guardar el contenido del archivo en un archivo local con el nombre recibido.
                        File outputFile = new File("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\" + FileName + ".html");
                        try (FileWriter fileWriter = new FileWriter(outputFile)) {
                            fileWriter.write(requestBody.toString());
                        }

                        System.out.println("Archivo recibido y guardado correctamente: " + FileName);
                        pw.println("HTTP/1.0 200 Okay");
                        pw.flush();
                        pw.println();
                        pw.flush();
                        pw.print("<html><head><title>SERVIDOR WEB");
                        pw.flush();
                        pw.print("</title></head><body bgcolor=\"#AACCFF\"><center><h1><br>Archivo guardado correctamente.</br></h1></center></body></html>");
                        pw.flush();
                    } else {
                        pw.println("HTTP/1.0 400 Bad Request");
                        pw.println();
                    }
                } else if (line.toUpperCase().startsWith("DELETE")) {
                    String fileToDelete = "";
                    while (br.ready() && !(line = br.readLine()).isEmpty()) {
                        if (line.toLowerCase().startsWith("content-length:")) {
                        } else if (line.toLowerCase().startsWith("info-contenido:")) {
                            String[] parts = line.split(";")[2].trim().split("=");
                            fileToDelete = parts[1].replace("\"", "");
                        }
                    }

                    // Eliminar el archivo
                    File file = new File("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\" + fileToDelete + ".html");
                    if (file.exists()) {
                        if (file.delete()) {
                            pw.println("HTTP/1.0 200 OK");
                            pw.println();
                            pw.flush();
                            pw.print("<html><head><title>SERVIDOR WEB");
                            pw.flush();
                            pw.print("</title></head><body bgcolor=\"#AACCFF\"><center><h1><br>Archivo eliminado correctamente.</br></h1></center></body></html>");
                            pw.flush();
                        } else {
                            pw.println("HTTP/1.0 500 Internal Server Error");
                            pw.println();
                            pw.flush();
                        }
                    } else {
                        pw.println("HTTP/1.0 404 Not Found");
                        pw.println();
                        pw.flush();
                    }
                } else if (line.toUpperCase().startsWith("HEAD")) {
                    String archivo = "";
                    while (br.ready() && !(line = br.readLine()).isEmpty()) {
                        if (line.toLowerCase().startsWith("content-length:")) {
                        } else if (line.toLowerCase().startsWith("info-contenido:")) {
                            String[] parts = line.split(";")[2].trim().split("=");
                            archivo = parts[1].replace("\"", "");
                        }
                    }

                    File file = new File("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\" + archivo + ".html");
                    if (file.exists()) {
                        BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\" + archivo + ".html"));
                        byte[] buf = new byte[1024];
                        int tam_bloque = 0;
                        if (bis2.available() >= 1024) {
                            tam_bloque = 1024;
                        } else {
                            bis2.available();
                        }

                        int tam_archivo = bis2.available();
                        String sb = "";
                        sb = sb + "HTTP/1.0 200 ok\n";
                        sb = sb + "Server: Servidor de Diego y Diego/1.0 \n";
                        sb = sb + "Date: " + new Date() + " \n";
                        sb = sb + "Content-Type: text/html \n";
                        sb = sb + "Content-Length: " + tam_archivo + " \n";
                        sb = sb + "\n";
                        bos.write(sb.getBytes());
                        bos.flush();
                    } else {
                        pw.println("HTTP/1.0 404 Not Found");
                        pw.println();
                        pw.flush();
                    }
                } else {
                    pw.println("HTTP/1.0 501 Not Implemented");
                    pw.println();
                }
                pw.flush();
                bos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void getArch(String line) {
            int i;
            int f;
            if (line.toUpperCase().startsWith("GET")) {
                i = line.indexOf("/");
                f = line.indexOf(" ", i);
                FileName = line.substring(i + 1, f);
            }
        }

        public void SendA(String fileName, Socket sc) {
            int fSize = 0;
            byte[] buffer = new byte[4096];
            try {
                DataOutputStream out = new DataOutputStream(sc.getOutputStream());

                //sendHeader();
                FileInputStream f = new FileInputStream(fileName);
                int x = 0;
                while ((x = f.read(buffer)) > 0) {
                    out.write(buffer, 0, x);
                }
                out.flush();
                f.close();
            } catch (FileNotFoundException e) {
                //msg.printErr("Transaction::sendResponse():1", "El archivo no existe: " + fileName);
            } catch (IOException e) {
                //			System.out.println(e.getMessage());
                //msg.printErr("Transaction::sendResponse():2", "Error en la lectura del archivo: " + fileName);
            }

        }

        public void SendA(String arg) {
            try {
                int b_leidos = 0;
                BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(arg));
                byte[] buf = new byte[1024];
                int tam_bloque = 0;
                if (bis2.available() >= 1024) {
                    tam_bloque = 1024;
                } else {
                    bis2.available();
                }

                int tam_archivo = bis2.available();
                String sb = "";
                sb = sb + "HTTP/1.0 200 ok\n";
                sb = sb + "Server: Servidor de Diego y Diego/1.0 \n";
                sb = sb + "Date: " + new Date() + " \n";
                sb = sb + "Content-Type: text/html \n";
                sb = sb + "Content-Length: " + tam_archivo + " \n";
                sb = sb + "\n";
                bos.write(sb.getBytes());
                bos.flush();

                while ((b_leidos = bis2.read(buf, 0, buf.length)) != -1) {
                    bos.write(buf, 0, b_leidos);

                }
                bos.flush();
                bis2.close();
            } catch (Exception e) {
                SendA("C:\\Users\\diego\\Documents\\Clases\\Aplicaciones para comunicaciones en red\\Aplicaciones-para-comunicaciones-en-red\\Prácticas\\Practica_04\\archivos_servidor\\noEncontrado.html");
            }

        }
    }

    public ServidorHttp() throws Exception {
        System.out.println("Iniciando Servidor http...");
        this.ss = new ServerSocket(PUERTO);
        System.out.println("Servidor iniciado: --- OK");
        System.out.println("Esperando al cliente...");

        ExecutorService es = Executors.newFixedThreadPool(10);

        for (;;) {
            Socket conexion = ss.accept();
            Runnable tarea = new Manejador(conexion);
            es.execute(tarea);
        }
    }

    public static void main(String[] args) throws Exception {
        ServidorHttp sWEB = new ServidorHttp();
    }
}

import jdk.internal.access.JavaSecurityAccess;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
public class Server {

    public static final int MAXIMUM_DATAGRAM_SIZE = 255;
    public static final String ECS = "kraj";
    public static HashMap<String,Integer> tabela;

    public static void main(String[] args) throws IOException {
        tabela= new HashMap<>();
        byte[] clientData1 = new byte[MAXIMUM_DATAGRAM_SIZE];
        byte[] clientData2 = new byte[MAXIMUM_DATAGRAM_SIZE];
        DatagramPacket clientPacket1 = new DatagramPacket(clientData1, clientData1.length);
        DatagramPacket clientPacket2 = new DatagramPacket(clientData2, clientData2.length);
        DatagramSocket serverSocket;
        int serverPort;
        int clientPort1;
        int clientPort2;
        Runnable clientRun1;
        Runnable clientRun2;
        Thread clientThread1;
        Thread clientThread2;
        String clientAlias1;
        String clientAlias2;


        serverPort = 7777;
        serverSocket = new DatagramSocket(serverPort);

        while(true) {

            System.out.println("[" + getTime() + "] | Се чекаат клиенти... |");
            serverSocket.receive(clientPacket1);
            clientAlias1 = new String(clientPacket1.getData());
            clientPort1 = clientPacket1.getPort();
            System.out.println("[" + getTime() + "] | Се конектираше корисникот <" + clientAlias1.replaceAll("[^\\x20-\\x7e]", "")
                    + "> со socket адреса [" + clientPacket1.getSocketAddress() + "] |");
            tabela.put(clientAlias1,clientPort1);
            serverSocket.receive(clientPacket2);
            clientAlias2 = new String(clientPacket2.getData());
            clientPort2 = clientPacket2.getPort();
            System.out.println("[" + getTime() + "] | Се конектираше корисникот <" + clientAlias2.replaceAll("[^\\x20-\\x7e]", "")
                    + "> со socket адреса [" + clientPacket2.getSocketAddress() + "] |");
            tabela.put(clientAlias2,clientPort2);
            clientData2 = clientAlias2.getBytes();
            clientPacket1.setData(clientData2);
            serverSocket.send(clientPacket1);
            clientData1 = clientAlias1.getBytes();
            clientPacket2.setData(clientData1);
            serverSocket.send(clientPacket2);
            clientData2 = String.valueOf(clientPort2).getBytes();
            clientPacket1.setData(clientData2);
            serverSocket.send(clientPacket1);

            clientData1 = String.valueOf(clientPort1).getBytes();
            clientPacket2.setData(clientData1);
            serverSocket.send(clientPacket2);

            clientRun1 = new ServerThread(serverSocket, clientPacket1, clientPacket2, clientAlias1);
            clientThread1 = new Thread(clientRun1);
            clientRun2 = new ServerThread(serverSocket, clientPacket2, clientPacket1, clientAlias2);
            clientThread2 = new Thread(clientRun2);

            clientThread1.start();
            clientThread2.start();

            try{
                clientThread1.join();
                clientThread2.join();
            } catch(InterruptedException interrupt) {
                System.out.println("InterruptedException: " + interrupt);
            }

        }
    }

    private static String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}

class ServerThread implements Runnable {

    protected DatagramSocket socket;
    protected DatagramPacket readPacket;
    protected DatagramPacket writePacket;
    protected InetAddress readAddress;
    protected InetAddress writeAddress;
    protected int readPort;
    protected int writePort;
    protected String userName;

    public ServerThread(DatagramSocket serverSocket, DatagramPacket readPacket, DatagramPacket writePacket, String userName) {
        this.socket = serverSocket;
        this.readPacket = readPacket;
        this.writePacket = writePacket;
        this.readAddress = readPacket.getAddress();
        this.writeAddress = writePacket.getAddress();
        this.readPort = readPacket.getPort();
        this.writePort = writePacket.getPort();
        this.userName = userName;
    }

    public void run() {
        try {
            String message;
            byte[] readBytes;
            byte[] writeBytes;

            while(true) {

                readBytes = new byte[Server.MAXIMUM_DATAGRAM_SIZE];
                readPacket = new DatagramPacket(readBytes, readBytes.length, readAddress, readPort);

                socket.receive(readPacket);
                if(readPacket.getPort() == writePort)
                    continue;
                message = new String(readPacket.getData());
                if(message.equals("lista"))
                {
                    writeBytes=Server.tabela.toString().getBytes();
                    writePacket = new DatagramPacket(writeBytes, writeBytes.length, writeAddress, writePort);
                    socket.send(writePacket);
                }
                if(message.equals(Server.ECS))
                    System.out.println("[" + getTime() + "] | <" + userName + "> се исклучи. |");
                readBytes = Arrays.copyOfRange(readPacket.getData(), readPacket.getOffset(), readPacket.getOffset()+readPacket.getLength());

                writeBytes = readBytes;
                writePacket = new DatagramPacket(writeBytes, writeBytes.length, writeAddress, writePort);

                socket.send(writePacket);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


}
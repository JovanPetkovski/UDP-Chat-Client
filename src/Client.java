import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {

    public static final int MAXIMUM_DATAGRAM_SIZE = 255;
    public static final String ECS = "kraj";
    public static void main(String[] args) throws IOException {

        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        byte[] myData;
        byte[] clientData = new byte[255];
        DatagramSocket clientSocket;
        DatagramPacket myDataPacket;
        DatagramPacket clientDataPacket;
        InetAddress serverIP;
        int serverPort;
        WriteThread write;
        ReadThread read;
        String userName;
        String clientName;

        serverIP = InetAddress.getByName("localhost");
        serverPort = 7777;
        clientSocket = new DatagramSocket();

        clientSocket.connect(serverIP, serverPort);

        System.out.println("Внесете го Вашето корисничко име:");
        userName = userInput.readLine();

        System.out.println("Вашето име е сетирано на " + userName);
        myData = userName.getBytes(StandardCharsets.UTF_8);

        myDataPacket = new DatagramPacket(myData, myData.length, serverIP, serverPort);
        clientSocket.send(myDataPacket);

        clientDataPacket = new DatagramPacket(clientData, clientData.length);
        clientDataPacket.setLength(MAXIMUM_DATAGRAM_SIZE);
        clientSocket.receive(clientDataPacket);
        clientName = new String(clientDataPacket.getData(),StandardCharsets.US_ASCII).replaceAll("[^\\x20-\\x7e]", "");
        clientData = new byte[MAXIMUM_DATAGRAM_SIZE];
        clientDataPacket = new DatagramPacket(clientData, clientData.length);
        clientDataPacket.setLength(MAXIMUM_DATAGRAM_SIZE);
        clientSocket.receive(clientDataPacket);

        write = new WriteThread(clientSocket, serverPort, userName);
        read = new ReadThread(clientSocket, clientName);
        write.start();
        read.start();

        try {
            write.join();
            read.join();
        } catch(InterruptedException interrupt) {
            System.out.println("InterruptedException: " + interrupt);
        }

    }
}
class WriteThread extends Thread implements Runnable {

    protected InetAddress serverIP;
    protected int serverPort;
    protected DatagramSocket writeSocket;
    protected String userName;

    public WriteThread(DatagramSocket clientSocket, int serverPort, String userName) {
        this.writeSocket = clientSocket;
        this.serverPort = serverPort;
        this.serverIP = clientSocket.getInetAddress();
        this.userName = userName;
    }

    public void run() {

        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String writeString;
            byte[] writeBytes;
            DatagramPacket writePacket;

            while(true) {
                writeString = userInput.readLine();
                writeBytes = writeString.getBytes();
                writePacket = new DatagramPacket(writeBytes, writeBytes.length, serverIP, serverPort);
                writeSocket.send(writePacket);
                if((writeString).equals(Client.ECS))
                    break;
                System.out.println("[" + getTime() + "]<" + userName + "> " + new String(writePacket.getData()));
            }

            System.out.println("[" + getTime() + "] | <" + userName + "> се дисконектираше. |");
            writeBytes = writeString.getBytes();
            writePacket = new DatagramPacket(writeBytes, writeBytes.length, serverIP, serverPort);
            writeSocket.send(writePacket);

        } catch (IOException ex) {
            System.out.println("IOException: " + ex);
        }
    }

    private String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
class ReadThread extends Thread implements Runnable {

    protected InetAddress serverIP;
    protected int serverPort;
    protected DatagramSocket clientSocket;
    protected String clientName;

    public ReadThread(DatagramSocket clientSocket, String clientName) {
        this.serverIP = clientSocket.getInetAddress();
        this.serverPort = clientSocket.getPort();
        this.clientSocket = clientSocket;
        this.clientName = clientName;
    }


    public void run() {
        try {
            byte[] readData = new byte[Server.MAXIMUM_DATAGRAM_SIZE];
            DatagramPacket readPacket;
            String readMessage;

            while(true) {
                readPacket = new DatagramPacket(readData, readData.length);

                clientSocket.receive(readPacket);

                readMessage = new String(readPacket.getData(), 0, readPacket.getLength());
                if(readMessage.equals(Client.ECS))
                    break;
                System.out.println("[" + getTime() + "]<" + clientName + "> " + readMessage);
            }
            System.out.println("[" + getTime() + "] | <" + clientName + "> се дисконектираше. |");

        } catch(IOException ex) {
            System.err.println("IOException caught: " + ex);
        } finally {
            clientSocket.close();
        }
    }
    private String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
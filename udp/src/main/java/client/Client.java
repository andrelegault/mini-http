package client;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


public class Client {
    private final String filePath;
    public Client(final String filePath) {
        this.filePath = filePath;
        try {
            run();
        } catch (Exception e) {
             e.printStackTrace();
        }
    }


    /**
     * Loads a file's contents into a String.
     * 
     * @return String Contents of the file.
     * @throws IOException
     */
    private byte[] loadFileContents(Path filePath) throws IOException {
        return Files.readAllBytes(filePath);
    }

    String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    private void run() throws Exception {
        InetAddress address = InetAddress.getLocalHost();

        byte type = 1;
        byte[] seqNum = ByteBuffer.allocate(4).putInt(1).array();
        byte[] serverIP = address.getAddress();
        byte[] serverPort = ByteBuffer.allocate(2).putShort((short) 8007).array();
        byte[] data = loadFileContents(Path.of(this.filePath));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(type);
        baos.write(seqNum);
        baos.write(serverIP);
        baos.write(serverPort);
        baos.write(data);
        byte[] packetBuffer = baos.toByteArray();



        short routerPort = 3000;
        DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, address, routerPort);

        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        System.out.println("Client Sent Packet to host");
        byte[] receiveData = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(recvPacket);
        System.out.println("Client received packet from host:");
        byte[] message = Arrays.copyOfRange(recvPacket.getData(), 11, recvPacket.getLength());
        String messageString = new String(message);
        System.out.println(messageString);
    }

    public static void main(final String[] args) {
        new Client("/Users/michelrahme/Dev/IntelliJ/mini-http/udp/test");
    }
}

package server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


public class Server {
    public Server() {
        try {
            run(8007);
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
//    private byte[] loadFileContents(Path filePath) throws IOException {
//        return Files.readAllBytes(filePath);
//    }
    String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
    private void run(int port) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(port);
            byte[] receiveData = new byte[1024];
            String sendString = "polo";
            byte[] sendData = sendString.getBytes("UTF-8");
            System.out.printf("Listening on udp: %s:%d%n",
                    InetAddress.getLocalHost().getHostAddress(), port);
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            while(true)
            {
                serverSocket.receive(receivePacket);

                byte[] type = Arrays.copyOfRange(receivePacket.getData(), 0, 1);
                byte[] seqNumByte = Arrays.copyOfRange(receivePacket.getData(), 1, 5);
                byte[] peerAddressByte = Arrays.copyOfRange(receivePacket.getData(), 5, 9);
                byte[] portNumberByte = Arrays.copyOfRange(receivePacket.getData(), 9, 11);
                byte[] message = Arrays.copyOfRange(receivePacket.getData(), 11, receivePacket.getLength());
                int typeInt = Integer.parseInt(toBinary(type),2);
                int seqNum = Integer.parseInt(toBinary(seqNumByte),2);
                String peerAddress = InetAddress.getByAddress(peerAddressByte).getHostAddress();
                int portNumber = Integer.parseInt(toBinary(portNumberByte),2);
                String messageString = new String(message);

                System.out.println("Type: " + typeInt);
                System.out.println("Sequence Number: " + seqNum);
                System.out.println("Peer Address: " + peerAddress);
                System.out.println("Client Port: " + portNumber);
                System.out.println("Data: " + messageString);

                // now send acknowledgement packet back to sender
                byte[] replyMessage = "Hi".getBytes();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(type);
                baos.write(seqNumByte);
                baos.write(peerAddressByte);
                baos.write(portNumberByte);
                baos.write(replyMessage);
                byte[] packetBuffer = baos.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(packetBuffer, packetBuffer.length,
                        receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        // should close serverSocket in finally block
    }



    public static void main(final String[] args) {
        new Server();
    }
}

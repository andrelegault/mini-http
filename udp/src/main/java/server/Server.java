package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Server {
    public Server() {
        try {
            run();
        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    private byte[] createByteArray(byte type, byte[] seqNum, byte[] peerAddress, byte[] portNumberByte, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(type);
        baos.write(seqNum);
        baos.write(peerAddress);
        baos.write(portNumberByte);
        baos.write(data);
        return baos.toByteArray();
    }

    private void run() {
        try {
            System.out.printf("Listening on udp: %s:%d%n",
                    InetAddress.getLocalHost().getHostAddress(), 8007);
            DatagramSocket serverSocket = new DatagramSocket(8007);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            boolean synReceived = false;
            while(true)
            {
                int packetNumber = 1;
                serverSocket.receive(receivePacket);

                byte[] type = Arrays.copyOfRange(receivePacket.getData(), 0, 1);
                byte[] seqNumByte = Arrays.copyOfRange(receivePacket.getData(), 1, 5);
                byte[] peerAddressByte = Arrays.copyOfRange(receivePacket.getData(), 5, 9);
                byte[] portNumberByte = Arrays.copyOfRange(receivePacket.getData(), 9, 11);
                byte[] message = Arrays.copyOfRange(receivePacket.getData(), 11, receivePacket.getLength());
                int typeInt = Integer.parseInt(toBinary(type),2);
                int seqNum = Integer.parseInt(toBinary(seqNumByte),2);
                packetNumber++;
                String peerAddress = InetAddress.getByAddress(peerAddressByte).getHostAddress();
                int portNumber = Integer.parseInt(toBinary(portNumberByte),2);
                String messageString = new String(message);

                System.out.println("Type: " + typeInt);
                System.out.println("Sequence Number: " + seqNum);
                System.out.println("Peer Address: " + peerAddress);
                System.out.println("Client Port: " + portNumber);
                System.out.println("Data: " + messageString);

                if(typeInt == 1){
                    synReceived = true;
                    byte[] replyMessage = " ".getBytes();
                    byte requestType = 2;
                    byte[] newSeqNum = ByteBuffer.allocate(4).putInt(packetNumber).array();
                    byte[] packetBuffer = createByteArray(requestType, newSeqNum, peerAddressByte, portNumberByte, replyMessage);
                    DatagramPacket sendPacket = new DatagramPacket(packetBuffer, packetBuffer.length,
                            receivePacket.getAddress(), receivePacket.getPort());
                    serverSocket.send(sendPacket);
                }else if(typeInt == 3 && synReceived){
                    System.out.println("ThreeWay Handshake Completed");
                }else if(typeInt == 4 && synReceived){
                    byte[] replyMessage = "Hi".getBytes();
                    byte requestType = 4;
                    byte[] newSeqNum = ByteBuffer.allocate(4).putInt(packetNumber).array();
                    byte[] packetBuffer = createByteArray(requestType, newSeqNum, peerAddressByte, portNumberByte, replyMessage);
                    DatagramPacket sendPacket = new DatagramPacket(packetBuffer, packetBuffer.length,
                            receivePacket.getAddress(), receivePacket.getPort());
                    serverSocket.send(sendPacket);
                }
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

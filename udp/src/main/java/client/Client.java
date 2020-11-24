package client;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

enum Type { SYN, SYNACK, ACK, DATA }

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

    private byte[] createByteArray(byte type, byte[] seqNum, byte[] serverIP, byte[] serverPort, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(type);
        baos.write(seqNum);
        baos.write(serverIP);
        baos.write(serverPort);
        baos.write(data);
        return baos.toByteArray();
    }

    private DatagramPacket createPacket(InetAddress serverAddress, byte type, int sequenceNum, Boolean dataToSend) throws IOException {
        byte[] data;
        short routerPort = 3000;
        byte[] seqNum = ByteBuffer.allocate(4).putInt(sequenceNum).array();
        byte[] serverIP = serverAddress.getAddress();
        byte[] serverPort = ByteBuffer.allocate(2).putShort((short) 8007).array();
        if(dataToSend) {
            data = loadFileContents(Path.of(this.filePath));
        }else {
            data = new byte[0];
        }
        byte[] packetArray = createByteArray(type, seqNum, serverIP, serverPort, data);
        return new DatagramPacket(packetArray, packetArray.length, serverAddress, routerPort);
    }

    private void sendPacket(DatagramSocket socket, DatagramPacket packet) throws IOException {

        socket.send(packet);

    }

    private byte[] receivePacket(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        System.out.println("Client received packet from host.");

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
        if(typeInt == 4) {
            System.out.println("Data: " + messageString);
        }

        ByteArrayOutputStream packetInfo = new ByteArrayOutputStream();
        packetInfo.write(type);
        packetInfo.write(seqNumByte);
        return packetInfo.toByteArray();
    }

    private void tcpHandshake(InetAddress serverAddress, Type type, int sequenceNum) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        if(type == Type.SYN){
            System.out.println("ThreeWay Handshake Initiated\n");
            System.out.println("SYN Sent To Server\n");
            byte packetType = 1;
            sequenceNum++;
            DatagramPacket packet = createPacket(serverAddress, packetType, sequenceNum,false);
            sendPacket(socket, packet);
            byte[] receivedMessage = receivePacket(socket);

            int receivedPacketType = Integer.parseInt(toBinary(Arrays.copyOfRange(receivedMessage, 0, 1)),2);
            int receivedPacketSeqNum = Integer.parseInt(toBinary(Arrays.copyOfRange(receivedMessage, 1, 5)),2);

            if(receivedPacketType == 2){
                System.out.println("SYN/ACK Received From Server\n");
                tcpHandshake(serverAddress, Type.SYNACK, receivedPacketSeqNum);
            }else{
                System.out.println("SYN/ACK not received");
            }
        }else if(type == Type.SYNACK){
            System.out.println("ACK Sent To Server\n");
            byte packetType = 3;
            sequenceNum++;
            DatagramPacket packet = createPacket(serverAddress, packetType, sequenceNum,false);
            sendPacket(socket, packet);
            tcpHandshake(serverAddress, Type.DATA, sequenceNum);
        }else if(type == Type.DATA){
            System.out.println("ThreeWay Handshake Complete\n");
            System.out.println("Sending Data");
            byte packetType = 4;
            sequenceNum++;
            DatagramPacket packet = createPacket(serverAddress, packetType, sequenceNum,true);
            sendPacket(socket, packet);
            receivePacket(socket);
        }
    }
    private void run() throws Exception {
        InetAddress address = InetAddress.getLocalHost();
        tcpHandshake(address, Type.SYN, 0);

    }

    public static void main(final String[] args) {
        new Client("/Users/michelrahme/Dev/IntelliJ/mini-http/udp/test");
    }
}

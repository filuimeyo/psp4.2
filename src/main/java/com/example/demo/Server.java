package com.example.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static int countclients = 0;

    public static void main(String[] arg) throws IOException {
        ServerSocket serverSocket = null;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            System.out.println("server starting....");
            serverSocket = new ServerSocket(2525);
            while (true){
                Socket clientAccepted = serverSocket.accept();
                countclients++;
                System.out.println("=======================================");
                System.out.println("Client " + countclients + " connected");

                inputStream = new ObjectInputStream(clientAccepted.getInputStream());
                outputStream = new ObjectOutputStream(clientAccepted.getOutputStream());
                String clientMessageRecieved = (String) inputStream.readObject();


                while (true) {
                    if(clientMessageRecieved.equals("quite")) break;
                    String[] numbers = clientMessageRecieved.split("_");

                    System.out.println("Received from client: " + numbers[0] + " " + numbers[1]);
                    int nod = nod(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]));

                    System.out.println("nod of " + numbers[0] + " and " + numbers[1] + " is " + nod);
                    outputStream.writeObject(nod);

                    clientMessageRecieved = (String) inputStream.readObject();
                }

            }

        } catch (Exception ignored) {
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static int nod(int m, int n) {

        return n != 0 ? nod(n, m % n) : m;
    }
}

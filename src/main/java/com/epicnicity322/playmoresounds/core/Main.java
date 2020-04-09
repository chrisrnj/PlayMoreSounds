package com.epicnicity322.playmoresounds.core;

import javax.swing.*;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Main
{
    private static final int PORT = 9999;
    private static ServerSocket socket;

    private static void killIfRunning()
    {
        try {
            socket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
        } catch (BindException e) {
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args)
    {
        killIfRunning();
        System.out.println("I am a bukkit/sponge dependent application! I'm sorry.");

        JFrame frame = new JFrame("Sorry");
        JLabel textArea = new JLabel(" I am a bukkit/sponge dependent application!");

        frame.add(textArea);
        frame.setVisible(true);
        frame.setSize(270, 60);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            Thread.sleep(7500);
            System.exit(0);
        } catch (Exception ignored) {
        }
    }
}

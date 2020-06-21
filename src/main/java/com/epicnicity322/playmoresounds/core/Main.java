package com.epicnicity322.playmoresounds.core;

import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
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
        } catch (Exception ignored) {
        }

        System.exit(0);
    }
}

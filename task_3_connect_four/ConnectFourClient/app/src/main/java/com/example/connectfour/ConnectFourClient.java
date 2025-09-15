package com.example.connectfour;

import java.io.IOException;

public class ConnectFourClient {
    private MainActivity lobbyGUI;

    public ConnectFourClient() throws IOException {
        this.lobbyGUI = new MainActivity();
    }

    public static void main(String[] args) {
        try {
            new ConnectFourClient(); // entry point
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

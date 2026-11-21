package com.proyectodb;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JdiCotizacion().setVisible(true));
    }
}

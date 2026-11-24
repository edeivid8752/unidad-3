package com.proyectodb;

import java.awt.BorderLayout;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame menu = new JFrame("ProyectoCotizaciones - Unidad III");
            menu.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JMenuBar barra = new JMenuBar();
            JMenu opciones = new JMenu("Menú");
            JMenuItem cotizacion = new JMenuItem("Cotización");
            JMenuItem salir = new JMenuItem("Salir");
            cotizacion.addActionListener(e -> {
                JdiCotizacion ventana = new JdiCotizacion(menu, true);
                ventana.setVisible(true);
            });
            salir.addActionListener(e -> menu.dispose());
            opciones.add(cotizacion);
            opciones.addSeparator();
            opciones.add(salir);
            barra.add(opciones);
            menu.setJMenuBar(barra);
            menu.add(new JLabel("Sistema de cotizaciones", SwingConstants.CENTER), BorderLayout.CENTER);
            menu.setSize(560, 320);
            menu.setLocationRelativeTo(null);
            menu.setVisible(true);
        });
    }
}

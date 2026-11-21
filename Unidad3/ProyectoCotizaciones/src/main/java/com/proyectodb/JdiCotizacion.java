package com.proyectodb;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class JdiCotizacion extends JFrame {
    private final CotizacionDb db = new CotizacionDb();
    private final JTextField txtNumCotizacion = new JTextField(16);
    private final JTextField txtDescripcion = new JTextField(28);
    private final JTextField txtPrecio = new JTextField(12);
    private final JSpinner spnPorcentaje = new JSpinner(new SpinnerNumberModel(20.0, 0.0, 100.0, 1.0));
    private final JRadioButton rdb12 = new JRadioButton("12 meses", true);
    private final JRadioButton rdb24 = new JRadioButton("24 meses");
    private final JRadioButton rdb36 = new JRadioButton("36 meses");
    private final JRadioButton rdb48 = new JRadioButton("48 meses");
    private final JDateChooser jdcFecha = new JDateChooser();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[] {"Activo", "Inactivo"});
    private final JLabel lblPagoInicial = new JLabel("$0.00");
    private final JLabel lblPagoMensual = new JLabel("$0.00");
    private final JLabel lblTotalFin = new JLabel("$0.00");
    private final DefaultTableModel modelo = new DefaultTableModel(
        new String[] {"ID", "Núm. cotización", "Descripción", "Precio", "%", "Plazos", "Fecha", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tbDatos = new JTable(modelo);
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnHabilitar = new JButton("Habilitar");
    private final JButton btnDeshabilitar = new JButton("Deshabilitar");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnCerrar = new JButton("Cerrar");
    private int idActual = 0;

    public JdiCotizacion() {
        setTitle("Proyecto de Base de Datos - Cotizaciones");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        crearPantalla();
        configurarEventos();
        limpiar();
        recargarTabla();
    }

    private JPanel fila(String titulo, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setPreferredSize(new Dimension(135, 27));
        p.add(etiqueta, BorderLayout.WEST);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }
    private void crearPantalla() {
        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        setContentPane(contenido);

        JLabel titulo = new JLabel("Administración de cotizaciones");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 23f));
        contenido.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel();
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos de la cotización"));
        formulario.add(fila("Número", txtNumCotizacion));
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(fila("Descripción", txtDescripcion));
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(fila("Precio", txtPrecio));
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(fila("Pago inicial (%)", spnPorcentaje));
        formulario.add(Box.createVerticalStrut(6));

        ButtonGroup plazos = new ButtonGroup();
        JPanel panelPlazos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JRadioButton b : new JRadioButton[] {rdb12, rdb24, rdb36, rdb48}) {
            plazos.add(b);
            panelPlazos.add(b);
        }
        formulario.add(fila("Plazos", panelPlazos));
        formulario.add(Box.createVerticalStrut(6));
        jdcFecha.setDateFormatString("dd/MM/yyyy");
        formulario.add(fila("Fecha", jdcFecha));
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(fila("Estado", cmbStatus));
        formulario.add(Box.createVerticalStrut(14));

        JPanel resumen = new JPanel(new GridLayout(3, 2, 8, 5));
        resumen.setBorder(BorderFactory.createTitledBorder("Cálculo de pagos"));
        resumen.add(new JLabel("Pago inicial:")); resumen.add(lblPagoInicial);
        resumen.add(new JLabel("Total a financiar:")); resumen.add(lblTotalFin);
        resumen.add(new JLabel("Pago mensual:")); resumen.add(lblPagoMensual);
        formulario.add(resumen);

        JPanel botones = new JPanel(new GridLayout(4, 2, 8, 8));
        for (JButton b : new JButton[] {btnNuevo, btnGuardar, btnBuscar, btnLimpiar,
            btnHabilitar, btnDeshabilitar, btnCancelar, btnCerrar}) botones.add(b);
        formulario.add(Box.createVerticalStrut(15));
        formulario.add(botones);
        JPanel lado = new JPanel(new BorderLayout());
        lado.add(formulario, BorderLayout.NORTH);
        lado.setPreferredSize(new Dimension(440, 530));
        contenido.add(lado, BorderLayout.WEST);

        tbDatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbDatos.setRowHeight(26);
        tbDatos.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(tbDatos);
        scroll.setBorder(BorderFactory.createTitledBorder("Cotizaciones registradas (doble clic para editar)"));
        contenido.add(scroll, BorderLayout.CENTER);
        JLabel pie = new JLabel("Consejo: configura MySQL antes de guardar. Puedes seleccionar una fila para habilitarla o deshabilitarla.");
        contenido.add(pie, BorderLayout.SOUTH);
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> limpiar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCancelar.addActionListener(e -> limpiar());
        btnCerrar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        btnBuscar.addActionListener(e -> buscar());
        btnHabilitar.addActionListener(e -> cambiarEstado(1));
        btnDeshabilitar.addActionListener(e -> cambiarEstado(0));
        txtPrecio.getDocument().addDocumentListener(new Cambios(this::actualizarCalculos));
        spnPorcentaje.addChangeListener(e -> actualizarCalculos());
        for (JRadioButton b : new JRadioButton[] {rdb12,rdb24,rdb36,rdb48})
            b.addActionListener(e -> actualizarCalculos());
        tbDatos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) seleccionarFila();
            }
        });
    }

    private int plazo() { return rdb48.isSelected() ? 48 : rdb36.isSelected() ? 36 : rdb24.isSelected() ? 24 : 12; }
    private Cotizacion leerFormulario() {
        String num = txtNumCotizacion.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        if (num.isEmpty() || descripcion.isEmpty()) throw new IllegalArgumentException("Ingresa número y descripción.");
        float precio;
        try { precio = Float.parseFloat(txtPrecio.getText().trim().replace(",", ".")); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("El precio debe ser numérico."); }
        if (!Float.isFinite(precio) || precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (jdcFecha.getDate() == null) throw new IllegalArgumentException("Selecciona una fecha.");
        return new Cotizacion(idActual, num, descripcion, ((Number)spnPorcentaje.getValue()).floatValue(),
            precio, plazo(), jdcFecha.getDate(), cmbStatus.getSelectedIndex() == 0 ? 1 : 0);
    }
    private void actualizarCalculos() {
        try {
            float precio = Float.parseFloat(txtPrecio.getText().trim().replace(",", "."));
            if (!Float.isFinite(precio) || precio < 0) throw new NumberFormatException();
            Cotizacion c = new Cotizacion(0, "", "", ((Number)spnPorcentaje.getValue()).floatValue(), precio, plazo(), new Date(), 1);
            lblPagoInicial.setText(moneda(c.calcularPagoInicial()));
            lblTotalFin.setText(moneda(c.calcularTotalFin()));
            lblPagoMensual.setText(moneda(c.calcularPagoMensual()));
        } catch (NumberFormatException e) {
            lblPagoInicial.setText("$0.00"); lblTotalFin.setText("$0.00"); lblPagoMensual.setText("$0.00");
        }
    }
    private String moneda(float n) { return String.format(java.util.Locale.US, "$%,.2f", n); }
    private void limpiar() {
        idActual = 0;
        txtNumCotizacion.setText(""); txtDescripcion.setText(""); txtPrecio.setText("");
        spnPorcentaje.setValue(20.0); rdb12.setSelected(true);
        jdcFecha.setDate(new Date()); cmbStatus.setSelectedIndex(0);
        btnGuardar.setText("Guardar");
        tbDatos.clearSelection(); actualizarCalculos();
    }
    private void mostrar(Cotizacion c) {
        idActual = c.getIdCotizacion();
        txtNumCotizacion.setText(c.getNumCotizacion()); txtDescripcion.setText(c.getDescripcion());
        txtPrecio.setText(Float.toString(c.getPrecio())); spnPorcentaje.setValue((double)c.getPorcentaje());
        switch (c.getPlazos()) { case 24 -> rdb24.setSelected(true); case 36 -> rdb36.setSelected(true);
            case 48 -> rdb48.setSelected(true); default -> rdb12.setSelected(true); }
        jdcFecha.setDate(c.getFecha()); cmbStatus.setSelectedIndex(c.getStatus() == 1 ? 0 : 1);
        btnGuardar.setText("Actualizar"); actualizarCalculos();
    }
    private void error(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    private void guardar() {
        try {
            Cotizacion c = leerFormulario();
            int res = idActual == 0 ? db.insertar(c) : db.actualizar(c);
            if (res < 0) throw new IllegalStateException("No se pudo conectar a MySQL. Revisa la guía de instalación.");
            if (res == 0) throw new IllegalStateException("No se encontró el registro para actualizar.");
            JOptionPane.showMessageDialog(this, "Cotización guardada correctamente.");
            limpiar(); recargarTabla();
        } catch (Exception ex) { error(ex); }
    }
    private void buscar() {
        String num = JOptionPane.showInputDialog(this, "Número de cotización a buscar:", txtNumCotizacion.getText());
        if (num == null || num.trim().isEmpty()) return;
        try {
            Cotizacion c = (Cotizacion) db.buscar(num.trim());
            if (c == null) JOptionPane.showMessageDialog(this, "No se encontró la cotización.");
            else mostrar(c);
        } catch (Exception ex) { error(ex); }
    }
    private int idSeleccionado() {
        int row = tbDatos.getSelectedRow();
        return row < 0 ? idActual : ((Number)modelo.getValueAt(tbDatos.convertRowIndexToModel(row), 0)).intValue();
    }
    private void seleccionarFila() {
        int id = idSeleccionado();
        if (id <= 0) return;
        try {
            Cotizacion c = (Cotizacion)db.buscarId(id);
            if (c != null) mostrar(c);
        } catch (Exception ex) { error(ex); }
    }
    private void cambiarEstado(int estado) {
        int id = idSeleccionado();
        if (id <= 0) { JOptionPane.showMessageDialog(this, "Selecciona primero un registro de la tabla."); return; }
        try {
            int res = estado == 1 ? db.habilitar(id) : db.deshabilitar(id);
            if (res < 0) throw new IllegalStateException("No hay conexión con MySQL.");
            recargarTabla();
            if (idActual == id) cmbStatus.setSelectedIndex(estado == 1 ? 0 : 1);
        } catch (Exception ex) { error(ex); }
    }
    private void recargarTabla() {
        modelo.setRowCount(0);
        try {
            for (Cotizacion c : db.listarTodos()) {
                modelo.addRow(new Object[] {c.getIdCotizacion(), c.getNumCotizacion(), c.getDescripcion(),
                    moneda(c.getPrecio()), c.getPorcentaje(), c.getPlazos(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(c.getFecha()), c.getStatus() == 1 ? "Activo" : "Inactivo"});
            }
        } catch (Exception ex) { System.err.println("No se pudo cargar la tabla: " + ex.getMessage()); }
    }
    private record Cambios(Runnable onChange) implements javax.swing.event.DocumentListener {
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
    }
}

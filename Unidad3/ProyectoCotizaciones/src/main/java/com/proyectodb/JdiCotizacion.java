package com.proyectodb;

import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class JdiCotizacion extends JDialog {
    private final CotizacionDb db = new CotizacionDb();
    private final JTextField txtNumCotizacion = new JTextField(16);
    private final JTextField txtDescripcion = new JTextField(28);
    private final JTextField txtPrecio = new JTextField(12);
    private final JSpinner spnPorcentaje = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
    private final JRadioButton rdb12 = new JRadioButton("12 meses", true);
    private final JRadioButton rdb24 = new JRadioButton("24 meses");
    private final JRadioButton rdb36 = new JRadioButton("36 meses");
    private final JRadioButton rdb48 = new JRadioButton("48 meses");
    private final JDateChooser jdcFecha = new JDateChooser();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[] {"Activas", "Inactivas"});
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
    private int statusActual = 0;

    public JdiCotizacion() {
        this(null, false);
    }

    public JdiCotizacion(java.awt.Frame propietario, boolean modal) {
        super(propietario, modal);
        setTitle(":: Cotización ::");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        initComponents();
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(propietario);
        limpiar();
        deshabilitar();
        recargarTabla();
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        return lbl;
    }

    private void initComponents() {
        JPanel contenido = new JPanel(new BorderLayout(18, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        setContentPane(contenido);
        JPanel izquierda = new JPanel(new BorderLayout(0, 15));
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0;
        formulario.add(etiqueta("Num. Cotización"), g);
        g.gridx = 1; formulario.add(txtNumCotizacion, g);
        g.gridx = 2; formulario.add(btnBuscar, g);
        g.gridx = 0; g.gridy = 1; formulario.add(etiqueta("Descripción"), g);
        g.gridx = 1; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(txtDescripcion, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        g.gridx = 0; g.gridy = 2; formulario.add(etiqueta("Porcentaje"), g);
        g.gridx = 1; formulario.add(spnPorcentaje, g);
        g.gridx = 0; g.gridy = 3; formulario.add(etiqueta("Precio"), g);
        g.gridx = 1; formulario.add(txtPrecio, g);
        g.gridx = 0; g.gridy = 4; formulario.add(etiqueta("Plazos"), g);
        ButtonGroup grupo = new ButtonGroup();
        JPanel plazos = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (JRadioButton b : new JRadioButton[] {rdb12, rdb24, rdb36, rdb48}) {
            grupo.add(b); plazos.add(b);
        }
        g.gridx = 1; g.gridwidth = 2; formulario.add(plazos, g);
        g.gridx = 3; g.gridwidth = 1; g.weightx = 1;
        formulario.add(Box.createHorizontalGlue(), g);

        JPanel resumen = new JPanel(new GridLayout(2, 3, 8, 12));
        resumen.setBackground(new Color(196, 192, 250));
        resumen.setBorder(BorderFactory.createEmptyBorder(15, 8, 15, 8));
        for (String titulo : new String[] {"Pago Inicial", "Total a Financiar", "Pago Mensual"}) {
            JLabel l = etiqueta(titulo); l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(l.getFont().deriveFont(16f)); resumen.add(l);
        }
        for (JLabel l : new JLabel[] {lblPagoInicial, lblTotalFin, lblPagoMensual}) {
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(l.getFont().deriveFont(Font.BOLD, 17f)); resumen.add(l);
        }
        JPanel arriba = new JPanel(new BorderLayout(0, 16));
        arriba.add(formulario, BorderLayout.NORTH);
        arriba.add(resumen, BorderLayout.SOUTH);
        izquierda.add(arriba, BorderLayout.NORTH);

        JPanel listado = new JPanel(new BorderLayout(0, 8));
        JPanel encabezado = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 0));
        encabezado.add(etiqueta("Mis Cotizaciones")); encabezado.add(cmbStatus);
        listado.add(encabezado, BorderLayout.NORTH);
        tbDatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbDatos.setRowHeight(24); tbDatos.setAutoCreateRowSorter(true);
        listado.add(new JScrollPane(tbDatos), BorderLayout.CENTER);
        izquierda.add(listado, BorderLayout.CENTER);
        JPanel inferiores = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 0));
        inferiores.add(btnLimpiar); inferiores.add(btnCancelar); inferiores.add(btnCerrar);
        izquierda.add(inferiores, BorderLayout.SOUTH);
        contenido.add(izquierda, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new BorderLayout());
        JPanel fecha = new JPanel(new BorderLayout(8, 0));
        jdcFecha.setDateFormatString("dd/MM/yyyy");
        jdcFecha.setPreferredSize(new Dimension(130, 28));
        fecha.add(etiqueta("Fecha"), BorderLayout.WEST); fecha.add(jdcFecha, BorderLayout.CENTER);
        JPanel acciones = new JPanel(); acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        acciones.add(fecha); acciones.add(Box.createVerticalStrut(18));
        for (JButton b : new JButton[] {btnNuevo, btnGuardar, btnDeshabilitar, btnHabilitar}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT); b.setMaximumSize(new Dimension(155, 35));
            acciones.add(b); acciones.add(Box.createVerticalStrut(15));
        }
        derecha.add(acciones, BorderLayout.NORTH); contenido.add(derecha, BorderLayout.EAST);
        configurarEventos();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> { limpiar(); habilitar(); txtNumCotizacion.requestFocusInWindow(); });
        btnLimpiar.addActionListener(e -> limpiar());
        btnCancelar.addActionListener(e -> { limpiar(); deshabilitar(); });
        btnCerrar.addActionListener(e -> confirmarCierre());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { confirmarCierre(); }
        });
        cmbStatus.addActionListener(e -> { limpiar(); deshabilitar(); recargarTabla(); });
        tbDatos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualizarBotonesEstado();
        });
        btnGuardar.addActionListener(e -> guardar());
        btnBuscar.addActionListener(e -> buscar());
        btnHabilitar.addActionListener(e -> cambiarEstado(0));
        btnDeshabilitar.addActionListener(e -> cambiarEstado(1));
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
    public boolean validar() {
        try { comprobarCampos(); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }
    private void comprobarCampos() {
        String num = txtNumCotizacion.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        if (num.isEmpty() || descripcion.isEmpty()) throw new IllegalArgumentException("Ingresa número y descripción.");
        if (idActual == 0 && (num.length() > 15 || descripcion.length() > 50))
            throw new IllegalArgumentException("Número: máximo 15 caracteres; descripción: máximo 50.");
        float precio;
        try { precio = Float.parseFloat(txtPrecio.getText().trim().replace(",", ".")); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("El precio debe ser numérico."); }
        if (!Float.isFinite(precio) || precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (jdcFecha.getDate() == null) throw new IllegalArgumentException("Selecciona una fecha.");
        try { spnPorcentaje.commitEdit(); }
        catch (java.text.ParseException ex) { throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100."); }
    }
    public Cotizacion llenarObjeto() {
        comprobarCampos();
        return new Cotizacion(idActual, txtNumCotizacion.getText().trim(), txtDescripcion.getText().trim(),
            ((Number)spnPorcentaje.getValue()).floatValue(),
            Float.parseFloat(txtPrecio.getText().trim().replace(",", ".")), plazo(),
            new java.sql.Date(jdcFecha.getDate().getTime()), statusActual);
    }
    private void actualizarCalculos() {
        try {
            float precio = Float.parseFloat(txtPrecio.getText().trim().replace(",", "."));
            if (!Float.isFinite(precio) || precio < 0) throw new NumberFormatException();
            Cotizacion c = new Cotizacion(0, "", "", ((Number)spnPorcentaje.getValue()).floatValue(), precio, plazo(), new Date(), 0);
            lblPagoInicial.setText(moneda(c.calcularPagoInicial()));
            lblTotalFin.setText(moneda(c.calcularTotalFin()));
            lblPagoMensual.setText(moneda(c.calcularPagoMensual()));
        } catch (NumberFormatException e) {
            lblPagoInicial.setText("$0.00"); lblTotalFin.setText("$0.00"); lblPagoMensual.setText("$0.00");
        }
    }
    private String moneda(float n) { return String.format(java.util.Locale.US, "$%,.2f", n); }
    public void limpiar() {
        idActual = 0; statusActual = 0;
        txtNumCotizacion.setText(""); txtDescripcion.setText(""); txtPrecio.setText("");
        spnPorcentaje.setValue(0.0); rdb12.setSelected(true);
        jdcFecha.setDate(new Date());
        btnGuardar.setText("Guardar");
        tbDatos.clearSelection(); actualizarCalculos(); actualizarBotonesEstado();
        if (txtNumCotizacion.isEnabled()) txtNumCotizacion.requestFocusInWindow();
    }
    public void mostrarDatos(Cotizacion c) {
        tbDatos.clearSelection();
        idActual = c.getIdCotizacion(); statusActual = c.getStatus();
        txtNumCotizacion.setText(c.getNumCotizacion()); txtDescripcion.setText(c.getDescripcion());
        txtPrecio.setText(Float.toString(c.getPrecio())); spnPorcentaje.setValue((double)c.getPorcentaje());
        switch (c.getPlazos()) { case 24 -> rdb24.setSelected(true); case 36 -> rdb36.setSelected(true);
            case 48 -> rdb48.setSelected(true); default -> rdb12.setSelected(true); }
        jdcFecha.setDate(c.getFecha());
        habilitar(); actualizarBotonesEstado();
        btnGuardar.setText("Actualizar"); actualizarCalculos();
    }
    public void mostrarMensaje(String msg, String titulo) {
        JOptionPane.showMessageDialog(this, msg, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
    private void error(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    public void habilitar() { cambiarCaptura(true); }
    public void deshabilitar() { cambiarCaptura(false); btnHabilitar.setEnabled(false); btnDeshabilitar.setEnabled(false); }
    private void cambiarCaptura(boolean activo) {
        for (JComponent c : new JComponent[] {txtNumCotizacion, txtDescripcion, txtPrecio,
                spnPorcentaje, rdb12, rdb24, rdb36, rdb48, jdcFecha, btnGuardar}) c.setEnabled(activo);
    }
    private void actualizarBotonesEstado() {
        boolean seleccionado = idSeleccionado() > 0;
        int estado = tbDatos.getSelectedRow() >= 0 ? cmbStatus.getSelectedIndex() : statusActual;
        btnHabilitar.setEnabled(seleccionado && estado == 1);
        btnDeshabilitar.setEnabled(seleccionado && estado == 0);
    }
    private void confirmarCierre() {
        if (JOptionPane.showConfirmDialog(this, "¿Deseas salir?", "Cerrar cotización",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) dispose();
    }
    private void guardar() {
        try {
            if (!validar()) { comprobarCampos(); return; }
            Cotizacion c = llenarObjeto();
            int res = idActual == 0 ? db.insertar(c) : db.actualizar(c);
            if (res < 0) throw new IllegalStateException("No se pudo conectar a MySQL. Revisa la guía de instalación.");
            if (res == 0) throw new IllegalStateException("No se encontró el registro para actualizar.");
            mostrarMensaje("Cotización guardada correctamente.", "Cotización");
            limpiar(); deshabilitar(); recargarTabla();
        } catch (Exception ex) { error(ex); }
    }
    private void buscar() {
        String num = JOptionPane.showInputDialog(this, "Número de cotización a buscar:", txtNumCotizacion.getText());
        if (num == null || num.trim().isEmpty()) return;
        try {
            Cotizacion c = (Cotizacion) db.buscar(num.trim());
            if (c == null) JOptionPane.showMessageDialog(this, "No se encontró la cotización.");
            else mostrarDatos(c);
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
            if (c != null) mostrarDatos(c);
        } catch (Exception ex) { error(ex); }
    }
    private void cambiarEstado(int estado) {
        int id = idSeleccionado();
        if (id <= 0) { JOptionPane.showMessageDialog(this, "Selecciona primero un registro de la tabla."); return; }
        try {
            int res = estado == 0 ? db.habilitar(id) : db.deshabilitar(id);
            if (res < 0) throw new IllegalStateException("No hay conexión con MySQL.");
            if (res == 0) throw new IllegalStateException("No se encontró el registro.");
            limpiar(); deshabilitar(); recargarTabla();
        } catch (Exception ex) { error(ex); }
    }
    private void recargarTabla() {
        modelo.setRowCount(0);
        tbDatos.setToolTipText(null);
        try {
            for (Cotizacion c : db.listarTodos()) {
                if (c.getStatus() != cmbStatus.getSelectedIndex()) continue;
                modelo.addRow(new Object[] {c.getIdCotizacion(), c.getNumCotizacion(), c.getDescripcion(),
                    moneda(c.getPrecio()), c.getPorcentaje(), c.getPlazos(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(c.getFecha()), c.getStatus() == 0 ? "Activo" : "Inactivo"});
            }
        } catch (Exception ex) { System.err.println("No se pudo cargar la tabla: " + ex.getMessage());
            tbDatos.setToolTipText("No se pudo cargar MySQL. Revisa el README y la consola."); }
    }
    private record Cambios(Runnable onChange) implements javax.swing.event.DocumentListener {
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
    }
}

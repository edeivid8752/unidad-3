package com.proyectodb;

import java.sql.*;

public class CotizacionDb implements dbMetodos {
    private final String url = System.getenv().getOrDefault("COT_DB_URL", "jdbc:mysql://localhost:3306/proyecto_cotizaciones");
    private final String usuario = System.getenv().getOrDefault("COT_DB_USER", "root");
    private final String password = System.getenv().getOrDefault("COT_DB_PASSWORD", "");
    private Connection conexion;

    @Override public boolean abrir() {
        try {
            if (conexion == null || conexion.isClosed()) conexion = DriverManager.getConnection(url, usuario, password);
            return true;
        } catch (SQLException e) { System.err.println("Error de conexión: " + e.getMessage()); return false; }
    }
    @Override public boolean cerrar() {
        try {
            if (conexion != null) conexion.close();
            conexion = null;
            return true;
        } catch (SQLException e) { System.err.println(e.getMessage()); return false; }
    }
    public String probarConexion() {
        if (!abrir()) return "No se pudo conectar. Revisa MySQL, la base y las credenciales.";
        cerrar();
        return "Conexión correcta a MySQL.";
    }
    private int ejecutar(String sql, Object... datos) {
        if (!abrir()) return -1;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (int i = 0; i < datos.length; i++) ps.setObject(i + 1, datos[i]);
            return ps.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Error en la base de datos: " + e.getMessage(), e); }
        finally { cerrar(); }
    }
    private Object consultarUno(String sql, Object valor) {
        if (!abrir()) return null;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? convertir(rs) : null; }
        } catch (SQLException e) { throw new IllegalStateException(e.getMessage(), e); }
        finally { cerrar(); }
    }
    private Cotizacion convertir(ResultSet rs) throws SQLException {
        return new Cotizacion(rs.getInt("idCotizacion"), rs.getString("numCotizacion"),
            rs.getString("descripcion"), rs.getFloat("porcentaje"), rs.getFloat("precio"),
            rs.getInt("plazos"), rs.getDate("fecha"), rs.getInt("status"));
    }
    private Object[] valores(Cotizacion c) {
        if (c.getFecha() == null) throw new IllegalArgumentException("La fecha es obligatoria");
        return new Object[] {c.getNumCotizacion(), c.getDescripcion(), c.getPrecio(),
            c.getPorcentaje(), c.getPlazos(), new java.sql.Date(c.getFecha().getTime()), c.getStatus()};
    }
    @Override public int insertar(Object obj) {
        return ejecutar("INSERT INTO Cotizacion(numCotizacion,descripcion,precio,porcentaje,plazos,fecha,status) VALUES (?,?,?,?,?,?,?)", valores((Cotizacion) obj));
    }
    @Override public int actualizar(Object obj) {
        Cotizacion c = (Cotizacion) obj;
        Object[] valores = valores(c);
        return ejecutar("UPDATE Cotizacion SET numCotizacion=?,descripcion=?,precio=?,porcentaje=?,plazos=?,fecha=?,status=? WHERE idCotizacion=?",
            valores[0],valores[1],valores[2],valores[3],valores[4],valores[5],valores[6],c.getIdCotizacion());
    }
    @Override public int habilitar(int id) { return ejecutar("UPDATE Cotizacion SET status=1 WHERE idCotizacion=?", id); }
    @Override public int deshabilitar(int id) { return ejecutar("UPDATE Cotizacion SET status=0 WHERE idCotizacion=?", id); }
    @Override public Object buscarId(int id) { return consultarUno("SELECT * FROM Cotizacion WHERE idCotizacion=?", id); }
    @Override public Object buscar(String num) { return consultarUno("SELECT * FROM Cotizacion WHERE numCotizacion=?", num); }
    @Override public int borrar(int id) { return ejecutar("DELETE FROM Cotizacion WHERE idCotizacion=?", id); }

    // Estos ResultSet se devuelven desconectados mediante CachedRowSet para evitar fugas de conexiones.
    private ResultSet listado(int status) {
        if (!abrir()) return null;
        try (PreparedStatement ps = conexion.prepareStatement("SELECT * FROM Cotizacion WHERE status=? ORDER BY idCotizacion DESC")) {
            ps.setInt(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                javax.sql.rowset.CachedRowSet copia = javax.sql.rowset.RowSetProvider.newFactory().createCachedRowSet();
                copia.populate(rs);
                return copia;
            }
        } catch (SQLException e) { throw new IllegalStateException(e.getMessage(), e); }
        finally { cerrar(); }
    }
    @Override public ResultSet buscarActivos() { return listado(1); }
    @Override public ResultSet buscarNoActivos() { return listado(0); }
    public java.util.List<Cotizacion> listarTodos() {
        java.util.List<Cotizacion> lista = new java.util.ArrayList<>();
        if (!abrir()) return lista;
        try (Statement st = conexion.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM Cotizacion ORDER BY idCotizacion DESC")) {
            while (rs.next()) lista.add(convertir(rs));
            return lista;
        } catch (SQLException e) { throw new IllegalStateException(e.getMessage(), e); }
        finally { cerrar(); }
    }
}

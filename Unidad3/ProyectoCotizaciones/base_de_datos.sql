CREATE DATABASE IF NOT EXISTS proyecto_cotizaciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE proyecto_cotizaciones;

CREATE TABLE IF NOT EXISTS Cotizacion (
    idCotizacion INT AUTO_INCREMENT PRIMARY KEY,
    numCotizacion VARCHAR(40) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    plazos INT NOT NULL,
    fecha DATE NOT NULL,
    status TINYINT NOT NULL DEFAULT 1
);

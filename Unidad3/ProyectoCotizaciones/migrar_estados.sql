-- SOLO para la base del ZIP anterior (1=activo, 0=inactivo).
-- Hacer respaldo y cerrar la aplicación antes de ejecutar todo el archivo.
-- Conserva registros y los tipos/longitudes existentes para evitar pérdidas.
USE proyecto_cotizaciones;
CREATE TABLE IF NOT EXISTS cotizacion_migraciones (
    nombre VARCHAR(100) PRIMARY KEY
) ENGINE=InnoDB;
DROP PROCEDURE IF EXISTS migrar_cotizacion_estado_u3;
DELIMITER $$
CREATE PROCEDURE migrar_cotizacion_estado_u3()
BEGIN
    DECLARE estado_default VARCHAR(100);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    SELECT COLUMN_DEFAULT INTO estado_default
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'Cotizacion' AND COLUMN_NAME = 'status';
    IF NOT EXISTS (SELECT 1 FROM cotizacion_migraciones WHERE nombre = 'status_0_activo_u3') THEN
        IF estado_default IS NULL OR estado_default NOT IN ('0','1') THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tabla o valor por defecto no reconocido. No se modificaron los datos.';
        END IF;
        START TRANSACTION;
        IF estado_default = '1' THEN
            IF EXISTS (SELECT 1 FROM Cotizacion WHERE status IS NULL OR status NOT IN (0,1)) THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Existen estados no reconocidos. Revisa antes de migrar.';
            END IF;
            UPDATE Cotizacion SET status = 1 - status WHERE status IN (0,1) AND idCotizacion > 0;
        END IF;
        INSERT INTO cotizacion_migraciones VALUES ('status_0_activo_u3');
        COMMIT;
    END IF;
    ALTER TABLE Cotizacion ALTER COLUMN status SET DEFAULT 0;
END$$
DELIMITER ;
CALL migrar_cotizacion_estado_u3();
DROP PROCEDURE IF EXISTS migrar_cotizacion_estado_u3;
SELECT status, COUNT(*) AS registros FROM Cotizacion GROUP BY status;

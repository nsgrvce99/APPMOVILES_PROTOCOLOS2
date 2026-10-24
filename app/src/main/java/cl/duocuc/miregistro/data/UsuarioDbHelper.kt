package cl.duocuc.miregistro.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.miregistro.model.*

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztekterreno.db"
        private const val VERSION_BD = 9  // Subimos versión por la nueva tabla

        // ---- ROL ----
        private const val TABLA_ROLES = "rol"
        private const val ROL_ID = "id_rol"
        private const val ROL_CODIGO = "codigo"
        private const val ROL_NOMBRE = "nombre"

        // ---- USUARIO ----
        private const val TABLA_USUARIOS = "usuario"
        private const val USU_ID = "id_usuario"
        private const val USU_ROL = "id_rol"
        private const val USU_RUT = "rut"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_CONTRA = "contraseña"
        private const val USU_TELEFONO = "telefono"
        private const val USU_ACTIVO = "activo"
        private const val USU_GIRO = "giro"
        private const val USU_CONTACTO_NOMBRE = "contacto_nombre"
        private const val USU_CONTACTO_CARGO = "contacto_cargo"

        // ---- EQUIPO ----
        private const val TABLA_EQUIPOS = "equipo"
        private const val EQ_ID = "id_equipo"
        private const val EQ_TAG = "tag"
        private const val EQ_TIPO = "tipo_equipo"
        private const val EQ_MARCA = "marca"
        private const val EQ_SERIE = "numero_serie"
        private const val EQ_INSTALACION = "instalacion"

        private const val EQ_ID_CLIENTE = "id_cliente"

        // ---- PROTOCOLO ----
        private const val TABLA_PROTOCOLOS = "protocolo"
        private const val PRO_ID = "id_protocolo"
        private const val PRO_EQUIPO_ID = "id_equipo"
        private const val PRO_TECNICO_ID = "id_usuario_registra"
        private const val PRO_FECHA = "fecha_hora"
        private const val PRO_TEMP = "temperatura"
        private const val PRO_HUM = "humedad"
        private const val PRO_RESULTADO = "resultado_global"
        private const val PRO_ESTADO = "estado"

        private const val PRO_MOTIVO_OBS = "motivo_observacion"

        // ---- MEDICION ----
        private const val TABLA_MEDICIONES = "medicion"
        private const val MED_ID = "id_medicion"
        private const val MED_PROTOCOLO_ID = "id_protocolo"
        private const val MED_TIPO_PRUEBA = "tipo_prueba"
        private const val MED_LECTURA_1MIN = "lectura_1min"
        private const val MED_LECTURA_10MIN = "lectura_10min"
        private const val MED_IP = "indice_polarizacion"

        // ---- RECEPCION ACTA ----
        private const val TABLA_RECEPCION = "recepcion_acta"
        private const val REC_ID = "id_recepcion"
        private const val REC_PROTOCOLO_ID = "id_protocolo"
        private const val REC_NOMBRE = "nombre_receptor"
        private const val REC_RUT = "rut_receptor"
        private const val REC_CARGO = "cargo_receptor"
        private const val REC_FECHA = "fecha_hora_recepcion"

        // -- INSTRUMENTOS --
        private const val TABLA_INSTRUMENTOS = "instrumento"
        private const val INS_ID = "id_instrumento"
        private const val INS_NOMBRE = "nombre"
        private const val INS_MARCA = "marca"
        private const val INS_SERIE = "numero_serie"
        private const val INS_VENCIMIENTO = "fecha_vencimiento"

        // INSTALACIONES
        private const val TABLA_INSTALACIONES = "instalacion"
        private const val INST_ID = "id_instalacion"
        private const val INST_NOMBRE = "nombre"
        private const val INST_CLIENTE_ID = "id_cliente"

        // ---- SQL DE CREACIÓN ----
        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_CODIGO TEXT NOT NULL UNIQUE, " +
                    "$ROL_NOMBRE TEXT NOT NULL)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_ROL INTEGER NOT NULL, " +
                    "$USU_RUT TEXT NOT NULL UNIQUE, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL UNIQUE, " +
                    "$USU_CONTRA TEXT NOT NULL UNIQUE, " +
                    "$USU_TELEFONO TEXT, " +
                    "$USU_ACTIVO INTEGER NOT NULL DEFAULT 1, " +
                    "$USU_GIRO TEXT, " +
                    "$USU_CONTACTO_NOMBRE TEXT, " +
                    "$USU_CONTACTO_CARGO TEXT, " +
                    "FOREIGN KEY ($USU_ROL) REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_CREAR_INSTRUMENTOS =
            "CREATE TABLE $TABLA_INSTRUMENTOS (" +
                    "$INS_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$INS_NOMBRE TEXT NOT NULL, " +
                    "$INS_MARCA TEXT, " +
                    "$INS_SERIE TEXT, " +
                    "$INS_VENCIMIENTO TEXT NOT NULL)"

        private const val SQL_CREAR_INSTALACIONES =
            "CREATE TABLE $TABLA_INSTALACIONES (" +
                    "$INST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$INST_NOMBRE TEXT NOT NULL, " +
                    "$INST_CLIENTE_ID INTEGER NOT NULL)"

        private const val SQL_PRECARGAR_INSTALACIONES =
            "INSERT INTO $TABLA_INSTALACIONES ($INST_ID, $INST_NOMBRE, $INST_CLIENTE_ID) VALUES " +
                    "(1, 'Planta Lampa', 5), " +
                    "(2, 'Planta Elevadora Sur', 5), " +
                    "(3, 'Faena Til Til', 5), " +
                    "(4, 'Edificio Clínico', 5), " +
                    "(5, 'Planta Molienda', 5), " +
                    "(6, 'Site Huechuraba', 5)"
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_CODIGO, $ROL_NOMBRE) VALUES " +
                    "(1, 'TECNICO', 'P-01 Técnico de terreno'), " +
                    "(2, 'INGENIERO', 'P-02 Ingeniero de pruebas'), " +
                    "(3, 'JEFE_TECNICO', 'P-03 Jefe técnico'), " +
                    "(4, 'CLIENTE', 'P-04 Usuario cliente (solo consulta)')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_ROL, $USU_RUT, $USU_NOMBRE, $USU_CORREO, $USU_CONTRA, $USU_ACTIVO) VALUES " +
                    "(1, '17.284.519-3', 'Matías Godoy Herrera', 'mgodoy@induztek.ejemplo', '1470MAT', 1), " +
                    "(1, '19.412.087-7', 'Javiera Tapia Muñoz', 'jtapia@induztek.ejemplo', '1470JA', 1), " +
                    "(2, '15.906.334-8', 'Andrea Contreras Silva', 'acontreras@induztek.ejemplo', '1470AN', 1), " +
                    "(3, '13.871.905-7', 'Ricardo Espinoza Lagos', 'respinoza@induztek.ejemplo', '1470RI', 1), " +
                    "(4, '14.226.791-8', 'Marcela Fuentes Rojas', 'mfuentes@envpacifico.ejemplo', '1470MA', 1)"

        private const val SQL_PRECARGAR_INSTRUMENTOS =
            "INSERT INTO $TABLA_INSTRUMENTOS ($INS_NOMBRE, $INS_MARCA, $INS_SERIE, $INS_VENCIMIENTO) VALUES " +
                    "('Megóhmetro 5 kV', 'Megger MIT525', 'MIT-0045821', '15-03-2027'), " +
                    "('Megóhmetro 10 kV', 'Megger MIT1025', 'MIT-0071233', '10-10-2026'), " +
                    "('Medidor de relación (TTR)', 'Megger TTR300', 'TTR-300440', '02-06-2027'), " +
                    "('Micro-ohmímetro', 'Megger DLRO10X', 'DLR-10882', '20-08-2026'), " +
                    "('Equipo de inyección secundaria', 'Omicron CMC 356', 'CMC-HB411', '05-01-2027')"

        private const val SQL_CREAR_EQUIPOS =
            "CREATE TABLE $TABLA_EQUIPOS (" +
                    "$EQ_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$EQ_TAG TEXT NOT NULL, " +
                    "$EQ_TIPO TEXT NOT NULL, " +
                    "$EQ_MARCA TEXT, " +
                    "$EQ_SERIE TEXT, " +
                    "$EQ_INSTALACION TEXT NOT NULL, " +
                    "$EQ_ID_CLIENTE INTEGER NOT NULL DEFAULT 0)"

        private const val SQL_CREAR_PROTOCOLOS =
            "CREATE TABLE $TABLA_PROTOCOLOS (" +
                    "$PRO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$PRO_EQUIPO_ID INTEGER NOT NULL, " +
                    "$PRO_TECNICO_ID INTEGER NOT NULL, " +
                    "$PRO_FECHA TEXT NOT NULL, " +
                    "$PRO_TEMP TEXT, " +
                    "$PRO_HUM TEXT, " +
                    "$PRO_RESULTADO TEXT, " +
                    "$PRO_ESTADO TEXT NOT NULL, " +
                    "$PRO_MOTIVO_OBS TEXT, " +  // 👈 NUEVA COLUMNA
                    "FOREIGN KEY ($PRO_EQUIPO_ID) REFERENCES $TABLA_EQUIPOS($EQ_ID), " +
                    "FOREIGN KEY ($PRO_TECNICO_ID) REFERENCES $TABLA_USUARIOS($USU_ID))"

        private const val SQL_CREAR_MEDICIONES =
            "CREATE TABLE $TABLA_MEDICIONES (" +
                    "$MED_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$MED_PROTOCOLO_ID INTEGER NOT NULL, " +
                    "$MED_TIPO_PRUEBA TEXT NOT NULL, " +
                    "$MED_LECTURA_1MIN TEXT, " +
                    "$MED_LECTURA_10MIN TEXT, " +
                    "$MED_IP TEXT, " +
                    "FOREIGN KEY ($MED_PROTOCOLO_ID) REFERENCES $TABLA_PROTOCOLOS($PRO_ID))"

        private const val SQL_CREAR_RECEPCION =
            "CREATE TABLE $TABLA_RECEPCION (" +
                    "$REC_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$REC_PROTOCOLO_ID INTEGER NOT NULL, " +
                    "$REC_NOMBRE TEXT NOT NULL, " +
                    "$REC_RUT TEXT NOT NULL, " +
                    "$REC_CARGO TEXT, " +
                    "$REC_FECHA TEXT NOT NULL, " +
                    "FOREIGN KEY ($REC_PROTOCOLO_ID) REFERENCES $TABLA_PROTOCOLOS($PRO_ID))"

        private const val SQL_PRECARGAR_EQUIPOS =
            "INSERT INTO $TABLA_EQUIPOS ($EQ_ID, $EQ_TAG, $EQ_TIPO, $EQ_MARCA, $EQ_SERIE, $EQ_INSTALACION, $EQ_ID_CLIENTE) VALUES " +
                    // Planta Lampa (Cliente 5)
                    "(1, 'TR-01', 'Transformador', 'Rhona', 'RH-18733', 'Planta Lampa', 5), " +
                    "(2, 'TR-02', 'Transformador', 'Rhona', 'RH-21904', 'Planta Lampa', 5), " +
                    // Planta Elevadora Sur (Cliente 5)
                    "(3, 'TR-A', 'Transformador', 'Tusan', 'TS-5521', 'Planta Elevadora Sur', 5), " +
                    // Faena Til Til (Cliente 5)
                    "(4, 'TR-SS-01', 'Transformador', 'ABB', 'ABB-1180442', 'Faena Til Til', 5), " +
                    "(5, 'INT-52-1', 'Interruptor', 'Schneider', 'SE-449021', 'Faena Til Til', 5), " +
                    // Edificio Clínico (Cliente 5)
                    "(6, 'CEL-MT-03', 'Celda MT', 'Ormazabal', 'OR-77812', 'Edificio Clínico', 5), " +
                    // Planta Molienda (Cliente 5)
                    "(7, 'REL-50/51-A', 'Relé de protección', 'SEL', 'SEL-751-30219', 'Planta Molienda', 5), " +
                    // Site Huechuraba (Cliente 5)
                    "(8, 'CAB-MT-02', 'Cable de poder', 'Nexans', 'NX-2201-B', 'Site Huechuraba', 5)"

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_CODIGO AS rol_codigo, r.$ROL_NOMBRE AS rol_nombre " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
        db.execSQL(SQL_CREAR_EQUIPOS)
        db.execSQL(SQL_CREAR_PROTOCOLOS)
        db.execSQL(SQL_CREAR_MEDICIONES)
        db.execSQL(SQL_CREAR_RECEPCION)
        db.execSQL(SQL_PRECARGAR_EQUIPOS)
        db.execSQL(SQL_CREAR_INSTRUMENTOS)
        db.execSQL(SQL_CREAR_INSTALACIONES)
        db.execSQL(SQL_PRECARGAR_INSTALACIONES)
        db.execSQL(SQL_PRECARGAR_INSTRUMENTOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_RECEPCION")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_MEDICIONES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_PROTOCOLOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_EQUIPOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_INSTRUMENTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_INSTALACIONES")
        onCreate(db)
    }

    // ---------------- ROLES ----------------
    fun listarRoles(): List<Rol> {
        val roles = mutableListOf<Rol>()
        readableDatabase.rawQuery(
            "SELECT $ROL_ID, $ROL_CODIGO, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ROL_ID)),
                        codigo = cursor.getString(cursor.getColumnIndexOrThrow(ROL_CODIGO)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
                    )
                )
            }
        }
        return roles
    }

    // ---------------- USUARIOS ----------------
    fun insertarUsuario(usuario: Usuario): Long =
        writableDatabase.insert(TABLA_USUARIOS, null, valoresDe(usuario))

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null)

    fun buscarPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString())).firstOrNull()

    fun autenticarUsuario(correo: String, contrasena: String): Usuario? {
        val sql = "$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ? AND u.$USU_CONTRA = ? AND u.$USU_ACTIVO = 1"
        return consultarUsuarios(sql, arrayOf(correo, contrasena)).firstOrNull()
    }

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(TABLA_USUARIOS, valoresDe(usuario), "$USU_ID = ?", arrayOf(usuario.id.toString()))

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
        put(USU_NOMBRE, usuario.nombre)
        put(USU_CORREO, usuario.correo)
        put(USU_CONTRA, usuario.contrasena)
        put(USU_RUT, usuario.rut)
        put(USU_ROL, usuario.rolId.toInt())   // 👈 Aquí corregimos el mismatch Long → Int
        put(USU_ACTIVO, if (usuario.activo) 1 else 0)
    }

    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerUsuario(cursor))
            }
        }
        return lista
    }

    private fun leerUsuario(cursor: Cursor) = Usuario(
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        contrasena = cursor.getString(cursor.getColumnIndexOrThrow(USU_CONTRA)),
        rut = cursor.getString(cursor.getColumnIndexOrThrow(USU_RUT)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow("rol_nombre")),
        activo = cursor.getInt(cursor.getColumnIndexOrThrow(USU_ACTIVO)) == 1
    )

    // ---------------- EQUIPOS ----------------
    fun listarEquipos(): List<Equipo> {
        val lista = mutableListOf<Equipo>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_EQUIPOS", null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Equipo(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(EQ_ID)).toInt(),
                        tag = cursor.getString(cursor.getColumnIndexOrThrow(EQ_TAG)),
                        tipoEquipo = cursor.getString(cursor.getColumnIndexOrThrow(EQ_TIPO)),
                        clasificacion = "Primario",
                        instalacion = cursor.getString(cursor.getColumnIndexOrThrow(EQ_INSTALACION)),
                        marca = cursor.getString(cursor.getColumnIndexOrThrow(EQ_MARCA)) ?: "",
                        serie = cursor.getString(cursor.getColumnIndexOrThrow(EQ_SERIE)) ?: "",
                        potenciaTension = ""
                    )
                )
            }
        }
        return lista
    }
    fun insertarEquipo(equipo: Equipo): Long {
        val values = ContentValues().apply {
            put(EQ_TAG, equipo.tag)
            put(EQ_TIPO, equipo.tipoEquipo)
            put(EQ_MARCA, equipo.marca)
            put(EQ_SERIE, equipo.serie)
            put(EQ_INSTALACION, equipo.instalacion)
            put(EQ_ID_CLIENTE, equipo.idCliente)
        }
        return writableDatabase.insert(TABLA_EQUIPOS, null, values)
    }

    fun existeTagEnInstalacion(tag: String, instalacion: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLA_EQUIPOS WHERE $EQ_TAG = ? AND $EQ_INSTALACION = ?",
            arrayOf(tag, instalacion)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0
            }
        }
        return false
    }

    // ---------------- PROTOCOLOS ----------------
    fun guardarProtocolo(protocolo: Protocolo): Long {
        val values = ContentValues().apply {
            put(PRO_EQUIPO_ID, protocolo.equipoId)
            put(PRO_TECNICO_ID, protocolo.tecnicoId)
            put(PRO_FECHA, protocolo.fecha)
            put(PRO_TEMP, protocolo.temperatura)
            put(PRO_HUM, protocolo.humedad)
            put(PRO_RESULTADO, protocolo.resultadoGlobal)
            put(PRO_ESTADO, protocolo.estado)
        }
        return writableDatabase.insert(TABLA_PROTOCOLOS, null, values)
    }

    fun listarProtocolosPorEquipo(equipoId: Long): List<Protocolo> {
        val lista = mutableListOf<Protocolo>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_PROTOCOLOS WHERE $PRO_EQUIPO_ID = ? ORDER BY $PRO_FECHA DESC",
            arrayOf(equipoId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) lista.add(leerProtocolo(cursor))
        }
        return lista
    }

    fun listarTodosLosProtocolos(): List<Protocolo> {
        val lista = mutableListOf<Protocolo>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_PROTOCOLOS ORDER BY $PRO_FECHA DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) lista.add(leerProtocolo(cursor))
        }
        return lista
    }

    private fun leerProtocolo(cursor: Cursor) = Protocolo(
        id = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_ID)),
        equipoId = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_EQUIPO_ID)),
        tecnicoId = cursor.getLong(cursor.getColumnIndexOrThrow(PRO_TECNICO_ID)),
        fecha = cursor.getString(cursor.getColumnIndexOrThrow(PRO_FECHA)),
        temperatura = cursor.getString(cursor.getColumnIndexOrThrow(PRO_TEMP)) ?: "",
        humedad = cursor.getString(cursor.getColumnIndexOrThrow(PRO_HUM)) ?: "",
        resultadoGlobal = cursor.getString(cursor.getColumnIndexOrThrow(PRO_RESULTADO)) ?: "Pendiente",
        estado = cursor.getString(cursor.getColumnIndexOrThrow(PRO_ESTADO)),
        motivoObservacion = cursor.getString(cursor.getColumnIndexOrThrow(PRO_MOTIVO_OBS)) // 👈 NUEVO
    )

    fun actualizarEstadoProtocolo(protocoloId: Long, nuevoEstado: String) {
        val values = ContentValues().apply { put(PRO_ESTADO, nuevoEstado) }
        writableDatabase.update(TABLA_PROTOCOLOS, values, "$PRO_ID = ?", arrayOf(protocoloId.toString()))
    }
    fun observarProtocolo(protocoloId: Long, motivo: String) {
        val values = ContentValues().apply {
            put(PRO_ESTADO, "OBSERVADO")
            put(PRO_MOTIVO_OBS, motivo)
        }
        writableDatabase.update(TABLA_PROTOCOLOS, values, "$PRO_ID = ?", arrayOf(protocoloId.toString()))
    }

    // ---------------- MEDICIONES ----------------
    fun guardarMedicion(protocoloId: Long, tipoPrueba: String, lectura1: String, lectura10: String, ip: String): Long {
        val values = ContentValues().apply {
            put(MED_PROTOCOLO_ID, protocoloId)
            put(MED_TIPO_PRUEBA, tipoPrueba)
            put(MED_LECTURA_1MIN, lectura1)
            put(MED_LECTURA_10MIN, lectura10)
            put(MED_IP, ip)
        }
        return writableDatabase.insert(TABLA_MEDICIONES, null, values)
    }

    fun obtenerMedicionDeProtocolo(protocoloId: Long): Medicion? {
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_MEDICIONES WHERE $MED_PROTOCOLO_ID = ? LIMIT 1",
            arrayOf(protocoloId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return Medicion(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MED_ID)),
                    protocoloId = cursor.getLong(cursor.getColumnIndexOrThrow(MED_PROTOCOLO_ID)),
                    tipoPrueba = cursor.getString(cursor.getColumnIndexOrThrow(MED_TIPO_PRUEBA)),
                    lectura1Min = cursor.getString(cursor.getColumnIndexOrThrow(MED_LECTURA_1MIN)) ?: "",
                    lectura10Min = cursor.getString(cursor.getColumnIndexOrThrow(MED_LECTURA_10MIN)) ?: "",
                    indicePolarizacion = cursor.getString(cursor.getColumnIndexOrThrow(MED_IP)) ?: ""
                )
            }
        }
        return null
    }

    // ---------------- RECEPCION ACTA ----------------
    fun guardarRecepcionActa(protocoloId: Long, nombre: String, rut: String, cargo: String, fechaHora: String): Long {
        val values = ContentValues().apply {
            put(REC_PROTOCOLO_ID, protocoloId)
            put(REC_NOMBRE, nombre)
            put(REC_RUT, rut)
            put(REC_CARGO, cargo)
            put(REC_FECHA, fechaHora)
        }
        return writableDatabase.insert(TABLA_RECEPCION, null, values)
    }

    fun buscarClientePorCorreo(correo: String): Usuario? {
        return consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_CORREO = ?", arrayOf(correo)).firstOrNull()
    }

    // NUEVO MÉTODO: Listar equipos asociados a un cliente
    fun listarEquiposPorCliente(clienteId: Long): List<Equipo> {
        val lista = mutableListOf<Equipo>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_EQUIPOS WHERE $EQ_ID_CLIENTE = ?", arrayOf(clienteId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Equipo(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(EQ_ID)).toInt(),
                        tag = cursor.getString(cursor.getColumnIndexOrThrow(EQ_TAG)),
                        tipoEquipo = cursor.getString(cursor.getColumnIndexOrThrow(EQ_TIPO)),
                        clasificacion = "Primario",
                        instalacion = cursor.getString(cursor.getColumnIndexOrThrow(EQ_INSTALACION)),
                        marca = cursor.getString(cursor.getColumnIndexOrThrow(EQ_MARCA)) ?: "",
                        serie = cursor.getString(cursor.getColumnIndexOrThrow(EQ_SERIE)) ?: "",
                        potenciaTension = "",
                        idCliente = cursor.getLong(cursor.getColumnIndexOrThrow(EQ_ID_CLIENTE))
                    )
                )
            }
        }
        return lista
    }
    fun insertarInstrumento(nombre: String, marca: String, serie: String, vencimiento: String): Long {
        val values = ContentValues().apply {
            put(INS_NOMBRE, nombre)
            put(INS_MARCA, marca)
            put(INS_SERIE, serie)
            put(INS_VENCIMIENTO, vencimiento)
        }
        return writableDatabase.insert(TABLA_INSTRUMENTOS, null, values)
    }

    fun listarInstrumentos(): List<Instrumento> {
        val lista = mutableListOf<Instrumento>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_INSTRUMENTOS", null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Instrumento(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(INS_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(INS_NOMBRE)),
                        marca = cursor.getString(cursor.getColumnIndexOrThrow(INS_MARCA)) ?: "",
                        serie = cursor.getString(cursor.getColumnIndexOrThrow(INS_SERIE)) ?: "",
                        fechaVencimiento = cursor.getString(cursor.getColumnIndexOrThrow(INS_VENCIMIENTO))
                    )
                )
            }
        }
        return lista
    }
    fun insertarInstalacion(nombre: String, clienteId: Long): Long {
        val values = ContentValues().apply {
            put(INST_NOMBRE, nombre)
            put(INST_CLIENTE_ID, clienteId)
        }
        return writableDatabase.insert(TABLA_INSTALACIONES, null, values)
    }

    fun listarInstalaciones(): List<Pair<Long, String>> {
        val lista = mutableListOf<Pair<Long, String>>()
        readableDatabase.rawQuery("SELECT * FROM $TABLA_INSTALACIONES", null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Pair(
                        cursor.getLong(cursor.getColumnIndexOrThrow(INST_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(INST_NOMBRE))
                    )
                )
            }
        }
        return lista
    }
}
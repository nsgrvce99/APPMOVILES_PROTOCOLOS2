package cl.duocuc.miregistro.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario

// =====================================================================
//  UsuarioDbHelper.kt  —  ADAPTADO PARA INDUZTEK INGENIERÍA SpA
//  ---------------------------------------------------------------------
//  Implementa el modelo relacional normalizado con las tablas de roles
//  y los 4 perfiles obligatorios: Técnico, Ingeniero, Jefe Técnico y Cliente.
// =====================================================================

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "induztekterreno.db"
        private const val VERSION_BD = 1

        // Tabla de Roles según Anexo 2 y MER Induztek
        private const val TABLA_ROLES = "rol"
        private const val ROL_ID = "id_rol"
        private const val ROL_CODIGO = "codigo"
        private const val ROL_NOMBRE = "nombre"

        // Tabla de Usuarios / Personal e Ingenieros / Clientes
        private const val TABLA_USUARIOS = "usuario"
        private const val USU_ID = "id_usuario"
        private const val USU_ROL = "id_rol"
        private const val USU_RUT = "rut"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_TELEFONO = "telefono"
        private const val USU_ACTIVO = "activo"
        private const val USU_GIRO = "giro"
        private const val USU_CONTACTO_NOMBRE = "contacto_nombre"
        private const val USU_CONTACTO_CARGO = "contacto_cargo"

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
                    "$USU_TELEFONO TEXT, " +
                    "$USU_ACTIVO INTEGER NOT NULL DEFAULT 1, " +
                    "$USU_GIRO TEXT, " +
                    "$USU_CONTACTO_NOMBRE TEXT, " +
                    "$USU_CONTACTO_CARGO TEXT, " +
                    "FOREIGN KEY ($USU_ROL) REFERENCES $TABLA_ROLES($ROL_ID))"

        // Precarga de los 4 roles del caso Induztek (Anexo 2 y Anexo 7)
        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_CODIGO, $ROL_NOMBRE) VALUES " +
                    "(1, 'TECNICO', 'P-01 Técnico de terreno'), " +
                    "(2, 'INGENIERO', 'P-02 Ingeniero de pruebas'), " +
                    "(3, 'JEFE_TECNICO', 'P-03 Jefe técnico'), " +
                    "(4, 'CLIENTE', 'P-04 Usuario cliente (solo consulta)')"

        // Datos de ejemplo basados en el Anexo 9 (Personal Induztek y Usuarios cliente)
        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_ROL, $USU_RUT, $USU_NOMBRE, $USU_CORREO, $USU_ACTIVO) VALUES " +
                    "(1, '17.284.519-3', 'Matías Godoy Herrera', 'mgodoy@induztek.ejemplo', 1), " +
                    "(1, '19.412.087-7', 'Javiera Tapia Muñoz', 'jtapia@induztek.ejemplo', 1), " +
                    "(2, '15.906.334-8', 'Andrea Contreras Silva', 'acontreras@induztek.ejemplo', 1), " +
                    "(3, '13.871.905-7', 'Ricardo Espinoza Lagos', 'respinoza@induztek.ejemplo', 1), " +
                    "(4, '14.226.791-8', 'Marcela Fuentes Rojas', 'mfuentes@envpacifico.ejemplo', 1)"

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_ROLES")
        onCreate(db)
    }

    // ----------------------------- ROLES -----------------------------

    fun listarRoles(): List<Rol> {
        val roles = mutableListOf<Rol>()
        // Agregamos $ROL_CODIGO a la consulta SELECT
        readableDatabase.rawQuery(
            "SELECT $ROL_ID, $ROL_CODIGO, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ROL_ID)),
                        codigo = cursor.getString(cursor.getColumnIndexOrThrow(ROL_CODIGO)), // <-- Agregado
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
                    )
                )
            }
        }
        return roles
    }

    // ---------------------------- USUARIOS ---------------------------

    fun insertarUsuario(usuario: Usuario): Long =
        writableDatabase.insert(TABLA_USUARIOS, null, valoresDe(usuario))

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null)

    fun buscarPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(
            TABLA_USUARIOS, valoresDe(usuario), "$USU_ID = ?", arrayOf(usuario.id.toString())
        )

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    // ---------------------------- AYUDANTES --------------------------

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
        put(USU_NOMBRE, usuario.nombre)
        put(USU_CORREO, usuario.correo)
        put(USU_RUT, usuario.rut)
        put(USU_ROL, usuario.rolId)
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
        rut = cursor.getString(cursor.getColumnIndexOrThrow(USU_RUT)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow("rol_nombre")),
        activo = cursor.getInt(cursor.getColumnIndexOrThrow(USU_ACTIVO)) == 1
    )
}

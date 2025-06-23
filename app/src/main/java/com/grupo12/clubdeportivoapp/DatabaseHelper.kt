package com.grupo12.clubdeportivoapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "club_deportivo.db"
        const val DATABASE_VERSION = 8

        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_ES_EMPLEADO = "es_empleado"

        const val TABLE_SOCIOS = "socios"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_ALIAS = "alias"
        const val COLUMN_DNI = "dni"
        const val COLUMN_VENCIMIENTO = "vencimiento"

        const val TABLE_PAGOS = "pagos"
        const val COLUMN_MONTO = "monto"
        const val COLUMN_FECHA_PAGO = "fecha_pago"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USUARIOS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_ES_EMPLEADO INTEGER DEFAULT 1
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_SOCIOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_ALIAS TEXT UNIQUE NOT NULL,
                $COLUMN_DNI TEXT UNIQUE NOT NULL,
                $COLUMN_VENCIMIENTO TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_PAGOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DNI TEXT NOT NULL,
                $COLUMN_MONTO REAL NOT NULL,
                $COLUMN_FECHA_PAGO TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_DNI) REFERENCES $TABLE_SOCIOS($COLUMN_DNI)
            )
        """)
    }

    fun crearUsuarioAdmin() {
        val db = writableDatabase
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_USUARIOS 
            ($COLUMN_USERNAME, $COLUMN_PASSWORD, $COLUMN_ES_EMPLEADO)
            VALUES ('admin', '1234', 1)
        """)
    }

    fun isAliasUnique(alias: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_SOCIOS WHERE $COLUMN_ALIAS = ?",
            arrayOf(alias)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count == 0
    }

    fun insertarSocio(nombre: String, alias: String, dni: String): Long {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COLUMN_NOMBRE, nombre)
            put(COLUMN_ALIAS, alias)
            put(COLUMN_DNI, dni)
            put(COLUMN_VENCIMIENTO, "") // Vencimiento vacío inicial
        }
        return db.insert(TABLE_SOCIOS, null, valores)
    }

    fun registrarPago(
        dniSocio: String,
        monto: Double,
        tipoPago: String,
        actividad: String,
        frecuencia: String,
        mesesPagados: Int,
        fechaVencimiento: String
    ): Boolean {
        val db = writableDatabase
        return try {
            db.beginTransaction()

            // 1. Registrar el pago
            val valoresPago = ContentValues().apply {
                put(COLUMN_DNI, dniSocio)
                put(COLUMN_MONTO, monto)
                put(COLUMN_FECHA_PAGO, LocalDate.now().toString())
                put("tipo_pago", tipoPago)
                put("actividad", actividad)
                put("frecuencia", frecuencia)
                put("meses_pagados", mesesPagados)
            }
            db.insert(TABLE_PAGOS, null, valoresPago)

            // 2. Actualizar vencimiento del socio (siempre suma meses)
            val nuevaFecha = LocalDate.now().plusMonths(mesesPagados.toLong()).toString()
            db.update(
                TABLE_SOCIOS,
                ContentValues().apply { put(COLUMN_VENCIMIENTO, nuevaFecha) },
                "$COLUMN_DNI = ?",
                arrayOf(dniSocio)
            )

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SOCIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAGOS")
        onCreate(db)
    }


    fun existeAlias(alias: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_SOCIOS WHERE $COLUMN_ALIAS = ?",
            arrayOf(alias)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    fun actualizarVencimiento(dni: String, fechaVencimiento: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COLUMN_VENCIMIENTO, fechaVencimiento)
        }
        val filasActualizadas = db.update(
            TABLE_SOCIOS,
            valores,
            "$COLUMN_DNI = ?",
            arrayOf(dni)
        )
        return filasActualizadas > 0
    }

    fun obtenerSocioPorDni(dni: String): Socio? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SOCIOS,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_ALIAS, COLUMN_DNI, COLUMN_VENCIMIENTO),
            "$COLUMN_DNI LIKE ?",
            arrayOf(dni),
            null, null, null
        )

        return try {
            if (cursor.moveToFirst()) {
                val nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val partesNombre = nombreCompleto.split(" ")
                val nombre = partesNombre.firstOrNull() ?: ""
                val apellido = partesNombre.drop(1).joinToString(" ")

                Socio(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = nombre,
                    apellido = apellido,
                    dni = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DNI)),
                    alias = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALIAS)),
                    vencimiento = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VENCIMIENTO))
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener socio", e)
            null
        } finally {
            cursor.close()
        }
    }

    fun buscarSociosPorDni(partialDni: String): List<Socio> {
        val db = readableDatabase
        val socios = mutableListOf<Socio>()

        val cursor = db.rawQuery("""
        SELECT * FROM $TABLE_SOCIOS 
        WHERE $COLUMN_DNI LIKE ?
        ORDER BY $COLUMN_DNI
    """, arrayOf("$partialDni%"))

        cursor.use {
            while (it.moveToNext()) {
                val nombreCompleto = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val partesNombre = nombreCompleto.split(" ")
                val nombre = partesNombre.firstOrNull() ?: ""
                val apellido = partesNombre.drop(1).joinToString(" ")

                socios.add(Socio(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = nombre,
                    apellido = apellido,
                    dni = it.getString(it.getColumnIndexOrThrow(COLUMN_DNI)),
                    alias = it.getString(it.getColumnIndexOrThrow(COLUMN_ALIAS)),
                    vencimiento = it.getString(it.getColumnIndexOrThrow(COLUMN_VENCIMIENTO))
                ))
            }
        }

        return socios
    }

    fun existeDni(dni: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_SOCIOS WHERE $COLUMN_DNI = ?",
            arrayOf(dni)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

    fun obtenerHistorialPagos(dniSocio: String): List<HistorialPago> {
        val db = readableDatabase
        val pagos = mutableListOf<HistorialPago>()

        val cursor = db.rawQuery("""
        SELECT 
            p.$COLUMN_FECHA_PAGO, 
            p.$COLUMN_MONTO, 
            p.tipo_pago, 
            p.actividad,
            p.frecuencia,
            s.$COLUMN_NOMBRE
        FROM $TABLE_PAGOS p
        JOIN $TABLE_SOCIOS s ON p.$COLUMN_DNI = s.$COLUMN_DNI
        WHERE p.$COLUMN_DNI = ?
        ORDER BY p.$COLUMN_FECHA_PAGO DESC
    """, arrayOf(dniSocio))

        cursor.use {
            while (it.moveToNext()) {
                pagos.add(HistorialPago(
                    nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    monto = it.getDouble(it.getColumnIndexOrThrow(COLUMN_MONTO)),
                    metodoPago = it.getString(it.getColumnIndexOrThrow("tipo_pago")),
                    fecha = it.getString(it.getColumnIndexOrThrow(COLUMN_FECHA_PAGO)),
                    actividad = it.getString(it.getColumnIndexOrThrow("actividad")),
                    frecuencia = it.getString(it.getColumnIndexOrThrow("frecuencia"))
                ))
            }
        }

        return pagos
    }

    // Obtener socios con vencimientos próximos (7 días)
    fun obtenerSociosConVencimientosProximos(): List<SocioVencimiento> {
        val db = readableDatabase
        val socios = mutableListOf<SocioVencimiento>()
        val fechaLimite = LocalDate.now().plusDays(7)

        val cursor = db.rawQuery("""
        SELECT $COLUMN_NOMBRE, $COLUMN_DNI, $COLUMN_VENCIMIENTO
        FROM $TABLE_SOCIOS
        WHERE $COLUMN_VENCIMIENTO BETWEEN date('now') AND ?
        ORDER BY $COLUMN_VENCIMIENTO ASC
    """, arrayOf(fechaLimite.toString()))

        cursor.use {
            while (it.moveToNext()) {
                socios.add(SocioVencimiento(
                    nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    dni = it.getString(it.getColumnIndexOrThrow(COLUMN_DNI)),
                    vencimiento = LocalDate.parse(it.getString(it.getColumnIndexOrThrow(COLUMN_VENCIMIENTO)))
                ))
            }
        }
        return socios
    }

    fun obtenerSociosConCuotasVencidas(): List<SocioVencimiento> {
        val socios = mutableListOf<SocioVencimiento>()
        val hoy = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val cursor = readableDatabase.rawQuery("""
        SELECT $COLUMN_NOMBRE, $COLUMN_DNI, $COLUMN_VENCIMIENTO
        FROM $TABLE_SOCIOS
        WHERE $COLUMN_VENCIMIENTO IS NOT NULL 
        AND $COLUMN_VENCIMIENTO != ''
        ORDER BY $COLUMN_VENCIMIENTO ASC
    """, null)

        cursor.use {
            while (it.moveToNext()) {
                val vencimientoStr = it.getString(it.getColumnIndexOrThrow(COLUMN_VENCIMIENTO))

                if (!vencimientoStr.isNullOrEmpty()) {
                    try {
                        val fechaVencimiento = LocalDate.parse(vencimientoStr, dateFormatter)

                        if (fechaVencimiento.isBefore(hoy)) {
                            socios.add(SocioVencimiento(
                                nombre = it.getString(it.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                                dni = it.getString(it.getColumnIndexOrThrow(COLUMN_DNI)),
                                vencimiento = fechaVencimiento
                            ))
                        }
                    } catch (e: DateTimeParseException) {
                        Log.e("DatabaseHelper", "Error al parsear fecha: $vencimientoStr", e)
                    } catch (e: Exception) {
                        Log.e("DatabaseHelper", "Error inesperado", e)
                    }
                }
            }
        }
        return socios
    }

    // En DatabaseHelper.kt
    fun obtenerInformacionCompletaSocio(dni: String): SocioCompleto? {
        val db = readableDatabase

        // Primero obtener datos básicos del socio
        val socioCursor = db.query(
            TABLE_SOCIOS,
            arrayOf(COLUMN_ID, COLUMN_NOMBRE, COLUMN_ALIAS, COLUMN_DNI, COLUMN_VENCIMIENTO),
            "$COLUMN_DNI = ?",
            arrayOf(dni),
            null, null, null
        )

        return try {
            if (socioCursor.moveToFirst()) {
                val nombreCompleto = socioCursor.getString(socioCursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val partesNombre = nombreCompleto.split(" ")
                val nombre = partesNombre.firstOrNull() ?: ""
                val apellido = partesNombre.drop(1).joinToString(" ")

                // Obtener el último pago registrado
                val pagoCursor = db.query(
                    TABLE_PAGOS,
                    arrayOf("actividad", "frecuencia", "meses_pagados"),
                    "$COLUMN_DNI = ?",
                    arrayOf(dni),
                    null, null,
                    "$COLUMN_FECHA_PAGO DESC",
                    "1"
                )

                val actividad = if (pagoCursor.moveToFirst()) {
                    pagoCursor.getString(pagoCursor.getColumnIndexOrThrow("actividad"))
                } else ""

                val frecuencia = if (pagoCursor.moveToFirst()) {
                    pagoCursor.getString(pagoCursor.getColumnIndexOrThrow("frecuencia"))
                } else ""

                val mesesPagados = if (pagoCursor.moveToFirst()) {
                    pagoCursor.getInt(pagoCursor.getColumnIndexOrThrow("meses_pagados"))
                } else 0

                pagoCursor.close()

                SocioCompleto(
                    id = socioCursor.getInt(socioCursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = nombre,
                    apellido = apellido,
                    dni = socioCursor.getString(socioCursor.getColumnIndexOrThrow(COLUMN_DNI)),
                    alias = socioCursor.getString(socioCursor.getColumnIndexOrThrow(COLUMN_ALIAS)),
                    vencimiento = socioCursor.getString(socioCursor.getColumnIndexOrThrow(COLUMN_VENCIMIENTO)),
                    actividad = actividad,
                    frecuencia = frecuencia,
                    mesesPagados = mesesPagados
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al obtener información completa del socio", e)
            null
        } finally {
            socioCursor.close()
        }
    }

    override fun close() {
        super.close()
        readableDatabase.close()
        writableDatabase.close()
    }

}
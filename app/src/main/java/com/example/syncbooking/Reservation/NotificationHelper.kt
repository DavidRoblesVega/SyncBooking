package com.example.syncbooking.Reservation

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.syncbooking.Main.AlarmNotification
import com.example.syncbooking.Main.Reserva
import com.example.syncbooking.Reservation.ReservationRegisterActivity.Companion.reservaNueva
import java.io.Serializable
import java.util.Calendar
import kotlin.math.absoluteValue

object NotificationHelper {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(context: Context, reservaDateTimeMillis: Long, reservaId: String) {
        Log.d("scheduleNotification", "Recibido reservaDateTimeMillis: $reservaDateTimeMillis")

        val nombre = reservaNueva?.name
        val apellido = reservaNueva?.surname
        val hora = reservaNueva?.time


        val notificacionId = reservaId.hashCode().absoluteValue
        Log.d("NotificationHelper", "Notification ID: $notificacionId")

        val intent = Intent(context, AlarmNotification::class.java).apply {
            putExtra("notificacionId", notificacionId)
            putExtra("nombre", nombre)
            putExtra("apellido", apellido)
            putExtra("hora", hora)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificacionId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val currentTime = Calendar.getInstance().timeInMillis

        // Verificar si la reserva es en el futuro
        if (reservaDateTimeMillis > currentTime) {
            // Si la reserva es posterior al momento actual, configurar la alarma 2 horas antes del inicio
            val twoHoursInMillis = 2 * 60 * 60 * 1000 // 2 horas en milisegundos
            val alarmTime = reservaDateTimeMillis - twoHoursInMillis
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
            Log.d("scheduleNotification", "Alarma programada para $alarmTime")
        } else {
            // Si la reserva es anterior al momento actual, no configurar la alarma
            Log.e("scheduleNotification", "La reserva ya ha pasado, no se programará la alarma.")
        }
    }

}


package com.example.syncbooking.Main

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.syncbooking.R
import kotlin.math.absoluteValue

class AlarmNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            // Verifica si el Intent contiene el notificacionId como un extra
            if (it.hasExtra("notificacionId")) {
                // Obtiene el notificacionId del Intent
                val notificacionId = it.getIntExtra("notificacionId", -1)

                // Obtiene los datos adicionales de la reserva del Intent
                val nombre = intent.getStringExtra("nombre")
                val apellido = intent.getStringExtra("apellido")
                val hora = intent.getStringExtra("hora")

                // Verifica si alguno de los valores es nulo antes de crear la notificación
                if (nombre == null || apellido == null || hora == null) {
                    Log.d(
                        "onReceive",
                        "Alguno de los valores recibidos es nulo. No se puede crear la notificación."
                    )
                    return
                }

                // Crea la notificación utilizando los datos obtenidos
                createSimpleNotification(context, notificacionId, nombre, apellido, hora)
            }
        }
    }

    companion object {
        fun createSimpleNotification(
            context: Context,
            notificacionId: Int,
            nombre: String?,
            apellido: String?,
            hora: String?
        ) {
            if (nombre == null || apellido == null || hora == null) {
                Log.d(
                    "createSimpleNotification",
                    "Alguno de los valores recibidos es nulo. No se puede crear la notificación."
                )
                return
            }

            Log.d(
                "createSimpleNotification",
                "Nombre recibido: $nombre, Apellido recibido: $apellido, Hora recibida: $hora"
            )

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Agrega el notificacionId como un extra en el Intent
                putExtra("notificacionId", notificacionId)
            }

            val flag =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, notificacionId, intent, flag)

            val notification = NotificationCompat.Builder(context, MainActivity.MY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("¡Recordatorio de Reserva!")
                .setContentText("¡No lo olvides! Tienes una reserva dentro de poco.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Recuerda que tienes una reserva a las $hora con $nombre $apellido.  ")
                )
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(longArrayOf(200))
                .build()


            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificacionId, notification)
        }

        fun cancelNotification(context: Context, reservaId: String) {
            val notificacionId = reservaId.hashCode().absoluteValue

            try {
                // Cancela la notificación
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificacionId)
                Log.d("cancelNotification", "Notificación cancelada exitosamente: $notificacionId")

                // Cancela la alarma asociada
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, AlarmNotification::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, notificacionId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel() // Cancela también el PendingIntent para liberar recursos
                Log.d("cancelNotification", "Alarma cancelada exitosamente")

            } catch (e: Exception) {
                Log.e("cancelNotification", "Error al cancelar la notificación y la alarma: ${e.message}")
            }
        }
    }
}

package com.example.restaraunt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OrderStatusService : Service() {

    private val dbService = DBService()
    private var job: Job? = null
    private var lastStatuses = mutableMapOf<String, String>()

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("Отслеживание заказов"))

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                checkOrders()
                delay(5000)
            }
        }

        return START_STICKY
    }

    private suspend fun checkOrders() {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val email = prefs.getString("email", null) ?: return
        val password = prefs.getString("password", null) ?: return

        val orders = dbService.getUserOrderHistory(email, password)
        for ((order, _) in orders) {
            val currentState = order.state_id
            val lastState = lastStatuses[order.id]
            if (lastState != null && lastState != currentState) {
                val state = dbService.getStateById(currentState)
                sendNotification(order.id, state.name)
            }
            lastStatuses[order.id] = currentState
        }
    }

    private fun sendNotification(orderId: String, stateId: String) {

        val notification = NotificationCompat.Builder(this, "orders_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Статус заказа обновлён")
            .setContentText("Заказ ${orderId.take(8)}... теперь ${stateId}") // можно мапить stateId -> name
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this).notify(orderId.hashCode(), notification)
    }

    private fun createNotification(text: String): Notification {
        val channelId = "orders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Updates",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(text)
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }
}
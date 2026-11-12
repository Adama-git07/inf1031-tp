package com.example.citoyenactif.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.example.citoyenactif.R;
import com.example.citoyenactif.ui.report.ReportActivity;
import com.example.citoyenactif.util.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service Firebase Cloud Messaging (FCM).
 * Reçoit les messages envoyés par le serveur de notifications (dossier server/)
 * et les affiche dans la barre de statut.
 *
 * Cas d'usage pour Citoyen Actif :
 *   - Notifier un citoyen que son signalement est passé à EN_COURS, RESOLU ou IGNORE
 *
 * Le serveur envoie des messages "data" (title, body, reportId) : onMessageReceived
 * est donc appelé que l'application soit au premier plan ou en arrière-plan.
 * Un clic sur la notification ouvre le signalement concerné.
 */
public class CitoyenMessagingService extends FirebaseMessagingService {

    private static final String TAG              = "FCM";
    private static final String CHANNEL_ID       = "citoyen_actif_channel";
    private static final String CHANNEL_NAME     = "Citoyen Actif";
    private static final String CHANNEL_DESC     = "Notifications des rapports CitoyenActif";

    // ─── Nouveau token FCM ────────────────────────────────────────────────────

    /**
     * Appelé quand FCM génère ou renouvelle le token de cet appareil.
     *
     * @param token nouveau token FCM
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nouveau token FCM : " + token);
        SessionManager.saveFcmToken(this, token);
    }

    // ─── Réception d'un message ───────────────────────────────────────────────

    /**
     * Appelé quand un message FCM est reçu.
     *
     * @param message message FCM reçu (peut contenir notification et/ou data)
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "Message FCM reçu de : " + message.getFrom());

        // Extraire le titre et le corps du message
        String title = "Citoyen Actif";
        String body  = "Vous avez une nouvelle notification.";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }
            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }
        if (message.getData().containsKey("title")) {
            title = message.getData().get("title");
        }
        if (message.getData().containsKey("body")) {
            body = message.getData().get("body");
        }
        showNotification(title, body, message.getData().get("reportId"));
    }

    // ─── Affichage de la notification ────────────────────────────────────────

    /**
     * Crée le canal de notification (obligatoire depuis Android 8).
     * Appelé au démarrage de l'application (CitoyenActifApp) ET avant chaque affichage.
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Crée et affiche une notification système dans la barre de statut.
     *
     * @param title    titre de la notification
     * @param body     texte de la notification
     * @param reportId id du rapport concerné (peut être null)
     */
    private void showNotification(String title, String body, String reportId) {

        // Android 13+ : sans la permission, on n'affiche rien
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission POST_NOTIFICATIONS refusée : notification ignorée");
            return;
        }

        createNotificationChannel(this);
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Id du signalement (-1 si absent ou invalide)
        int id = -1;
        if (reportId != null) {
            try {
                id = Integer.parseInt(reportId);
            } catch (NumberFormatException ignored) { }
        }

        // Id unique par signalement : une nouvelle notification remplace l'ancienne du même rapport
        int notificationId = (id != -1) ? id : (int) System.currentTimeMillis();

        // Clic : ouvre le signalement, avec l'écran principal du citoyen derrière (bouton retour)
        PendingIntent pendingIntent = null;
        if (id != -1) {
            Intent intent = new Intent(this, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_REPORT_ID, id);

            // Le parent de ReportActivity est déclaré dans le manifest (parentActivityName)
            pendingIntent = TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(notificationId,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location_pin)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)                          // disparaît au clic
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        manager.notify(notificationId, builder.build());
    }
}
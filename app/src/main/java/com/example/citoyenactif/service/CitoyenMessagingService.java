package com.example.citoyenactif.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.citoyenactif.R;
import com.example.citoyenactif.ui.authentification.AuthentificationActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service Firebase Cloud Messaging (FCM).
 *
 * Reçoit les notifications push envoyées depuis la console Firebase
 * (ou depuis un serveur backend) et les affiche dans la barre de statut.
 *
 * Cas d'usage typiques pour CitoyenActif :
 *   - Notifier un citoyen que son rapport est passé à EN_COURS ou RESOLU
 *   - Notifier un agent qu'un nouveau rapport est disponible
 *   - Diffuser un message municipal à tous les citoyens
 *
 * Enregistrement du service dans AndroidManifest.xml requis.
 */
public class CitoyenMessagingService extends FirebaseMessagingService {

    private static final String TAG              = "FCM";
    private static final String CHANNEL_ID       = "citoyen_actif_channel";
    private static final String CHANNEL_NAME     = "Citoyen Actif";
    private static final String CHANNEL_DESC     = "Notifications des rapports CitoyenActif";
    private static final int    NOTIFICATION_ID  = 1001;

    // ─── Nouveau token FCM ────────────────────────────────────────────────────

    /**
     * Appelé quand FCM génère ou renouvelle le token de cet appareil.
     *
     * En production, ce token devrait être envoyé au serveur backend
     * pour pouvoir cibler cet appareil spécifiquement.
     * Ici on le logue simplement (suffisant pour un projet scolaire).
     *
     * @param token nouveau token FCM
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nouveau token FCM : " + token);
        // TODO (production) : envoyer ce token au serveur backend
        //   pour associer cet appareil à l'utilisateur connecté
    }

    // ─── Réception d'un message ───────────────────────────────────────────────

    /**
     * Appelé quand un message FCM est reçu alors que l'application est au
     * premier plan (foreground).
     *
     * Si l'application est en arrière-plan (background), FCM affiche
     * automatiquement la notification sans passer par cette méthode.
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

        // Priorité 1 : payload "notification" (le plus courant depuis la console Firebase)
        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }
            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        // Priorité 2 : payload "data" (envoyé par un serveur backend)
        if (message.getData().containsKey("title")) {
            title = message.getData().get("title");
        }
        if (message.getData().containsKey("body")) {
            body = message.getData().get("body");
        }

        showNotification(title, body);
    }

    // ─── Affichage de la notification ────────────────────────────────────────

    /**
     * Crée et affiche une notification système dans la barre de statut.
     *
     * Sur Android 8+ (API 26), un NotificationChannel est requis.
     * Le clic sur la notification ouvre AuthentificationActivity.
     *
     * @param title titre de la notification
     * @param body  texte de la notification
     */
    private void showNotification(String title, String body) {

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Créer le canal (Android 8+) — idempotent (ignoré si déjà existant)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            manager.createNotificationChannel(channel);
        }

        // Intent au clic : ouvre l'application
        Intent intent = new Intent(this, AuthentificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construction de la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location_pin)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)                          // disparaît au clic
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        manager.notify(NOTIFICATION_ID, builder.build());
    }
}

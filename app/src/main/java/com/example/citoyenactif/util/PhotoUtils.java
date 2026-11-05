package com.example.citoyenactif.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilitaire pour la gestion des photos de rapports.
 *
 * Fournit des méthodes pour :
 *   - Créer un fichier image temporaire (pour l'appareil photo)
 *   - Sauvegarder un Bitmap dans un fichier
 *   - Charger un Bitmap depuis un Uri
 */
public class PhotoUtils {

    /**
     * Crée un fichier image temporaire dans le stockage externe.
     * Le nom du fichier contient un timestamp pour éviter les doublons.
     *
     * @param context contexte Android
     * @return File prêt à recevoir une photo
     * @throws IOException si la création du fichier échoue
     */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "RAPPORT_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Compresse et sauvegarde un Bitmap dans un fichier JPEG.
     *
     * @param bitmap  image à sauvegarder
     * @param context contexte Android
     * @return Uri du fichier sauvegardé, ou null si erreur
     */
    public static Uri saveBitmap(Bitmap bitmap, Context context) {
        try {
            File file = createImageFile(context);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
            out.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Charge un Bitmap depuis un Uri.
     * Utile pour afficher une photo sélectionnée depuis la galerie.
     *
     * @param context contexte Android
     * @param uri     Uri de l'image
     * @return Bitmap chargé, ou null si erreur
     */
    public static Bitmap loadBitmap(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            return null;
        }
    }
}
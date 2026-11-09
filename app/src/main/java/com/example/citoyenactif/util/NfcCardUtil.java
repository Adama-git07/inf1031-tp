package com.example.citoyenactif.util;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Outils pour fabriquer et lire la carte NFC d'un agent municipal.
 *
 * Contenu de la carte : un enregistrement NDEF de type externe "ca.uqtr:agent"
 * dont le texte est :  v1:<numéroAgent>:<signature>
 * Exemple : v1:AGT-1001:3fa9c01b7d22e4aa
 *
 * La signature (HMAC) empêche de fabriquer une carte juste en connaissant
 * un numéro d'agent. Attention : la clé est dans l'app, c'est donc une
 * protection de niveau TP, pas de production.
 */
public final class NfcCardUtil {

    /** Domaine et type de l'enregistrement externe (comme dans le cours : ca.uqtr:poi). */
    public static final String DOMAIN = "ca.uqtr";
    public static final String TYPE = "agent";

    /** Version du format, pour pouvoir le faire évoluer plus tard. */
    private static final String VERSION = "v1";

    /** Clé secrète de signature (niveau TP). */
    private static final String SECRET = "citoyen-actif-inf1031-cle-tp";

    /** Taille maximale acceptée pour le texte lu (on ne fait pas confiance à la carte). */
    private static final int MAX_LENGTH = 64;

    /** Format strict attendu : v1:<numéro>:<16 caractères hexadécimaux>. */
    private static final Pattern FORMAT =
            Pattern.compile("^" + VERSION + ":([A-Za-z0-9-]{3,20}):([0-9a-f]{16})$");

    private static final Pattern AGENT_NUMBER = Pattern.compile("^[A-Za-z0-9-]{3,20}$");

    private NfcCardUtil() { }

    /**
     * Construit le message NDEF à écrire sur une carte pour cet agent.
     *
     * @throws IllegalArgumentException si le numéro d'agent est invalide
     */
    public static NdefMessage buildMessage(String agentNumber) {
        if (agentNumber == null || !AGENT_NUMBER.matcher(agentNumber).matches()) {
            throw new IllegalArgumentException("Numéro d'agent invalide");
        }
        String signature = sign(agentNumber);
        if (signature == null) {
            throw new IllegalStateException("Signature impossible");
        }
        String text = VERSION + ":" + agentNumber + ":" + signature;
        NdefRecord record = NdefRecord.createExternal(
                DOMAIN, TYPE, text.getBytes(StandardCharsets.UTF_8));
        return new NdefMessage(record);
    }

    /**
     * Lit un message NDEF et retourne le numéro d'agent s'il est valide.
     * Retourne null si : pas de bon type, texte trop long, mauvais format
     * ou mauvaise signature.
     */
    public static String extractAgentNumber(NdefMessage message) {
        if (message == null) return null;

        byte[] expectedType = (DOMAIN + ":" + TYPE).getBytes(StandardCharsets.US_ASCII);

        for (NdefRecord record : message.getRecords()) {
            // 1) Bon type d'enregistrement ?
            if (record.getTnf() != NdefRecord.TNF_EXTERNAL_TYPE) continue;
            if (!java.util.Arrays.equals(record.getType(), expectedType)) continue;

            // 2) Taille raisonnable ?
            byte[] payload = record.getPayload();
            if (payload == null || payload.length > MAX_LENGTH) return null;

            // 3) Format strict ?
            String text = new String(payload, StandardCharsets.UTF_8);
            Matcher m = FORMAT.matcher(text);
            if (!m.matches()) return null;

            String agentNumber = m.group(1);
            String signatureRead = m.group(2);

            // 4) Signature correcte ? (comparaison à temps constant)
            String signatureExpected = sign(agentNumber);
            if (signatureExpected == null) return null;
            boolean ok = MessageDigest.isEqual(
                    signatureRead.getBytes(StandardCharsets.US_ASCII),
                    signatureExpected.getBytes(StandardCharsets.US_ASCII));
            return ok ? agentNumber : null;
        }
        return null;
    }

    /** Calcule la signature : 16 premiers caractères hexa du HMAC-SHA256. */
    private static String sign(String agentNumber) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(agentNumber.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            return null;
        }
    }
}
// Relais entre l'app Android et FCM (API HTTP v1).
// L'app envoie une demande ici, le serveur obtient un jeton OAuth 2.0 puis publie sur FCM.
const express = require("express");
const { GoogleAuth } = require("google-auth-library");

const PORT = process.env.PORT || 3000;

// Lit le compte de service indiqué par GOOGLE_APPLICATION_CREDENTIALS
const auth = new GoogleAuth({
  scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
});

// Publie un message "data" sur le sujet du citoyen (ex. citizen-12)
async function sendToCitizen(citizenId, reportId, title, body) {
  const projectId = await auth.getProjectId();
  const token = await auth.getAccessToken();

  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          topic: `citizen-${citizenId}`,
          // Les valeurs de "data" doivent toutes être des chaînes
          data: { title, body, reportId: String(reportId) },
          android: { priority: "high" },
        },
      }),
    }
  );

  const json = await response.json();
  if (!response.ok) {
    throw new Error(`FCM ${response.status} : ${json.error?.message}`);
  }
  return json.name;
}

const app = express();
app.use(express.json());

// Appelé par l'app Android quand un agent traite un signalement
app.post("/notify", async (req, res) => {
  const citizenId = Number(req.body?.citizenId);
  const reportId = Number(req.body?.reportId);
  const title = String(req.body?.title ?? "").trim().slice(0, 80);
  const body = String(req.body?.body ?? "").trim().slice(0, 300);

  // Validation simple des entrées
  if (!Number.isInteger(citizenId) || citizenId <= 0 ||
      !Number.isInteger(reportId) || reportId <= 0 || !title || !body) {
    return res.status(400).json({ error: "citizenId, reportId, title et body sont obligatoires" });
  }

  try {
    const id = await sendToCitizen(citizenId, reportId, title, body);
    console.log(`Notification pour citizen-${citizenId} : ${title}`);
    res.status(201).json({ id });
  } catch (err) {
    console.error(err.message);
    res.status(502).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`Serveur de notifications : http://localhost:${PORT}`);
});
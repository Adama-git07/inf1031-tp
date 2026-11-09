package com.example.citoyenactif.ui.binding;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.citoyenactif.BR;
import com.example.citoyenactif.data.local.entity.ReportEntity;

/**
 * Modèle observable du dialogue de détail d'un signalement (côté agent).
 *
 * BaseObservable + @Bindable : quand on appelle notifyPropertyChanged,
 * le XML se met à jour tout seul. Avec @={model.ignoreReason}, ce qui est
 * tapé dans le champ revient aussi dans ce modèle (double binding).
 */
public class ReportEditModel extends BaseObservable {

    /** Actions déclenchées par les boutons du dialogue (implémentées dans le fragment). */
    public interface Actions {
        void onInProgress();
        void onResolve();
        void onIgnore(String reason);
    }

    private final ReportEntity report;
    private boolean ignoreSelected = false;
    private String ignoreReason = "";

    public ReportEditModel(ReportEntity report) {
        this.report = report;
        // Si le signalement a déjà une raison d'ignorance, on la préremplit
        if (report != null && report.ignoreReason != null) {
            this.ignoreReason = report.ignoreReason;
        }
    }

    /** Signalement affiché (lecture seule dans le XML). */
    public ReportEntity getReport() {
        return report;
    }

    /** Vrai quand l'agent a cliqué sur "Ignorer" : le champ raison apparaît. */
    @Bindable
    public boolean isIgnoreSelected() {
        return ignoreSelected;
    }

    /** Affiche ou cache le champ raison. */
    public void toggleIgnore() {
        ignoreSelected = !ignoreSelected;
        notifyPropertyChanged(BR.ignoreSelected);
    }

    /** Raison saisie (liée en double sens avec le EditText). */
    @Bindable
    public String getIgnoreReason() {
        return ignoreReason;
    }

    public void setIgnoreReason(String value) {
        // On évite la boucle infinie : on ne notifie que si la valeur change
        if (value == null) value = "";
        if (!value.equals(ignoreReason)) {
            ignoreReason = value;
            notifyPropertyChanged(BR.ignoreReason);
        }
    }
}
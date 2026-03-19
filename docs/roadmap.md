# Roadmap & Script de démonstration — Gourmandine v1.0

---

## Vue d'ensemble

Gourmandine est une application Android de découverte et réservation de restaurants.
Cette version v1.0 couvre l'intégralité du parcours utilisateur : trouver un restaurant → consulter sa fiche → réserver → gérer ses réservations → laisser un avis → gérer ses favoris.

---

## Parcours de démonstration recommandé

### Séquence 1 — Découverte (30 s)

**Écran : Accueil / Carte**

1. Ouvrir l'application → la carte Google Maps s'affiche avec les restaurants proches
2. Montrer les **marqueurs oranges** et le **clustering** (zoom arrière = regroupement)
3. Appuyer sur le bouton **localisation** → la carte centre sur la position actuelle
4. Faire glisser le **panneau du bas** vers le haut → liste des restaurants apparaît
5. Montrer la **barre de recherche** → taper un nom de restaurant
6. Ouvrir les **filtres** (icône filtre) → montrer les options : note, prix, distance, horaires

> Capture à inclure : carte avec marqueurs + panneau mi-hauteur

---

### Séquence 2 — Fiche restaurant (30 s)

**Écran : Fiche détail restaurant**

1. Appuyer sur un marqueur → bulle de détail avec nom et note
2. Appuyer sur **"More details"** → la fiche complète s'ouvre
3. Faire défiler : **carrousel photos**, note, type de cuisine, prix, adresse, téléphone
4. Montrer la **mini-carte** en bas de la fiche
5. Dérouler jusqu'aux **avis** → montrer les avis Google + avis Gourmandine
6. Appuyer sur un avis → **popover plein écran** avec carrousel des avis

> Capture à inclure : fiche ouverte avec photos + section avis

---

### Séquence 3 — Authentification (20 s)

**Écran : Connexion / Inscription**

1. Depuis la barre de navigation → appuyer sur l'icône **Profil**
2. Montrer l'écran de connexion → **connexion par email** ou **Google Sign-In**
3. Se connecter → retour automatique à l'écran précédent

> Alternatively : appuyer sur Réserver sans être connecté → overlay de connexion contextuel s'affiche → après connexion, la réservation s'ouvre automatiquement

> Capture à inclure : écran login + overlay contextuel

---

### Séquence 4 — Réservation (40 s)

**Écran : Dialog de réservation → Mes Réservations**

1. Sur la fiche d'un restaurant → appuyer sur **Réserver**
2. Choisir une **date** via le date picker
3. Sélectionner le **nombre de personnes**
4. Ajouter une **note** (ex. "Table en terrasse")
5. Appuyer sur **Confirmer** → redirection vers **Mes Réservations**
6. Montrer l'onglet **À venir** → la réservation apparaît avec nom, date, convives
7. Appuyer sur **Modifier la date** → date picker s'ouvre
8. Appuyer sur **Ajouter au calendrier** → export vers le calendrier Android
9. Basculer sur l'onglet **Passées**

> Capture à inclure : dialog réservation + liste réservations à venir

---

### Séquence 5 — Avis (30 s)

**Écran : Ajouter un avis**

1. Depuis une réservation passée → appuyer sur **Laisser un avis**
2. Sélectionner une note en **étoiles**
3. Rédiger un **commentaire**
4. Appuyer sur **Ajouter une photo** → importer depuis la galerie
5. Montrer l'**éditeur photo** : recadrage + ajout d'un sticker
6. Appuyer sur **Publier** → l'avis apparaît sur la fiche du restaurant

> Capture à inclure : formulaire avis + éditeur photo

---

### Séquence 6 — Favoris (20 s)

**Écran : Mes Favoris**

1. Sur une fiche restaurant → appuyer sur l'icône **cœur** → ajout aux favoris
2. Naviguer vers l'onglet **Favoris** → le restaurant apparaît avec photo, note, adresse
3. Appuyer sur le cœur plein → suppression du favori

> Capture à inclure : liste favoris avec carte restaurant

---

### Séquence 7 — Profil & Navigation (20 s)

**Écran : Mon Profil**

1. Naviguer vers **Mon Profil** via la barre de navigation
2. Montrer les informations : nom, email, téléphone
3. Appuyer sur **Modifier mes informations** → bottom sheet édition
4. Modifier le prénom → **Enregistrer**
5. Montrer la **navigation swipe** depuis l'accueil : glisser droite → Profil, gauche → Réservations

> Capture à inclure : profil connecté + bottom sheet édition

---

## Captures d'écran recommandées pour le document de conception

| N° | Écran | Moment idéal |
|---|---|---|
| 1 | Carte + marqueurs + panneau mi-hauteur | Séquence 1 |
| 2 | Bottom sheet filtres ouverts | Séquence 1 |
| 3 | Fiche restaurant (photos + infos) | Séquence 2 |
| 4 | Popover avis plein écran | Séquence 2 |
| 5 | Overlay connexion contextuel | Séquence 3 |
| 6 | Dialog de réservation | Séquence 4 |
| 7 | Liste réservations à venir | Séquence 4 |
| 8 | Formulaire avis + éditeur photo | Séquence 5 |
| 9 | Liste favoris | Séquence 6 |
| 10 | Profil + bottom sheet édition | Séquence 7 |

---

## Durée totale estimée : ~3 min

| Séquence | Durée |
|---|---|
| 1 — Découverte carte | 30 s |
| 2 — Fiche restaurant | 30 s |
| 3 — Connexion | 20 s |
| 4 — Réservation | 40 s |
| 5 — Avis | 30 s |
| 6 — Favoris | 20 s |
| 7 — Profil & navigation | 20 s |
| **Total** | **~3 min** |

---

## Conseil d'enregistrement

- **Outil** : Android Studio → émulateur → bouton enregistrement (icône caméra dans la barre latérale), ou `scrcpy --record demo.mp4` sur device physique
- **Données de test** : créer un compte test avec quelques réservations passées pour pouvoir montrer l'onglet "Passées" et le formulaire d'avis sans attendre
- **Résolution** : enregistrer en Pixel 6 (1080×2400) pour un rendu propre

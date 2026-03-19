# Script vidéo — Gourmandine v1.0

> **Durée cible : 2 min 30 — 3 min**
> Enregistrer sur émulateur Pixel 6 ou device physique.
> Préparer un compte test connecté avec 2-3 réservations passées avant de filmer.

---

## Préparation avant tournage

- [ ] Compte test créé : `demo@gourmandine.fr` / `Demo1234!`
- [ ] 2 réservations passées enregistrées dans Firestore pour cet utilisateur
- [ ] 1 restaurant en favori déjà ajouté
- [ ] App en mode release (ou debug propre, pas de toast Firebase visible)
- [ ] Connexion WiFi active
- [ ] Émulateur : Pixel 6, Android 14, langue FR

---

## SCÈNE 1 — Ouverture (0:00 → 0:10)

**Objectif : montrer le splash + la carte qui se charge**

| # | Action | Geste |
|---|---|---|
| 1 | Lancer l'application depuis le launcher | Tap sur icône |
| 2 | Attendre que la carte Google Maps se charge | — |
| 3 | Laisser les marqueurs orange apparaître progressivement | — |

> La carte est centrée sur Paris avec des marqueurs orange et un panneau bas visible (~140dp).

---

## SCÈNE 2 — Carte & découverte (0:10 → 0:40)

**Objectif : montrer la carte, le clustering, la géolocalisation et le panneau**

| # | Action | Geste |
|---|---|---|
| 4 | Faire un **zoom arrière** sur la carte | Pinch out |
| 5 | Observer le **clustering** des marqueurs (cercles avec chiffre) | — |
| 6 | Faire un **zoom avant** pour les voir se séparer | Pinch in |
| 7 | Appuyer sur le bouton **localisation** (en bas à gauche) | Tap |
| 8 | La carte se recentre sur la position actuelle | — |
| 9 | **Glisser le panneau bas vers le haut** jusqu'à mi-hauteur | Swipe up |
| 10 | Faire défiler la liste des restaurants | Scroll down |
| 11 | **Glisser encore vers le haut** pour voir le panneau plein écran | Swipe up |
| 12 | La barre de recherche est visible et active | — |

---

## SCÈNE 3 — Recherche & Filtres (0:40 → 1:00)

**Objectif : montrer la recherche textuelle et les filtres**

| # | Action | Geste |
|---|---|---|
| 13 | Taper dans la barre de recherche : `"Sushi"` | Tap + type |
| 14 | Observer les résultats filtrés dans la liste et sur la carte | — |
| 15 | Effacer la recherche (croix) | Tap ✕ |
| 16 | Appuyer sur l'icône **Filtre** à droite de la barre de recherche | Tap |
| 17 | Le bottom sheet des filtres s'ouvre | — |
| 18 | Appuyer sur **⭐⭐⭐⭐** (4 étoiles min) | Tap |
| 19 | Glisser le **slider distance** vers 2 km | Slide |
| 20 | Fermer le filtre (glisser vers le bas) | Swipe down |
| 21 | Observer le badge orange sur l'icône filtre (nombre de filtres actifs) | — |

---

## SCÈNE 4 — Fiche restaurant (1:00 → 1:30)

**Objectif : montrer la fiche complète avec photos, infos et avis**

| # | Action | Geste |
|---|---|---|
| 22 | Appuyer sur un **marqueur orange** sur la carte | Tap |
| 23 | La bulle de détail apparaît (nom + note + bouton "More details") | — |
| 24 | Appuyer sur **"More details"** | Tap |
| 25 | La fiche restaurant s'ouvre avec le **carrousel de photos** | — |
| 26 | **Glisser les photos** du carrousel (gauche/droite) | Swipe horizontal |
| 27 | Faire défiler la fiche vers le bas | Scroll down |
| 28 | Observer : note, type cuisine, prix, adresse, téléphone | — |
| 29 | Continuer à défiler jusqu'à la **section avis** | Scroll down |
| 30 | Appuyer sur un **avis** pour l'ouvrir en plein écran | Tap |
| 31 | Le **popover** s'affiche avec la review complète | — |
| 32 | **Glisser horizontalement** pour passer à l'avis suivant | Swipe horizontal |
| 33 | Fermer le popover (tap en dehors) | Tap outside |

---

## SCÈNE 5 — Connexion (1:30 → 1:50)

**Objectif : montrer l'auth guard + l'overlay de connexion contextuel**

| # | Action | Geste |
|---|---|---|
| 34 | Sur la fiche → appuyer sur **Réserver** (sans être connecté) | Tap |
| 35 | L'**overlay de connexion** s'affiche automatiquement | — |
| 36 | Saisir l'email : `demo@gourmandine.fr` | Type |
| 37 | Saisir le mot de passe : `Demo1234!` | Type |
| 38 | Appuyer sur **Se connecter** | Tap |
| 39 | La connexion réussit → le **dialog de réservation s'ouvre directement** | — |

> Ce comportement démontre que l'action est mémorisée et exécutée après connexion.

---

## SCÈNE 6 — Réservation (1:50 → 2:20)

**Objectif : montrer le booking complet + gestion des réservations**

| # | Action | Geste |
|---|---|---|
| 40 | Le **dialog de réservation** est ouvert | — |
| 41 | Appuyer sur le champ **date** → date picker s'ouvre | Tap |
| 42 | Sélectionner une date dans 3 jours | Tap date |
| 43 | Confirmer la date → **OK** | Tap |
| 44 | Appuyer sur **+** pour passer à **2 personnes** | Tap |
| 45 | Taper une note : `"Table en terrasse svp"` | Type |
| 46 | Appuyer sur **Confirmer la réservation** | Tap |
| 47 | Redirection automatique vers **Mes Réservations** | — |
| 48 | La nouvelle réservation apparaît dans l'onglet **À venir** | — |
| 49 | Appuyer sur **Modifier la date** | Tap |
| 50 | Changer la date → confirmer | Tap |
| 51 | Appuyer sur **Ajouter au calendrier** | Tap |
| 52 | Basculer sur l'onglet **Passées** | Tap tab |
| 53 | Observer les réservations passées | — |

---

## SCÈNE 7 — Avis (2:20 → 2:50)

**Objectif : montrer le dépôt d'un avis avec photo**

| # | Action | Geste |
|---|---|---|
| 54 | Sur une réservation passée → appuyer sur **Laisser un avis** | Tap |
| 55 | Le formulaire d'avis s'ouvre | — |
| 56 | Appuyer sur la **4e étoile** pour noter 4/5 | Tap |
| 57 | Taper un commentaire : `"Excellent repas, service impeccable !"` | Type |
| 58 | Appuyer sur **Ajouter une photo** | Tap |
| 59 | Sélectionner une photo dans la galerie | Tap |
| 60 | L'**éditeur photo** s'ouvre (recadrage) | — |
| 61 | Recadrer légèrement la photo | Drag handles |
| 62 | Appuyer sur un **sticker** (ex. étoile) | Tap |
| 63 | Appuyer sur **Valider** | Tap |
| 64 | Appuyer sur **Publier l'avis** | Tap |
| 65 | L'avis apparaît sur la fiche du restaurant | — |

---

## SCÈNE 8 — Favoris (2:50 → 3:05)

**Objectif : montrer l'ajout/suppression de favoris**

| # | Action | Geste |
|---|---|---|
| 66 | Revenir sur une fiche restaurant | Tap |
| 67 | Appuyer sur l'icône **cœur** | Tap |
| 68 | Le cœur devient plein (rouge/orange) | — |
| 69 | Naviguer vers **Mes Favoris** via la barre de navigation | Tap icône cœur |
| 70 | Le restaurant apparaît dans la liste | — |
| 71 | Appuyer sur le **cœur plein** pour retirer | Tap |
| 72 | Le restaurant disparaît de la liste | — |

---

## SCÈNE 9 — Profil & Navigation (3:05 → 3:20)

**Objectif : montrer le profil, l'édition et la navigation swipe**

| # | Action | Geste |
|---|---|---|
| 73 | Naviguer vers **Mon Profil** via la barre de navigation | Tap icône personne |
| 74 | Observer : nom, email, initiales dans l'avatar orange | — |
| 75 | Appuyer sur **Modifier mes informations** | Tap |
| 76 | Le bottom sheet d'édition s'ouvre | — |
| 77 | Modifier le prénom | Clear + type |
| 78 | Appuyer sur **Enregistrer** | Tap |
| 79 | Le profil se met à jour | — |
| 80 | Revenir sur la **carte** via l'icône Map | Tap |
| 81 | **Glisser le panneau vers la droite** → navigate vers Profil | Swipe right |
| 82 | Revenir → **glisser vers la gauche** → navigate vers Réservations | Swipe left |

---

## SCÈNE 10 — Clôture (3:20 → 3:30)

| # | Action |
|---|---|
| 83 | Revenir sur la carte avec la liste de restaurants visible |
| 84 | Zoom sur un marqueur sélectionné avec bulle de détail |
| 85 | Laisser l'image fixe 3 secondes → fin |

---

## Résumé des gestes clés à ne pas rater

| Geste | Où | Pourquoi l'inclure |
|---|---|---|
| Swipe up sur panneau | Accueil | Feature principale de l'UI |
| Pinch zoom clustering | Carte | Montre la scalabilité |
| Swipe horizontal avis | Popover | Carrousel de reviews |
| Tap Réserver sans login | Fiche | Auth guard contextuel |
| Swipe gauche/droite panneau | Accueil | Navigation gestuelle |
| Éditeur photo + sticker | Avis | Feature différenciante |

# Document de Conception — Gourmandine

## 1. Présentation du projet

Gourmandine est une application Android de découverte et réservation de restaurants. Elle permet à l'utilisateur de localiser des restaurants à proximité sur une carte, de consulter leurs fiches détaillées, de gérer ses favoris, d'effectuer des réservations et de déposer des avis illustrés.

---

## 2. Fonctionnalités présentes et absentes

### Fonctionnalités implémentées

| Fonctionnalité | Détail |
|---|---|
| Carte interactive | Google Maps avec marqueurs clustering, sélection et bulle de détail |
| Recherche & filtres | Recherche textuelle, filtres par note / prix / distance / horaires |
| Fiche restaurant | Photos, infos, adresse, téléphone, mini-carte, avis Google + internes |
| Favoris | Ajout/suppression persisté dans Firestore, écran dédié |
| Réservation | Booking avec date, convives, notes ; modification de date ; export calendrier |
| Avis | Dépôt avec note, texte, photo ; éditeur photo (recadrage, stickers) |
| Authentification | Email/mot de passe + Google Sign-In (Credential Manager) |
| Profil | Édition nom, prénom, téléphone |
| Navigation | Barre de navigation haut unifiée, swipe horizontal sur le panneau |
| Vérification Firebase | Toast d'erreur au démarrage si Firestore inaccessible |

### Fonctionnalités absentes (hors périmètre ou non développées)

| Fonctionnalité | Raison |
|---|---|
| Paiement en ligne | Hors périmètre (nécessite intégration Stripe/PayPal) |
| Notifications push | Non implémenté (requiert Firebase Cloud Messaging) |
| Modération des avis | Pas de back-office admin |
| Pagination restaurants | Liste chargée en une fois depuis Places API |
| Mode hors-ligne | Pas de cache local Firestore activé |
| Partage de réservation | Non développé |

---

## 3. Choix techniques

### Stack principale

- **Kotlin** + **Jetpack Compose** (Material 3) : interface déclarative, composants modernes, cohérence visuelle.
- **MVVM** avec `StateFlow` / `MutableStateFlow` : séparation claire UI / logique métier. Pas de framework DI (Hilt) pour limiter la complexité — les repositories sont instanciés directement dans les ViewModels.
- **Jetpack Navigation Compose** : navigation type-safe entre écrans avec `NavHost` et routes nommées.

### Backend & données

- **Firebase Authentication** : gestion sécurisée des comptes email et Google Sign-In via Android Credential Manager.
- **Cloud Firestore** : stockage des réservations (`reservations/{userId}/items/`), favoris (`favorites/{userId}/items/`) et avis (`reviews/`).
- **Firebase Storage** : hébergement des photos d'avis (`review_images/`).

### Cartographie & lieux

- **Google Maps Compose** (`maps-compose 6.1.0`) : carte native avec clustering, marqueurs composables personnalisés.
- **Google Places API (New)** : recherche de restaurants à proximité, détails (horaires, photos, numéro, type de cuisine, livraison…).

### Chargement d'images

- **Coil** (`coil-compose 2.7.0`) : chargement asynchrone des photos restaurants et avis avec placeholder.

### Édition photo

- **UCrop** (`yalantis:ucrop 2.2.9`) : recadrage de photo avant publication d'un avis.
- Éditeur custom (Canvas) pour l'ajout de stickers emoji sur les photos.

### Build & release

- **Gradle Kotlin DSL** avec `signingConfig` release chargé depuis `local.properties` (exclu du contrôle de version).
- **R8** + `isShrinkResources = true` : minification et réduction de taille de l'APK.

---

## 4. Structure du projet

```
app/src/main/java/com/assgui/gourmandine/
│
├── data/
│   ├── model/           # Data classes : Restaurant, Favorite, Reservation, Review
│   └── repository/      # Accès données : PlacesRepository, FavoritesRepository, ReservationRepository
│
├── navigation/          # AppDestinations (routes et helpers de navigation)
│
├── ui/
│   ├── components/      # Composants partagés : RestaurantCard, RestaurantDetailSheet,
│   │                    #   SwipeableSheet, ReviewCard, AppBottomNavBar, AppButton
│   │   └── restaurantdetail/   # Sous-composants fiche : ImageCarousel, ReviewsSection,
│   │                           #   RestaurantInfoHeader, RestaurantActionButtons
│   │
│   ├── screens/
│   │   ├── home/        # HomeScreen, HomeViewModel, composants carte + sheet + filtres
│   │   ├── profile/     # LoginScreen, RegisterScreen, ProfileScreen, AuthViewModel
│   │   ├── reservation/ # ReservationScreen, ReservationViewModel, composants booking
│   │   ├── favorites/   # FavoritesScreen, FavoritesViewModel
│   │   └── addreview/   # AddReviewScreen, PhotoEditorScreen
│   │
│   └── theme/           # AppColors, AppShapes, AppTextStyles, GourmandineTheme
│
└── MainActivity.kt      # Point d'entrée, NavHost, initialisation Firebase & Places
```

**Patterns architecturaux notables :**
- Les écrans secondaires (AddReview, login depuis la carte) sont affichés en **overlay composable** dans `HomeScreen` plutôt que par navigation — évite les problèmes de synchronisation d'état avec le `HomeViewModel` partagé.
- Le `HomeViewModel` est scopé au niveau activité et partagé entre routes pour préserver l'état de la carte.

---

## 5. Limites connues

- **Résultats Places API** : limités à 20 restaurants par requête ; pas de pagination infinie.
- **Clustering** : la librairie `maps-compose-utils` peut provoquer des lags sur des listes > 100 marqueurs.
- **Avis Google** : l'API Places renvoie au maximum 5 avis par restaurant.
- **Keystore** : le fichier `gourmandine-release.jks` est stocké localement hors repo ; à intégrer dans un CI/CD sécurisé pour la production.
- **Pas de tests** : l'application ne possède pas de tests unitaires ou d'intégration automatisés.

---

## 6. Cas d'utilisation — Réservation d'un restaurant

*Scénario : un utilisateur connecté réserve un restaurant depuis la carte.*

1. **Accueil** → la carte affiche les restaurants proches. L'utilisateur appuie sur un marqueur orange → une bulle de détail apparaît avec le nom et la note du restaurant.
2. **Fiche restaurant** → il fait glisser le panneau vers le haut. La fiche complète s'affiche : photos en carrousel, informations, avis.
3. **Réservation** → il appuie sur **Réserver**. Le dialog de réservation s'ouvre. Il choisit une date via le date picker, sélectionne 2 personnes, ajoute une note.
4. **Confirmation** → il appuie sur **Confirmer**. La réservation est enregistrée dans Firestore (`reservations/{userId}/items/`) et il est redirigé vers l'onglet **Mes Réservations**.
5. **Gestion** → depuis l'onglet Réservations, il peut modifier la date ou exporter l'événement dans son calendrier Android.

> Si l'utilisateur n'est pas connecté à l'étape 3, un overlay de connexion s'affiche. Après connexion réussie, l'action de réservation s'exécute automatiquement.

# Document de Conception — Gourmandine

## 1. Présentation du projet

Gourmandine est une application Android de découverte et réservation de restaurants, développée en Kotlin avec Jetpack Compose. Elle permet à l'utilisateur de localiser des restaurants à proximité sur une carte interactive, de consulter leurs fiches détaillées, de gérer ses favoris, d'effectuer des réservations et de déposer des avis illustrés.

---

## 2. Fonctionnalités

### Fonctionnalités implémentées

| Fonctionnalité | Détail |
|---|---|
| Carte interactive | Google Maps avec marqueurs clustering, sélection et bulle de détail |
| Recherche & filtres | Recherche textuelle, filtres par note / prix / distance / horaires |
| Fiche restaurant | Photos, infos, adresse, téléphone, mini-carte, avis Google + internes |
| Favoris | Ajout/suppression persisté dans Firestore, écran dédié |
| Réservation | Booking avec date, convives, notes ; modification de date ; export calendrier |
| Avis | Dépôt avec note, texte, photo ; éditeur photo (recadrage) |
| Authentification | Email/mot de passe + Google Sign-In (Credential Manager) |
| Profil | Édition nom, prénom, téléphone |
| Navigation | Barre de navigation bas standard Material3 |
| Connectivité | Toast d'avertissement au démarrage si pas de réseau |

### Fonctionnalités absentes (hors périmètre)

| Fonctionnalité | Raison |
|---|---|
| Paiement en ligne | Nécessite intégration Stripe/PayPal |
| Notifications push | Requiert Firebase Cloud Messaging |
| Modération des avis | Pas de back-office admin |
| Pagination restaurants | Liste chargée en une fois depuis Places API (max 20) |
| Mode hors-ligne complet | Pas de cache local Firestore persistant |
| Partage de réservation | Non développé |
| Tests automatisés | Aucun test unitaire ou d'intégration |

---

## 3. Architecture

### Pattern MVVM

On a structuré le projet selon une architecture **MVVM** sans framework d'injection de dépendances. Un **ServiceLocator** expose des singletons paresseux pour tous les repositories. Chaque ViewModel expose un `StateFlow<UiState>` immuable ; les écrans collectent cet état via `collectAsState()` et ne modifient rien directement.

```
MainActivity
│
├── GourmandineApp()                   ← Composable racine
│   ├── NavigationBar (4 onglets)
│   ├── HomeScreen
│   ├── FavoritesScreen
│   ├── ReservationScreen
│   └── ProfileScreen
│
├── HomeViewModel (scope activité)     ← partagé entre tous les onglets
└── ReservationViewModel (scope activité)
```

**Patterns notables :**
- `HomeViewModel` est scopé au niveau activité et partagé entre onglets pour préserver l'état de la carte.
- Les overlays (détail restaurant, ajout d'avis, login, booking) sont gérés comme des états dans `MainActivity` plutôt que par navigation — évite les problèmes de synchronisation.
- Les mises à jour optimistes (ex : favoris) : l'UI bascule immédiatement, puis Firebase confirme. En cas d'échec, l'état précédent est restauré.

### Structure des packages

```
app/src/main/java/com/assgui/gourmandine/
│
├── data/
│   ├── cache/           # CacheManager (TTL : 3-30 min selon type)
│   ├── model/           # Restaurant, Favorite, Reservation, Review, User
│   ├── repository/      # PlacesRepository, FavoritesRepository,
│   │                    # ReservationRepository, ReviewRepository,
│   │                    # AuthRepository, UserRepository, ImageStorageRepository
│   └── ServiceLocator.kt
│
├── ui/
│   ├── components/      # RestaurantDetailSheet, ReviewCard, RestaurantCard…
│   │   └── restaurantdetail/   # ImageCarousel, ReviewsSection, RestaurantInfoHeader…
│   │
│   ├── screens/
│   │   ├── home/        # HomeScreen, HomeViewModel, carte + sheet + filtres
│   │   ├── profile/     # LoginScreen, RegisterScreen, ProfileScreen, AuthViewModel
│   │   ├── reservation/ # ReservationScreen, ReservationViewModel, booking dialog
│   │   ├── favorites/   # FavoritesScreen, FavoritesViewModel
│   │   └── addreview/   # AddReviewScreen, PhotoEditorScreen
│   │
│   └── theme/           # AppColors, AppShapes, AppTypography, GourmandineTheme
│
└── MainActivity.kt
```

---

## 4. Modèles de données

```kotlin
Restaurant   id, name, imageUrls, rating, reviewCount, country,
             priceLevel, isOpen, latitude, longitude, address,
             description, phoneNumber, cuisineType,
             hasDineIn, hasDelivery, hasTakeout

Review       id, restaurantId, restaurantName, userId, userName,
             imageUrls, text, rating, createdAt, visitDate,
             isGoogleReview, userPhotoUrl

Reservation  id, userId, restaurantId, restaurantName,
             restaurantAddress, restaurantImageUrl,
             dateMs, partySize, notes, createdAt
             isPast: Boolean  ← propriété calculée

Favorite     restaurantId, restaurantName, restaurantImageUrl,
             restaurantAddress, restaurantRating, addedAt

User         uid, nom, prenom, email, phone, createdAt
```

---

## 5. Backend Firebase

### Structure Firestore

```
reviews/
  {docId}/                        ← avis publics (tous restaurants)

favorites/
  {userId}/
    items/
      {restaurantId}/             ← favoris par utilisateur

reservations/
  {userId}/
    items/
      {reservationId}/            ← réservations par utilisateur

users/
  {uid}/                          ← profil utilisateur
```

### Firebase Storage

Les photos d'avis sont stockées sous `review_images/{userId}_{UUID}.jpg`. On upload d'abord les fichiers, puis on stocke les URLs de téléchargement dans le document Firestore de l'avis.

### Authentification — partie complexe

On supporte deux modes :
- **Email / mot de passe** via `FirebaseAuth`
- **Google Sign-In** via le **Credential Manager** Android (API moderne, remplaçant `GoogleSignInClient`)

Le Google Sign-In a été difficile à configurer car il nécessite un **Web Client ID OAuth 2.0** (différent de l'Android Client ID), défini dans la Google Cloud Console et injecté via `BuildConfig` depuis `local.properties`. Cette distinction est rarement explicitée dans la documentation officielle.

```kotlin
val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(false)
    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)  // ← Web Client ID
    .build()
```

---

## 6. Intégration Google Places API — partie complexe

### Pourquoi c'était difficile

La **nouvelle API v2** (`places:4.0.0`) a une interface entièrement différente de l'ancienne. La quasi-totalité des tutoriels disponibles documentent l'ancienne version, ce qui a rendu l'intégration difficile.

### Ce qu'on a mis en place

**Recherche de proximité** via `PlacesClient.searchNearby()` avec un `CircularBounds` de 1 500 mètres. On demande uniquement les champs nécessaires pour maîtriser les coûts :

```
DISPLAY_NAME, LOCATION, RATING, USER_RATING_COUNT, PRICE_LEVEL,
CURRENT_OPENING_HOURS, FORMATTED_ADDRESS, PHOTO_METADATAS,
NATIONAL_PHONE_NUMBER, REVIEWS, EDITORIAL_SUMMARY,
TYPES, DINE_IN, DELIVERY, TAKEOUT
```

**Recherche textuelle** via `searchByText()` avec un `LocationBias` de 1 000 km. Les résultats non-alimentaires sont filtrés sur les types retournés par l'API.

**Photos** — Les images Google ne s'obtiennent pas directement : il faut récupérer les `PhotoMetadata` puis appeler `fetchPhoto()` pour chaque image (asynchrone, largeur max 400 px). Ce double appel est géré de façon transparente dans `PlacesRepository`.

**Avis Google** — Fusionnés avec nos propres avis Firebase dans le même écran. On distingue les deux sources via `isGoogleReview: Boolean` dans le modèle `Review`. L'API Places renvoie au maximum 5 avis par restaurant.

**Rafraîchissement automatique** — La recherche se relance quand la caméra de la carte se déplace de plus de 800 mètres par rapport à la dernière requête (méthode `onCameraIdle` avec distance Haversine).

---

## 7. Système de filtres

On a implémenté un filtrage multi-critères local (sans appel API supplémentaire) dans `HomeViewModel`.

### Critères disponibles

| Filtre | Logique |
|---|---|
| Ouvert maintenant | `r.isOpen == true` |
| Note ≥ 3 / 3.5 / 4 / 4.5 | `r.rating >= seuil` — le seuil le plus élevé sélectionné s'applique |
| Prix €  / €€ / €€€ | `r.priceLevel == N` — disjonctif : un seul critère doit correspondre |
| Distance (500 m → 10 km) | `haversineKm(userPos, restaurantPos) <= max` |

**Logique de combinaison** : tous les critères sont en conjonction (ET), sauf les prix qui sont en disjonction (OU entre eux).

`applyFilters()` s'exécute sur la liste locale déjà chargée à chaque modification — aucun appel réseau supplémentaire.

---

## 8. Gestion de la caméra et de la localisation

### Position au démarrage

1. On tente de récupérer la **dernière position connue** de façon synchrone (rapide, potentiellement ancienne).
2. En parallèle, on lance une **requête GPS haute précision** asynchrone via `getCurrentLocation()`.
3. Si aucune permission de localisation n'est accordée → la carte démarre centrée sur Paris (48.8566, 2.3522).

### Bouton "Ma position"

La caméra est pilotée par les champs `cameraPosition` et `cameraZoom` du `HomeUiState`. On utilise un **`locationTrigger: Int`** (compteur incrémental) comme clé du `LaunchedEffect` qui anime la caméra — plutôt que les coordonnées directement. Cela garantit que l'animation se déclenche même si l'utilisateur appuie sur le bouton sans avoir bougé.

```kotlin
LaunchedEffect(uiState.locationTrigger) {
    cameraPositionState.animate(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(uiState.cameraPosition, uiState.cameraZoom)
        )
    )
}
```

### Calcul de distance

On utilise la **formule de Haversine** pour calculer la distance entre deux coordonnées géographiques (rayon terrestre : 6 371 km). Elle sert à la fois pour le filtre de distance et pour décider si la carte s'est suffisamment déplacée pour relancer une recherche (seuil : 0,8 km).

---

## 9. Cas d'utilisation — Réservation d'un restaurant

*Scénario : un utilisateur connecté réserve un restaurant depuis la carte.*

1. **Accueil** → la carte affiche les restaurants proches. L'utilisateur appuie sur un marqueur → une bulle de détail apparaît avec le nom et la note.
2. **Fiche restaurant** → il appuie sur "Plus de détails". La fiche complète s'affiche : photos en carrousel, informations, avis.
3. **Réservation** → il appuie sur **Réserver**. Le dialog de booking s'ouvre. Il choisit une date, sélectionne le nombre de convives, ajoute une note optionnelle.
4. **Confirmation** → il appuie sur **Confirmer**. La réservation est enregistrée dans Firestore et il est redirigé vers l'onglet **Mes Réservations**.
5. **Gestion** → depuis l'onglet Réservations, il peut modifier la date, supprimer la réservation ou exporter l'événement dans son calendrier Android.

> Si l'utilisateur n'est pas connecté à l'étape 3, un overlay de connexion s'affiche. Après connexion réussie, l'action de réservation s'exécute automatiquement via le mécanisme `pendingLoginAction`.

---

## 10. Limites connues

- **Places API** : 20 restaurants maximum par requête, pas de pagination.
- **Avis Google** : 5 avis maximum par restaurant (limite de l'API).
- **Clustering** : peut provoquer des lags sur des listes de plus de 100 marqueurs.
- **Cache** : en mémoire uniquement — perdu à la fermeture de l'app.
- **Keystore** : `gourmandine-release.jks` stocké localement, à intégrer dans un CI/CD sécurisé en production.
- **Tests** : aucun test automatisé (unitaire ou intégration).

---

## 11. Stack technique

| Technologie | Version | Usage |
|---|---|---|
| Kotlin | — | Langage principal |
| Jetpack Compose + Material3 | BOM 2024 | Interface utilisateur |
| Firebase Auth | BOM 34.8.0 | Authentification |
| Firebase Firestore | BOM 34.8.0 | Base de données |
| Firebase Storage | BOM 34.8.0 | Stockage photos |
| Google Places API (New) | 4.0.0 | Données restaurants |
| Google Maps Compose | 6.1.0 | Carte interactive |
| Play Services Location | 21.3.0 | GPS |
| Credential Manager | 1.3.0 | Google Sign-In |
| Coil | 2.7.0 | Chargement d'images |
| Navigation Compose | 2.8.4 | Navigation |
| UCrop | 2.2.9 | Recadrage de photos |
| R8 + ProGuard | — | Minification APK release |

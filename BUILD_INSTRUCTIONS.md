# Quincallerie Scanner - Instructions de construction

## Structure du projet

Cette application Android permet de scanner des codes-barres de produits de quincaillerie et de vérifier leur disponibilité chez plusieurs fournisseurs : Legallais, Trenois Descamps, Cogeferm, Häfele et Foussier.

## Prérequis

- Android Studio (version 2022.3.1 ou supérieure)
- JDK 17 ou supérieure
- Android SDK avec API level 34 installé
- Un appareil Android avec caméra (API level 24 minimum)

## Configuration du projet

### 1. Ouvrir le projet dans Android Studio

1. Lancez Android Studio
2. Sélectionnez "Open an Existing Project"
3. Naviguez vers le dossier `QuincallerieScanner` et ouvrez-le

### 2. Synchroniser les dépendances Gradle

Android Studio devrait automatiquement synchroniser les dépendances. Si ce n'est pas le cas :
- Cliquez sur "Sync Project with Gradle Files" dans la barre d'outils

### 3. Configurer l'émulateur ou l'appareil

Pour tester l'application, vous avez besoin :
- **Option 1** : Un appareil Android physique avec USB Debugging activé
- **Option 2** : Un émulateur Android (recommandé : API 34, avec caméra virtuelle)

## Construction et exécution

### Construction en mode Debug

```bash
# Dans le terminal, naviguer vers le projet
cd QuincallerieScanner

# Construire l'APK de debug
./gradlew assembleDebug

# L'APK sera généré dans :
# app/build/outputs/apk/debug/app-debug.apk
```

### Exécuter sur un appareil

1. Connectez votre appareil Android via USB
2. Activez le mode développeur et USB Debugging sur l'appareil
3. Dans Android Studio, cliquez sur le bouton "Run" (icône triangle vert)
4. Sélectionnez votre appareil dans la liste
5. L'application sera installée et lancée automatiquement

### Construction en mode Release

```bash
# Générer un APK signé pour la release
./gradlew assembleRelease

# L'APK sera généré dans :
# app/build/outputs/apk/release/app-release.apk
```

## Fonctionnalités de l'application

### Interface utilisateur
- **Écran principal** : Scanner de code-barres avec caméra
- **Affichage des résultats** : Liste des fournisseurs avec statut de disponibilité
- **Références produits** : Affichage des références chez chaque fournisseur

### Fonctionnement
1. L'utilisateur scanne un code-barres avec la caméra
2. L'application recherche le produit chez tous les fournisseurs
3. Les résultats sont affichés avec :
   - Nom du fournisseur
   - Référence du produit (quand disponible)
   - Statut de disponibilité (Disponible/Non disponible)

### Fournisseurs supportés
- ✅ Legallais
- ✅ Trenois Descamps
- ✅ Cogeferm
- ✅ Häfele
- ✅ Foussier

## Personnalisation

### Modifier les fournisseurs

Pour ajouter ou modifier des fournisseurs, éditez le fichier :
```java
app/src/main/java/com/example/quincalleriescanner/SupplierAPI.java
```

Ajoutez de nouvelles méthodes de recherche pour chaque fournisseur.

### Intégration avec des APIs réelles

Actuellement, l'application utilise une simulation de recherche. Pour intégrer des APIs réelles :

1. Obtenez les clés d'API pour chaque fournisseur
2. Modifiez `SupplierAPI.java` pour effectuer des appels HTTP réels
3. Mettez à jour les endpoints dans le fichier

Exemple pour une intégration Retrofit :

```java
// Dans SupplierAPI.java
public interface LegallaisService {
    @GET("/api/search")
    Call<LegallaisResponse> searchProduct(@Query("barcode") String barcode);
}

// Puis appelez l'API
Retrofit retrofit = createRetrofitClient(LEGALLAIS_BASE);
LegallaisService service = retrofit.create(LegallaisService.class);
Call<LegallaisResponse> call = service.searchProduct(barcode);
Response<LegallaisResponse> response = call.execute();
```

## Résolution des problèmes

### Erreur de dépendances
Si vous obtenez des erreurs de dépendances :
1. Vérifiez votre connexion Internet
2. Cliquez sur "File > Invalidate Caches / Restart" dans Android Studio
3. Synchronisez à nouveau les fichiers Gradle

### Problèmes de caméra
- Assurez-vous que l'application a la permission CAMERA
- Vérifiez que la caméra n'est pas utilisée par une autre application
- Sur émulateur, utilisez les contrôles de caméra virtuelle

### Problèmes de build
- Vérifiez que le SDK Android 34 est installé
- Assurez-vous que Java 17+ est installé et configuré dans Android Studio

## Architecture du code

```
app/
├── src/main/
│   ├── java/com/example/quincalleriescanner/
│   │   ├── MainActivity.java          # Activité principale
│   │   ├── SupplierAPI.java           # API de recherche des fournisseurs
│   │   ├── SupplierResult.java        # Modèle de données pour les résultats
│   │   └── ResultAdapter.java         # Adaptateur pour RecyclerView
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml      # Layout principal
│       │   └── item_result.xml        # Layout des résultats
│       ├── values/
│       │   ├── strings.xml            # Textes de l'application
│       │   ├── colors.xml             # Couleurs
│       │   ├── themes.xml             # Thèmes
│       │   └── styles.xml             # Styles
│       └── drawable/
│           └── scan_guide_bg.xml      # Fond du guide de scan
└── build.gradle                        # Configuration Gradle
```

## Notes importantes

1. **Permissions** : L'application nécessite la permission CAMERA pour fonctionner
2. **Internet** : La permission INTERNET est requise pour les futures intégrations API
3. **Orientation** : L'application est configurée en mode portrait uniquement
4. **Compatibilité** : Minimum SDK 24 (Android 7.0 Nougat)

## Prochaines étapes pour une version complète

1. ✅ Implémenter les appels API réels aux fournisseurs
2. ✅ Ajouter un cache local pour les résultats de recherche
3. ✅ Implémenter un historique des scans
4. ✅ Ajouter la possibilité de sauvegarder les résultats
5. ✅ Implémenter une fonction de recherche manuelle

## Licence

Ce projet est fourni tel quel pour une utilisation interne. Pour une utilisation commerciale, veuillez vérifier les conditions d'utilisation des APIs des fournisseurs.

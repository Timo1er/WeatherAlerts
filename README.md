# VeryWeather

Une application météo moderne pour Android développée avec Kotlin et Jetpack Compose. L'application récupère les données météorologiques en temps réel, propose des alertes en arrière-plan, et inclut un widget d'écran d'accueil interactif.

## Fonctionnalités

- **Météo en temps réel** : Affichage des conditions météorologiques actuelles (Température, Vitesse du vent, Humidité, etc.) grâce à l'API [Open-Meteo](https://open-meteo.com/).
- **Widget d'écran d'accueil** : Un widget Glance qui affiche les données météo clés directement sur l'écran d'accueil.
- **Alertes Météo (Arrière-plan)** : Vérification des alertes météorologiques en arrière-plan à l'aide de WorkManager.
- **Gestion des Redémarrages / Déverrouillages** : Mise à jour automatique des widgets et relance des tâches en arrière-plan au redémarrage ou au déverrouillage de l'appareil (Boot & Unlock Receivers).
- **Géolocalisation** : Récupération de la position de l'utilisateur pour afficher la météo locale (Play Services Location).
- **Paramètres** : Écran de configuration permettant de personnaliser les préférences de l'utilisateur.

## Architecture & Technologies

L'application suit la **Clean Architecture** (couches Data, Domain, UI) et le motif **MVVM** (Model-View-ViewModel) pour garantir une séparation claire des responsabilités, une meilleure testabilité et un code maintenable.

### Stack Technique

- **Langage** : Kotlin
- **Interface Utilisateur (UI)** : Jetpack Compose, Material 3
- **Architecture** : Clean Architecture, MVVM
- **Injection de Dépendances** : Hilt / Dagger
- **Réseau** : Retrofit, OkHttp, Kotlinx Serialization
- **Asynchronisme** : Coroutines, Flow
- **Travail en arrière-plan** : WorkManager (avec Hilt pour l'injection)
- **Widget** : Jetpack Glance
- **Localisation** : Google Play Services Location
- **Images** : Coil (Compose)
- **Navigation** : Jetpack Navigation Compose

## Structure du Projet

- `data/` : Implémentation du Repository, appels réseau (`OpenMeteoApi`), modèles de données (`OpenMeteoResponse`) et gestion des workers (`WeatherAlertWorker`).
- `domain/` : Modèles métiers (`WeatherInfo`, `LocationModel`), interface du Repository et cas d'usage (`GetWeatherUseCase`, `CheckAlertsUseCase`).
- `ui/` : Écrans Compose (`WeatherScreen`, `SettingsScreen`) et leurs ViewModels respectifs (`WeatherViewModel`, `SettingsViewModel`).
- `widget/` : Implémentation du widget Glance et ses différents BroadcastReceivers (`WeatherWidget`, `BootReceiver`, `UnlockReceiver`).
- `di/` : Modules d'injection de dépendances Hilt (`AppModule`).

## Démarrage Rapide

1. Clonez ce dépôt.
2. Ouvrez le projet dans Android Studio (Koala ou version plus récente recommandée).
3. Synchronisez le projet avec les fichiers Gradle.
4. Lancez l'application sur un émulateur ou un appareil physique (API 24 ou supérieure).

## API Utilisée

L'application utilise l'API publique et gratuite [Open-Meteo](https://open-meteo.com/). Aucune clé d'API n'est requise.
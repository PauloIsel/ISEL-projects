# Chelas Multi-Player Poker Dice

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/e5Yj8iZn)

## 👥 Team Members

- **Diogo Cardoso** - 51474 - [A51474@alunos.isel.pt](mailto:A51474@alunos.isel.pt)
- **Daniel Santos** - 51701 - [A51701@alunos.isel.pt](mailto:A51701@alunos.isel.pt)
- **Paulo Magalhães** - 51702 - [A51702@alunos.isel.pt](mailto:A51702@alunos.isel.pt)

---

## 🎲 About the Game

**Chelas Multi-Player Poker Dice** is a turn-based dice game for **2 to 6 players**.

The game is played over several rounds. In each round, players roll dice to form the strongest possible hand. At the end of each round, hands are compared and the strongest one wins.

---

## 📜 Game Rules

### Basic Rules

Each round, every player takes one turn.

During their turn, a player may roll the dice up to **three times**. After each roll, the player may **keep any number of dice** and re-roll the remaining ones.

Once a player finishes their rolls, their dice form a final hand for that round.

### Dice Hands

Hands are ranked from **strongest to weakest**:

- **Five of a Kind** - All five dice show the same value
- **Four of a Kind** - Four dice show the same value
- **Full House** - Three of one value and two of another
- **Straight** - Sequential values (1–2–3–4–5 or 2–3–4–5–6)
- **Three of a Kind** - Three dice show the same value
- **Two Pair** - Two different pairs of matching dice
- **Pair** - Two dice show the same value
- **Bust** - No matching dice

When two players have the same hand, **higher dice values break the tie**.

### Winning the Game

The player with the **strongest hand wins each round**.

After all rounds are completed, the player with the **most round wins** is declared the overall winner.

If a player leaves the game, they automatically lose. If only one player remains, that player is immediately declared the winner.

---

## ✨ Features

### 🎮 Core Gameplay
- **Multiplayer Support**: 2 to 6 players can join a game
- **Turn-Based System**: Each player takes turns rolling dice
- **Strategic Decisions**: Choose which dice to keep and which to re-roll

### 🌐 Online Functionality
- **Lobby System**: Create and join game lobbies
- **Real-time Multiplayer**: Play with friends online using Firebase
- **Lobby Browser**: View available lobbies and their details

### 👤 User Profile
- **Username Customization**: Set and edit your display name
- **Statistics Tracking**: 
  - Total games played
  - Games won
  - Hand frequency statistics (track which hands you've achieved)

### 📊 Game Statistics
- **Hand Frequency Tracker**: See how often you get each hand type
- **Persistent Data**: All stats saved using DataStore in cache

### 🎨 User Interface
- **Responsive Design**: Adapts to different screen sizes
- **Dice Animations**: Dice rolling animations and transitions
- **Intuitive Controls**: Easy-to-use interface 
- **Visual Feedback**: Clear indication of game state and player actions

### 🔧 Technical Features
- **Firebase Integration**: Real-time database 
- **State Management**: ViewModel architecture
- **Extensive Testing**: Unit and UI tests for reliability

---

## 🏗️ Architecture

The application follows **Clean Architecture** principles with clear separation of layers:

- **UI Layer**: Composables and ViewModels
- **Services**: Profile management, game logic, lobby management 
- **Data Layer**: Repositories and data sources (Firebase, DataStore)

---

## 📹 Video Demonstration

🎥 **[Click here to watch the video demonstration](https://www.youtube.com/watch?v=5KQLoQfjWXk)**

---

## 📦 Installation

1. Clone the repository
   ```bash
   git clone https://github.com/isel-leic-pdm/course-assignment-leirtg02.git
   ```
2. Open the project in Android Studio 
3. Sync Gradle and build the project
4. Run the application on an emulator or physical device

---

## 🔗 Links

- **GitHub Repository**: [Course Assignment - leirtg02](https://github.com/isel-leic-pdm/course-assignment-leirtg02)
- **Course**: Mobile Device Programming (PDM)
- **Institution**: ISEL - Instituto Superior de Engenharia de Lisboa

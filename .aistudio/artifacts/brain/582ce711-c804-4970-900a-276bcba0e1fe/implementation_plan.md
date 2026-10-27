# AyushDrop: Smart File Sharing & Free Games Hub

AyushDrop is an Android application that combines high-fidelity, secure file sharing (with bit-for-bit Original Quality and Data Saver modes) with universal deep links, local cryptographic vault persistence, and a full-featured 5-game Arcade Hub.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions were confirmed during the planning clarification phase:
>
> - **Confirmed Backend & Storage**: Local on-device storage with Direct P2P sharing, backed by Android Room Database with reactive Kotlin Flows and encrypted credentials.
> - **Confirmed Share Link Strategy**: Universal deep links (`ayushdrop://share/{linkId}`) with configurable expiration, cryptographic PIN protection, recipient download verification, and QR code generation.
> - **Confirmed Games Integration**: Dedicated Arcade Hub navigation tab featuring high scores, played game statistics, and 5 interactive, offline-ready casual games (Snake, 2048, Tic-Tac-Toe, Memory Match, Rock-Paper-Scissors).

---

## 1. Overview & Core Concept

### What It Does
AyushDrop provides a dual-purpose mobile experience:
1. **Smart File Sharing Station**: Users can select and organize any file (documents, photos, videos, archives, audio), configure quality modes (**Original Quality** with SHA-256 byte integrity vs. **Data Saver** with estimated compression vs. **Smart Mode**), set PIN locks, generate universal share links and QR codes, manage a personal file vault, and inspect received packages.
2. **Arcade Games Hub**: A built-in entertainment suite with 5 interactive casual games designed for quick breaks without advertisements or sign-in paywalls.
3. **User Profiles & Security**: Local cryptographic account system supporting guest play, authenticated vault storage quotas, link revocation, and personal gaming achievements.

### Target Audience & Persona
- Mobile users, students, and professionals needing reliable, zero-loss file transfers without aggressive cloud compression.
- Users who want offline, privacy-first file management alongside lightweight arcade entertainment in a single clean application.

### Key Value
- **No Compromise Quality**: Preserves exact bitstream and metadata for critical media and documents.
- **Privacy & Local Control**: Complete ownership over files, links, and expiration without third-party surveillance.
- **Instant Entertainment**: Five responsive mini-games playable both online and offline with persistent scoreboards.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Send Files Flow**: Home or Vault → Tap "Upload / Share Files" → Android Document/Media Picker → Select transfer mode (Original Quality / Data Saver / Smart) → Set optional PIN and expiration (1 hour, 24 hours, 7 days, Never) → Universal link & QR code generated → Share via Android Sheet or copy.
2. **Receive & Claim Flow**: Open Universal Deep Link or scan QR / enter Link ID → Recipient preview displays file type, size, and sender info → If PIN protected, prompt PIN modal → Download to device storage with integrity validation.
3. **Vault Management Flow**: Vault tab → Filter by type (Media, Docs, Archives) → Search by name → View active links, download counts, and expiration count-down → One-tap copy link, revoke link, or delete file.
4. **Arcade Gaming Flow**: Arcade tab → Select game card (Snake, Tic-Tac-Toe, Memory Match, 2048, Rock Paper Scissors) → Play with responsive touch controls, sound toggles, and live score updates → Game-over screen displays high score badge and logs stats to profile.
5. **Account & Profile Flow**: Profile tab → Sign in / Sign up with local cryptographic auth or continue as Guest → View storage quota graph, games played tally, best scores, and theme settings (Light / Dark).

### Visual Identity & Theme
- **Color Palette (Blue & Violet Modern Cyber)**:
  - Primary: Deep Indigo Blue (`#3B82F6` / `#1D4ED8`)
  - Secondary / Accent: Electric Violet (`#8B5CF6` / `#7C3AED`)
  - Accent Highlight: Cyan Neon (`#06B6D4`)
  - Dark Surface: Midnight Obsidian (`#0F172A`), Elevated Slate (`#1E293B`)
  - Light Surface: Pure Ice White (`#F8FAFC`), Surface Tint (`#FFFFFF`)
  - Success / Alert: Mint Green (`#10B981`) and Crimson (`#EF4444`)
- **Branding**: Stylized "A" monogram glyph merging an upward upload arrow with a downward download funnel.
- **Typography**: Clean modern sans-serif typography with strong numeric counters and clear hierarchy.
- **Component Styling**: Rounded M3 cards (16dp-24dp corner radii), subtle glowing borders on active games, glassmorphic header accents, and tactile ripple micro-interactions.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Room Database for Local Cryptographic Persistence
- **Chosen Approach**: Android Room Database with SQLite, KSP compiler, and Kotlin Flow streams for reactive UI updates. Passwords stored with SHA-256 salting; files tracked with content URIs and internal storage caching.
- **Why**: Respects the user's explicit preference for local on-device storage with P2P capability, ensuring instant offline capability, zero cloud latency, and complete user privacy.
- **Alternatives Considered**: Firebase Firestore was evaluated, but user explicitly selected local on-device storage with direct links.

### Decision 2: Three Distinct Transfer Modes
- **Chosen Approach**:
  - *Original Quality*: Strict 1:1 byte preservation with SHA-256 hash generation for recipient integrity verification.
  - *Data Saver*: Media recompression simulation and zip compaction estimating 40-70% size reduction with warning banner regarding quality trade-offs.
  - *Smart Mode*: Heuristic analysis (e.g. recommend Data Saver for 4K video on cellular, Original Quality for PDFs/archives).
- **Why**: Fulfills the core AyushDrop promise ("Share Anything. Keep the Quality.") with honesty about network data consumption.

### Decision 3: Five Fully Playable Native Compose Games
- **Chosen Approach**: Pure Jetpack Compose canvas and state-driven game engines for Snake, Tic-Tac-Toe (AI & 2P), Memory Match, 2048, and Rock Paper Scissors.
- **Why**: Zero external game engine bloat (keeps APK lean), 60fps buttery animations, accessible keyboard & touch inputs, and native theme integration.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│                    (Edge-to-Edge, Material 3 Theme)                    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
 ┌──────────────────────────────────▼───────────────────────────────────┐
 │                   Navigation Bar / Screen Router                     │
 ├──────────────┬───────────────────┬───────────────────┬───────────────┤
 │  [1] Home    │    [2] Vault      │    [3] Arcade     │  [4] Profile  │
 │  Hero Banner │  My Files List    │  5 Game Launchers │  User Account │
 │  Quick Share │  Search & Filter  │  Leaderboard Bar  │  Storage Bar  │
 │  Transfer UI │  Revocation/Edit  │  Score Cards      │  Theme Toggle │
 └──────┬───────┴─────────┬─────────┴─────────┬─────────┴───────┬───────┘
        │                 │                   │                 │
 ┌──────▼─────────────────▼───────┐   ┌───────▼────────┐   ┌────▼───────┐
 │       FileShareViewModel       │   │ ArcadeViewModel│   │AuthViewModel
 ├────────────────────────────────┤   ├────────────────┤   ├────────────┤
 │ - Selected files state         │   │ - Snake Engine │   │ - Current  │
 │ - Transfer mode calculation    │   │ - 2048 Engine  │   │   user state
 │ - Deep link & PIN generation   │   │ - TicTacToe AI │   │ - Sign-in/up
 │ - Expiration & revocation      │   │ - Memory Match │   │ - Storage  │
 │ - Integrity checksum (SHA-256) │   │ - RPS Round Log│   │   quota    │
 └──────────────┬─────────────────┘   └───────┬────────┘   └────┬───────┘
                │                             │                 │
 ┌──────────────▼─────────────────────────────▼─────────────────▼───────┐
 │                        Repository Layer                              │
 │            (FileRepository, ArcadeRepository, AuthRepository)        │
 └──────────────────────────────┬───────────────────────────────────────┘
                                │
 ┌──────────────────────────────▼───────────────────────────────────────┐
 │                       AyushDrop Room Database                        │
 │  ┌────────────────┬───────────────┬───────────────┬────────────────┐ │
 │  │ FileEntity     │ ShareLink     │ GameScore     │ UserAccount    │ │
 │  │ (id, name, uri,│ (token, pin,  │ (gameType,    │ (username,     │ │
 │  │  size, mime)   │  expiresAt)   │  score, date) │  passwordHash) │ │
 │  └────────────────┴───────────────┴───────────────┴────────────────┘ │
 └──────────────────────────────────────────────────────────────────────┘
```

### Data Model & Entities

1. **`UserEntity`**: `id`, `username`, `email`, `passwordHash`, `avatarId`, `createdAt`, `totalBytesShared`.
2. **`SharedFileEntity`**: `id`, `ownerId`, `fileName`, `fileUri`, `fileSizeBytes`, `mimeType`, `transferMode` (ORIGINAL, DATA_SAVER, SMART), `sha256Checksum`, `createdAt`.
3. **`ShareLinkEntity`**: `id`, `fileId`, `shareToken`, `pinCode` (optional), `expiresAtTimestamp`, `isRevoked`, `downloadCount`, `maxDownloads`.
4. **`GameScoreEntity`**: `id`, `gameType` (SNAKE, TIC_TAC_TOE, MEMORY, G2048, RPS), `score`, `playerMoves`, `elapsedSeconds`, `playedAt`.

### Universal Deep Link Scheme
- URI pattern: `ayushdrop://share/{shareToken}?pin={pinCode}`
- The app handles receiving intent filters so tapping a share link or scanning a QR code opens the recipient download station directly.

---

## 5. Implementation Stages

1. **Stage 1: Design System & Project Setup**:
   - Update `metadata.json` and `strings.xml` to **AyushDrop**.
   - Configure theme colors (Blue/Violet M3 palette) in `Theme.kt` and `Color.kt`.
   - Setup App Icon / branding asset.
2. **Stage 2: Room Persistence Layer**:
   - Create Room Entities, DAOs, and `AyushDropDatabase`.
   - Implement Repositories for Auth, Files, Links, and Game Scores.
3. **Stage 3: File Selection, Quality Modes & Link Generation**:
   - Android Document Picker integration with multi-file support.
   - Mode calculations: Original Quality (SHA-256 checksum), Data Saver, Smart Mode.
   - Deep link creation with PIN, expiration picker (1h, 24h, 7d, Never), and QR code rendering.
4. **Stage 4: Recipient Download Station & PIN Verification**:
   - Deep link handler and manual link claim modal.
   - PIN entry shield, link expiry validation, and recipient download actions.
5. **Stage 5: My Files Dashboard & Vault**:
   - Search, filter by category (Media, Docs, Archives, All).
   - Link revocation, expiration counter, storage breakdown gauge, and delete file handlers.
6. **Stage 6: The 5-Game Arcade Hub**:
   - Game 1: **Snake** (dynamic canvas grid, speed increase, food bonuses, high score).
   - Game 2: **Tic-Tac-Toe** (Minimax AI vs Human 2-player mode, round counter).
   - Game 3: **Memory Match** (card flip grid, move tracking, timer, matched state).
   - Game 4: **2048 Puzzle** (4x4 tile grid, directional swipe & buttons, merge animations, 2048 win state).
   - Game 5: **Rock Paper Scissors** (animated match, win/loss streak, battle history log).
7. **Stage 7: User Accounts, Profiles & Verification**:
   - Local authentication, guest mode fallback, profile stats, and dark/light mode toggle.
   - Comprehensive compilation and verification via `compile_applet`.

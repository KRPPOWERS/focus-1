# 🛡️ FocusGuard — Android Focus & Digital Wellbeing App

FocusGuard helps you defeat digital distractions by blocking apps during your focus sessions,
showing progressive motivational warnings, and challenging you with quick mind games.

## ✨ Features

| Feature | Description |
|---------|-------------|
| ⏰ Focus Time Ranges | Set up to **10** custom time windows with specific days |
| 🚫 App Blocking | Select which apps (Twitter, Instagram, etc.) are blocked |
| ⚠️ 6-Level Warning System | Each attempt shows an escalating motivational message |
| 🧠 Mind Games | Math quiz + Memory game after 6 warnings (~90 seconds) |
| 📊 Usage Alerts | Full-screen reminder when total daily usage exceeds threshold |
| 🔋 Low Power | Uses Accessibility Service (event-driven, no polling) |
| 🌙 Daily Reset | Warning counts and usage times reset automatically at midnight |

## 🚀 Building the APK via GitHub Actions

### Step 1 — Upload to GitHub
1. Create a **new repository** on GitHub (e.g. `FocusGuard`)
2. Extract the zip file on your computer
3. Upload all files to the repository (drag & drop or git push)

### Step 2 — Run the Workflow
1. Go to your repository → **Actions** tab
2. Click **"Build Debug APK"** → **"Run workflow"**
3. Wait ~5 minutes for the build to complete

### Step 3 — Download the APK
1. Click on the completed workflow run
2. Scroll down to **Artifacts**
3. Download **FocusGuard-debug**
4. Extract the zip → you have `app-debug.apk`

### Step 4 — Install on Android
1. Transfer APK to your phone (email / USB / Google Drive)
2. On your phone: **Settings → Security → Install unknown apps** → enable for your file manager
3. Open the APK file and install

## ⚙️ First-Time Setup (IMPORTANT)

After installing, open FocusGuard and tap **"Check & Grant Permissions"**:

### Permission 1: Accessibility Service *(Required)*
- Tap "Open Accessibility Settings"
- Find **FocusGuard** in the list
- Enable it → confirm the dialog
- **This is what detects app launches**

### Permission 2: Usage Access *(Required)*
- Tap "Open Usage Access Settings"
- Find **FocusGuard** → enable it
- Used for tracking time spent in apps

### Permission 3: Display Over Other Apps *(Required)*
- Tap "Open Overlay Permission"  
- Find **FocusGuard** → enable it
- Used to show warning screens on top of blocked apps

## 📱 How to Use

1. **Add Focus Ranges** → Tap "Manage Focus Time Ranges" → Add up to 10 windows
2. **Block Apps** → Tap "Select Apps to Block" → Check Twitter, Instagram, etc.
3. **Enable FocusGuard** → Toggle the main switch ON
4. Done! The shield is active.

### Warning System During Focus Time
| Attempt | Response |
|---------|----------|
| 1st | Motivational warning screen |
| 2nd | Stronger motivational message |
| 3rd | Thought-provoking message |
| 4th | Champion mindset message |
| 5th | Research-backed urge surfing message |
| 6th | Final warning + redirect to Mind Game |
| After game | Counter resets, cycle begins again |

### Outside Focus Time
- Total daily usage is tracked per app
- After **30 minutes** (configurable) of daily use → Full-screen usage alert

## 🔋 Battery Usage
- The Accessibility Service is **event-driven** (fires only on window changes)
- The foreground service runs at **low priority** with minimal CPU usage
- Expected battery impact: < 1% per day

## 🛠️ Technical Stack
- **Language:** Java
- **Min SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 34)  
- **Key APIs:** AccessibilityService, AlarmManager, NotificationManager
- **Dependencies:** Gson, Material Components, RecyclerView

## ⚠️ Limitations
- FocusGuard **cannot fully prevent** a determined user from accessing apps  
  (Android doesn't allow true app locking without Device Owner mode)
- It creates **effective friction** through warnings and mind games
- Some launchers may require re-granting the overlay permission after reboot

---
*Built with ❤️ to help you reclaim your focus and build better digital habits.*

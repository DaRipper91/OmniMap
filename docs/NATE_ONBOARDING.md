# OmniMap: Onboarding Guide for Nate

Welcome to the OmniMap project, Nate! 🚀

OmniMap is a complex, high-performance Android app built with Kotlin, Jetpack Compose, and local AI integration. Because the main codebase is moving extremely fast and uses advanced concepts (like Coroutines and Room databases), jumping straight into the core Android app might be frustrating and could cause bottlenecks.

Instead, we have carved out **three critical, standalone "Side-Quests"** for you. These projects are essential to the OmniMap ecosystem, but they are isolated from the main Android codebase. This means you can experiment, break things, and learn at your own pace without worrying about breaking the main app!

Please read through these options and pick the one that sounds the most exciting to you. Ask the AI agent in your session to help you get started on your chosen path.

---

### Option A: The Python Data Importer (Focus: Logic & Scripting)
**The Goal:** OmniMap needs a way to import users' existing notes and tasks from other apps (like a CSV export from Excel or Notion) and convert them into OmniMap's native JSON format.
**The Task:** Write a standalone Python script (`omnimap-importer.py`) that reads a `.csv` file, loops through the rows, and outputs a properly formatted `.json` file containing "Nodes" and "Edges".
**What You Will Learn:**
- Fundamentals of programming (Variables, Loops, Functions, IF/ELSE statements).
- Working with Data Structures (Lists and Dictionaries).
- Reading and writing files.
- **Why it matters:** Data portability is a massive feature. Your script will be the bridge that brings users into our ecosystem.

### Option B: The Static Web Viewer (Focus: Web Fundamentals)
**The Goal:** OmniMap will eventually be available on desktop browsers. We need a simple, read-only proof-of-concept to visualize our graph data on a webpage.
**The Task:** Build a basic webpage (using only `index.html`, `style.css`, and `script.js`) inside a new `web-viewer/` folder. The page should load a static JSON file (representing our graph) and draw boxes on the screen for each "Node".
**What You Will Learn:**
- The core trinity of the web: HTML (structure), CSS (styling), and JavaScript (logic/interactivity).
- DOM manipulation (making things appear on the screen using code).
- **Why it matters:** You will lay the groundwork for OmniMap's future multi-platform expansion.

### Option C: UI/UX Prototyping (Focus: Product Design)
**The Goal:** Before we code the new "Live AI Feed" and "Global Search" screens for the Android app, we need to know exactly what they should look like.
**The Task:** Use a free design tool like [Figma](https://www.figma.com/) or [Penpot](https://penpot.app/). Study the Material Design 3 guidelines, and create wireframes/mockups for these new screens. Think about where buttons should go, how the AI chat bubbles look, and how the user interacts with the app.
**What You Will Learn:**
- User Experience (UX) and User Interface (UI) design principles.
- Color theory, typography, and layout spacing.
- How to design for mobile devices.
- **Why it matters:** You will be the architect of how the app *feels*. The engineering team will use your designs as the exact blueprint for what to build.

---
**Next Step:** Tell your AI session which option you want to tackle, and it will guide you step-by-step from zero to completion!
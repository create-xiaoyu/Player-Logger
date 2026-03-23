### WARNING: This mod is a university assignment, so there are certainly many aspects that may not have been fully considered. The author assumes no responsibility for any losses or damages caused by this mod.

# Github: https://github.com/create-xiaoyu/Player-Logger

## 📦 Mod Description (CurseForge)

**PlayerLogger** is a lightweight and efficient server-side logging mod designed for NeoForge (Minecraft 1.21.1). It provides reliable tracking of player activities and stores them in a structured database for easy access and analysis.

Whether you're running a small private server or a large multiplayer environment, PlayerLogger helps you keep full visibility over player actions.

---

## ✨ Features

* 🧾 **Comprehensive Player Logging**
  Track important player events such as joins, quits, and more.

* 🗄️ **MySQL Database Support**
  All data is stored in a persistent MySQL database for scalability and reliability.

* ⚡ **High Performance**
  Uses connection pooling (HikariCP) for efficient database operations.

* 🔧 **Easy Configuration**
  Simple configuration file with automatic generation and validation.

* 🖥️ **Server-Side Only**
  No client installation required — just drop it into your server.

---

## ⚙️ Requirements

* Minecraft **1.21.1**
* NeoForge **21.1.220+**
* Java **21+**
* A running **MySQL database**

---

## 🚀 Setup

1. Install the mod into your server `mods` folder
2. Start the server once to generate the config
3. Edit the config file:

   ```
   config/playerlogger-common.toml
   ```
4. Enter your MySQL connection details
5. Restart the server

---

## 🧠 Use Cases

* Server moderation & auditing
* Player behavior tracking
* Data analysis & statistics
* Anti-abuse investigation

---

## ⚠️ Notes

* This mod requires a working MySQL database connection
* Make sure your database credentials are correct
* If the database fails to connect, the server may stop to prevent data loss

---

## ❤️ Support

If you encounter any issues or have suggestions, feel free to open an issue or contact the author.

---

## 🏷️ Keywords

`logging` `mysql` `server` `utility` `admin` `database` `tracking`

---

thanks very much for ChatGPT
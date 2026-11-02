# Purdue Dining Dietary Guard

A Python-based command-line tool that automatically filters Purdue Dining hall menus based on user-selected dietary restrictions, religious preferences, and allergens.

## The Problem

Navigating campus dining halls with strict religious observances (such as avoiding beef or pork as a Hindu or Muslim) or severe food allergies using the official Purdue Dining app is tedious because it requires manually opening long, unbroken ingredient lists for every single dish. This friction increases the risk of overlooking hidden ingredients or making mistakes when trying to find safe, compliant food options across campus.

## How It Works

This tool fetches live daily menu JSON data from the Purdue Dining API, processes the items, and screens them against user-specified dietary filters. Safe options are then cleanly organized and printed by dining hall and food station directly in the terminal.

![System Diagram](docs/system_diagram.png)

## Tech Stack

- **Language:** Python
- **Libraries / Modules:** `requests`, `json`
- **Data Storage:** Local JSON files / cache
- **Data Source:** Public Purdue Dining API (HTTP GET)

## Setup

Step-by-step instructions to get the project running locally:

1. Clone the repository:
   ```bash
   git clone [https://github.com/Chaitanya-Negi/purdue-dining-guard](https://github.com/Chaitanya-Negi/purdue-dining-guard)
   cd purdue-dining-guard
   ```

2. Install dependencies:
  ```bash
    pip install requests
```
3. Run the tool
   

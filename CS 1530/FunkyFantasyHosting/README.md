# FunkyFantasyHosting
Our Software Engineering project. <insert description here written by someone who understands what we're doing>

Link to our SRS document https://docs.google.com/document/d/18NrsWcBkmGw1IVi9h9_hrusEn-hX39au/edit?usp=sharing&ouid=107440439754087862810&rtpof=true&sd=true

# How to run espntest.js
Install NodeJS :https://nodejs.org/en/download/

Go to the main directory in terminal and run this command: node espntest.js

# How to run the actual site (Different sets of parenthesis are for different machines, if one doesn't work try another)
0. Create your python enviornment -ONLY DO THIS ONCE- (python3 -m venv nameOfEnviornment)
1. Run your python enviornment (source nameOfEnviornment/bin/activate)
2. Install Requirements.txt onto your python enviornment (pip install -r requirements.txt)
3. Set your FLASK_APP to main.py (set FLASK_APP=main.py) (export FLASK_APP=main.py)
3b. Set your FLASK_ENV to Development if you are changing code (set FLASK_ENV=Development) (export FLASK_ENV=Development)
3c. Set the GOOGLE_APPLICATION_CREDENTIALS (export GOOGLE_APPLICATION_CREDENTIALS=ffbigquery.json)
4. Run flask(flask run)
5. ctrl-click on the link provided in the terminal


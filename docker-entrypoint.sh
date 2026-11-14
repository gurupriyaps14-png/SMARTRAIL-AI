#!/bin/sh
set -e

# ==============================================================================
# SmartRail AI - Production Cloud Container Entrypoint
# Dynamically binds assigned platform PORT (Render / Railway / Fly.io / Heroku)
# Configures persistent database directory and launches Apache Tomcat
# ==============================================================================

# 1. Determine Port (Default to 8080 if not injected by cloud host)
APP_PORT="${PORT:-8080}"
echo "=========================================================="
echo "  SmartRail AI - Cloud Production Deployment Engine"
echo "  Binding to platform port: $APP_PORT"
echo "=========================================================="

# 2. Dynamically patch Tomcat conf/server.xml with the assigned PORT
if [ -f "$CATALINA_HOME/conf/server.xml" ]; then
    sed -i "s/port=\"[0-9]*\" protocol=\"HTTP\/1.1\"/port=\"$APP_PORT\" protocol=\"HTTP\/1.1\"/g" "$CATALINA_HOME/conf/server.xml"
    sed -i "s/<Connector port=\"[0-9]*\"/<Connector port=\"$APP_PORT\"/g" "$CATALINA_HOME/conf/server.xml"
fi

# 3. Ensure persistent database directory exists
export SMARTRAIL_H2_PATH="${SMARTRAIL_H2_PATH:-/data/smartrail_db}"
DATA_DIR=$(dirname "$SMARTRAIL_H2_PATH")
mkdir -p "$DATA_DIR" 2>/dev/null || true

echo "  Database Configuration:"
if [ -n "$SMARTRAIL_DB_URL" ]; then
    echo "    Mode: External Cloud Database ($SMARTRAIL_DB_URL)"
else
    echo "    Mode: Persistent Embedded H2 Database ($SMARTRAIL_H2_PATH)"
fi
echo "=========================================================="

# 4. Start Tomcat in foreground
exec catalina.sh run


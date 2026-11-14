#!/bin/bash
set -e

# ==============================================================================
# SmartRail AI - Linux Production Server Starter
# Supports dynamic $PORT and persistent database storage
# ==============================================================================

PORT="${PORT:-8080}"
echo "=========================================================="
echo "  SmartRail AI - Production Starter (Linux)"
echo "  Configuring target port: $PORT"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Ensure persistent database directory
export SMARTRAIL_H2_PATH="${SMARTRAIL_H2_PATH:-$SCRIPT_DIR/smartrail_data/smartrail_db}"
mkdir -p "$(dirname "$SMARTRAIL_H2_PATH")"

# Build with maven if available
if command -v mvn &> /dev/null; then
    echo "Compiling with Maven..."
    mvn clean package -DskipTests
fi

echo "SmartRail AI is configured for production on port $PORT"


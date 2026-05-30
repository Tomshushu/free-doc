#!/bin/sh
set -e

CONFIG_FILE="/app/config/free-doc.conf"

if [ -f "$CONFIG_FILE" ]; then
    echo "[FreeDoc] Loading configuration from $CONFIG_FILE"
    while IFS='=' read -r key value; do
        key=$(echo "$key" | tr -d '[:space:]')
        value=$(echo "$value" | tr -d '[:space:]')
        case "$key" in
            ''|\#*) continue ;;
        esac
        if [ -z "$(eval echo \"\${$key+x}\")" ]; then
            export "$key=$value"
            echo "[FreeDoc] Set $key from config file"
        else
            echo "[FreeDoc] $key already set via environment variable, skipping"
        fi
    done < "$CONFIG_FILE"
else
    echo "[FreeDoc] No config file found at $CONFIG_FILE, using environment variables only"
fi

echo "[FreeDoc] Starting FreeDoc..."
exec java -jar /app/free-doc-server.jar

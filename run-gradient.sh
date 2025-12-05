#!/usr/bin/env sh
DESTINATION="$HOME/Downloads/gradient-example-$(date --utc "+%F-%H.%M.%S")"
git clone https://github.com/collektive/collektive-examples "$DESTINATION"
cd "$DESTINATION"
./gradlew runGradientGraphic
#!/usr/bin/env bash
# Windows + Git Bash friendly venv setup script
VENV_NAME="bootcamp-env"

echo "[INFO] Checking if already in a virtual environment..."
if [[ -n "$VIRTUAL_ENV" ]]; then
  echo "[INFO] Deactivating current environment..."
  deactivate
fi

# Remove old venv if it exists
if [[ -d "$VENV_NAME" ]]; then
  echo "[INFO] Removing old virtual environment..."
  rm -rf "$VENV_NAME"
fi

echo "[INFO] Creating new virtual environment..."
python -m venv "$VENV_NAME"

echo "[INFO] Activating virtual environment..."
# Git Bash on Windows uses Scripts/activate (with forward slashes)
source "$VENV_NAME/Scripts/activate"

echo "[INFO] Upgrading pip..."
python -m pip install --upgrade pip

if [[ -f "requirement.txt" ]]; then
  echo "[INFO] Installing packages from requirement.txt..."
  pip install -r requirement.txt
elif [[ -f "requirements.txt" ]]; then
  echo "[INFO] Installing packages from requirements.txt..."
  pip install -r requirements.txt
else
  echo "[WARN] No requirement.txt or requirements.txt found; skipping pip install"
fi

echo "[INFO] Installing ipykernel and registering kernel..."
pip install ipykernel --quiet
python -m ipykernel install --user --name="$VENV_NAME" --display-name "Python ($VENV_NAME)"

echo ""
echo "Environment information:"
python --version
pip --version
jupyter --version 2>/dev/null || echo "(jupyter not installed yet)"
echo "----------------------------------------"
echo ""
echo "Environment setup complete."
echo "You are now inside $VENV_NAME"
echo "When finished, just type:"
echo "  deactivate"
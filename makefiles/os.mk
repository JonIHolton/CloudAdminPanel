ifeq ($(OS),Windows_NT)
	PYTHON := python
	VENV_ACTIVATE := venv\Scripts\activate.bat
	RM := del /s /q
else
	PYTHON := python3
	VENV_ACTIVATE := . venv/bin/activate
	RM := rm -rf
endif

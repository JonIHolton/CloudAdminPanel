init: check-terraform check-aws-cli venv requirements npm-install

venv:
	@echo "Creating python virtual environment in '$(VENV_NAME)' folder..."
	$(PYTHON) -m venv $(VENV_NAME)

requirements:
	@echo "Installing Python requirements..."
	@$(VENV_ACTIVATE) && pip install -r requirements.txt
	
deactivate-venv:
	@echo "Deactivating virtual environment..."
	$(RM) $(VENV_NAME)
	@find . -name "*.pyc" -delete
	@exit 0

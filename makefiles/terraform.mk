validate-tf:
	@echo "Validating Terraform with Checkov and TFLint..."
	@$(VENV_ACTIVATE) && \
	echo "Current Python interpreter: $$which python" && \
	echo "Virtual environment: $${VIRTUAL_ENV}" && \
	(if [ -z "$$(pip list | grep checkov)" ]; then \
		echo "Checkov is not installed. Installing..."; \
		pip install checkov; \
	fi) && \
	(if ! tflint -v &>/dev/null; then \
		echo "TFLint is not installed. Installing with Homebrew..."; \
		brew install tflint; \
	fi) && \
	checkov -d ./modules || true && \
	for dir in $$(find ./modules -type d | grep -vE '/\.'); do \
		echo "Running tflint in $$dir..."; \
		tflint --chdir $$dir; \
	done
	@$(VENV_DEACTIVATE)
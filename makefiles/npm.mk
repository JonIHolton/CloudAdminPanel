npm-install:
	@echo "Installing npm dependencies..."
	@npm install

npm-install-subdirectories: $(addprefix npm-install-subdirectory-,$(NPM_SUBDIRS))
npm-install-subdirectory-%:
	$(call run_npm_install,$*)

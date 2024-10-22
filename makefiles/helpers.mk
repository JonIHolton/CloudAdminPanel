define run_npm_install
	@echo "Running npm install in $1 service..."
	@if [ ! -d "$1/node_modules" ]; then \
		(cd $1 && npm install); \
	fi
endef


PROJECT_NAME = "itsa"
LOCAL_DEPLOY_DIR = "deployment"


# ---------------------------------------
# For deploying docker containers locally
# ---------------------------------------
up:
	@docker compose -p ${PROJECT_NAME} \
					-f ${LOCAL_DEPLOY_DIR}/docker-compose.yml \
					up --build -d


# ---------------------------------
# For tearing down local deployment
# ---------------------------------
down:
	@docker compose -p ${PROJECT_NAME} \
				    -f ${LOCAL_DEPLOY_DIR}/docker-compose.yml \
				    down

down-clean:
	@docker compose -p ${PROJECT_NAME} \
				    -f ${LOCAL_DEPLOY_DIR}/docker-compose.yml \
				    down --volumes --remove-orphans
	@docker system prune -f


# -------------------------------
# For restarting local deployment
# -------------------------------
restart: down up

hard-restart: down-clean up

nobuild/restart: down nobuild/up

nobuild/hard-restart: down-clean nobuild/up


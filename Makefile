# Root Makefile

# include makefiles/os.mk
# include makefiles/variables.mk
# include makefiles/helpers.mk
# include makefiles/check-tools.mk
# include makefiles/init.mk
# include makefiles/terraform.mk
# include makefiles/npm.mk
# include makefiles/deployment.mk
# include makefiles/rover.mk
# include makefiles/help.mk


PROJECT_NAME = "itsa"
LOCAL_DEPLOY_DIR = "local/deployment"
NPM_SUBDIRS = client 
AWS_ACCOUNT_ID = "717942231127"
AWS_REGION = "ap-southeast-1"

npm-install: npm-install-subdirectories
	@echo "Running npm install to set up Husky and other dependencies..."
	@if [ ! -d "node_modules" ]; then \
		npm install; \
	fi
	@echo "All npm dependencies installed."

npm-install-subdirectories:
	@echo "Running npm install in subdirectories..."
	@for dir in ${NPM_SUBDIRS}; do \
		if [ ! -d "$${dir}/node_modules" ]; then \
			echo "Running npm install in $${dir} service..."; \
			cd $${dir} && npm install && cd ..; \
		fi \
	done
	@echo "All subdirectory dependencies installed."

init-husky:
	@echo "Initializing Husky..."
	@npm run prepare

# ---------------------------------------
# For deploying docker containers locally
# ---------------------------------------
up: 
	@docker compose -p ${PROJECT_NAME} \
		-f ${LOCAL_DEPLOY_DIR}/docker-compose.yml \
		up --build -d --remove-orphans

nobuild/up: npm-install
	@docker-compose -p ${PROJECT_NAME} \
		-f ${LOCAL_DEPLOY_DIR}/docker-compose.yml \
		up -d

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

prune-all:
	@echo "Running this command will prune all images. Do you want to proceed [y/N]?"; \
	read ans; \
	case "$$ans" in \
		[Yy]*) docker image prune -a -f ;; \
		*) echo "Aborting." ;; \
	esac

# ------------------------------
# For deployment tasks
# ------------------------------
tag-all:
	@echo "Tagging all images..."

	@docker tag ${PROJECT_NAME}-admin-proxy:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-admin-proxy

	@docker tag ${PROJECT_NAME}-authorisation:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-authorisation

	@docker tag ${PROJECT_NAME}-points:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-points

	@docker tag ${PROJECT_NAME}-user-orchestrator:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-user-orchestrator

	@docker tag ${PROJECT_NAME}-users:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-users

	@docker tag ${PROJECT_NAME}-logs:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-logs

	@echo "All images tagged."

push-all:
	@echo "Logging into ECR..."

	@aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin 717942231127.dkr.ecr.ap-southeast-1.amazonaws.com

	@echo "Logged in."

	@echo "Pushing all images to ECR..."

	@docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-admin-proxy

	@docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-authorisation

	@docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-points

	@docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-user-orchestrator

	@docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-users

	@docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-prod-deployment-repository:${PROJECT_NAME}-logs


	@echo "All images pushed."

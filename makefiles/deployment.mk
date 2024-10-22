init:
	$(MAKE) -C modules init-all
	
deploy:
	$(MAKE) -C modules deploy-all

destroy:
	$(MAKE) -C modules destroy-all

plan:
	$(MAKE) -C modules plan-all

deploy-shared:
	$(MAKE) -C modules/shared deploy-shared

destroy-shared:
	$(MAKE) -C modules/shared destroy-shared

plan-shared:
	$(MAKE) -C modules/shared plan-shared


rover-up:
	@terraform plan -out plan.out
	@terraform show -json plan.out > plan.json
	@docker run --rm -it -p 9000:9000 -v $(PWD)/plan.json:/src/plan.json im2nguyen/rover:latest -planJSONPath=plan.json

rover-down:
	@docker stop $$(docker ps -q --filter "ancestor=im2nguyen/rover:latest")

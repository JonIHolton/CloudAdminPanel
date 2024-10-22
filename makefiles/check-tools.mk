check-terraform:
	@echo "Checking if Terraform is installed..."
	@terraform -v || (echo "Terraform is not installed. Please install Terraform." && exit 1)

check-aws-cli:
	@echo "Checking if AWS CLI is installed..."
	@aws --version || (echo "AWS CLI is not installed. Please install AWS CLI." && exit 1)

## Directory Structure and Resource Management
Each resource you create must have its own directory. This directory will contain all the necessary files related to that resource. The naming convention for both the directory and the .tf file should be identical and descriptive of the resource it manages.

### Example Structure:
```
modules/
├── example-resource/
│   ├── example-resource.tf
│   ├── Makefile
│   └── ...
```
## Naming Conventions
- Resource Directories and Files: descriptive name in kebab-case (e.g., example-resource/, example-resource.tf).
- Resource Names: follow snake_case. Incorporate the environment name as a suffix (e.g., example_resource_${var.environment}).
- Variables: Lowercase with underscores to separate words (e.g., subnet_ids).
- Outputs: Prefix with the resource/module name (e.g., example_resource_id).


## Makefiles
Each resource directory must include a Makefile with three targets: deploy, destroy, and apply. 
These resources need to be references into the root MAKEFILE as well. 

The resource name below is the same as directory resource name as thats how the root MAKEFILE executes the command. 
### Makefile Target Naming:
Deploy: deploy-resource - To deploy the resource.
Destroy: destroy-resource - To safely destroy the resource.
Apply: apply-resource - To apply changes to the resource.

Example Makefile:
```makefile
.PHONY: deploy-example-resource destroy-example-resource apply-example-resource

deploy-example-resource:
    @echo "Deploying example resource..."
    @terraform init && terraform apply -auto-approve
    @echo "Deployment complete."

destroy-example-resource:
    @echo "Destroying example resource..."
    @terraform destroy -auto-approve
    @echo "Destruction complete."

apply-example-resource:
    @echo "Applying changes to example resource..."
    @terraform apply -auto-approve
    @echo "Apply complete."
```
## Environment Tagging
All resources must include an environment tag. This tag is crucial for identifying and segregating resources across different environments (development, staging, production).

Example:

```hcl
resource "aws_example" "example" {
  ...
  tags = {
    Name = "example-${var.environment}"
  }
}
```
### Variables and Outputs
Variables: Define variables for configurations that change between environments or deployments.

Outputs: Use outputs to expose important information about your resources, especially when these need to be referenced by other Terraform configurations.

Example Output:

```hcl
output "example_resource_id" {
  value = aws_example.example.id
}
```

## Using Modules in main.tf
Main module is where everything is orchestrated. When you call `make deploy` in root, it is what is called as compared to `make deploy-resource` which calls the `deploy` function in the resource dir. 

### Steps for Using Modules:
Define the Module in main.tf: Reference the module by specifying its source path and required input variables.

Example:

```hcl
module "example_module" {
  source = "./modules/example-module"
  example_variable = var.example_variable
}
```
Pass Variables to the Module: Provide values for the module's variables. These can come from variables.tf, terraform.tfvars, or secrets.tfvars.


## Referencing Module Outputs
To use the output of one module in another, you can reference the module's output variables.

### Steps for Referencing Outputs:
Define Outputs in the Module: In your module, define an output that other configurations can use.

Example in modules/example-module/outputs.tf:

```hcl
output "example_output" {
  value = aws_resource.example_attribute
}
```

alternatively, you can just define the output in the same file itself. However, it is good practice to separate them. 

### Reference the Output in main.tf:
Use the syntax module.<module_name>.<output_name> to reference the module's output.

Example in main.tf:

```hcl
resource "aws_other_resource" "example" {
  attribute = module.example_module.example_output
}
```


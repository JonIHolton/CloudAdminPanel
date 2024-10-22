# Project Name


- [Project Name](#project-name)
  - [🚀 Quick Start](#-quick-start)
  - [📁 Folder Structure](#-folder-structure)
  - [🎉 Deployment hooks](#-deployment-hooks)
  - [🔨 Commit Hooks](#-commit-hooks)
  - [🔍 Miscellaneous](#-miscellaneous)
    - [🛠 Makefile](#-makefile)
    - [✍️ Signed commits](#️-signed-commits)
    - [🏗️ How to create new aws resource using terraform ?](#️-how-to-create-new-aws-resource-using-terraform-)

## 🚀 Quick Start

To get started:

1. Fork this repository 
2. Sign up to your own aws account and create credentials
3. develop, deploy and test
4. Merge with `development` branch. 

## 📁 Folder Structure

| Folder               | Description                                                                              |
|----------------------|------------------------------------------------------------------------------------------|
| `Makefile`           | Makefile for various project-related tasks.                                              |
| `Modules`            | Contains all the modules for the terraform deployment                                    |
| `docs`               | Contains documentation and report for this project                                       |
| `client`             | Contains all the frontend for this project                                               |

## 🎉 Deployment hooks
`make init` initialises the terraform deployment. It should only be run once. 

`make apply` updates the terraform deployment.

`make destroy` removes all resources from the deployment. Should not be run unless you know what you are doing.

`make plan` provides the update plan for this deployment.

## 🔨 Commit Hooks

Utilizing Husky with:

- **lint-staged** for linting files on commit.
- **commitlint** to ensure commit messages adhere to the [convention](https://www.conventionalcommits.org/en/v1.0.0/).

## 🔍 Miscellaneous

### 🛠 Makefile

For instructions on installing `Make` on Windows and Ubuntu, refer to [`/docs/Makefile.md`](/docs/Makefile.md).

### ✍️ Signed commits
Repo mandates all commits to be signed. Refer to [`/docs/SignedCommits.md`](/docs/SignedCommits.md) to set up signed commits (Linux/MacOs).


### 🏗️ How to create new aws resource using terraform ?
refer to [`/docs/NewTerraformResource.md`](/docs/NewTerraformResource.md)


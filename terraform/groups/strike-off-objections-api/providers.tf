provider "aws" {
  region = var.region

  default_tags {
    tags = {
      "Environment" = var.environment,
      "Terraform" = var.repository_name
    }
  }
}

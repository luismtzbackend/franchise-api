variable "mongodbatlas_public_key" {
  type      = string
  sensitive = true
}

variable "mongodbatlas_private_key" {
  type      = string
  sensitive = true
}

variable "mongodbatlas_org_id" {
  type = string
}

variable "project_name" {
  type    = string
  default = "franchise-api"
}

variable "mongo_root_user" {
  type      = string
  sensitive = true
}

variable "mongo_root_password" {
  type      = string
  sensitive = true
}

variable "mongo_region" {
  type    = string
  default = "US_EAST_1"
}

variable "upstash_api_key" {
  type      = string
  sensitive = true
}

variable "upstash_email" {
  type      = string
  sensitive = true
}

variable "redis_region" {
  type    = string
  default = "us-east-1"
}

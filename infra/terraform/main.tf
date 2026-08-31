terraform {
  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 1.16"
    }
    upstash = {
      source  = "upstash/upstash"
      version = "~> 1.5"
    }
  }
}

provider "mongodbatlas" {
  public_key  = var.mongodbatlas_public_key
  private_key = var.mongodbatlas_private_key
}

provider "upstash" {
  api_key = var.upstash_api_key
  email   = var.upstash_email
}

resource "mongodbatlas_project" "franchise_api" {
  name   = var.project_name
  org_id = var.mongodbatlas_org_id
}

resource "mongodbatlas_cluster" "franchise_api" {
  project_id = mongodbatlas_project.franchise_api.id
  name       = "franchise-api-cluster"

  provider_name                = "TENANT"
  backing_provider_name        = "AWS"
  provider_region_name         = var.mongo_region
  provider_instance_size_name  = "M0"
}

resource "mongodbatlas_database_user" "franchise_api" {
  project_id         = mongodbatlas_project.franchise_api.id
  username           = var.mongo_root_user
  password           = var.mongo_root_password
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = "franchise"
  }
}

resource "mongodbatlas_project_ip_access_list" "allow_all" {
  project_id = mongodbatlas_project.franchise_api.id
  cidr_block = "0.0.0.0/0"
  comment    = "Acceso abierto para entorno de prueba tecnica"
}

resource "upstash_redis_database" "franchise_api" {
  database_name = "franchise-api-cache"
  region        = var.redis_region
  tls           = true
}

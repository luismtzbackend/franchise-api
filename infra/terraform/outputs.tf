output "mongodb_connection_string" {
  value     = mongodbatlas_cluster.franchise_api.connection_strings[0].standard_srv
  sensitive = true
}

output "mongodb_database_user" {
  value     = mongodbatlas_database_user.franchise_api.username
  sensitive = true
}

output "redis_endpoint" {
  value = upstash_redis_database.franchise_api.endpoint
}

output "redis_port" {
  value = upstash_redis_database.franchise_api.port
}

output "redis_password" {
  value     = upstash_redis_database.franchise_api.password
  sensitive = true
}

param (
  [switch] $RemoveDatabase=$false
)

docker stop mysql | Out-Null
if ($RemoveDatabase) {
  docker rm -f mysql | Out-Null
}

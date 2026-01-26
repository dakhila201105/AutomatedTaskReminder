try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method Get -UseBasicParsing
    Write-Host "Health Check Status: $($response.StatusCode)"
    Write-Host "Content Length: $($response.Content.Length)"
} catch {
    Write-Host "Health Check Failed: $_"
}
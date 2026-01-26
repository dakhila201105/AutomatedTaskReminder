
$username = "newuser1@example.com"
$password = "password123"
$baseUrl = "http://localhost:8081"

Write-Host "1. Registering $username..."
$registerFields = @{
    email = $username
    password = $password
}

try {
    # Get initial session/cookies
    $session = $null
    $initResponse = Invoke-WebRequest -Uri "$baseUrl/register" -SessionVariable session -UseBasicParsing
    
    # Submit registration
    $response = Invoke-WebRequest -Uri "$baseUrl/register" -Method Post -Body $registerFields -WebSession $session -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    
    Write-Host "Register Status: $($response.StatusCode)"
    Write-Host "Headers: $($response.Headers)"
    
    if ($response.StatusCode -eq 302) {
        Write-Host "Registration successful (Redirected to verify-otp)."
    } else {
        Write-Host "Registration response content:"
        Write-Host $response.Content
    }
} catch {
    Write-Host "Error during registration: $_"
    if ($_.Exception.Response) {
         Write-Host "Status: $($_.Exception.Response.StatusCode)"
    }
}

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginUrl = "http://localhost:8081/login"
$loginBody = @{
    username = "akhil3@example.com"
    password = "password123"
}

Write-Host "1. Logging in..."
try {
    $response = Invoke-WebRequest -Uri $loginUrl -Method Post -Body $loginBody -WebSession $session -UseBasicParsing
    Write-Host "Login Status: $($response.StatusCode)"
} catch {
    Write-Host "Login Failed: $_"
    if ($_.Exception.Response) {
         $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
         Write-Host "Error Body: $($reader.ReadToEnd())"
    }
}

$username = "akhil3@example.com"
$password = "password"
$baseUrl = "http://localhost:8081"

Write-Host "1. Attempting Login for $username..."
try {
    # Initial request to get any cookies (like JSESSIONID if already set)
    $response = Invoke-WebRequest -Uri "$baseUrl/login" -SessionVariable session -UseBasicParsing
    
    # Construct form data
    $formFields = @{
        username = $username
        password = $password
    }
    
    # Submit login form
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $formFields -WebSession $session -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    
    Write-Host "Login Status: $($loginResponse.StatusCode)"
    Write-Host "Headers: $($loginResponse.Headers)"
    
    if ($loginResponse.StatusCode -eq 302) {
        Write-Host "Login successful (Redirected)."
        $redirectUrl = $loginResponse.Headers['Location']
        Write-Host "Redirecting to: $redirectUrl"
        
        # Follow redirect manually to keep control
        if ($redirectUrl -notmatch "^http") {
            $redirectUrl = "$baseUrl$redirectUrl"
        }
        
        $tasksResponse = Invoke-WebRequest -Uri $redirectUrl -WebSession $session -UseBasicParsing
        Write-Host "Tasks Page Status: $($tasksResponse.StatusCode)"
        Write-Host "Content Length: $($tasksResponse.Content.Length)"
        
        # simple check for task content
        if ($tasksResponse.Content -match "No tasks found") {
            Write-Host "Result: No tasks found in HTML."
        } elseif ($tasksResponse.Content -match "task-item") {
             Write-Host "Result: Tasks found in HTML."
        } else {
             Write-Host "Result: Tasks table might be empty or page structure is different."
        }
        
        # Print a snippet of the HTML to see what's rendered
        Write-Host "HTML Snippet:"
        $tasksResponse.Content | Select-Object -First 20
        
    } else {
        Write-Host "Login might have failed or not redirected as expected."
    }

} catch {
    Write-Host "Error: $_"
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)"
    }
}

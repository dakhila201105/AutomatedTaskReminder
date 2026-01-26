
$username = "newuser1@example.com"
$password = "password123"
$otp = "164579"
$baseUrl = "http://localhost:8081"

# Create a session to persist cookies
$session = $null
$initialReq = Invoke-WebRequest -Uri "$baseUrl/login" -SessionVariable session -UseBasicParsing

Write-Host "1. Verifying OTP..."
$verifyFields = @{
    email = $username
    otp = $otp
}
try {
    $vResponse = Invoke-WebRequest -Uri "$baseUrl/verify-otp" -Method Post -Body $verifyFields -WebSession $session -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Write-Host "Verify Status: $($vResponse.StatusCode)"
    
    if ($vResponse.StatusCode -eq 302) {
        Write-Host "Verification successful."
    }
} catch {
    Write-Host "Verification failed: $_"
}

Write-Host "2. Logging in..."
$loginFields = @{
    username = $username
    password = $password
}
try {
    $lResponse = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $loginFields -WebSession $session -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Write-Host "Login Status: $($lResponse.StatusCode)"
    Write-Host "Login Location: $($lResponse.Headers['Location'])"
} catch {
    Write-Host "Login failed: $_"
}

Write-Host "3. Listing Tasks (Expect Empty)..."
try {
    $tResponse = Invoke-WebRequest -Uri "$baseUrl/tasks" -WebSession $session -UseBasicParsing
    Write-Host "Tasks Status: $($tResponse.StatusCode)"
    if ($tResponse.Content -match "No tasks found") {
        Write-Host "Confirmed: No tasks found."
    } else {
        Write-Host "Hmm, content is different or tasks exist."
    }
} catch {
    Write-Host "List tasks failed: $_"
}

Write-Host "4. Adding a Task..."
$taskFields = @{
    title = "My First Task"
    description = "Testing PowerShell"
    priority = "HIGH"
    dueDate = "2026-02-01"
}
try {
    $aResponse = Invoke-WebRequest -Uri "$baseUrl/tasks/add" -Method Post -Body $taskFields -WebSession $session -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
    Write-Host "Add Task Status: $($aResponse.StatusCode)"
    if ($aResponse.StatusCode -eq 302) {
        Write-Host "Task added successfully (Redirected)."
    }
} catch {
    Write-Host "Add Task failed: $_"
    if ($_.Exception.Response) {
         Write-Host "Status: $($_.Exception.Response.StatusCode)"
         $reader = New-Object System.IO.StreamReader $_.Exception.Response.GetResponseStream()
         $errContent = $reader.ReadToEnd()
         Write-Host "Error Content: $errContent"
    }
}

Write-Host "5. Listing Tasks (Expect 1 Task)..."
try {
    $headers = @{ "Accept" = "text/html" }
    $tResponse2 = Invoke-WebRequest -Uri "$baseUrl/tasks" -WebSession $session -UseBasicParsing -Headers $headers
    if ($tResponse2.Content -match "My First Task") {
        Write-Host "SUCCESS: Found 'My First Task' in the list."
    } else {
        Write-Host "FAILURE: Task not found in the list."
        # Write-Host "HTML Snippet:"
        # $tResponse2.Content | Select-Object -First 20
    }
} catch {
    Write-Host "List tasks failed: $_"
}

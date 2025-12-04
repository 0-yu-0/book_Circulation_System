# Simple API smoke tests for local server
# Usage: from PowerShell run: .\api-smoke-tests.ps1

$base = 'http://localhost:8080'
Write-Host "Create test reader"
$reader = @{
    readerId = 'r-test-1'
    readerName = 'Test Reader'
    readerCardType = 'Student'
    readerCardNumber = 'CARD-1001'
    readerPhoneNumber = '1234567890'
    registerDate = '2025-12-04'
    readerStatus = 0
    totalBorrowNumber = 0
    nowBorrowNumber = 0
} | ConvertTo-Json -Compress

try {
    $res = Invoke-RestMethod -Uri ($base + '/api/readers') -Method Post -Body $reader -ContentType 'application/json'
    Write-Host "Create reader response:" ($res | ConvertTo-Json -Compress)
} catch { Write-Host "Create reader failed: $_"; exit 1 }

Write-Host "List readers (first page)"
try {
    $list = Invoke-RestMethod -Uri ($base + '/api/readers?offset=0&limit=10') -Method Get
    Write-Host ($list | ConvertTo-Json -Compress)
} catch { Write-Host "List readers failed: $_" }

# Ensure a test book exists (bookId test-1)
Write-Host "Create test borrow (book test-1, reader r-test-1)"
$borrow = @{
    readerId = 'r-test-1'
    bookId = 'test-1'
    borrowDate = (Get-Date -Format 'yyyy-MM-dd')
    dueDate = (Get-Date).AddDays(14).ToString('yyyy-MM-dd')
} | ConvertTo-Json -Compress
try {
    $b = Invoke-RestMethod -Uri ($base + '/api/borrow') -Method Post -Body $borrow -ContentType 'application/json'
    Write-Host "Borrow response:" ($b | ConvertTo-Json -Compress)
    $borrowId = $b.data.borrowId
} catch { Write-Host "Borrow failed: $_"; exit 1 }

Start-Sleep -Seconds 1

Write-Host "Create return for borrowId $borrowId"
$return = @{
    borrowId = $borrowId
    returnDate = (Get-Date -Format 'yyyy-MM-dd')
} | ConvertTo-Json -Compress
try {
    $r = Invoke-RestMethod -Uri ($base + '/api/return') -Method Post -Body $return -ContentType 'application/json'
    Write-Host "Return response:" ($r | ConvertTo-Json -Compress)
} catch { Write-Host "Return failed: $_"; exit 1 }

Write-Host "Delete test reader r-test-1"
try {
    $del = Invoke-RestMethod -Uri ($base + '/api/readers/r-test-1') -Method Delete
    Write-Host ($del | ConvertTo-Json -Compress)
} catch { Write-Host "Delete reader failed: $_" }

Write-Host 'Smoke tests completed.'

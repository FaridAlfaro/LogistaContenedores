# Test tokens script
$token_operador = 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJXU2QwdWZySjlCSnFud2I3QldEMHVob3Bpazlrbi0yRTJISnZiRC02dU9FIn0.eyJleHAiOjE3NjM4NTExMzgsImlhdCI6MTc2Mzg1MDgzOCwianRpIjoiODQzYzk1ZjMtZTllYy00OGRhLTkwMDEtYTRmNWM4YzE1NWJmIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg4L3JlYWxtcy90cGktYmFja2VuZCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlODBhNDY5ZS0yNjc5LTQyYzItODVmYS0xZGFkYTI1ZWU1ZmIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJiYWNrZW5kLWNsaWVudCIsInNlc3Npb25fc3RhdGUiOiIyNTIwOGY2Mi01MGMzLTRhZTEtODg1ZS0zNTRiMjg1ZjE2ZjQiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImh0dHA6Ly9sb2NhbGhvc3Q6NTE3MyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsIk9QRVJBRE9SIiwiZGVmYXVsdC1yb2xlcy10cGktYmFja2VuZCJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoiZW1haWwgcHJvZmlsZSIsInNpZCI6IjI1MjA4ZjYyLTUwYzMtNGFlMS04ODVlLTM1NGIyODVmMTZmNCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6Im9wZXJhZG9yIDEiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJvcGVyYWRvciIsImdpdmVuX25hbWUiOiJvcGVyYWRvciIsImZhbWlseV9uYW1lIjoiMSIsImVtYWlsIjoib3BlcmFkb3JAZXhhbXBsZS5jb20ifQ.S3oOMMOLD3XxsCegsUOytXzye9HJ8d4dVIXCUXr8Oo9a1jFmkhbGbIUtW_uaseB46YByCaomMU3ZOhXBVshzudQLYSVdPpS-lEggN7Z1WEybla4bIt3vQZ9gSKaEnkw9bnzr_TtpvBQEEFwwG4g1UFXwJE_GXoe4zEx7s2rretY3UntVSwuAMZlaL_kfuZqEcm8PWTJYsvPeiIfRXw1p_p_rZaOLzNCVBdtw-imAslLk5t5n_AlaAuYDtAtCA2ugqYpm5reb-mengoaZWS2jqZ_Qi-NMZHeWkKHScxeQXc5j2zzoH1NmOr6sZ5dUNut4HfHUuLSc0tuG87pbPLFpkw'

$token_transportista = 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJXU2QwdWZySjlCSnFud2I3QldEMHVob3Bpazlrbi0yRTJISnZiRC02dU9FIn0.eyJleHAiOjE3NjM4NTExMzksImlhdCI6MTc2Mzg1MDgzOSwianRpIjoiZmFlZmVlNzgtZDU5NS00Yzc1LWI2ODItNDIyY2Q2YzNmZjg1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg4L3JlYWxtcy90cGktYmFja2VuZCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI0MTg4ZjFmNC1kNDZlLTQ0MTMtODc4Zi1kM2NjOWJkNzFlZjkiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJiYWNrZW5kLWNsaWVudCIsInNlc3Npb25fc3RhdGUiOiJmOWQ1OGE4My1jNDJhLTRlY2MtODlmNy1hMGI0MjJhZjRlMTciLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImh0dHA6Ly9sb2NhbGhvc3Q6NTE3MyJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiVFJBTlNQT1JUSVNUQSIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLXRwaS1iYWNrZW5kIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwic2lkIjoiZjlkNThhODMtYzQyYS00ZWNjLTg5ZjctYTBiNDIyYWY0ZTA3IiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoidHJhbnNwb3J0aXN0YSAxIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidHJhbnNwb3J0aXN0YSIsImdpdmVuX25hbWUiOiJ0cmFuc3BvcnRpc3RhIiwiZmFtaWx5X25hbWUiOiIxIiwiZW1haWwiOiJ0cmFuc3BvcnRpc3RhQGV4YW1wbGUuY29tIn0.U_L9Cp8Hm4i1-Sba7Qyn_Fsax81mLdWNYyXj2jpk_KPkLUOJ32BfoRlOM7BKuAAQW3CsXzx3Poiua-U_Sc7koxMZxNwnEusB1RFO4z9beZr4UDR2Wnb288jVtPR1N7bYug1AGnAh8UBbDfnq5RETPlB4uRJ0NmSBqm7oL2_spPAmHDvr8B5g51joWuc6_0EgLOGNLvwg5sXUd4ffqEyuW4F_7eGWdCt3__9tFOJAUGJGzQlKLVjXf4VLQ0HXoYD2caj88n-_NYGTDFYgxJYVZSElQSucpnGFLrwxVPXCGJXHDZZ2yOIWYhkKQ_Xr4KrUN5THriPlqeMl3BAXW-iDtQ'

Write-Host '=== Probando conexión a MS Logística (8082) ===' 
try {
  $response = Invoke-RestMethod -Uri 'http://localhost:8082/api/v1/depositos' -Method Get -Headers @{Authorization='Bearer '+ $token_operador} -ErrorAction Stop
  Write-Host 'ÉXITO: Conectado a ms-logistica' -ForegroundColor Green
  Write-Host ('Depositos: ' + $response.Count + ' registros')
} catch {
  Write-Host ('ERROR: No se pudo conectar - ' + $_.Exception.Message) -ForegroundColor Red
}

Write-Host '`n=== Probando conexión a MS Flota (8083) ===' 
try {
  $response = Invoke-RestMethod -Uri 'http://localhost:8083/api/v1/transportistas' -Method Get -Headers @{Authorization='Bearer '+ $token_transportista} -ErrorAction Stop
  Write-Host 'ÉXITO: Conectado a ms-flota' -ForegroundColor Green
  Write-Host ('Transportistas: ' + $response.Count + ' registros')
} catch {
  Write-Host ('ERROR: No se pudo conectar - ' + $_.Exception.Message) -ForegroundColor Red
}

Write-Host '`n=== Probando conexión a MS Solicitudes (8081) ===' 
try {
  $response = Invoke-RestMethod -Uri 'http://localhost:8081/api/v1/solicitudes' -Method Get -Headers @{Authorization='Bearer '+ $token_operador} -ErrorAction Stop
  Write-Host 'ÉXITO: Conectado a ms-solicitudes' -ForegroundColor Green
  Write-Host ('Solicitudes: ' + $response.Count + ' registros')
} catch {
  Write-Host ('ERROR: No se pudo conectar - ' + $_.Exception.Message) -ForegroundColor Red
}

Write-Host '`n=== API Gateway (8080) ===' 
try {
  $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/depositos' -Method Get -Headers @{Authorization='Bearer '+ $token_operador} -ErrorAction Stop
  Write-Host 'ÉXITO: API Gateway funcionando' -ForegroundColor Green
  Write-Host ('Respuesta: ' + $response.Count + ' registros')
} catch {
  Write-Host ('ERROR: Gateway no responde - ' + $_.Exception.Message) -ForegroundColor Red
}

Write-Host '`nTodos los tokens están listos en test-endpoints.http'

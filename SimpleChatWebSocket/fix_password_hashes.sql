USE simplechat;
GO

-- Update with correct BCrypt hashes from server generate-hash endpoint
UPDATE users SET password_hash = '$2a$10$/uqdirNZ4HEaXCm6SpkADeo5DkSabwdXJJgby3eBz2LQum8eDyIDC' WHERE username = 'admin';
UPDATE users SET password_hash = '$2a$10$fL.n6KzFMc5GIeKzwOMamu5OMLY4XfrsKEYOX8tiw.A/xDrDRalVq' WHERE username IN ('user1','user2','user3','user4','user5');  
UPDATE users SET password_hash = '$2a$10$JGw5CeGwxtpBUqJkZhiuxeLn9EN/7kT1hsRemrHMsX6PipHM85PsC' WHERE username = 'mod1';

SELECT username, password_hash FROM users WHERE username IN ('admin','user1','mod1');
GO

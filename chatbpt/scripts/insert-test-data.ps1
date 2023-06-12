$schema = @'
use ChatBptDb;

-- Insert Users
INSERT INTO User (name, password, shortname) VALUES
    ('John', 'pass', 'jodoe'),
    ('Jane', 'pass', 'jadoe'),
    ('Admin', 'pass', 'a'),
    ('Tobias', 'pass', 'tobi'),
    ('Maxi', 'pass', 'max'),
    ('Franz', 'pass', 'sepp');

-- Insert Chatrooms
INSERT INTO Chatroom (name, owner_id) VALUES
    ('Anonymous', 1), -- Owner: John
    ('Top Secret', 3), -- Owner: a
    ('Friends', 4); -- Owner: Tobias

-- Insert Users into Chatrooms
INSERT INTO User_Chatroom (user_id, chatroom_name) VALUES
    (1, 'Anonymous'), -- User: John
    (2, 'Anonymous'), -- User: Jane
    (3, 'Top Secret'), -- User: Admin
    (4, 'Friends'), -- User: Tobias
    (5, 'Friends'), -- User: Maxi
    (6, 'Friends'); -- User: Franz

-- Insert Messages
INSERT INTO Message (user_id, text, timestamp, chatroom_name) VALUES
    (1, 'Hello World!', NOW(), 'Anonymous'), -- Sender: John
    (2, 'Hello Mars!', NOW(), 'Anonymous'), -- Sender: Jane
    (1, 'Hello Jupyter!', NOW(), 'Anonymous'), -- Sender: John
    (2, 'Hello World!', NOW(), 'Anonymous'), -- Sender: Jane
    (3, 'Hello Admin!', NOW(), 'Top Secret'), -- Sender: Admin
    (4, 'Hello, I am Tobias!', NOW(), 'Friends'), -- Sender: Tobias
    (5, 'Hello, I am Maxi!', NOW(), 'Friends'), -- Sender: Maxi
    (6, 'Hello, I am Franz!', NOW(), 'Friends'); -- Sender: Franz
'@

echo $schema | docker exec -i mysql mysql

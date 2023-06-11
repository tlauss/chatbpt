$schema = @'
use ChatBptDb;

-- Insert Users
INSERT INTO User (name, password, shortname) VALUES
    ('John Doe', 'password', 'John'),
    ('Jane Doe', 'password', 'Jane'),
    ('a', 'a', 'admin'),
    ('b', 'b', 'b');

-- Insert Chatrooms
INSERT INTO Chatroom (name, owner_id) VALUES
    ('Test Chatroom 1', 3), -- Owner: a
    ('Test Chatroom 2', 2), -- Owner: Jane Doe
    ('Test Chatroom 3', 4); -- Owner: b

-- Insert Users into Chatrooms
INSERT INTO User_Chatroom (user_id, chatroom_name) VALUES
    (1, 'Test Chatroom 1'), -- User: John Doe
    (2, 'Test Chatroom 1'), -- User: Jane Doe
    (3, 'Test Chatroom 1'), -- User: a
    (2, 'Test Chatroom 2'), -- User: Jane Doe
    (4, 'Test Chatroom 3'); -- User: b

-- Insert Messages
INSERT INTO Message (user_id, text, timestamp, chatroom_name) VALUES
    (1, 'Hello World!', NOW(), 'Test Chatroom 1'), -- Sender: John Doe
    (2, 'Hello Mars!', NOW(), 'Test Chatroom 1'), -- Sender: Jane Doe
    (1, 'Hello Jupyter!', NOW(), 'Test Chatroom 1'), -- Sender: John Doe
    (2, 'Hello World!', NOW(), 'Test Chatroom 2'), -- Sender: Jane Doe
    (3, 'Hello Admin!', NOW(), 'Test Chatroom 2'); -- Sender: admin

'@

echo $schema | docker exec -i mysql mysql

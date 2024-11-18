'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var fileInput = document.querySelector('#file-input');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.send("/app/chat.addUser", {}, JSON.stringify({sender: username, type: 'JOIN'}));
    connectingElement.classList.add('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    
    // Send text message
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = ''; // Clear input after sending
    }

    // Send file message if a file is selected
    sendFile(event);
    event.preventDefault();
}

function sendFile(event) {
    var file = fileInput.files[0];
    if (file) {
        var formData = new FormData();
        formData.append('file', file);
        formData.append('sender', username);

        fetch('/chat/sendFile', {
            method: 'POST',
            body: formData
        }).then(response => {
            if (!response.ok) {
                throw new Error('File upload failed');
            }
            console.log('File uploaded successfully');
            // Optionally send a message to chat that a file was uploaded
            var fileMessage = {
                sender: username,
                fileUrl: response.url, // Assuming the server returns the file URL
                fileName: file.name,
                type: 'FILE'
            };
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(fileMessage));
        }).catch(error => {
            console.error('Error:', error);
        });
    }
    fileInput.value = ''; // Clear file input after sending
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left';
    } else if (message.type === 'FILE') {
        messageElement.classList.add('chat-message');
        
        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        usernameElement.style.display = 'block'; // Ensures the name appears above the file
        messageElement.appendChild(usernameElement);

        var fileLink = document.createElement('a');
        fileLink.href = message.fileUrl; // URL where the file can be downloaded
        fileLink.textContent = 'Download-> ' + message.fileName;
        
        messageElement.appendChild(fileLink);
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        usernameElement.style.display = 'block'; // Ensures the name appears above the message
        messageElement.appendChild(usernameElement);
    }
    if(message.type !== 'FILE') {
       var textElement = document.createElement('p');
       var messageText = document.createTextNode(message.content || '');
       textElement.appendChild(messageText);
       messageElement.appendChild(textElement);
    }
    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
fileInput.addEventListener('change', sendFile, true);
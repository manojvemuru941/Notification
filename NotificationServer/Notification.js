/**
 * Created by manoj on 5/28/15.
 */


var WebSocketServer = require('websocket').server
    ,mysql = require('mysql')
    ,http = require('http');

var server = http.createServer(function(request, response) {

    send("manoj","mesage","hie Manoj");
//    sendAndroidNotification(["APA91bEQNBe47LcYmnvp-7P21fkYDUhmwHbzq6gAYqR003kdTEWF6y4aYN9_zs6DCAIhMP3I1wtyz7J86aq-O5LVMZhG6Qmzrat8dwLQ09ZPiIpvCDeAK7LV8Lb01M5up4zX6LGIvtldfx8oD5-xtSEKVcW3wAbG4eX47p9WBvXmmLlb3pJZzahQd9ZPAakI1Hqn4MHVj0Zr"],"mesage","hello");

    response.end();
});
var connection = null;
var dataArray = [];

function connect() {
    return mysql.createConnection({
        host : '127.0.0.1',
        user : 'root',
        password : '',
        database : 'notificationDB'
    });
}
function registerDevice(vidaoId,type,deviceId){
    var sql = "INSERT INTO device (vidaoId, type, deviceId) VALUES (?,?,?)";

    var inserts = [vidaoId, type, deviceId];
    var connection = connect();
    connection.query(sql, inserts, function (err, result) {
        if (err) throw err;

    });
    connection.end();
}
server.listen(1337, function(err) {
    if(err){
        console.log(err);
    }else{
        console.log('Listening on port 1337')
    }
});

// create the server
wsServer = new WebSocketServer({
    httpServer: server
});

// WebSocket server
wsServer.on('request', function(request) {
    connection = request.accept(null, request.origin);

    // This is the most important callback for us, we'll handle
    // all messages from users here.
    connection.on('message', function(message) {
        console.log(message)
        if (message.type === 'utf8') {
            var content = JSON.parse(message.utf8Data);
            switch(content.category) {
                case "register":
                    if(content.data.vidaoId != '' && content.data.vidaoId != undefined){
                        registerDevice(content.data.vidaoId,content.data.type,content.data.deviceId);
                    }
                    break;
                case "chat" :
                    send(content.data.to,"mesage",JSON.stringify(content.data.msg));
                    break;
                case "call" :

                    break;
            }
        }

    });

    connection.on('close', function(connection) {
        // close user connection
    });

});
function unRegister(vidaoId,deviceId){
    var connection = connect();

    var sql = "DELETE FROM device WHERE userID = ? and deviceId = ?";
}
function send(to,title, message){

    var connection = connect();

    var sql = "SELECT * FROM device WHERE vidaoId = '" + to + "'";

    connection.query(sql, function (err, rows) {
        if (err) throw err;
        var ids = [];
        for (var row = 0;row <rows.length;row++) {
            if(rows[row].type === 'android'){
             ids.push(rows[row].deviceId);
            }
            if(row === (rows.length-1)){
                sendAndroidNotification(ids,title,message);
            }
        }

    });
    connection.close();
}


function sendAndroidNotification(senderIds, messageTitle, messageContent) {
    // api key AIzaSyDVfiJyIaT75Fl9-Ot6kx-Vc24dyud3Tas
console.log(senderIds);
    var gcm = require('node-gcm')  ,message = new gcm.Message();

    var sender = new gcm.Sender('AIzaSyDVfiJyIaT75Fl9-Ot6kx-Vc24dyud3Tas');
    message = new gcm.Message();
    message.addData(messageTitle,messageContent);
//    sender.send(message, 'APA91bEQNBe47LcYmnvp-7P21fkYDUhmwHbzq6gAYqR003kdTEWF6y4aYN9_zs6DCAIhMP3I1wtyz7J86aq-O5LVMZhG6Qmzrat8dwLQ09ZPiIpvCDeAK7LV8Lb01M5up4zX6LGIvtldfx8oD5-xtSEKVcW3wAbG4eX47p9WBvXmmLlb3pJZzahQd9ZPAakI1Hqn4MHVj0Zr', function(err, result) {
//            if(err) console.log(err);
//            else    console.log(result);
//        });
    for (var id = 0;id <senderIds.length;id++){
        console.log("ID :"+senderIds[id]);
        sender.send(message, senderIds[id], function(err, result) {
            if(err) console.log(err);
            else    console.log(result);
        });
    }


}

//function sendIOSNotification(){
//    var apns = require("apns"), options, connection, notification;
//
//    options = {
//        keyFile : "conf/key.pem",
//        certFile : "conf/cert.pem",
//        debug : true
//    };
//
//    connection = new apns.Connection(options);
//
//    notification = new apns.Notification();
//    notification.device = new apns.Device("iphone_token");
//    notification.alert = "Hello World !";
//
//    connection.sendNotification(notification);
//}



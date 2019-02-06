const server = require('http').createServer();
const io = require('socket.io')(server);
const spawn = require("child_process").spawn;
var ecgStatus;
io.sockets.on('connection', client => {
    console.log("client connected !");

    client.on('foo', function (message) { 
        inbound = JSON.stringify(message);
        //console.log(message);
        
        // const pythonProcess = spawn('python',["./script.py", inbound]);
        // pythonProcess.stdout.on('data', (data) => {
        // ecgStatus = Buffer.from(data, 'hex').toString('utf8');        
        // });
    client.broadcast.emit('event', {status: "test" , raw_data: message});
    }); 
});
server.listen(3000);


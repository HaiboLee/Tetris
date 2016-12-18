var playState = function (game) {
    var flag, num, players = {};
    var d = 10, r = 100;
    var drawMap, chick, createBox;
    var tileMap, layer1, mybox;
    var stats, cursors;
    // var x = d * 20, y = r + d;
    var chickLine = null;
    var webSocket;
    this.init = function () {
        createBox = new CreateBox();

        stats = new Stats();
        stats.setMode(0); // 0: fps, 1: ms
        stats.domElement.style.position = 'absolute';
        stats.domElement.style.left = '0px';
        stats.domElement.style.top = '0px';
        document.body.appendChild(stats.domElement);
    }
    this.create = function () {
        game.stage.disableVisibilityChange = true;

        webSocket = new WebSocket('ws://' + window.location.host
            + '/Tetris/websocket');

        cursors = this.input.keyboard.createCursorKeys();
        tileMap = game.add.tilemap();
        tileMap.addTilesetImage('tileset', 'tile', d, d);
        layer1 = tileMap
            .create('leave1', game.width / d, game.height / d, d, d);
        layer1.scrollFactorX = 0.5;
        layer1.scrollFactorY = 0.5;
        layer1.resizeWorld();

        drawMap = new DrawMap(tileMap, layer1, d, r);
        chick = new Chick(tileMap, layer1, d, r);
        drawMap.drawGrid();
        drawMap.drawBound(3);

        tileMap.putTile(4, 50, 30, layer1);
        tileMap.fill(0, 10, 49, 77, 1, layer1);
        game.input.onDown.add(function () {
            // tileMap.putTile(1,10,481,layer1);
        });
        // 玩家头像位置
        var dd = (game.width - 2 * r) / 5;

        var begindd = r + dd / 2;
        //mybox = createBox.createMyBox(3, begindd, r);
        createBox.createUser('one', begindd, r / 2);
        createBox.createUser('two', begindd + dd, r / 2);
        createBox.createUser('three', begindd + 2 * dd, r / 2);
        createBox.createUser('four', begindd + 3 * dd, r / 2);
        createBox.createUser('five', begindd + 4 * dd, r / 2);

        // 键盘监听
        document.onkeydown = function (event) {
            var e = event || window.event
                || arguments.callee.caller.arguments[0];
            if (e && e.keyCode == 38) { // 按 up
                //chick.chickAngle(mybox)
                sendMsg("a:" + flag + ":" + num);
            }
            if (e && e.keyCode == 40) { // 按 down
                if (chick.chickMove(players[num], 40)) {
                    //mybox.y += 10;
                    sendMsg("m:" + flag + ":" + num + ":" + players[num].x + ":" + (players[num].y + d));
                }
            }
            if (e && e.keyCode == 37) { // 按 left
                if (chick.chickMove(players[num], 37)) {
                    //mybox.x -= d;
                    sendMsg("m:" + flag + ":" + num + ":" + (players[num].x - d) + ":" + players[num].y);
                }
            }

            if (e && e.keyCode == 39) { // 按 right
                if (chick.chickMove(players[num], 39)) {
                    //mybox.x += d
                    sendMsg("m:" + flag + ":" + num + ":" + (players[num].x + d) + ":" + players[num].y);
                }
            }
            if (e && e.keyCode == 65) {
                //mybox.y -= 10
                sendMsg("m:" + flag + ":" + num + ":" + players[num].x + ":" + (players[num].y - 10));
            }
        }

        window.onbeforeunload = function () {//关闭窗口
            leave();
            console.log('关闭窗口');
        }


        webSocket.onmessage = function (event) {
            //console.log(event.data);
            var arr = event.data.split(":");

            if (arr[0] == "m") {//移动
                players[arr[2]].x = parseInt(arr[3]);
                players[arr[2]].y = parseInt(arr[4]);
            } else if (arr[0] == 'a') {
                chick.chickAngle(players[parseInt(arr[2])]);
            } else if (arr[0] == 'n') {//新方块产生
                players[arr[2]] = createBox.createMyBox(arr[3], begindd + arr[2] * dd, r);

            } else if (arr[0] == 'd') {//绘制方块
                drawMap.drawBox(players[parseInt(arr[2])]);
                chickLine = chick.chickLine(players[num]);
                if (chickLine.length != 0) {
                    sendMsg("g:" + flag + ":" + chickLine.toString())
                    chickLine.splice(0, chickLine.length);
                }
            } else if (arr[0] == 'k') {//销毁方块
                players[parseInt(arr[2])].destroy();
            } else if (arr[0] == 'g') {//得分
                var gg = arr[2].split(',');
                drawMap.removeLine(gg);
                drawMap.downTile(gg);
            } else if(arr[0] == 'l'){//有玩家离开
                //players[parseInt(arr[2])];
                removeByValue(players,players[parseInt(arr[2])]);
                players[parseInt(arr[2])].destroy();
            } else if (arr[0] == 'j') {
                flag = arr[1];
                num = arr[2];
                console.log(flag + ":" + num);
                sendMsg("n:" + flag + ":" + num + ":" + Math.floor(Math.random() * 4));
                setInterval(function () {
                    if (chick.chickMove(players[num], 40)) {
                        //players[num].y += 10;
                        sendMsg("m:" + flag + ":" + num + ":" + players[num].x + ":" + (players[num].y + 10));
                    } else {
                        sendMsg("d:" + flag + ":" + num);
                        sendMsg("k:" + flag + ":" + num);
                        sendMsg("n:" + flag + ":" + num + ":" + Math.floor(Math.random() * 4));
                    }
                }, 500);

            }
        }

    }
    this.update = function () {
        stats.update();
    }

    function sendMsg(msg) {
        webSocket.send(msg);
    }

    function leave(){
        webSocket.send('l:' + flag + ':' + num);
        webSocket.close();
    }

    function removeByValue(array, val) {
        for(var i=0; i<array.length; i++) {
            if(array[i] == val) {
                array.splice(i, 1);
                break;
            }
        }
    }

}
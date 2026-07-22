<?php
require 'vendor/autoload.php';

use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;
use Ratchet\Http\HttpServer;
use Ratchet\WebSocket\WsServer;
use Ratchet\Server\IoServer;

class Server implements MessageComponentInterface {
    public $clients = [];

    public function onOpen(ConnectionInterface $conn) {
        $this->clients[$conn->resourceId] = ['conn' => $conn, 'key' => null];
        echo "✅ New connection\n";
    }

    public function onMessage(ConnectionInterface $from, $msg) {
        $data = json_decode($msg, true);
        if (!$data) return;

        $type = $data['type'] ?? null;

        if ($type == 'register') {
            $this->clients[$from->resourceId]['key'] = $data['key'];
            echo "🔑 Registered: " . $data['key'] . "\n";
        }

        if ($type == 'connect') {
            foreach ($this->clients as $client) {
                if ($client['key'] == $data['to']) {
                    $client['conn']->send(json_encode([
                        'type' => 'incoming',
                        'from' => $data['from']
                    ]));
                }
            }
        }

        if ($type == 'accept' || $type == 'reject') {
            foreach ($this->clients as $client) {
                if ($client['key'] == $data['to']) {
                    $client['conn']->send(json_encode($data));
                }
            }
        }

        // ✅ إضافة معالجة رسائل التحكم (control)
        if ($type == 'control') {
            foreach ($this->clients as $client) {
                if ($client['key'] == $data['to']) {
                    $client['conn']->send(json_encode($data));
                }
            }
        }
    }

    public function onClose(ConnectionInterface $conn) {
        unset($this->clients[$conn->resourceId]);
    }

    public function onError(ConnectionInterface $conn, \Exception $e) {
        $conn->close();
    }
}

$server = IoServer::factory(
    new HttpServer(new WsServer(new Server())),
    8080
);
echo "🚀 Server running on ws://0.0.0.0:8080\n";
$server->run();
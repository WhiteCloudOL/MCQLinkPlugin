package cn.meowyun.mcqlink.api;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Socks {
    private Client client;
    private JavaPlugin plugin;
    private Logger logger;
    private String serverUrl;
    private String token;
    private boolean connected = false;
    private static final ObjectMapper mapper = new ObjectMapper();  // 移到外部类

    public Socks(JavaPlugin plugin, String serverIp, int serverPort, String token) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.token = token;

        // 构建 WebSocket URL
        this.serverUrl = "ws://" + serverIp + ":" + serverPort;

        initializeWebSocket();
    }

    private void initializeWebSocket() {
        try {
            URI serverUri = new URI(serverUrl);
            this.client = new Client(serverUri, plugin);
            logger.info("正在连接至 WebSocket 服务器: " + serverUrl);

            // 在新线程中连接，避免阻塞主线程
            new Thread(() -> {
                try {
                    client.connect();
                } catch (Exception e) {
                    logger.warning("WebSocket 连接失败: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            logger.severe("初始化 WebSocket 客户端失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return client != null && client.isOpen() && client.isAuthenticated();
    }

    public void sendMessage(String content) {
        if (!isConnected()) {
            logger.warning("WebSocket 未连接，无法发送消息");
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "minecraft_message");
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());

            client.send(mapper.writeValueAsString(message));  // 现在可以访问 mapper
        } catch (Exception e) {
            logger.warning("发送消息失败: " + e.getMessage());
        }
    }

    public void sendChatMessage(String playerName, String message) {
        if (!isConnected()) return;

        try {
            Map<String, Object> chatMessage = new HashMap<>();
            chatMessage.put("type", "minecraft_chat");
            chatMessage.put("player", playerName);
            chatMessage.put("content", message);
            chatMessage.put("timestamp", System.currentTimeMillis());

            client.send(mapper.writeValueAsString(chatMessage));  // 现在可以访问 mapper
        } catch (Exception e) {
            logger.warning("发送聊天消息失败: " + e.getMessage());
        }
    }

    public void sendPlayerJoin(String playerName) {
        if (!isConnected()) return;

        try {
            Map<String, Object> joinMessage = new HashMap<>();
            joinMessage.put("type", "player_join");
            joinMessage.put("player", playerName);
            joinMessage.put("timestamp", System.currentTimeMillis());

            client.send(mapper.writeValueAsString(joinMessage));  // 现在可以访问 mapper
        } catch (Exception e) {
            logger.warning("发送玩家加入消息失败: " + e.getMessage());
        }
    }

    public void sendPlayerQuit(String playerName) {
        if (!isConnected()) return;

        try {
            Map<String, Object> quitMessage = new HashMap<>();
            quitMessage.put("type", "player_quit");
            quitMessage.put("player", playerName);
            quitMessage.put("timestamp", System.currentTimeMillis());

            client.send(mapper.writeValueAsString(quitMessage));  // 现在可以访问 mapper
        } catch (Exception e) {
            logger.warning("发送玩家退出消息失败: " + e.getMessage());
        }
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }

    // 内部 Client 类
    class Client extends WebSocketClient {
        private boolean authenticated = false;
        private JavaPlugin plugin;

        public Client(URI serverUri, JavaPlugin plugin) {
            super(serverUri);
            this.plugin = plugin;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            logger.info("已连接到 WebSocket 服务器");

            // 发送认证消息
            Map<String, Object> auth = new HashMap<>();
            auth.put("type", "auth");
            auth.put("token", token);
            auth.put("client_type", "minecraft");

            try {
                send(mapper.writeValueAsString(auth));  // 现在可以访问 mapper
                logger.info("认证消息已发送");
            } catch (Exception e) {
                logger.warning("发送认证消息失败: " + e.getMessage());
            }
        }

        @Override
        public void onMessage(String message) {
            try {
                Map<String, Object> data = mapper.readValue(message, Map.class);  // 现在可以访问 mapper
                String type = (String) data.get("type");

                if ("auth_response".equals(type)) {
                    String status = (String) data.get("status");
                    if ("success".equals(status)) {
                        authenticated = true;
                        logger.info("WebSocket 认证成功");
                    } else {
                        String errorMsg = (String) data.get("message");
                        logger.warning("WebSocket 认证失败: " + errorMsg);
                        close();
                    }
                }
                else if ("broadcast".equals(type)) {
                    String content = (String) data.get("content");
                    if (content != null) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            plugin.getServer().broadcastMessage("§6[QQ] §f" + content);
                        });
                    }
                }
                else if ("minecraft_command".equals(type)) {
                    String command = (String) data.get("command");
                    if (command != null) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                        });
                    }
                }
                else {
                    logger.info("收到消息: " + data);
                }

            } catch (Exception e) {
                logger.warning("处理消息失败: " + e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            authenticated = false;
            logger.info("WebSocket 连接已关闭: " + reason + " (代码: " + code + ")");
        }

        @Override
        public void onError(Exception ex) {
            logger.warning("WebSocket 错误: " + ex.getMessage());
        }

        public boolean isAuthenticated() {
            return authenticated;
        }
    }
}
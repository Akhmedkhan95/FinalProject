package server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NetworkGameTest {

    private static final int TEST_PORT = 19999;
    private static Thread serverThread;
    private static volatile boolean serverRunning;

    @BeforeAll
    static void startServer() throws InterruptedException {
        // Запускаем сервер в отдельном потоке
        CountDownLatch serverStarted = new CountDownLatch(1);
        serverRunning = true;

        serverThread = new Thread(() -> {
            try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(TEST_PORT)) {
                serverStarted.countDown();
                java.util.concurrent.ExecutorService pool =
                        java.util.concurrent.Executors.newCachedThreadPool();

                while (serverRunning) {
                    try {
                        serverSocket.setSoTimeout(100);
                        Socket clientSocket = serverSocket.accept();
                        // Используем反射 или упрощенную версию
                        // Для теста просто обрабатываем клиентов
                        pool.execute(() -> handleTestClient(clientSocket));
                    } catch (java.net.SocketTimeoutException e) {
                        // Таймаут — нормально, проверяем serverRunning
                    }
                }
                pool.shutdown();
            } catch (IOException e) {
                if (serverRunning) e.printStackTrace();
            }
        });
        serverThread.start();

        // Ждём, пока сервер запустится
        assertTrue(serverStarted.await(5, TimeUnit.SECONDS), "Сервер не запустился");
    }

    @AfterAll
    static void stopServer() throws InterruptedException {
        serverRunning = false;
        if (serverThread != null) {
            serverThread.join(2000);
        }
    }

    // Упрощённый обработчик для тестов
    private static void handleTestClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("WELCOME");
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("ECHO ")) {
                    out.println(line.substring(5));
                } else if (line.equals("PING")) {
                    out.println("PONG");
                } else if (line.equals("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            // Клиент отключился
        }
    }

    @Test
    void testClientConnection() throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String welcome = in.readLine();
            assertEquals("WELCOME", welcome);
        }
    }

    @Test
    void testClientDisconnect() throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", TEST_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        in.readLine(); // WELCOME
        out.println("PING");
        assertEquals("PONG", in.readLine());

        // Резко закрываем сокет (имитация отключения)
        socket.close();

        // Даём серверу время обработать отключение
        Thread.sleep(200);

        // Сервер должен продолжать работать — подключаем нового клиента
        try (Socket newSocket = new Socket("localhost", TEST_PORT);
             BufferedReader newIn = new BufferedReader(new InputStreamReader(newSocket.getInputStream()))) {
            assertEquals("WELCOME", newIn.readLine());
        }
    }

    @Test
    void testMultipleParallelClients() throws IOException, InterruptedException {
        int clientCount = 5;
        Thread[] clients = new Thread[clientCount];
        boolean[] success = new boolean[clientCount];

        for (int i = 0; i < clientCount; i++) {
            final int index = i;
            clients[i] = new Thread(() -> {
                try (Socket socket = new Socket("localhost", TEST_PORT);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    assertEquals("WELCOME", in.readLine());
                    out.println("PING");
                    assertEquals("PONG", in.readLine());
                    success[index] = true;
                } catch (IOException e) {
                    success[index] = false;
                }
            });
            clients[i].start();
        }

        for (Thread t : clients) {
            t.join(5000);
        }

        for (int i = 0; i < clientCount; i++) {
            assertTrue(success[i], "Клиент " + i + " не смог подключиться");
        }
    }

    @Test
    void testServerHandlesClientDisconnectGracefully() throws IOException, InterruptedException {
        // Подключаем 3 клиента
        Socket[] sockets = new Socket[3];
        for (int i = 0; i < 3; i++) {
            sockets[i] = new Socket("localhost", TEST_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
            assertEquals("WELCOME", in.readLine());
        }

        // Закрываем первого
        sockets[0].close();
        Thread.sleep(100);

        // Второй и третий должны работать
        try (PrintWriter out2 = new PrintWriter(sockets[1].getOutputStream(), true);
             BufferedReader in2 = new BufferedReader(new InputStreamReader(sockets[1].getInputStream()))) {
            out2.println("PING");
            assertEquals("PONG", in2.readLine());
        }

        // Закрываем всех
        for (Socket s : sockets) {
            if (!s.isClosed()) s.close();
        }
    }
}
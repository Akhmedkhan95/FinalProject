package server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NetworkGameTest {

    private static final int TEST_PORT = 19876;
    private static Thread serverThread;
    private static volatile boolean serverRunning;

       private static List<GameRoom> sharedGameRooms;

    @BeforeAll
    static void startServer() throws InterruptedException {
        CountDownLatch serverStarted = new CountDownLatch(1);
        serverRunning = true;

        // Инициализируем общий список ОДИН раз при запуске сервера
        sharedGameRooms = Collections.synchronizedList(new java.util.ArrayList<>());

        serverThread = new Thread(() -> {
            try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(TEST_PORT)) {
                serverStarted.countDown();
                java.util.concurrent.ExecutorService pool =
                        java.util.concurrent.Executors.newCachedThreadPool();

                while (serverRunning) {
                    try {
                        serverSocket.setSoTimeout(100);
                        Socket clientSocket = serverSocket.accept();

                        // ВАЖНО: передаем sharedGameRooms, а не создаем новый список!
                        ClientSession session = new ClientSession(clientSocket, sharedGameRooms);
                        pool.execute(session);
                    } catch (java.net.SocketTimeoutException e) {
                        // Таймаут — нормальное поведение при проверке флага serverRunning
                    }
                }
                pool.shutdown();
            } catch (IOException e) {
                if (serverRunning) e.printStackTrace();
            }
        });
        serverThread.start();

        assertTrue(serverStarted.await(5, TimeUnit.SECONDS), "Сервер не запустился");
        Thread.sleep(300); // Даём время серверу полностью инициализироваться
    }

    @AfterAll
    static void stopServer() throws InterruptedException {
        serverRunning = false;
        if (serverThread != null) {
            serverThread.join(2000);
        }
    }

    @Test
    @Order(1)
    void testTwoPlayersJoinAndPlay() throws IOException, InterruptedException {
        Socket socket1 = new Socket("localhost", TEST_PORT);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);

        readAllAvailable(in1);

        out1.println("JOIN Игрок1");
        Thread.sleep(200);
        String response1 = readAllAvailable(in1);
        assertTrue(response1.contains("Ожидание"), "Первый игрок должен ждать второго. Ответ: " + response1);

        Socket socket2 = new Socket("localhost", TEST_PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

        readAllAvailable(in2);

        out2.println("JOIN Игрок2");
        Thread.sleep(300);

        String msg1 = readAllAvailable(in1);
        String msg2 = readAllAvailable(in2);

        assertTrue(msg1.contains("Игра начинается") || msg2.contains("Игра начинается"),
                "Игроки должны получить сообщение о начале игры. msg1: " + msg1 + ", msg2: " + msg2);

        out1.println("MOVE КАМЕНЬ");
        Thread.sleep(200);

        String responseAfterMove1 = readAllAvailable(in2);
        assertTrue(responseAfterMove1.contains("сделал") || responseAfterMove1.contains("Ждем"),
                "Второй игрок должен получить уведомление о ходе первого. Ответ: " + responseAfterMove1);

        out2.println("MOVE НОЖНИЦЫ");
        Thread.sleep(300);

        String result1 = readAllAvailable(in1);
        String result2 = readAllAvailable(in2);

        assertTrue(result1.contains("РЕЗУЛЬТАТЫ") || result2.contains("РЕЗУЛЬТАТЫ"),
                "Игроки должны получить результат раунда. result1: " + result1 + ", result2: " + result2);
        assertTrue(result1.contains("КАМЕНЬ") || result2.contains("КАМЕНЬ"),
                "Результат должен содержать ход КАМЕНЬ");

        socket1.close();
        socket2.close();
    }

    @Test
    @Order(2)
    void testCannotMoveTwice() throws IOException, InterruptedException {
        Socket socket1 = new Socket("localhost", TEST_PORT);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);

        readAllAvailable(in1);
        out1.println("JOIN Тестер1");
        readAllAvailable(in1);

        Socket socket2 = new Socket("localhost", TEST_PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

        readAllAvailable(in2);
        out2.println("JOIN Тестер2");
        Thread.sleep(300);
        readAllAvailable(in1);
        readAllAvailable(in2);

        out1.println("MOVE КАМЕНЬ");
        Thread.sleep(200);
        readAllAvailable(in2);

        out1.println("MOVE БУМАГА");
        Thread.sleep(200);

        String errorMsg = readAllAvailable(in1);
        assertTrue(errorMsg.contains("уже сделал") || errorMsg.contains("Ожидайте"),
                "Сервер должен отклонить повторный ход. Ответ: " + errorMsg);

        socket1.close();
        socket2.close();
    }

    @Test
    @Order(3)
    void testPlayerDisconnect() throws IOException, InterruptedException {
        Socket socket1 = new Socket("localhost", TEST_PORT);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);

        readAllAvailable(in1);
        out1.println("JOIN ИгрокA");
        readAllAvailable(in1);

        Socket socket2 = new Socket("localhost", TEST_PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

        readAllAvailable(in2);
        out2.println("JOIN ИгрокB");
        Thread.sleep(300);
        readAllAvailable(in1);
        readAllAvailable(in2);

        socket1.close();
        Thread.sleep(500);

        String disconnectMsg = readAllAvailable(in2);
        assertTrue(disconnectMsg.contains("покинул") || disconnectMsg.contains("прервана"),
                "Второй игрок должен получить уведомление об отключении. Ответ: " + disconnectMsg);

        socket2.close();
    }

    @Test
    @Order(4)
    void testNewPlayerAfterDisconnect() throws IOException, InterruptedException {
        Socket socket1 = new Socket("localhost", TEST_PORT);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);

        readAllAvailable(in1);
        out1.println("JOIN Старый1");
        readAllAvailable(in1);

        Socket socket2 = new Socket("localhost", TEST_PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

        readAllAvailable(in2);
        out2.println("JOIN Старый2");
        Thread.sleep(300);
        readAllAvailable(in1);
        readAllAvailable(in2);

        socket1.close();
        socket2.close();
        Thread.sleep(500);

        Socket socket3 = new Socket("localhost", TEST_PORT);
        BufferedReader in3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
        PrintWriter out3 = new PrintWriter(socket3.getOutputStream(), true);

        readAllAvailable(in3);
        out3.println("JOIN Новый1");
        Thread.sleep(200);
        String response3 = readAllAvailable(in3);
        assertTrue(response3.contains("Ожидание"), "Новый игрок должен ждать. Ответ: " + response3);

        Socket socket4 = new Socket("localhost", TEST_PORT);
        BufferedReader in4 = new BufferedReader(new InputStreamReader(socket4.getInputStream()));
        PrintWriter out4 = new PrintWriter(socket4.getOutputStream(), true);

        readAllAvailable(in4);
        out4.println("JOIN Новый2");
        Thread.sleep(300);

        String response4 = readAllAvailable(in3);
        String response5 = readAllAvailable(in4);

        assertTrue(response4.contains("Игра начинается") || response5.contains("Игра начинается"),
                "Новая пара должна начать игру. response4: " + response4 + ", response5: " + response5);

        socket3.close();
        socket4.close();
    }

    @Test
    @Order(5)
    void testMultipleParallelRooms() throws IOException, InterruptedException {
        Socket socket1 = new Socket("localhost", TEST_PORT);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
        readAllAvailable(in1);
        out1.println("JOIN Комната1-Игрок1");
        readAllAvailable(in1);

        Socket socket2 = new Socket("localhost", TEST_PORT);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
        readAllAvailable(in2);
        out2.println("JOIN Комната1-Игрок2");
        Thread.sleep(300);
        readAllAvailable(in1);
        readAllAvailable(in2);

        Socket socket3 = new Socket("localhost", TEST_PORT);
        BufferedReader in3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
        PrintWriter out3 = new PrintWriter(socket3.getOutputStream(), true);
        readAllAvailable(in3);
        out3.println("JOIN Комната2-Игрок1");
        readAllAvailable(in3);

        Socket socket4 = new Socket("localhost", TEST_PORT);
        BufferedReader in4 = new BufferedReader(new InputStreamReader(socket4.getInputStream()));
        PrintWriter out4 = new PrintWriter(socket4.getOutputStream(), true);
        readAllAvailable(in4);
        out4.println("JOIN Комната2-Игрок2");
        Thread.sleep(300);
        readAllAvailable(in3);
        readAllAvailable(in4);

        out1.println("MOVE КАМЕНЬ");
        out3.println("MOVE БУМАГА");
        Thread.sleep(200);

        out2.println("MOVE НОЖНИЦЫ");
        out4.println("MOVE КАМЕНЬ");
        Thread.sleep(300);

        String result1 = readAllAvailable(in1);
        String result2 = readAllAvailable(in2);
        String result3 = readAllAvailable(in3);
        String result4 = readAllAvailable(in4);

        assertTrue(result1.contains("РЕЗУЛЬТАТЫ") || result2.contains("РЕЗУЛЬТАТЫ"),
                "Комната 1 должна получить результат. result1: " + result1 + ", result2: " + result2);
        assertTrue(result3.contains("РЕЗУЛЬТАТЫ") || result4.contains("РЕЗУЛЬТАТЫ"),
                "Комната 2 должна получить результат. result3: " + result3 + ", result4: " + result4);

        socket1.close();
        socket2.close();
        socket3.close();
        socket4.close();
    }

    @Test
    @Order(6)
    void testInvalidCommands() throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", TEST_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        readAllAvailable(in);

        out.println("INVALID_COMMAND");
        Thread.sleep(200);
        String response1 = readAllAvailable(in);
        assertTrue(response1.contains("Неизвестная") || response1.contains("UNKNOWN"),
                "Сервер должен отклонить неизвестную команду. Ответ: " + response1);

        out.println("MOVE КАМЕНЬ");
        Thread.sleep(200);
        String response2 = readAllAvailable(in);
        assertTrue(response2.contains("присоединитесь") || response2.contains("JOIN"),
                "Сервер должен потребовать сначала присоединиться. Ответ: " + response2);

        out.println("JOIN");
        Thread.sleep(200);
        String response3 = readAllAvailable(in);
        assertTrue(response3.contains("укажите имя") || response3.contains("Ошибка"),
                "Сервер должен потребовать имя. Ответ: " + response3);

        socket.close();
    }

    private String readAllAvailable(BufferedReader in) throws IOException, InterruptedException {
        Thread.sleep(150);
        StringBuilder sb = new StringBuilder();
        while (in.ready()) {
            String line = in.readLine();
            if (line != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
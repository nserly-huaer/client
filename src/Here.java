import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Here {//如果退出代码小于2则为正常退出，否则为异常退出
    public static final Scanner sc = new Scanner(System.in);
    public static final File PATH = new File("client.log");
    public static boolean div2;

    public static void main(String[] args) {
        try {
            System.out.println("请输入服务器IP");
            Write("INFO", "请输入服务器IP");
            String IP = sc.next();
            Write("INFO", "用户输入:" + IP);
            System.out.println("请输入服务器端口");
            Write("INFO", "请输入服务器端口");
            int port = sc.nextInt();
            Write("INFO", "用户输入:" + port);
            // 创建客户端套接字，连接服务器
            Socket socket = new Socket(IP, port);

            // 创建读取线程
            Thread readThread = new Thread(new ReadThread(socket));
            readThread.start();

            // 创建发送线程
            Thread sendThread = new Thread(new SendThread(socket));
            sendThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Write(String level, String message) {
        String write = getFirst(level) + message;
        WriteLog(write);
    }

    public static void ReadServerMessage(String message) {
        if (message.startsWith("log")) {
            String[] cache1 = message.split(" ", 2);
            String[] cache2 = cache1[0].split("-", 2);
            //日志信息
            String LogMessage = cache1[1];
            //日志等级
            String level = cache2[1];
            String result = getFirst(level) + LogMessage;
            level(level, result);
            WriteLog(result);
        } else if (message.startsWith("messageSender")) {
            String[] info = message.split(" ", 2);
            System.out.println(info[1]);
        } else if (message.startsWith("information")) {

            String[] info = message.split(" ", 2);
            System.out.println("服务器管理员发送：" + info[1]);

        } else if (message.startsWith("delay")) {
            String[] de = message.split(" ", 2);
            long del = Long.parseLong(de[1]);
            long time = System.currentTimeMillis();
            if (div2)
                System.out.println("服务器与客户端之间的延迟为：" + ((time - del) / 2) + "ms");
            else
                System.out.println("服务器与客户端之间的延迟为：" + (time - del) + "ms");
            try {
                String time1 = "reDelay " + String.valueOf(time - del);
                if (!div2)
                    SendThread.out.write(time1.getBytes());
                div2 = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(message);
            Write("INFO", message);
        }
    }

    private static void level(String level, String Cola) {
        if (level.equals("info")) return;
        else if (level.equals("debug")) return;
        else System.out.println(Cola);
    }

    private static String getTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(date);
    }

    private static String getFirst(String level) {
        String result = null;
        result = getTime();
        Thread t = Thread.currentThread();
        result += " [" + t.getName() + "/" + level + "]: ";
        return result;

    }

    private static void WriteLog(String message) {
        message = message.trim();
        message += "\n";
        FileOutputStream f = null;
        BufferedOutputStream bu = null;
        try {
            if (!PATH.exists()) {
                PATH.createNewFile();
            }
            f = new FileOutputStream(PATH, true);
            bu = new BufferedOutputStream(f);
            bu.write(message.getBytes());
            bu.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 读取线程
class ReadThread implements Runnable {
    private Socket socket;

    public ReadThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];

            while (true) {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                // 处理读取到的字节数据
                String message = new String(buffer, 0, bytesRead);
//                System.out.println(message);
                Here.ReadServerMessage(message);
            }

        } catch (IOException e) {
            Here.Write("Error", "用户输入：" + e.toString());
        }
    }
}

// 发送线程
class SendThread implements Runnable {
    public static OutputStream out = null;
    private static final Scanner sc = new Scanner(System.in);
    private Socket socket;

    public SendThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                out = socket.getOutputStream();
                // 发送消息给服务器
                String message = sc.nextLine();
                Here.Write("INFO", "用户输入：" + message);
                if (message.trim().toLowerCase().equals("$exit")) {
                    out.write("exit".getBytes());
                    out.flush();
                    socket.close();
                    System.exit(1);
                } else if (message.trim().toLowerCase().equals("delay")) {
                    Here.div2 = true;
                    String cache = "getdelay " + System.currentTimeMillis();
                    out.write(cache.getBytes());
                    continue;
                } else {
                    message = "messageSender " + message;
                    out.write(message.getBytes());
                    out.flush();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
            Here.Write("Error", "用户输入：" + e.toString());
        }
    }
}
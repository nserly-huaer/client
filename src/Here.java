import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Here {
    public static final Scanner sc = new Scanner(System.in);
    public static final File PATH = new File("client.log");

    public static void main(String[] args) {
        try {
            System.out.println("请输入服务器IP");
            String IP = sc.next();
            System.out.println("请输入服务器端口");
            int port = sc.nextInt();
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

    public static void ReadServerMessage(String message) {
        message = message.trim();
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
        } else if (message.startsWith("information")) {

            String[] info = message.split(" ", 2);
            System.out.println("服务器管理员发送：" + info);

        } else if (message.startsWith("delay")) {
            String[] de = message.split(" ", 2);
            long del = Long.parseLong(de[1]);
            long time = System.currentTimeMillis();
            System.out.println("服务器与客户端之间的延迟为：" + (time - del));
            try {
                String time1 = "reDelay " + String.valueOf(time - del);
                SendThread.out.write(time1.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(message);
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
        FileOutputStream f = null;
        BufferedOutputStream bu = null;
        try {
            f = new FileOutputStream(PATH, true);
            bu = new BufferedOutputStream(f);
            bu.write(message.getBytes());
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
            e.printStackTrace();
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
                if (message.toLowerCase().trim().equals("$exit")) {
                    socket.close();
                }
                out.write(message.getBytes());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
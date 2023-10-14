import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Here {
    public static final Scanner sc = new Scanner(System.in);

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
                System.out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 发送线程
class SendThread implements Runnable {
    public static final Scanner sc = new Scanner(System.in);
    private Socket socket;

    public SendThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (true){
                OutputStream out = socket.getOutputStream();

                // 发送消息给服务器
                String message = sc.nextLine();
                if(message.toLowerCase().trim().equals("$exit")){
                    socket.close();
                }
                out.write(message.getBytes());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
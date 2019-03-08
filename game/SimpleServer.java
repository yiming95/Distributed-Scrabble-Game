/*
 * Name: Yunlu WEN
 * ID: 869338
 * Username: yunluw
 *
 * */


//
//                       _oo0oo_
//                      o8888888o
//                      88" . "88
//                      (| -_- |)
//                      0\  =  /0
//                    ___/`---'\___
//                  .' \\|     |// '.
//                 / \\|||  :  |||// \
//                / _||||| -:- |||||- \
//               |   | \\\  -  /// |   |
//               | \_|  ''\---/''  |_/ |
//               \  .-\__  '-'  ___/-. /
//             ___'. .'  /--.--\  `. .'___
//          ."" '<  `.___\_<|>_/___.' >' "".
//         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//         \  \ `_.   \_ __\ /__ _/   .-` /  /
//     =====`-.____`.___ \_____/___.-`___.-'=====
//                       `=---='
//
//
//     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//               佛祖保佑         永无BUG
//
//
//

import java.net.*;
import java.io.*;
import org.json.simple.*;
import java.util.HashMap;
import java.util.UUID;




public class SimpleServer{



    public static void startServer(int port){
        try{
            ServerSocket server = new ServerSocket(port);
            while(true){
                // LISTEN
                Socket s1 = server.accept();
                UserThread newThread= new UserThread(s1);
                UserManager.add(newThread.getUId(),newThread);
                newThread.start();
                UserManager.dispTasks();
                newThread.broadcastSystemState();
            }


        } catch (IOException e ) {
            System.out.println("IOException: " + e.getMessage());
        } catch (ClientDisconnectException c) {
            System.out.println("Broadcast state error");
        }
        finally {

        }

    }

    public static void main(String args[]) throws IOException{
        try{
            int port = Integer.parseInt(System.getProperty("port"));
            System.out.println("Daemon running on port " + port);
            startServer(port);
        }
        catch (Exception e) {
            System.out.println("Daemon running on port 8000");
            startServer(8000);
        }


    }
}


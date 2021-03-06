package NotesSystem.client;

import NotesSystem.main.CommonData;
import NotesSystem.main.RequestsParser;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Alex on 19.11.2015.
 */
public class SocketListener extends Thread {
    private Socket _sock;
    private RequestsParser _parser;
    private BufferedReader _in;
    private PrintWriter _out;

    private ArrayList<String> _buffin;
    private ArrayList<String> _buffout;

    private boolean termFlag;

    public SocketListener(int port, String host) {
        try {
            _sock = new Socket(host, port);
            _in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
            _out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        termFlag = false;
        _buffin = new ArrayList<>();
        _buffout = new ArrayList<>();
    }

    public void StopListener() {
        //synchronized (termFlag) {
            termFlag = true;
        //}
    }

    public String ReadBuffer() {
        String str = new String();
        synchronized (_buffin){
            if (_buffin.size() > 0) {
                str = _buffin.get(_buffin.size() - 1);
                _buffin.remove(_buffin.size() - 1);
            }
        }
        return str;
    }

    public void WriteToBuffer(String s) {
        String str = new String();
        synchronized (_buffout){
            _buffout.add(s);
        }
    }

    @Override
    public void run() {
        try{
            String str = new String();
            while (true) {
                str = _in.readLine();
                if (str.equals(CommonData.TERMCOMMAND))
                    break;

                synchronized (_buffin){
                    if (str.length()>0)
                        _buffin.add(str);
                }

                synchronized (_buffout){
                    if (_buffout.size()>0) {
                        _out.println(_buffout.get(_buffout.size()-1));
                        _buffout.remove(_buffout.size()-1);
                    }
                }

                if (termFlag)
                    break;

                try {
                    this.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("closing...");
            try {
                _sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package kr.koreait.networkTest3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server3 extends Frame implements ActionListener, Runnable {
	
	JTextArea textArea;		// 대화 내용이 출력될 영역
	JPanel panel;			// 대화 내용을 입력하는 텍스트 필드와 전송 버튼이 올라갈 패널
	JTextField textField;	// 대화 내용을 입력하는 텍스트 필드
	JButton btn;			// 전송 버튼
	
	ServerSocket serverSocket;
	Socket socket;
	PrintWriter pw;
	Scanner sc;
	String message = "";	// 서버와 클라이언트의 대화 내용을 저장했다가 대화 내용이 출력되는 텍스트 영역에 뿌려줄 때 사용할 변수
	
	public Server3() {
		setTitle("1:1 채팅 프로그램(서버)");
		setBounds(100, 50, 500, 700);
		addWindowListener(new WindowAdapter() {
//			서버 채팅창이 닫힐 때 클라이언트에게 나간다고 알려준다. => 통신을 종료한다.

			@Override
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(textArea, "채팅을 종료하겠습니까?","채팅 종료",JOptionPane.YES_NO_OPTION);
//				System.out.println(result);
				if(result ==0) {
//					서버 채팅창이 닫힐 때 클라이언트에게 나간다고 알려준다.
					pw.write("ㅂㅇㅂㅇ\n");
					pw.write("bye\n");
					pw.flush();
					System.exit(0);
//					채팅에 사용한 모든 객체를 닫는다.
					if(serverSocket != null) { try {
						serverSocket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}}
					if(socket != null) { try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}}
					if(pw != null) { pw.close();}
					if(sc != null) { sc.close();}
				}
			}
		});
		
//		채팅창 만들기
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.YELLOW);
		add(textArea);
		
		panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(500, 40));
		textField = new JTextField();
		panel.add(textField, BorderLayout.CENTER);
		btn = new JButton("전송");
		panel.add(btn, BorderLayout.EAST);
		add(panel, BorderLayout.SOUTH);
		
//		텍스트 필드와 전송 버튼에 ActionListener를 걸어준다.
		textField.addActionListener(this);
		btn.addActionListener(this);
		
		
		setVisible(true);
	}
	
	public static void main(String[] args) {
		Server3 server = new Server3();
		
//		서버를 시작한다.
		try {
			server.serverSocket = new ServerSocket(10004);
			server.message = "192.168.7.10 서버의 10004번 포트로 서버 시작\n";
			server.message += "클라이언트가 접속하기를 기다립니다.\n";
			server.textArea.setText(server.message);
			
//			클라이언트가 접속하기 전에는 텍스트 필드와 전송 버튼을 비활성화 시킨다.
			server.textField.setEnabled(false);
			server.btn.setEnabled(false);
			
//			클라이언트가 접속하기를 기다린다.
			server.socket = server.serverSocket.accept();
			server.message = server.socket + "접속 성송\n" + server.message;
			server.textArea.setText(server.message);
			
//			클라이언트가 접속했으므로 텍스트 필드와 전송 버튼을 활성화시키고 메시지를 입력할 수 있게 텍스트 필드로 포커스를 이동시킨다.
			server.textField.setEnabled(true);
			server.btn.setEnabled(true);
			server.textField.requestFocus();
			
//			클라이언트와 메시지를 주고받기 위해서 데이터 전송에 사용할 객체를 생성한다.
			server.pw = new PrintWriter(server.socket.getOutputStream());
			server.sc = new Scanner(server.socket.getInputStream());
			
//			클라이언트에서 전송되는 메시지를 받는 스레드를 실행한다.
			Thread thread = new Thread(server);
			thread.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	텍스트 필드와 전송 버튼에 ActionListener를 걸어서 클라이언트로 메시지를 전송한다.
	@Override
	public void actionPerformed(ActionEvent e) {
		
//		텍스트 필드에 입력된 데이터를 받는다.
		String str = textField.getText().trim();
//		텍스트 필드에 데이터가 입력된 상태일 경우 메시지를 서버 채팅창에 표시하고 클라이언트로 전송한다.
		if(str.length()>0) {
//			입력한 메세지를 서버 채팅창에 표시한다.
			message = "server >> "+str + "\n"+message;
			textArea.setText(message);
//			입력한 메세지를 클라이언트로 전송한다.
			if(pw != null) {
				pw.write(str+"\n");
				pw.flush();
			}
		}
//		클라이언트로 메시지를 전송했으면 다음 메시지를 입력받기 위해 텍스트 필드의 메시지를 지우고 포커스를 옮겨준다.
		textField.setText("");
		textField.requestFocus();
		
	}

	@Override
	public void run() {
		
//		클라이언트와 통신이 유지되고 있는 동안 반복한다. => 통신 소켓이 null이 아닌 동안 반복한다.
		while(socket != null) {
//			클라이언트에서 전송된 메시지를 받는다.
			String str = "";
			try {
			str = sc.nextLine().trim();
			} catch(NoSuchElementException e) {
				break;
			}
//			클라이언트에서 전송된 메시지를 서버 채팅창에 표시한다.
			if(str.length()>0) {
				message ="client >> "+str+"\n"+message;
				textArea.setText(message);
//				클라이언트 채팅 창이 닫히거나 "bye"를 전송받으면 채팅을 종료해야 하므로 반복을 탈출한다.
				if(str.toLowerCase().equals("bye")) {
					break;
				}
			}
		}
//		클라이언트와 채팅이 종료되면 메시지를 입력할 수 없도록 텍스트 필드와 전송 버튼을 비활성화 시킨다.
		textField.setEnabled(false);
		btn.setEnabled(false);
//		채팅에 사용한 모든 객체를 닫는다.
		if(serverSocket != null) { try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		if(socket != null) { try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}
		if(pw != null) { pw.close();}
		if(sc != null) { sc.close();}
	}
} 
